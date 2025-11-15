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
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock private CommentRepository commentRepository;
    @Mock private TaskRepository taskRepository;
    @Mock private CompanyRepository companyRepository;
    @Mock private UserRepository userRepository;
    @Mock private CommentMapper commentMapper;
    @Mock private SecurityUtils securityUtils;

    @InjectMocks
    private CommentService commentService;

    private static final long TENANT_ID = 99L;
    private static final long CURRENT_USER_ID = 777L;

    private Company company;
    private Task task;
    private User author;

    @BeforeEach
    void setUp() {
        // Mock instance methods instead of static
        when(securityUtils.getCurrentTenantId()).thenReturn(TENANT_ID);
        when(securityUtils.getCurrentUserId()).thenReturn(CURRENT_USER_ID);

        company = new Company(); company.setId(TENANT_ID);
        task = new Task(); task.setId(10L);
        author = new User(); author.setId(CURRENT_USER_ID);
    }

    // No need for tearDown() anymore - no static mocking!

    // createComment
    @Test
    void givenValidDto_whenCreateComment_thenMapsSavesAndReturnsDto() {
        CommentCreateDto dto = mock(CommentCreateDto.class);
        when(dto.taskId()).thenReturn(10L);

        Comment mapped = new Comment();
        Comment saved = new Comment(); saved.setId(123L);
        CommentResponseDto response = mock(CommentResponseDto.class);

        when(taskRepository.findByIdAndCompany_Id(10L, TENANT_ID)).thenReturn(Optional.of(task));
        when(companyRepository.findById(TENANT_ID)).thenReturn(Optional.of(company));
        when(userRepository.findByIdAndCompany_Id(CURRENT_USER_ID, TENANT_ID)).thenReturn(Optional.of(author));
        when(commentMapper.fromCreateDto(dto, task, company, author)).thenReturn(mapped);
        when(commentRepository.save(mapped)).thenReturn(saved);
        when(commentMapper.toResponseDto(saved)).thenReturn(response);

        CommentResponseDto result = commentService.createComment(dto);

        assertThat(result).isEqualTo(response);
        verify(commentMapper).fromCreateDto(dto, task, company, author);
        verify(commentRepository).save(mapped);
    }

    @Test
    void givenMissingTask_whenCreateComment_thenThrowsNotFound() {
        CommentCreateDto dto = mock(CommentCreateDto.class);
        when(dto.taskId()).thenReturn(404L);
        when(taskRepository.findByIdAndCompany_Id(404L, TENANT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commentService.createComment(dto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Task not found");

        verifyNoInteractions(companyRepository, userRepository, commentMapper, commentRepository);
    }

    @Test
    void givenMissingCompany_whenCreateComment_thenThrowsNotFound() {
        CommentCreateDto dto = mock(CommentCreateDto.class);
        when(dto.taskId()).thenReturn(10L);
        when(taskRepository.findByIdAndCompany_Id(10L, TENANT_ID)).thenReturn(Optional.of(task));
        when(companyRepository.findById(TENANT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commentService.createComment(dto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Company not found");

        verifyNoInteractions(userRepository, commentMapper, commentRepository);
    }

    @Test
    void givenMissingAuthor_whenCreateComment_thenThrowsNotFound() {
        CommentCreateDto dto = mock(CommentCreateDto.class);
        when(dto.taskId()).thenReturn(10L);
        when(taskRepository.findByIdAndCompany_Id(10L, TENANT_ID)).thenReturn(Optional.of(task));
        when(companyRepository.findById(TENANT_ID)).thenReturn(Optional.of(company));
        when(userRepository.findByIdAndCompany_Id(CURRENT_USER_ID, TENANT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commentService.createComment(dto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found");

        verifyNoInteractions(commentMapper, commentRepository);
    }

    // findCommentById
    @Test
    void givenExistingComment_whenFindCommentById_thenReturnDto() {
        Comment c = new Comment(); c.setId(5L);
        CommentResponseDto response = mock(CommentResponseDto.class);

        when(commentRepository.findByIdAndCompany_Id(5L, TENANT_ID)).thenReturn(Optional.of(c));
        when(commentMapper.toResponseDto(c)).thenReturn(response);

        assertThat(commentService.findCommentById(5L)).isEqualTo(response);
    }

    @Test
    void givenMissingComment_whenFindCommentById_thenThrowNotFound() {
        when(commentRepository.findByIdAndCompany_Id(404L, TENANT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commentService.findCommentById(404L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Comment not found");
    }

    // findAllCommentsByTaskId
    @Test
    void givenTaskId_whenFindAllCommentsByTaskId_thenMapsAll() {
        Comment c1 = new Comment(); c1.setId(1L);
        Comment c2 = new Comment(); c2.setId(2L);
        CommentResponseDto d1 = mock(CommentResponseDto.class);
        CommentResponseDto d2 = mock(CommentResponseDto.class);

        when(commentRepository.findAllByTask_IdAndCompany_Id(10L, TENANT_ID)).thenReturn(List.of(c1, c2));
        when(commentMapper.toResponseDto(c1)).thenReturn(d1);
        when(commentMapper.toResponseDto(c2)).thenReturn(d2);

        List<CommentResponseDto> result = commentService.findAllCommentsByTaskId(10L);

        assertThat(result).containsExactly(d1, d2);
        verify(commentRepository).findAllByTask_IdAndCompany_Id(10L, TENANT_ID);
    }

    // updateMyCommentById
    @Test
    void givenAuthorIsCurrentUser_whenUpdateMyCommentById_thenUpdatesAndReturnsDto() {
        long id = 12L;
        CommentUpdateDto dto = mock(CommentUpdateDto.class);

        Comment existing = new Comment(); existing.setId(id);
        existing.setAuthor(author);

        Comment saved = new Comment(); saved.setId(id);
        CommentResponseDto response = mock(CommentResponseDto.class);

        when(commentRepository.findByIdAndCompany_Id(id, TENANT_ID)).thenReturn(Optional.of(existing));
        doAnswer(inv -> null).when(commentMapper).updateFromDto(existing, dto);
        when(commentRepository.save(existing)).thenReturn(saved);
        when(commentMapper.toResponseDto(saved)).thenReturn(response);

        CommentResponseDto result = commentService.updateMyCommentById(id, dto);

        assertThat(result).isEqualTo(response);
        verify(commentMapper).updateFromDto(existing, dto);
        verify(commentRepository).save(existing);
    }

    @Test
    void givenNotAuthor_whenUpdateMyCommentById_thenThrowsAccessDenied() {
        long id = 13L;
        Comment existing = new Comment(); existing.setId(id);
        User another = new User(); another.setId(555L);
        existing.setAuthor(another);

        when(commentRepository.findByIdAndCompany_Id(id, TENANT_ID)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> commentService.updateMyCommentById(id, mock(CommentUpdateDto.class)))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("author is not the current user");

        verify(commentRepository, never()).save(any());
    }

    @Test
    void givenMissingComment_whenUpdateMyCommentById_thenThrowsNotFound() {
        when(commentRepository.findByIdAndCompany_Id(404L, TENANT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commentService.updateMyCommentById(404L, mock(CommentUpdateDto.class)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Comment not found");
    }

    // deleteMyCommentById
    @Test
    void givenAuthorIsCurrentUser_whenDeleteMyCommentById_thenDeletes() {
        long id = 21L;
        Comment existing = new Comment(); existing.setId(id);
        existing.setAuthor(author);

        when(commentRepository.findByIdAndCompany_Id(id, TENANT_ID)).thenReturn(Optional.of(existing));

        commentService.deleteMyCommentById(id);

        verify(commentRepository).delete(existing);
    }

    @Test
    void givenNotAuthor_whenDeleteMyCommentById_thenThrowsAccessDenied() {
        long id = 22L;
        Comment existing = new Comment(); existing.setId(id);
        User another = new User(); another.setId(1L);
        existing.setAuthor(another);

        when(commentRepository.findByIdAndCompany_Id(id, TENANT_ID)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> commentService.deleteMyCommentById(id))
                .isInstanceOf(AccessDeniedException.class);

        verify(commentRepository, never()).delete(any());
    }

    @Test
    void givenMissingComment_whenDeleteMyCommentById_thenThrowsNotFound() {
        when(commentRepository.findByIdAndCompany_Id(23L, TENANT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commentService.deleteMyCommentById(23L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Comment not found");
    }
}