package pl.ttsw.GameRev.service;

import jakarta.persistence.criteria.Join;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import pl.ttsw.GameRev.dto.ForumCommentDTO;
import pl.ttsw.GameRev.dto.ForumPostDTO;
import pl.ttsw.GameRev.mapper.ForumCommentMapper;
import pl.ttsw.GameRev.mapper.ForumPostMapper;
import pl.ttsw.GameRev.model.ForumComment;
import pl.ttsw.GameRev.model.ForumPost;
import pl.ttsw.GameRev.model.WebsiteUser;
import pl.ttsw.GameRev.repository.ForumCommentRepository;
import pl.ttsw.GameRev.repository.ForumPostRepository;
import pl.ttsw.GameRev.repository.RoleRepository;

import java.time.LocalDateTime;

@Service
public class ForumCommentService {
    private final ForumCommentRepository forumCommentRepository;
    private final ForumPostRepository forumPostRepository;
    private final RoleRepository roleRepository;
    private final ForumCommentMapper forumCommentMapper;
    private final ForumPostMapper forumPostMapper;
    private final WebsiteUserService websiteUserService;

    public ForumCommentService(ForumCommentRepository forumCommentRepository, ForumPostRepository forumPostRepository, RoleRepository roleRepository, ForumCommentMapper forumCommentMapper, ForumPostMapper forumPostMapper, WebsiteUserService websiteUserService) {
        this.forumCommentRepository = forumCommentRepository;
        this.forumPostRepository = forumPostRepository;
        this.roleRepository = roleRepository;
        this.forumCommentMapper = forumCommentMapper;
        this.forumPostMapper = forumPostMapper;
        this.websiteUserService = websiteUserService;
    }

    public Page<ForumCommentDTO> getForumCommentsByPost(Long id, Long userId, String searchText, Pageable pageable) {
        ForumPost post = forumPostRepository.findById(id).orElse(null);
        if (post == null) {
            return null;
        }
        Specification<ForumComment> spec = ((root, query, builder) -> builder.equal(root.get("forumPost"), post));
        if (userId != null) {
            spec = spec.and((root, query, builder) -> {
                Join<ForumComment, WebsiteUser> join = root.join("author");
                return builder.equal(join.get("id"), userId);
            });
        }

        if (searchText != null) {
            searchText = searchText.toLowerCase();
            String likePattern = "%" + searchText + "%";
            spec = spec.and((root, query, builder) -> builder.like(builder.lower(root.get("content")), likePattern));
        }
        Page<ForumComment> forumComments = forumCommentRepository.findAll(spec, pageable);
        //TODO: Add SimplifiedUserDTO for those types of situations
        for (ForumComment forumComment : forumComments) {
            forumComment.getAuthor().setPassword(null);
            forumComment.getAuthor().setUsername(null);
            forumComment.getAuthor().setEmail(null);
            forumComment.getAuthor().setIsBanned(null);
            forumComment.getAuthor().setIsDeleted(null);
            forumComment.getAuthor().setRoles(null);
        }
        return forumComments.map(forumCommentMapper::toDto);
    }

    public ForumPostDTO getOriginalPost(Long id) {
        ForumPost post = forumPostRepository.findById(id).orElse(null);
        if (post == null) {
            return null;
        }
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
        if (currentUser.getRoles().contains(roleRepository.findByRoleName("Admin").get()) || currentUser == forumComment.getAuthor()) {
            forumCommentRepository.save(forumComment);
            return forumCommentMapper.toDto(forumComment);
        }else {
            throw new BadCredentialsException("You dont have permission to perform this action");
        }
    }

    public boolean deleteForumComment(Long id) {
        WebsiteUser currentUser = websiteUserService.getCurrentUser();
        ForumComment forumComment = forumCommentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Forum comment not found"));
        if (currentUser.getRoles().contains(roleRepository.findByRoleName("Admin").get()) || currentUser == forumComment.getAuthor()) {
            forumCommentRepository.delete(forumComment);
            return true;
        }else {
            throw new BadCredentialsException("You dont have permission to perform this action");
        }
    }
}
