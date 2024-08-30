package pl.ttsw.GameRev.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pl.ttsw.GameRev.dto.ForumPostDTO;
import pl.ttsw.GameRev.filter.ForumPostFilter;
import pl.ttsw.GameRev.service.ForumPostService;

import java.io.IOException;

@RestController
@RequestMapping("/forum-post")
@RequiredArgsConstructor
public class ForumPostController {
    private final ForumPostService forumPostService;

    @GetMapping("/{id}")
    public ResponseEntity<Page<ForumPostDTO>> getById(@PathVariable Long id, ForumPostFilter forumPostFilter, Pageable pageable){
        return ResponseEntity.ok(forumPostService.getForumPosts(id, forumPostFilter, pageable));
    }

    @GetMapping("/picture/{id}")
    public ResponseEntity<byte[]> getPicture(@PathVariable Long id){
        try {
            byte[] image = forumPostService.getPostPicture(id);
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_JPEG)
                    .body(image);
        } catch (IOException e) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
    }

    @PostMapping("/create")
    public ResponseEntity<ForumPostDTO> create(
            @RequestParam(value = "post") String postJson,
            @RequestParam(value = "picture", required = false) MultipartFile picture) throws IOException {
        ObjectMapper objectMapper = JsonMapper.builder()
                .addModule(new JavaTimeModule())
                .build();
        ForumPostDTO request = objectMapper.readValue(postJson, ForumPostDTO.class);
        ForumPostDTO post = forumPostService.createForumPost(request, picture);
        if (post == null){
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(post);
    }

    @PutMapping("/edit/{id}")
    public ResponseEntity<ForumPostDTO> edit(
            @PathVariable Long id,
            @RequestParam(value = "post") String postJson,
            @RequestParam(value = "picture", required = false) MultipartFile picture) throws IOException {
        ObjectMapper objectMapper = JsonMapper.builder()
                .addModule(new JavaTimeModule())
                .build();
        ForumPostDTO request = objectMapper.readValue(postJson, ForumPostDTO.class);
        ForumPostDTO post = forumPostService.updateForumPost(id, request, picture);
        return ResponseEntity.ok(post);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id, @RequestParam(name = "isDeleted") Boolean isDeleted){
        boolean gotDeleted = forumPostService.deleteForumPost(id, isDeleted);
        if (!gotDeleted){
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok().build();
    }
}
