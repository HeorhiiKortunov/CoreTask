package com.example.maven.service;

import com.example.maven.api.dto.request.company.CompanyCreateDto;
import com.example.maven.api.dto.request.user.UserCreateDto;
import com.example.maven.api.dto.request.user.UserUpdateRolesDto;
import com.example.maven.api.dto.response.CompanyResponseDto;
import com.example.maven.api.dto.response.UserResponseDto;
import com.example.maven.api.mapper.CompanyMapper;
import com.example.maven.enums.Role;
import com.example.maven.persistence.repository.CompanyRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@Transactional
@AllArgsConstructor
public class CompanyService {
	private final CompanyRepository companyRepository;
	private final CompanyMapper companyMapper;
	private final UserService userService;

	public CompanyResponseDto createCompany(CompanyCreateDto dto) {
		// 1) Создаём компанию
		var company = companyMapper.fromCreateDto(dto);
		var savedCompany = companyRepository.save(company);

		// 2) Создаём владельца, указав companyId явно
		var ownerDto = new UserCreateDto(
				dto.owner().username(),
				dto.owner().displayedName(),
				dto.owner().email(),
				dto.owner().password()
		);
		UserResponseDto owner = userService.createUserForRegistration(ownerDto, savedCompany.getId());

		// 3) Назначаем роли владельцу без методной безопасности
		UserUpdateRolesDto rolesDto = new UserUpdateRolesDto(
				Set.of(Role.ROLE_MEMBER, Role.ROLE_ADMIN, Role.ROLE_OWNER)
		);
		userService.updateUserRolesByIdWithoutSecurity(owner.id(), rolesDto);

		return companyMapper.toResponseDto(savedCompany);
	}




	public void deleteCompany(long id){
		companyRepository.deleteById(id);
	}
}
