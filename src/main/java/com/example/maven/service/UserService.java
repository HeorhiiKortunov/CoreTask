package com.example.maven.service;

import com.example.maven.api.dto.request.user.UserCreateDto;
import com.example.maven.api.dto.request.user.UserUpdateDto;
import com.example.maven.api.dto.request.user.UserUpdateRolesDto;
import com.example.maven.api.dto.response.UserResponseDto;
import com.example.maven.api.mapper.UserMapper;
import com.example.maven.exception.ResourceNotFoundException;
import com.example.maven.persistence.entity.User;
import com.example.maven.persistence.repository.UserRepository;
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

	public UserResponseDto findById(long id){
		return userMapper.toResponseDto(userRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("User not found")));
	}

	public List<UserResponseDto> findByCompanyId(long companyId){
		return userRepository.findByCompany_Id(companyId).stream()
				.map(userMapper::toResponseDto)
				.toList();
	}

	public UserResponseDto createUser(UserCreateDto dto){
		var user = userMapper.fromCreateDto(dto);
		var savedUser = userRepository.save(user);

		return userMapper.toResponseDto(savedUser);
	}

	public UserResponseDto updateUser(long id, UserUpdateDto dto){
		var user = userRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));

		userMapper.updateFromDto(user, dto);
		var savedUser = userRepository.save(user);

		return userMapper.toResponseDto(savedUser);
	}

	public UserResponseDto updateUserRolesById(long id, UserUpdateRolesDto dto){
		var user = userRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));

		userMapper.updateRolesFromDto(user, dto);
		var savedUser = userRepository.save(user);

		return userMapper.toResponseDto(savedUser);
	}

	public void deleteUserBy(long id){
		userRepository.deleteById(id);
	}
}
