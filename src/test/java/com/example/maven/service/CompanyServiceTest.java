package com.example.maven.service;

import com.example.maven.api.dto.request.company.CompanyCreateDto;
import com.example.maven.api.dto.request.user.UserCreateDto;
import com.example.maven.api.dto.request.user.UserUpdateRolesDto;
import com.example.maven.api.dto.response.CompanyResponseDto;
import com.example.maven.api.dto.response.UserResponseDto;
import com.example.maven.api.mapper.CompanyMapper;
import com.example.maven.enums.Role;
import com.example.maven.persistence.entity.Company;
import com.example.maven.persistence.repository.CompanyRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompanyServiceTest {

    @Mock private CompanyRepository companyRepository;
    @Mock private CompanyMapper companyMapper;
    @Mock private UserService userService;

    @InjectMocks
    private CompanyService companyService;

    @Test
    void givenValidDto_whenCreateCompany_thenCompanySavedOwnerCreatedRolesAssigned_andDtoReturned() {
        var ownerCreate = new UserCreateDto("ownerLogin", "Owner Name", "owner@mail.com", "pwd12345");
        var dto = new CompanyCreateDto("New Company", ownerCreate);

        var mappedCompany = new Company();
        var savedCompany = new Company(); savedCompany.setId(10L);

        var ownerResponse = new UserResponseDto(
                111L, "ownerLogin", "Owner Name", "owner@mail.com", Set.of()
        );

        var companyResponse = new CompanyResponseDto(10L, "New Company", null);

        when(companyMapper.fromCreateDto(dto)).thenReturn(mappedCompany);
        when(companyRepository.save(mappedCompany)).thenReturn(savedCompany);
        when(userService.createUserForRegistration(ownerCreate, 10L)).thenReturn(ownerResponse);
        when(companyMapper.toResponseDto(savedCompany)).thenReturn(companyResponse);

        ArgumentCaptor<UserUpdateRolesDto> rolesCaptor = ArgumentCaptor.forClass(UserUpdateRolesDto.class);

        CompanyResponseDto result = companyService.createCompany(dto);

        assertThat(result).isEqualTo(companyResponse);

        InOrder inOrder = inOrder(companyMapper, companyRepository, userService);
        inOrder.verify(companyMapper).fromCreateDto(dto);
        inOrder.verify(companyRepository).save(mappedCompany);
        inOrder.verify(userService).createUserForRegistration(ownerCreate, 10L);
        inOrder.verify(userService).updateUserRolesByIdWithoutSecurity(eq(111L), rolesCaptor.capture());
        inOrder.verify(companyMapper).toResponseDto(savedCompany);

        var rolesDto = rolesCaptor.getValue();
        assertThat(rolesDto.roles())
                .containsExactlyInAnyOrder(Role.ROLE_MEMBER, Role.ROLE_ADMIN, Role.ROLE_OWNER);

        verifyNoMoreInteractions(companyMapper, companyRepository, userService);
    }

    @Test
    void givenCompanyId_whenDeleteCompany_thenRepositoryDeleteByIdCalled() {
        companyService.deleteCompany(77L);

        verify(companyRepository).deleteById(77L);
        verifyNoMoreInteractions(companyRepository);
    }
}
