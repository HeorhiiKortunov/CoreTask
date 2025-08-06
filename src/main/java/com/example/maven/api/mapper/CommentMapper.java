package com.example.maven.api.mapper;

import com.example.maven.api.dto.request.comment.CommentCreateDto;
import com.example.maven.api.dto.request.comment.CommentUpdateDto;
import com.example.maven.api.dto.response.CommentResponseDto;
import com.example.maven.persistence.entity.Comment;
import com.example.maven.persistence.entity.Company;
import com.example.maven.persistence.entity.Task;
import com.example.maven.persistence.entity.User;
import org.springframework.stereotype.Component;

@Component
public class CommentMapper {
	public Comment fromCreateDto(CommentCreateDto dto, Task task, Company company, User author){
		Comment comment = new Comment();
		comment.setTask(task);
		comment.setCompany(company);
		comment.setAuthor(author);
		comment.setContents(dto.contents());

		return comment;
	}

	public CommentResponseDto toResponseDto(Comment comment){
		return new CommentResponseDto(
				comment.getId(),
				comment.getTask().getId(),
				comment.getAuthor().getId(),
				comment.getContents()
		);
	}

	public void updateFromDto(Comment comment, CommentUpdateDto dto){
		if(dto.getContents() != null) comment.setContents(dto.getContents());
	}

}
