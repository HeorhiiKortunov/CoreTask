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
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;

@Service
@Transactional
@AllArgsConstructor
public class UserService {

	private final UserRepository userRepository;
	private final UserMapper userMapper;
	private final CompanyRepository companyRepository;
	private final PasswordEncoder passwordEncoder;
	private final SecurityUtils securityUtils;

	// Evict company users list cache when creating new user
	@CacheEvict(value = "companyUsers", key = "@securityUtils.getCurrentTenantId()")
	public UserResponseDto createUser(UserCreateDto dto){
		var user = userMapper.fromCreateDto(dto);
		user.setPassword(passwordEncoder.encode(dto.password()));
		user.setCompany(companyRepository.findById(securityUtils.getCurrentTenantId())
				.orElseThrow(() -> new AccessDeniedException("No current company found")));
		var savedUser = userRepository.save(user);

		return userMapper.toResponseDto(savedUser);
	}

	public UserResponseDto createUserForRegistration(UserCreateDto dto, Long companyId) {
		var user = userMapper.fromCreateDto(dto);
		user.setPassword(passwordEncoder.encode(dto.password()));
		user.setCompany(companyRepository.getReferenceById(companyId));
		var saved = userRepository.save(user);
		return userMapper.toResponseDto(saved);
	}

	// Cache individual user by ID + tenantId
	@Cacheable(value = "users", key = "#id + '_' + @securityUtils.getCurrentTenantId()")
	public UserResponseDto findById(long id) {
		return userMapper.toResponseDto(getUserById(id));
	}

	// Cache company users list by tenantId
	@Cacheable(value = "companyUsers", key = "@securityUtils.getCurrentTenantId()")
	public List<UserResponseDto> findCompanyUsers() {
		return userRepository.findAllByCompany_Id(securityUtils.getCurrentTenantId()).stream()
				.map(userMapper::toResponseDto)
				.toList();
	}

	// Evict both individual user cache and company users list cache
	@Caching(evict = {
			@CacheEvict(value = "users", key = "#id + '_' + @securityUtils.getCurrentTenantId()"),
			@CacheEvict(value = "companyUsers", key = "@securityUtils.getCurrentTenantId()")
	})
	public UserResponseDto updateUser(long id, UserUpdateDto dto) {
		var user = getUserById(id);
		userMapper.updateFromDto(user, dto);
		var savedUser = userRepository.save(user);

		return userMapper.toResponseDto(savedUser);
	}

	// Evict both caches when updating roles
	@Caching(evict = {
			@CacheEvict(value = "users", key = "#id + '_' + @securityUtils.getCurrentTenantId()"),
			@CacheEvict(value = "companyUsers", key = "@securityUtils.getCurrentTenantId()")
	})
	public UserResponseDto updateUserRolesById(long id, UserUpdateRolesDto dto) {
		var user = getUserById(id);
		userMapper.updateRolesFromDto(user, dto);
		var savedUser = userRepository.save(user);

		return userMapper.toResponseDto(savedUser);
	}

	// Evict all company users caches since we don't have tenant context
	@CacheEvict(value = "companyUsers", allEntries = true)
	public void updateUserRolesByIdWithoutSecurity(Long userId, UserUpdateRolesDto dto) {
		var user = userRepository.findById(userId)
				.orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

		user.setRoles(new HashSet<>(dto.roles()));
		userRepository.save(user);
	}

	// Evict both caches when deleting
	@Caching(evict = {
			@CacheEvict(value = "users", key = "#id + '_' + @securityUtils.getCurrentTenantId()"),
			@CacheEvict(value = "companyUsers", key = "@securityUtils.getCurrentTenantId()")
	})
	public void deleteUser(long id) {
		userRepository.delete(getUserById(id));
	}

	private User getUserById(long id){
		return userRepository.findByIdAndCompany_Id(id, securityUtils.getCurrentTenantId())
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));
	}
}