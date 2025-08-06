package com.example.maven.persistence.repository;

import com.example.maven.persistence.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
	Optional<Comment> findByIdAndCompany_Id(long id, long companyId);
	List<Comment> findAllByTask_IdAndCompany_Id(long taskId, long companyId);
}
