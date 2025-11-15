package com.example.maven.service;

import com.example.maven.api.dto.request.comment.CommentCreateDto;
import com.example.maven.api.dto.request.comment.CommentUpdateDto;
import com.example.maven.api.dto.response.CommentResponseDto;
import com.example.maven.api.mapper.CommentMapper;
import com.example.maven.exception.ResourceNotFoundException;
import com.example.maven.persistence.entity.Comment;
import com.example.maven.persistence.entity.Company;
import com.example.maven.persistence.entity.Task;
import com.example.maven.persistence.entity.User;
import com.example.maven.persistence.repository.CommentRepository;
import com.example.maven.persistence.repository.CompanyRepository;
import com.example.maven.persistence.repository.TaskRepository;
import com.example.maven.persistence.repository.UserRepository;
import com.example.maven.utils.SecurityUtils;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
@AllArgsConstructor
public class CommentService {
	private final CommentRepository commentRepository;
	private final TaskRepository taskRepository;
	private final CompanyRepository companyRepository;
	private final UserRepository userRepository;
	private final CommentMapper commentMapper;
	private final SecurityUtils securityUtils;

	// Evict all task comments caches when creating (we don't know which taskId caches exist)
	@CacheEvict(value = "taskComments", allEntries = true)
	public CommentResponseDto createComment(CommentCreateDto dto){
		long tenantId = securityUtils.getCurrentTenantId();

		Task task = taskRepository.findByIdAndCompany_Id(dto.taskId(), tenantId)
				.orElseThrow(() -> new ResourceNotFoundException("Task not found"));

		Company company = companyRepository.findById(tenantId)
				.orElseThrow(() -> new ResourceNotFoundException("Company not found"));

		User author = userRepository.findByIdAndCompany_Id(securityUtils.getCurrentUserId(), tenantId)
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));

		Comment comment = commentMapper.fromCreateDto(dto, task, company, author);

		Comment savedComment = commentRepository.save(comment);

		return commentMapper.toResponseDto(savedComment);
	}

	// Cache individual comment by ID
	@Cacheable(value = "comments", key = "#id + '_' + @securityUtils.getCurrentTenantId()")
	public CommentResponseDto findCommentById(long id){
		return commentMapper.toResponseDto(findById(id));
	}

	// Cache comments list per task
	@Cacheable(value = "taskComments", key = "#taskId + '_' + @securityUtils.getCurrentTenantId()")
	public List<CommentResponseDto> findAllCommentsByTaskId(long taskId){
		long tenantId = securityUtils.getCurrentTenantId();

		return commentRepository.findAllByTask_IdAndCompany_Id(taskId, tenantId).stream()
				.map(commentMapper::toResponseDto)
				.toList();
	}

	// Evict both individual comment cache and all task comments lists
	@Caching(evict = {
			@CacheEvict(value = "comments", key = "#id + '_' + @securityUtils.getCurrentTenantId()"),
			@CacheEvict(value = "taskComments", allEntries = true)
	})
	public CommentResponseDto updateMyCommentById(long id, CommentUpdateDto dto){
		var comment = checkAuthor(id);
		commentMapper.updateFromDto(comment, dto);
		var savedComment = commentRepository.save(comment);

		return commentMapper.toResponseDto(savedComment);
	}

	// Evict both individual comment cache and all task comments lists
	@Caching(evict = {
			@CacheEvict(value = "comments", key = "#id + '_' + @securityUtils.getCurrentTenantId()"),
			@CacheEvict(value = "taskComments", allEntries = true)
	})
	public void deleteMyCommentById(long id){
		var comment = checkAuthor(id);
		commentRepository.delete(comment);
	}

	private Comment findById(long id){
		return commentRepository.findByIdAndCompany_Id(id, securityUtils.getCurrentTenantId())
				.orElseThrow(() -> new ResourceNotFoundException("Comment not found"));
	}

	private Comment checkAuthor(long id){
		long currentUserId = securityUtils.getCurrentUserId();
		Comment comment = findById(id);
		if(comment.getAuthor().getId() != currentUserId){
			throw new AccessDeniedException("Comment author is not the current user");
		}
		return comment;
	}
}