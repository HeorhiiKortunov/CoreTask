package com.example.maven.persistence.repository;

import com.example.maven.persistence.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
	Optional<Project> findByIdAndCompany_Id(long id, long companyId);
	List<Project> findAllByCompany_Id(long companyId);
}
