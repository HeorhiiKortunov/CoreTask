package com.example.maven.persistence.repository;

import com.example.maven.persistence.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
	Optional<User> findByIdAndCompany_Id(long id, long companyId);
	List<User> findAllByCompany_Id(long companyId);
}
