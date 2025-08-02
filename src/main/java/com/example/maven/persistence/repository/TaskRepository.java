package com.example.maven.persistence.repository;

import com.example.maven.persistence.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskRepository extends JpaRepository<User, Long> {
}
