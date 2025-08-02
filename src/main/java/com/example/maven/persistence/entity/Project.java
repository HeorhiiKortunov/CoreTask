package com.example.maven.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "projects")
public class Project extends TenantEntity{
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	long id;

	@Column(length = 20)
	private String name;

	private String description;

	@Column(nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@PrePersist
	public void onCreate() {
		this.createdAt = LocalDateTime.now();
	}
}
