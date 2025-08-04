package com.example.maven.persistence.repository;

import com.example.maven.persistence.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
	Optional<Task> findByIdAndCompany_Id(long companyId, Long id);
	List<Task> findAllByCompany_IdAndProject_Id(Long companyId, Long projectId);
}
