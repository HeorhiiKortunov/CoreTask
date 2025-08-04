package com.example.maven.api.mapper;

import com.example.maven.api.dto.request.user.UserCreateDto;
import com.example.maven.api.dto.request.user.UserUpdateDto;
import com.example.maven.api.dto.request.user.UserUpdateRolesDto;
import com.example.maven.api.dto.response.UserResponseDto;
import com.example.maven.persistence.entity.User;
import com.example.maven.persistence.repository.CompanyRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.lang.module.ResolutionException;


@Component
@AllArgsConstructor
public class UserMapper {
	private final CompanyRepository companyRepository;

	public User fromCreateDto(UserCreateDto dto){
		User user = new User();
		user.setCompany(companyRepository.findById(dto.companyId()).orElseThrow(() -> new ResolutionException("Company not found")));
		user.setUsername(dto.username());
		user.setDisplayedName(dto.displayedName());
		user.setEmail(dto.email());
		user.setPassword(dto.password());

		return user;
	}

	public UserResponseDto toResponseDto(User user){

		return new UserResponseDto(
				user.getId(),
				user.getUsername(),
				user.getDisplayedName(),
				user.getEmail(),
				user.getRoles()
		);
	}

	public void updateFromDto(User user, UserUpdateDto dto){
		if(dto.getDisplayedName() != null) user.setDisplayedName(dto.getDisplayedName());
		if(dto.getEmail() != null) user.setEmail(dto.getEmail());
	}

	public void updateRolesFromDto(User user, UserUpdateRolesDto dto){
		user.setRoles(dto.roles());
	}
}
