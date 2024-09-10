package pl.ttsw.GameRev.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import pl.ttsw.GameRev.dto.ForumPostDTO;
import pl.ttsw.GameRev.filter.ForumPostFilter;
import pl.ttsw.GameRev.model.Forum;
import pl.ttsw.GameRev.model.ForumPost;
import pl.ttsw.GameRev.model.WebsiteUser;
import pl.ttsw.GameRev.repository.*;
import pl.ttsw.GameRev.mapper.ForumPostMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ForumPostService {
    private final ForumPostRepository forumPostRepository;
    private final ForumRepository forumRepository;
    private final ForumPostMapper forumPostMapper;
    private final ForumModeratorRepository forumModeratorRepository;

    private final String postPicDirectory = "../Pictures/post_pics";
    private final WebsiteUserService websiteUserService;

    public Page<ForumPostDTO> getForumPosts(Long id, ForumPostFilter forumPostFilter, Pageable pageable) {
        Forum forum = forumRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Forum not found"));
        Specification<ForumPost> spec = (root, query, builder) -> builder.equal(root.get("forum"), forum);
        spec = spec.and((root, query, builder) -> builder.equal(root.get("isDeleted"), false));

        if (forumPostFilter.getPostDateFrom() != null) {
            spec = spec.and((root, query, builder) -> builder.greaterThanOrEqualTo(root.get("postDate"), forumPostFilter.getPostDateFrom()));
        }
        if (forumPostFilter.getPostDateTo() != null) {
            spec = spec.and((root, query, builder) -> builder.lessThanOrEqualTo(root.get("postDate"), forumPostFilter.getPostDateTo()));
        }
        if (forumPostFilter.getSearchText() != null) {
            String likePattern = "%" + forumPostFilter.getSearchText().toLowerCase() + "%";
            spec = spec.and((root, query, builder) -> builder.or(
                    builder.like(builder.lower(root.get("title")), likePattern),
                    builder.like(builder.lower(root.get("content")), likePattern)
            ));
        }
        Page<ForumPost> forumPosts = forumPostRepository.findAll(spec, pageable);
        return forumPosts.map(forumPostMapper::toDto);
    }

    public ForumPostDTO createForumPost(ForumPostDTO forumPostDTO, MultipartFile picture) throws IOException {
        ForumPost forumPost = forumPostMapper.toEntity(forumPostDTO);
        forumPost.setForum(forumRepository.findById(forumPostDTO.getForum().getId())
                .orElseThrow(() -> new RuntimeException("Forum not found")));

        forumPost.setAuthor(websiteUserService.getCurrentUser());

        forumPost = forumPostRepository.save(forumPost);
        Path filepath = null;
        try {
            if (picture != null && !picture.isEmpty()) {
                String fileName = "post"+ forumPost.getId() + "_" + picture.getOriginalFilename();
                filepath = Paths.get(postPicDirectory, fileName);
                Files.copy(picture.getInputStream(), filepath);
                forumPost.setPicture(filepath.toString());
                forumPost = forumPostRepository.save(forumPost);
            }
        } catch (Exception e) {
            if (filepath != null && Files.exists(filepath)) {
                try {
                    Files.delete(filepath);
                } catch (IOException ioException) {
                    System.err.println("Failed to delete file after an error: " + filepath);
                }
            }
            throw e;
        }
        return forumPostMapper.toDto(forumPost);
    }

    public ForumPostDTO updateForumPost(Long id, ForumPostDTO forumPostDTO, MultipartFile picture) throws IOException {
        WebsiteUser currentUser = websiteUserService.getCurrentUser();
        ForumPost forumPost = forumPostRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Forum post not found"));

        if (currentUser == forumPost.getAuthor() || currentUser.getRoles().stream()
                .anyMatch(role -> "Admin".equals(role.getRoleName()))) {

            forumPostMapper.partialUpdate(forumPostDTO, forumPost);

            if (picture != null && !picture.isEmpty()) {
                String oldPicturePath = forumPost.getPicture();
                if (oldPicturePath != null && !oldPicturePath.isEmpty()) {
                    Path oldFilePath = Paths.get(oldPicturePath);
                    Files.deleteIfExists(oldFilePath);
                }

                String fileName = "post" + forumPost.getId() + "_" + picture.getOriginalFilename();
                Path filepath = Paths.get(postPicDirectory, fileName);
                Files.copy(picture.getInputStream(), filepath);
                forumPost.setPicture(filepath.toString());
            }

            forumPost = forumPostRepository.save(forumPost);
            return forumPostMapper.toDto(forumPost);
        } else {
            throw new BadCredentialsException("You dont have permission to perform this action");
        }
    }

    public boolean deleteForumPost(Long id, Boolean isDeleted) {
        WebsiteUser currentUser = websiteUserService.getCurrentUser();
        ForumPost forumPost = forumPostRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Forum post not found"));
        Forum forum = forumPost.getForum();

        boolean isAdmin = currentUser.getRoles().stream()
                .anyMatch(role -> "Admin".equals(role.getRoleName()));
        boolean isAuthor = currentUser.equals(forumPost.getAuthor());

        boolean isModerator = forumModeratorRepository.existsByForumAndModerator(forum, currentUser);

        if (isAdmin || isAuthor || isModerator) {
            forumPost.setIsDeleted(isDeleted);
            if (isDeleted) {
                forumPost.setDeletedAt(LocalDateTime.now());
            }else{
                forumPost.setDeletedAt(null);
            }
            forumPostRepository.save(forumPost);
            return true;
        }else {
            throw new BadCredentialsException("You dont have permission to perform this action");
        }
    }

    public byte[] getPostPicture(Long postId) throws IOException {
        ForumPost forumPost = forumPostRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        if (forumPost.getPicture() == null) {
            throw new IOException("Post picture not found");
        }

        Path filepath = Paths.get(forumPost.getPicture());
        return Files.readAllBytes(filepath);
    }
}
