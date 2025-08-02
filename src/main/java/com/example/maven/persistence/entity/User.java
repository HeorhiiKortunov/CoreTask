package com.example.maven.persistence.entity;

import com.example.maven.enums.Role;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User extends TenantEntity{
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	long id;

	@Column(nullable = false)
	private String username;

	private String displayedName;

	@Column(nullable = false, unique = true)
	private String email;

	@Column(nullable = false)
	private String password;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private Role role;
}
