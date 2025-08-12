package com.example.maven.service;

import com.example.maven.api.dto.request.user.UserCreateDto;
import com.example.maven.api.dto.request.user.UserUpdateDto;
import com.example.maven.api.dto.request.user.UserUpdateRolesDto;
import com.example.maven.api.dto.response.UserResponseDto;
import com.example.maven.api.mapper.UserMapper;
import com.example.maven.enums.Role;
import com.example.maven.exception.ResourceNotFoundException;
import com.example.maven.persistence.entity.Company;
import com.example.maven.persistence.entity.User;
import com.example.maven.persistence.repository.CompanyRepository;
import com.example.maven.persistence.repository.UserRepository;
import com.example.maven.utils.SecurityUtils;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

	@Mock private UserRepository userRepository;
	@Mock private UserMapper userMapper;
	@Mock private CompanyRepository companyRepository;
	@Mock private PasswordEncoder passwordEncoder;

	@InjectMocks
	private UserService userService;

	private static final long TENANT_ID = 42L;

	private MockedStatic<SecurityUtils> securityUtilsMock;
	private Company company;

	@BeforeEach
	void setUp() {
		securityUtilsMock = mockStatic(SecurityUtils.class);
		securityUtilsMock.when(SecurityUtils::getCurrentTenantId).thenReturn(TENANT_ID);

		company = new Company();
		company.setId(TENANT_ID);
	}

	@AfterEach
	void tearDown() {
		if (securityUtilsMock != null) securityUtilsMock.close();
	}

	// createUser
	@Test
	void givenValidDtoAndTenant_whenCreateUser_thenSavesWithEncodedPassword_andReturnsDto() {
		UserCreateDto dto = new UserCreateDto("u1","User One","u1@mail.com","pass123");
		User mapped = new User();
		User saved = new User();
		saved.setId(100L);
		UserResponseDto response = new UserResponseDto(100L,"u1","User One","u1@mail.com", Set.of(Role.ROLE_MEMBER));

		when(userMapper.fromCreateDto(dto)).thenReturn(mapped);
		when(passwordEncoder.encode("pass123")).thenReturn("$2b$hash");
		when(companyRepository.findById(TENANT_ID)).thenReturn(Optional.of(company));
		when(userRepository.save(mapped)).thenReturn(saved);
		when(userMapper.toResponseDto(saved)).thenReturn(response);

		UserResponseDto result = userService.createUser(dto);

		assertThat(result).isEqualTo(response);
		assertThat(mapped.getPassword()).isEqualTo("$2b$hash");
		assertThat(mapped.getCompany()).isEqualTo(company);
		verify(userRepository).save(mapped);
	}

	@Test
	void givenNoCompanyForTenant_whenCreateUser_thenThrowsAccessDenied() {
		UserCreateDto dto = new UserCreateDto("u1","User One","u1@mail.com","pass123");
		when(userMapper.fromCreateDto(dto)).thenReturn(new User());
		when(companyRepository.findById(TENANT_ID)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> userService.createUser(dto))
				.isInstanceOf(AccessDeniedException.class)
				.hasMessageContaining("No current company found");
	}

	@Test
	void givenCreateUser_whenSaveCalled_thenEncodedPasswordActuallyPersisted() {
		UserCreateDto dto = new UserCreateDto("u1","User One","u1@mail.com","pass123");
		User mapped = new User();
		when(userMapper.fromCreateDto(dto)).thenReturn(mapped);
		when(passwordEncoder.encode("pass123")).thenReturn("$2b$enc");
		when(companyRepository.findById(TENANT_ID)).thenReturn(Optional.of(company));
		when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
		when(userMapper.toResponseDto(any())).thenReturn(
				new UserResponseDto(1L,"u1","User One","u1@mail.com", Set.of())
		);

		userService.createUser(dto);

		ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
		verify(userRepository).save(captor.capture());
		assertThat(captor.getValue().getPassword()).isEqualTo("$2b$enc");
		assertThat(captor.getValue().getCompany()).isEqualTo(company);
	}

	// createUserForRegistration
	@Test
	void givenValidDtoAndCompanyId_whenCreateUserForRegistration_thenEncodesPassword_andReturnsDto() {
		UserCreateDto dto = new UserCreateDto("u2","User Two","u2@mail.com","pw");
		User mapped = new User();
		Company extCompany = new Company(); extCompany.setId(7L);
		User saved = new User(); saved.setId(200L);
		UserResponseDto response = new UserResponseDto(200L,"u2","User Two","u2@mail.com", Set.of(Role.ROLE_MEMBER));

		when(userMapper.fromCreateDto(dto)).thenReturn(mapped);
		when(passwordEncoder.encode("pw")).thenReturn("$2b$enc");
		when(companyRepository.getReferenceById(7L)).thenReturn(extCompany);
		when(userRepository.save(mapped)).thenReturn(saved);
		when(userMapper.toResponseDto(saved)).thenReturn(response);

		UserResponseDto result = userService.createUserForRegistration(dto, 7L);

		assertThat(mapped.getPassword()).isEqualTo("$2b$enc");
		assertThat(mapped.getCompany()).isEqualTo(extCompany);
		assertThat(result).isEqualTo(response);
	}

	// findById
	@Test
	void givenExistingUser_whenFindById_thenReturnsDto() {
		User u = new User(); u.setId(10L);
		UserResponseDto dto = new UserResponseDto(10L,"u","U","u@x", Set.of());
		when(userRepository.findByIdAndCompany_Id(10L, TENANT_ID)).thenReturn(Optional.of(u));
		when(userMapper.toResponseDto(u)).thenReturn(dto);

		assertThat(userService.findById(10L)).isEqualTo(dto);
	}

	@Test
	void givenMissingUser_whenFindById_thenThrowsNotFound() {
		when(userRepository.findByIdAndCompany_Id(99L, TENANT_ID)).thenReturn(Optional.empty());
		assertThatThrownBy(() -> userService.findById(99L))
				.isInstanceOf(ResourceNotFoundException.class)
				.hasMessageContaining("User not found");
	}

	// findCompanyUsers
	@Test
	void givenTenant_whenFindCompanyUsers_thenMapsAll() {
		User u1 = new User(); u1.setId(1L);
		User u2 = new User(); u2.setId(2L);
		when(userRepository.findAllByCompany_Id(TENANT_ID)).thenReturn(List.of(u1, u2));
		when(userMapper.toResponseDto(u1)).thenReturn(new UserResponseDto(1L,"a","A","a@x", Set.of()));
		when(userMapper.toResponseDto(u2)).thenReturn(new UserResponseDto(2L,"b","B","b@x", Set.of()));

		List<UserResponseDto> result = userService.findCompanyUsers();

		assertThat(result).hasSize(2);
		verify(userRepository).findAllByCompany_Id(TENANT_ID);
	}

	// updateUser
	@Test
	void givenExistingUserAndUpdateDto_whenUpdateUser_thenSaves_andReturnsDto() {
		long id = 5L;
		UserUpdateDto dto = mock(UserUpdateDto.class);
		User existing = new User(); existing.setId(id);
		User saved = new User(); saved.setId(id);
		UserResponseDto response = new UserResponseDto(id,"u","U","u@x", Set.of());

		when(userRepository.findByIdAndCompany_Id(id, TENANT_ID)).thenReturn(Optional.of(existing));
		doAnswer(inv -> null).when(userMapper).updateFromDto(existing, dto);
		when(userRepository.save(existing)).thenReturn(saved);
		when(userMapper.toResponseDto(saved)).thenReturn(response);

		UserResponseDto result = userService.updateUser(id, dto);

		assertThat(result).isEqualTo(response);
		verify(userMapper).updateFromDto(existing, dto);
		verify(userRepository).save(existing);
	}

	// updateUserRolesById
	@Test
	void givenExistingUserAndRolesDto_whenUpdateUserRolesById_thenRolesUpdated_andDtoReturned() {
		long id = 9L;
		User existing = new User(); existing.setId(id);
		User saved = new User(); saved.setId(id);
		UserUpdateRolesDto rolesDto = new UserUpdateRolesDto(Set.of(Role.ROLE_MEMBER, Role.ROLE_ADMIN));
		UserResponseDto response = new UserResponseDto(id,"u","U","u@x", rolesDto.roles());

		when(userRepository.findByIdAndCompany_Id(id, TENANT_ID)).thenReturn(Optional.of(existing));
		doAnswer(inv -> { existing.setRoles(new HashSet<>(rolesDto.roles())); return null; })
				.when(userMapper).updateRolesFromDto(existing, rolesDto);
		when(userRepository.save(existing)).thenReturn(saved);
		when(userMapper.toResponseDto(saved)).thenReturn(response);

		UserResponseDto result = userService.updateUserRolesById(id, rolesDto);

		assertThat(result).isEqualTo(response);
		assertThat(existing.getRoles()).containsExactlyInAnyOrder(Role.ROLE_MEMBER, Role.ROLE_ADMIN);
		verify(userRepository).save(existing);
	}

	// updateUserRolesByIdWithoutSecurity
	@Test
	void givenExistingUser_whenUpdateUserRolesByIdWithoutSecurity_thenPersistsRoles() {
		long userId = 11L;
		User existing = new User(); existing.setId(userId);
		UserUpdateRolesDto rolesDto = new UserUpdateRolesDto(Set.of(Role.ROLE_OWNER));

		when(userRepository.findById(userId)).thenReturn(Optional.of(existing));

		userService.updateUserRolesByIdWithoutSecurity(userId, rolesDto);

		assertThat(existing.getRoles()).containsExactly(Role.ROLE_OWNER);
		verify(userRepository).save(existing);
	}

	@Test
	void givenMissingUser_whenUpdateUserRolesByIdWithoutSecurity_thenThrowsNotFound() {
		long userId = 404L;
		when(userRepository.findById(userId)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> userService.updateUserRolesByIdWithoutSecurity(
				userId, new UserUpdateRolesDto(Set.of(Role.ROLE_MEMBER))))
				.isInstanceOf(ResourceNotFoundException.class)
				.hasMessageContaining("User not found");
	}

	// deleteUser
	@Test
	void givenExistingUser_whenDeleteUser_thenRepositoryDeleteCalled() {
		long id = 15L;
		User existing = new User(); existing.setId(id);
		when(userRepository.findByIdAndCompany_Id(id, TENANT_ID)).thenReturn(Optional.of(existing));

		userService.deleteUser(id);

		verify(userRepository).delete(existing);
	}

	@Test
	void givenMissingUser_whenDeleteUser_thenThrowsNotFound() {
		when(userRepository.findByIdAndCompany_Id(123L, TENANT_ID)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> userService.deleteUser(123L))
				.isInstanceOf(ResourceNotFoundException.class)
				.hasMessageContaining("User not found");
	}
}
