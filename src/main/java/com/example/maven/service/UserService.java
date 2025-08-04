package com.example.maven.service;

import com.example.maven.api.dto.request.user.UserCreateDto;
import com.example.maven.api.dto.request.user.UserUpdateDto;
import com.example.maven.api.dto.request.user.UserUpdateRolesDto;
import com.example.maven.api.dto.response.UserResponseDto;
import com.example.maven.api.mapper.UserMapper;
import com.example.maven.exception.ResourceNotFoundException;
import com.example.maven.persistence.repository.CompanyRepository;
import com.example.maven.persistence.repository.UserRepository;
import com.example.maven.utils.SecurityUtils;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
@AllArgsConstructor
public class UserService {

	private final UserRepository userRepository;
	private final UserMapper userMapper;
	private final CompanyRepository companyRepository;

	public UserResponseDto createUser(UserCreateDto dto){
		var user = userMapper.fromCreateDto(dto);
		var savedUser = userRepository.save(user);

		return userMapper.toResponseDto(savedUser);
	}

	public UserResponseDto findById(long id) {
		var user = userRepository.findByIdAndCompany_Id(id, SecurityUtils.getCurrentTenantId())
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));
		return userMapper.toResponseDto(user);
	}

	public List<UserResponseDto> findUsersByCompany() {
		return userRepository.findAllByCompany_Id(SecurityUtils.getCurrentTenantId()).stream()
				.map(userMapper::toResponseDto)
				.toList();
	}

	public UserResponseDto updateUser(long id, UserUpdateDto dto) {
		var user = userRepository.findByIdAndCompany_Id(id, SecurityUtils.getCurrentTenantId())
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));

		userMapper.updateFromDto(user, dto);

		var savedUser = userRepository.save(user);
		return userMapper.toResponseDto(savedUser);
	}

	public UserResponseDto updateUserRolesById(long id, UserUpdateRolesDto dto) {
		var user = userRepository.findByIdAndCompany_Id(id, SecurityUtils.getCurrentTenantId())
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));

		userMapper.updateRolesFromDto(user, dto);
		var savedUser = userRepository.save(user);

		return userMapper.toResponseDto(savedUser);
	}

	//TODO: Only for the user himself or admin(in controller)
	public void deleteUserBy(long id) {
		var user = userRepository.findByIdAndCompany_Id(id, SecurityUtils.getCurrentTenantId())
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));

		userRepository.delete(user);
	}
}
