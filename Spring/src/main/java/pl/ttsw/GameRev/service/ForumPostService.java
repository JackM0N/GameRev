package pl.ttsw.GameRev.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import pl.ttsw.GameRev.dto.ForumPostDTO;
import pl.ttsw.GameRev.model.Forum;
import pl.ttsw.GameRev.model.ForumPost;
import pl.ttsw.GameRev.model.WebsiteUser;
import pl.ttsw.GameRev.repository.ForumPostRepository;
import pl.ttsw.GameRev.repository.ForumRepository;
import pl.ttsw.GameRev.mapper.ForumPostMapper;
import pl.ttsw.GameRev.repository.RoleRepository;
import pl.ttsw.GameRev.repository.WebsiteUserRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;

@Service
public class ForumPostService {
    private final ForumPostRepository forumPostRepository;
    private final ForumRepository forumRepository;
    private final ForumPostMapper forumPostMapper;
    private final WebsiteUserRepository websiteUserRepository;
    private final RoleRepository roleRepository;

    private final String postPicDirectory = "../Pictures/post_pics";
    private final WebsiteUserService websiteUserService;

    public ForumPostService(ForumPostRepository forumPostRepository, ForumRepository forumRepository, ForumPostMapper forumPostMapper, WebsiteUserRepository websiteUserRepository, RoleRepository roleRepository, WebsiteUserService websiteUserService) {
        this.forumPostRepository = forumPostRepository;
        this.forumRepository = forumRepository;
        this.forumPostMapper = forumPostMapper;
        this.websiteUserRepository = websiteUserRepository;
        this.roleRepository = roleRepository;
        this.websiteUserService = websiteUserService;
    }

    public Page<ForumPostDTO> getForumPosts(Long id,  LocalDate postDateFrom,
                                            LocalDate postDateTo, String searchText, Pageable pageable) {
        Forum forum = forumRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Forum not found"));
        Specification<ForumPost> spec = (root, query, builder) -> builder.equal(root.get("forum"), forum);

        if (postDateFrom != null) {
            spec = spec.and((root, query, builder) -> builder.greaterThanOrEqualTo(root.get("postDate"), postDateFrom));
        }
        if (postDateTo != null) {
            spec = spec.and((root, query, builder) -> builder.lessThanOrEqualTo(root.get("postDate"), postDateTo));
        }

        if (searchText != null) {
            searchText = searchText.toLowerCase();
            String likePattern = "%" + searchText + "%";
            spec = spec.and((root, query, builder) -> builder.or(
                    builder.like(builder.lower(root.get("title")), likePattern),
                    builder.like(builder.lower(root.get("content")), likePattern)
            ));
        }
        Page<ForumPost> forumPosts = forumPostRepository.findAll(spec, pageable);
        return forumPosts.map(forumPostMapper::toDto);
    }

    public ForumPostDTO createForumPost(ForumPostDTO forumPostDTO, MultipartFile picture) throws IOException {
        ForumPost forumPost = new ForumPost();

        forumPost.setForum(forumRepository.findById(forumPostDTO.getForum().getId())
                .orElseThrow(() -> new RuntimeException("Forum not found")));
        forumPost.setAuthor(websiteUserService.getCurrentUser());
        forumPost.setContent(forumPostDTO.getContent());
        forumPost.setPostDate(LocalDate.now());
        forumPost.setTitle(forumPostDTO.getTitle());

        Path filepath = null;
        try {
            if (picture != null && !picture.isEmpty()) {
                String fileName = "post"+ forumPost.getId() + "_" + picture.getOriginalFilename();
                filepath = Paths.get(postPicDirectory, fileName);
                Files.copy(picture.getInputStream(), filepath);
                forumPost.setPicture(filepath.toString());
            }
        } catch (Exception e) {
            if (filepath != null && Files.exists(filepath)) {
                try {
                    Files.delete(filepath);
                } catch (IOException ioException) {
                    System.err.println("Failed to delete file after an error: " + filepath.toString());
                }
            }
            throw e;
        }
        forumPost = forumPostRepository.save(forumPost);
        return forumPostMapper.toDto(forumPost);
    }

    public ForumPostDTO updateForumPost(Long id, ForumPostDTO forumPostDTO, MultipartFile picture) throws IOException {
        WebsiteUser currentUser = websiteUserService.getCurrentUser();
        ForumPost forumPost = forumPostRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Forum post not found"));

        if (currentUser.getRoles().contains(roleRepository.findByRoleName("Admin").get()) || currentUser == forumPost.getAuthor()) {
            if (forumPostDTO.getForum() != null) {
                forumPost.setForum(forumRepository.findById(forumPostDTO.getForum().getId())
                        .orElseThrow(() -> new RuntimeException("Forum not found")));
            }
            if (forumPostDTO.getAuthor() != null) {
                forumPost.setAuthor(websiteUserRepository.findById(forumPostDTO.getAuthor().getId())
                        .orElseThrow(() -> new RuntimeException("User not found")));
            }
            if (forumPostDTO.getContent() != null) {
                forumPost.setContent(forumPostDTO.getContent());
            }
            if (forumPostDTO.getTitle() != null) {
                forumPost.setTitle(forumPostDTO.getTitle());
            }
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
        }else {
            throw new BadCredentialsException("You dont have permission to perform this action");
        }
    }

    public boolean deleteForumPost(Long id) {
        WebsiteUser currentUser = websiteUserService.getCurrentUser();
        ForumPost forumPost = forumPostRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Forum post not found"));
        if (currentUser.getRoles().contains(roleRepository.findByRoleName("Admin").get()) || currentUser == forumPost.getAuthor()){
            forumPostRepository.delete(forumPost);
            return true;
        }else {
            throw new BadCredentialsException("You dont have permission to perform this action");
        }
    }
}