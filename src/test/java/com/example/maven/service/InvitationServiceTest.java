package com.example.maven.service;

import com.example.maven.api.dto.request.invitation.InvitationAcceptDto;
import com.example.maven.api.dto.request.invitation.InvitationCreateDto;
import com.example.maven.api.dto.response.UserResponseDto;
import com.example.maven.api.mapper.InvitationMapper;
import com.example.maven.api.mapper.UserMapper;
import com.example.maven.exception.ResourceNotFoundException;
import com.example.maven.persistence.entity.Company;
import com.example.maven.persistence.entity.Invitation;
import com.example.maven.persistence.entity.User;
import com.example.maven.persistence.repository.CompanyRepository;
import com.example.maven.persistence.repository.InvitationRepository;
import com.example.maven.persistence.repository.UserRepository;
import com.example.maven.utils.SecurityUtils;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvitationServiceTest {

	@Mock private InvitationRepository invitationRepository;
	@Mock private InvitationMapper invitationMapper;
	@Mock private CompanyRepository companyRepository;
	@Mock private UserRepository userRepository;
	@Mock private UserMapper userMapper;
	@Mock private EmailService emailService;
	@Mock private PasswordEncoder passwordEncoder;

	@InjectMocks
	private InvitationService invitationService;

	private static final long TENANT_ID = 55L;
	private MockedStatic<SecurityUtils> securityUtilsMock;

	private Company company;

	@BeforeEach
	void setUp() {
		securityUtilsMock = mockStatic(SecurityUtils.class);
		securityUtilsMock.when(SecurityUtils::getCurrentTenantId).thenReturn(TENANT_ID);

		company = new Company();
		company.setId(TENANT_ID);

		ReflectionTestUtils.setField(invitationService, "publicBaseUrl", "http://test-host:8080");
	}

	@AfterEach
	void tearDown() {
		securityUtilsMock.close();
	}

	// inviteUser
	@Test
	void givenValidDto_whenInviteUser_thenTokenAndExpirySet_andEmailSentWithLink() {
		InvitationCreateDto dto = mock(InvitationCreateDto.class);
		Invitation emptyInvitation = new Invitation();
		emptyInvitation.setEmail("to@example.com");

		when(companyRepository.findById(TENANT_ID)).thenReturn(Optional.of(company));
		when(invitationMapper.fromCreateDto(dto, company)).thenReturn(emptyInvitation);
		when(invitationRepository.save(any(Invitation.class)))
				.thenAnswer(inv -> inv.getArgument(0));

		ArgumentCaptor<String> toCap = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<String> subjCap = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<String> bodyCap = ArgumentCaptor.forClass(String.class);

		LocalDateTime before = LocalDateTime.now();
		invitationService.inviteUser(dto);
		LocalDateTime after = LocalDateTime.now();

		ArgumentCaptor<Invitation> invCap = ArgumentCaptor.forClass(Invitation.class);
		verify(invitationRepository, atLeastOnce()).save(invCap.capture());
		Invitation saved = invCap.getValue();

		assertThat(saved.getToken()).isNotBlank();

		LocalDateTime min = before.plusDays(2).minusSeconds(2);
		LocalDateTime max = after.plusDays(2).plusSeconds(2);
		assertThat(saved.getExpiresAt()).isBetween(min, max);

		verify(emailService).sendInvitationEmail(toCap.capture(), subjCap.capture(), bodyCap.capture());
		assertThat(toCap.getValue()).isEqualTo("to@example.com");
		assertThat(subjCap.getValue()).containsIgnoringCase("invitation");
		assertThat(bodyCap.getValue())
				.contains("http://test-host:8080/api/invitations/accept?token=" + saved.getToken());
	}

	@Test
	void givenNoCompany_whenInviteUser_thenThrowsNotFound_andNoEmailSent() {
		InvitationCreateDto dto = mock(InvitationCreateDto.class);
		when(companyRepository.findById(TENANT_ID)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> invitationService.inviteUser(dto))
				.isInstanceOf(ResourceNotFoundException.class)
				.hasMessageContaining("Company not found");

		verify(emailService, never()).sendInvitationEmail(anyString(), anyString(), anyString());
		verify(invitationRepository, never()).save(any());
	}

	// acceptInvitation
	@Test
	void givenValidTokenAndDto_whenAcceptInvitation_thenPasswordEncoded_userSaved_inviteMarkedAccepted() {
		InvitationAcceptDto dto = mock(InvitationAcceptDto.class);
		when(dto.password()).thenReturn("raw-pass");

		Invitation inv = new Invitation();
		inv.setEmail("new@user.com");
		inv.setAccepted(false);
		inv.setExpiresAt(LocalDateTime.now().plusHours(1));

		when(invitationRepository.findByToken("token-123")).thenReturn(Optional.of(inv));

		User mappedUser = new User();
		when(invitationMapper.fromAcceptDto(dto, inv)).thenReturn(mappedUser);
		when(passwordEncoder.encode("raw-pass")).thenReturn("$2b$encoded");
		when(userRepository.save(mappedUser)).thenAnswer(a -> {
			User u = a.getArgument(0);
			u.setId(777L);
			return u;
		});

		UserResponseDto response = new UserResponseDto(777L, "login", "name", "new@user.com", java.util.Set.of());
		when(userMapper.toResponseDto(any(User.class))).thenReturn(response);

		UserResponseDto result = invitationService.acceptInvitation("token-123", dto);

		assertThat(result).isEqualTo(response);
		assertThat(mappedUser.getPassword()).isEqualTo("$2b$encoded");
		assertThat(inv.isAccepted()).isTrue();

		verify(invitationRepository, times(1)).save(any(Invitation.class));
		verify(userRepository).save(mappedUser);
	}

	@Test
	void givenUnknownToken_whenAcceptInvitation_thenThrowsNotFound() {
		when(invitationRepository.findByToken("bad")).thenReturn(Optional.empty());

		assertThatThrownBy(() -> invitationService.acceptInvitation("bad", mock(InvitationAcceptDto.class)))
				.isInstanceOf(ResourceNotFoundException.class)
				.hasMessageContaining("Invitation not found");

		verify(userRepository, never()).save(any());
	}

	@Test
	void givenExpiredInvitation_whenAcceptInvitation_thenThrowsAccessDenied() {
		Invitation inv = new Invitation();
		inv.setAccepted(false);
		inv.setExpiresAt(LocalDateTime.now().minusMinutes(1));

		when(invitationRepository.findByToken("t")).thenReturn(Optional.of(inv));

		assertThatThrownBy(() -> invitationService.acceptInvitation("t", mock(InvitationAcceptDto.class)))
				.isInstanceOf(AccessDeniedException.class)
				.hasMessageContaining("expired");

		verify(userRepository, never()).save(any());
	}

	@Test
	void givenAlreadyAcceptedInvitation_whenAcceptInvitation_thenThrowsAccessDenied() {
		Invitation inv = new Invitation();
		inv.setAccepted(true);
		inv.setExpiresAt(LocalDateTime.now().plusDays(1));

		when(invitationRepository.findByToken("t")).thenReturn(Optional.of(inv));

		assertThatThrownBy(() -> invitationService.acceptInvitation("t", mock(InvitationAcceptDto.class)))
				.isInstanceOf(AccessDeniedException.class)
				.hasMessageContaining("already used");

		verify(userRepository, never()).save(any());
	}
}
