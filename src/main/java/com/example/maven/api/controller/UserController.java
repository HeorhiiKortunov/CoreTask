package com.example.maven.api.controller;

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

	@GetMapping("/me")
	public ResponseEntity<UserResponseDto> getMyUserProfile(@AuthenticationPrincipal UserPrincipal principal){
		return ResponseEntity.ok(userService.findById(principal.getId()));
	}

	@GetMapping
	public ResponseEntity<List<UserResponseDto>> getAllUsers(){
		return ResponseEntity.ok(userService.findCompanyUsers());
	}

	@PutMapping("/{id}")
	public ResponseEntity<UserResponseDto> updateUserRoles(@PathVariable long id, @RequestBody UserUpdateRolesDto dto){
		return ResponseEntity.ok(userService.updateUserRolesById(id, dto));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteUser(@PathVariable long id){
		userService.deleteUser(id);
		return ResponseEntity.noContent().build();
	}
}
