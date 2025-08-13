package com.example.maven.api.controller;

import com.example.maven.api.dto.request.user.UserUpdateDto;
import com.example.maven.api.dto.request.user.UserUpdateRolesDto;
import com.example.maven.api.dto.response.UserResponseDto;
import com.example.maven.security.UserPrincipal;
import com.example.maven.service.UserService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@AllArgsConstructor
public class UserController {
	private final UserService userService;

	@GetMapping
	@PreAuthorize("hasRole('MEMBER')")
	public ResponseEntity<List<UserResponseDto>> getAllUsers(){
		return ResponseEntity.ok(userService.findCompanyUsers());
	}

	@GetMapping("/me")
	@PreAuthorize("hasRole('MEMBER')")
	public ResponseEntity<UserResponseDto> getCurrentProfile(@AuthenticationPrincipal UserPrincipal principal){
		return ResponseEntity.ok(userService.findById(principal.getId()));
	}

	@GetMapping("/{id}")
	@PreAuthorize("hasRole('MEMBER')")
	public ResponseEntity<UserResponseDto> getUserProfile(@PathVariable long id){
		return ResponseEntity.ok(userService.findById(id));
	}

	@PutMapping("/roles/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<UserResponseDto> updateUserRoles(@PathVariable long id, @Valid @RequestBody UserUpdateRolesDto dto){
		return ResponseEntity.ok(userService.updateUserRolesById(id, dto));
	}

	@PatchMapping("/me")
	@PreAuthorize("hasRole('MEMBER')")
	public ResponseEntity<UserResponseDto> updateCurrentUser(@AuthenticationPrincipal UserPrincipal principal,
	                                                         @Valid @RequestBody UserUpdateRolesDto dto
	){
		return ResponseEntity.ok(userService.updateUserRolesById(principal.getId(), dto));
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<UserResponseDto> updateUser(@PathVariable long id, @Valid @RequestBody UserUpdateDto dto){
		return ResponseEntity.ok(userService.updateUser(id, dto));
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<Void> deleteUser(@PathVariable long id){
		userService.deleteUser(id);
		return ResponseEntity.noContent().build();
	}

	@DeleteMapping("/me")
	@PreAuthorize("hasRole('MEMBER')")
	public ResponseEntity<Void> deleteCurrentUser(@AuthenticationPrincipal UserPrincipal principal){
		userService.deleteUser(principal.getId());
		return ResponseEntity.noContent().build();
	}
}
