package pl.ttsw.GameRev.service;

import jakarta.persistence.criteria.Join;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import pl.ttsw.GameRev.dto.ForumCommentDTO;
import pl.ttsw.GameRev.mapper.ForumCommentMapper;
import pl.ttsw.GameRev.model.ForumComment;
import pl.ttsw.GameRev.model.ForumPost;
import pl.ttsw.GameRev.model.WebsiteUser;
import pl.ttsw.GameRev.repository.ForumCommentRepository;
import pl.ttsw.GameRev.repository.ForumPostRepository;
import pl.ttsw.GameRev.repository.RoleRepository;

import java.time.LocalDate;

@Service
public class ForumCommentService {
    private final ForumCommentRepository forumCommentRepository;
    private final ForumPostRepository forumPostRepository;
    private final RoleRepository roleRepository;
    private final ForumCommentMapper forumCommentMapper;
    private final WebsiteUserService websiteUserService;

    public ForumCommentService(ForumCommentRepository forumCommentRepository, ForumPostRepository forumPostRepository, RoleRepository roleRepository, ForumCommentMapper forumCommentMapper, WebsiteUserService websiteUserService) {
        this.forumCommentRepository = forumCommentRepository;
        this.forumPostRepository = forumPostRepository;
        this.roleRepository = roleRepository;
        this.forumCommentMapper = forumCommentMapper;
        this.websiteUserService = websiteUserService;
    }

    public Page<ForumCommentDTO> getForumCommentsByPost(Long id, Long userId, String searchText, Pageable pageable) {
        ForumPost post = forumPostRepository.findById(id).orElse(null);
        if (post == null) {
            return null;
        }
        Specification<ForumComment> spec = ((root, query, builder) -> builder.equal(root.get("post"), post));

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
        return forumComments.map(forumCommentMapper::toDto);
    }

    public ForumCommentDTO createForumComment(ForumCommentDTO forumCommentDTO) {
        ForumComment forumComment = new ForumComment();
        forumComment.setForumPost(forumPostRepository.findById(forumCommentDTO.getForumPost().getId())
                .orElseThrow(() -> new RuntimeException("Forum post not found")));
        forumComment.setAuthor(websiteUserService.getCurrentUser());
        forumComment.setContent(forumCommentDTO.getContent());
        forumComment.setPostDate(LocalDate.now());
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
