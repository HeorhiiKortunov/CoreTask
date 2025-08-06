package com.example.maven.service;

import com.example.maven.api.dto.request.user.UserCreateDto;
import com.example.maven.api.dto.request.user.UserUpdateDto;
import com.example.maven.api.dto.request.user.UserUpdateRolesDto;
import com.example.maven.api.dto.response.UserResponseDto;
import com.example.maven.api.mapper.UserMapper;
import com.example.maven.exception.ResourceNotFoundException;
import com.example.maven.persistence.entity.User;
import com.example.maven.persistence.repository.CompanyRepository;
import com.example.maven.persistence.repository.UserRepository;
import com.example.maven.utils.SecurityUtils;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
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
		user.setCompany(companyRepository.findById(SecurityUtils.getCurrentTenantId())
				.orElseThrow(() -> new AccessDeniedException("No current company found")));
		var savedUser = userRepository.save(user);

		return userMapper.toResponseDto(savedUser);
	}

	public UserResponseDto findById(long id) {
		return userMapper.toResponseDto(getUserById(id));
	}

	public List<UserResponseDto> findCompanyUsers() {
		return userRepository.findAllByCompany_Id(SecurityUtils.getCurrentTenantId()).stream()
				.map(userMapper::toResponseDto)
				.toList();
	}

	//for admin
	public UserResponseDto updateUser(long id, UserUpdateDto dto) {
		var user = getUserById(id);
		userMapper.updateFromDto(user, dto);
		var savedUser = userRepository.save(user);

		return userMapper.toResponseDto(savedUser);
	}

	//for user himself
	public UserResponseDto updateCurrentUser(UserUpdateDto dto){
		long currentUserId = SecurityUtils.getCurrentUserId();
		var user = getUserById(currentUserId);
		userMapper.updateFromDto(user, dto);
		var savedUser = userRepository.save(user);

		return userMapper.toResponseDto(savedUser);
	}

	public UserResponseDto updateUserRolesById(long id, UserUpdateRolesDto dto) {
		var user = getUserById(id);
		userMapper.updateRolesFromDto(user, dto);
		var savedUser = userRepository.save(user);

		return userMapper.toResponseDto(savedUser);
	}

	public void deleteUser(long id) {
		userRepository.delete(getUserById(id));
	}

	private User getUserById(long id){
		return userRepository.findByIdAndCompany_Id(id, SecurityUtils.getCurrentTenantId())
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));
	}
}
