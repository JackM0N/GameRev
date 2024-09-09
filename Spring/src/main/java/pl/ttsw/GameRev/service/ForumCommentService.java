package pl.ttsw.GameRev.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.Join;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

    private final String commentPicDirectory = "../Pictures/comment_pics";

    public Page<ForumCommentDTO> getForumCommentsByPost(Long id, ForumCommentFilter forumCommentFilter, Pageable pageable) {
        ForumPost post = forumPostRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Forum post not found"));

        Specification<ForumComment> spec = getForumCommentSpecification(forumCommentFilter, post);
        Page<ForumComment> forumComments = forumCommentRepository.findAll(spec, pageable);
        post.setViews(post.getViews() + 1);
        forumPostRepository.save(post);
        return forumComments.map(forumCommentMapper::toDto);
    }

    public ForumPostDTO getOriginalPost(Long id) {
        ForumPost post = forumPostRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Forum post not found"));
        return forumPostMapper.toDto(post);
    }

    public ForumCommentDTO createForumComment(ForumCommentDTO forumCommentDTO, MultipartFile picture) throws IOException {
        ForumComment forumComment = forumCommentMapper.toEntity(forumCommentDTO);
        forumComment.setForumPost(forumPostRepository.findById(forumCommentDTO.getForumPostId())
                .orElseThrow(() -> new RuntimeException("Forum post not found")));

        forumComment.setAuthor(websiteUserService.getCurrentUser());

        forumComment = forumCommentRepository.save(forumComment);

        Path filepath = null;
        try {
            if(picture != null && !picture.isEmpty()) {
                String fileName = "comment"+ forumComment.getId() + "_" + picture.getOriginalFilename();
                filepath = Paths.get(commentPicDirectory, fileName);
                Files.copy(picture.getInputStream(), filepath);
                forumComment.setPicture(filepath.toString());
                forumComment = forumCommentRepository.save(forumComment);
            }
        } catch (IOException e) {
            if (Files.exists(filepath)) {
                try {
                    Files.delete(filepath);
                } catch (IOException ioException) {
                    System.err.println("Failed to delete file after an error: " + filepath);
                }
            }
            throw e;
        }

        return forumCommentMapper.toDto(forumComment);
    }

    public ForumCommentDTO updateForumComment(Long id, ForumCommentDTO forumCommentDTO, MultipartFile picture) throws IOException {
        WebsiteUser currentUser = websiteUserService.getCurrentUser();
        ForumComment forumComment = forumCommentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Forum comment not found"));

        if (currentUser.getRoles().stream().anyMatch(role -> "Admin".equals(role.getRoleName()))
                || currentUser == forumComment.getAuthor()) {
            forumCommentMapper.partialUpdateContent(forumCommentDTO, forumComment);

            if(picture != null && !picture.isEmpty()) {
                String oldPicturePath = forumComment.getPicture();
                if(oldPicturePath != null && !oldPicturePath.isEmpty()) {
                    Path oldFilePath = Paths.get(oldPicturePath);
                    Files.deleteIfExists(oldFilePath);
                }

                String fileName = "post" + forumComment.getId() + "_" + picture.getOriginalFilename();
                Path filepath = Paths.get(commentPicDirectory, fileName);
                Files.copy(picture.getInputStream(), filepath);
                forumComment.setPicture(filepath.toString());
            }

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

    public byte[] getCommentPicture(Long id) throws IOException {
        ForumComment forumComment = forumCommentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Forum comment not found"));

        if (forumComment.getPicture() == null) {
            throw new IOException("Comment picture not found");
        }

        Path filepath = Paths.get(forumComment.getPicture());
        return Files.readAllBytes(filepath);
    }
}
