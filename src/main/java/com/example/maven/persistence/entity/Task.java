package com.example.maven.persistence.entity;

import com.example.maven.enums.TaskStatus;
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
@Table(name = "tasks")
public class Task extends TenantEntity{
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "project_id", nullable = false)
	private Project project;

	@Column(length = 30, nullable = false)
	private String name;

	private String description;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "assignee_id")
	private User assignee;

	@Enumerated(EnumType.STRING)
	private TaskStatus status = TaskStatus.TODO;

	@Column(nullable = false, updatable = false)
	private LocalDateTime createdAt;

	private LocalDateTime dueTo;

	@PrePersist
	public void onCreate() {
		this.createdAt = LocalDateTime.now();
	}
}
