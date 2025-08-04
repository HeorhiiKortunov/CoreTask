package com.example.maven.service;

import com.example.maven.api.dto.request.company.CompanyCreateDto;
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

	public CompanyResponseDto createCompany(CompanyCreateDto dto){
		//Creating first admin
		UserResponseDto user = userService.createUser(dto.firstAdmin());
		UserUpdateRolesDto rolesDto = new UserUpdateRolesDto(Set.of(Role.ROLE_MEMBER, Role.ROLE_ADMIN, Role.ROLE_OWNER));
		userService.updateUserRolesById(user.id(), rolesDto);

		//Creating the company itself
		var savedCompany = companyRepository.save(companyMapper.fromCreateDto(dto));

		return companyMapper.toResponseDto(savedCompany);
	}

	//TODO: Only for the owner(in controller)
	public void deleteCompany(long id){
		companyRepository.deleteById(id);
	}
}
