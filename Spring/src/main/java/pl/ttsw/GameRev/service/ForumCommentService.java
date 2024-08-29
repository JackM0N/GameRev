package pl.ttsw.GameRev.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.Join;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import pl.ttsw.GameRev.dto.ForumCommentDTO;
import pl.ttsw.GameRev.dto.ForumPostDTO;
import pl.ttsw.GameRev.filter.ForumCommentFilter;
import pl.ttsw.GameRev.mapper.ForumCommentMapper;
import pl.ttsw.GameRev.mapper.ForumPostMapper;
import pl.ttsw.GameRev.model.Forum;
import pl.ttsw.GameRev.model.ForumComment;
import pl.ttsw.GameRev.model.ForumPost;
import pl.ttsw.GameRev.model.WebsiteUser;
import pl.ttsw.GameRev.repository.*;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ForumCommentService {
    private final ForumCommentRepository forumCommentRepository;
    private final ForumPostRepository forumPostRepository;
    private final ForumCommentMapper forumCommentMapper;
    private final ForumPostMapper forumPostMapper;
    private final WebsiteUserService websiteUserService;
    private final ForumModeratorRepository forumModeratorRepository;

    public Page<ForumCommentDTO> getForumCommentsByPost(Long id, ForumCommentFilter forumCommentFilter, Pageable pageable) {
        ForumPost post = forumPostRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Forum post not found"));
        Specification<ForumComment> spec = getForumCommentSpecification(forumCommentFilter, post);
        Page<ForumComment> forumComments = forumCommentRepository.findAll(spec, pageable);
        return forumComments.map(forumCommentMapper::toDto);
    }

    public ForumPostDTO getOriginalPost(Long id) {
        ForumPost post = forumPostRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Forum post not found"));
        return forumPostMapper.toDto(post);
    }

    public ForumCommentDTO createForumComment(ForumCommentDTO forumCommentDTO) {
        ForumComment forumComment = new ForumComment();
        forumComment.setForumPost(forumPostRepository.findById(forumCommentDTO.getForumPostId())
                .orElseThrow(() -> new RuntimeException("Forum post not found")));
        forumComment.setAuthor(websiteUserService.getCurrentUser());
        forumComment.setContent(forumCommentDTO.getContent());
        forumComment.setPostDate(LocalDateTime.now());
        forumCommentRepository.save(forumComment);
        return forumCommentMapper.toDto(forumComment);
    }

    public ForumCommentDTO updateForumComment(Long id, ForumCommentDTO forumCommentDTO) {
        WebsiteUser currentUser = websiteUserService.getCurrentUser();
        ForumComment forumComment = forumCommentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Forum comment not found"));
        forumComment.setContent(forumCommentDTO.getContent());
        if (currentUser.getRoles().stream().anyMatch(role -> "Admin".equals(role.getRoleName()))
                || currentUser == forumComment.getAuthor()) {
            forumCommentRepository.save(forumComment);
            return forumCommentMapper.toDto(forumComment);
        }else {
            throw new BadCredentialsException("You dont have permission to perform this action");
        }
    }

    public boolean deleteForumComment(Long id, Boolean isDeleted) {
        WebsiteUser currentUser = websiteUserService.getCurrentUser();
        ForumComment forumComment = forumCommentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Forum comment not found"));
        Forum forum = forumComment.getForumPost().getForum();

        boolean isAdmin = currentUser.getRoles().stream()
                .anyMatch(role -> "Admin".equals(role.getRoleName()));

        boolean isAuthor = currentUser.equals(forumComment.getAuthor());

        boolean isModerator = forumModeratorRepository.existsByForumAndModerator(forum, currentUser);

        if (isAdmin || isAuthor || isModerator) {
            forumComment.setIsDeleted(isDeleted);
            if (isDeleted) {
                forumComment.setDeletedAt(LocalDateTime.now());
            }else {
                forumComment.setDeletedAt(null);
            }
            forumCommentRepository.save(forumComment);
            return true;
        }else {
            throw new BadCredentialsException("You dont have permission to perform this action");
        }
    }

    private static Specification<ForumComment> getForumCommentSpecification(ForumCommentFilter forumCommentFilter, ForumPost post) {
        Specification<ForumComment> spec = ((root, query, builder) -> builder.equal(root.get("forumPost"), post));
        spec = spec.and((root, query, builder) -> builder.equal(root.get("isDeleted"), false));
        if (forumCommentFilter.getUserId() != null) {
            spec = spec.and((root, query, builder) -> {
                Join<ForumComment, WebsiteUser> join = root.join("author");
                return builder.equal(join.get("id"), forumCommentFilter.getUserId());
            });
        }

        if (forumCommentFilter.getSearchText() != null) {
            String likePattern = "%" + forumCommentFilter.getSearchText().toLowerCase() + "%";
            spec = spec.and((root, query, builder) -> builder.like(builder.lower(root.get("content")), likePattern));
        }
        return spec;
    }
}
