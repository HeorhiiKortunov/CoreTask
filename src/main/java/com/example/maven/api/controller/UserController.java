package com.example.maven.api.controller;

import com.example.maven.api.dto.request.user.UserUpdateDto;
import com.example.maven.api.dto.request.user.UserUpdateRolesDto;
import com.example.maven.api.dto.response.UserResponseDto;
import com.example.maven.security.UserPrincipal;
import com.example.maven.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@AllArgsConstructor
public class UserController {
	private final UserService userService;

	@GetMapping
	public ResponseEntity<List<UserResponseDto>> getAllUsers(){
		return ResponseEntity.ok(userService.findCompanyUsers());
	}

	@GetMapping("/me")
	public ResponseEntity<UserResponseDto> getCurrentProfile(@AuthenticationPrincipal UserPrincipal principal){
		return ResponseEntity.ok(userService.findById(principal.getId()));
	}

	@GetMapping("/{id}")
	public ResponseEntity<UserResponseDto> getUserProfile(@PathVariable long id){
		return ResponseEntity.ok(userService.findById(id));
	}

	@PutMapping("/roles/{id}")
	public ResponseEntity<UserResponseDto> updateUserRoles(@PathVariable long id, @RequestBody UserUpdateRolesDto dto){
		return ResponseEntity.ok(userService.updateUserRolesById(id, dto));
	}

	@PutMapping("/me")
	public ResponseEntity<UserResponseDto> updateCurrentUser(@AuthenticationPrincipal UserPrincipal principal, @RequestBody UserUpdateRolesDto dto){
		return ResponseEntity.ok(userService.updateUserRolesById(principal.getId(), dto));
	}

	@PutMapping("/{id}")
	public ResponseEntity<UserResponseDto> updateUser(@PathVariable long id, @RequestBody UserUpdateDto dto){
		return ResponseEntity.ok(userService.updateUser(id, dto));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteUser(@PathVariable long id){
		userService.deleteUser(id);
		return ResponseEntity.noContent().build();
	}

	@DeleteMapping("/me")
	public ResponseEntity<Void> deleteCurrentUser(@AuthenticationPrincipal UserPrincipal principal){
		userService.deleteUser(principal.getId());
		return ResponseEntity.noContent().build();
	}
}
