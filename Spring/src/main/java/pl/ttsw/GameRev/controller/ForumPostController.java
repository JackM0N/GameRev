package pl.ttsw.GameRev.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pl.ttsw.GameRev.dto.ForumPostDTO;
import pl.ttsw.GameRev.service.ForumPostService;

import java.io.IOException;
import java.time.LocalDate;

@RestController
@RequestMapping("/forum-post")
public class ForumPostController {
    private final ForumPostService forumPostService;

    public ForumPostController(ForumPostService forumPostService) {
        this.forumPostService = forumPostService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(
            @PathVariable Long id,
            @RequestParam(value = "postDateFrom", required = false)  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate postDateFrom,
            @RequestParam(value = "postDateTo", required = false)  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate postDateTo,
            @RequestParam(value = "searchText", required = false)  String searchText,
            Pageable pageable){
        Page<ForumPostDTO> forumPostDTOS = forumPostService.getForumPosts(id, postDateFrom, postDateTo, searchText, pageable);
        if (forumPostDTOS.getTotalElements() == 0){
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return ResponseEntity.ok(forumPostDTOS);
    }

    @PostMapping("/create")
    public ResponseEntity<?> create(
            @RequestParam(value = "post") String postJson,
            @RequestParam(value = "picture", required = false) MultipartFile picture) throws IOException {
        ObjectMapper objectMapper = JsonMapper.builder()
                .addModule(new JavaTimeModule())
                .build();
        ForumPostDTO request = objectMapper.readValue(postJson, ForumPostDTO.class);
        ForumPostDTO post = forumPostService.createForumPost(request, picture);
        if (post == null){
            return ResponseEntity.badRequest().body("Post creation failed");
        }
        return ResponseEntity.ok(post);
    }

    @PutMapping("/edit/{id}")
    public ResponseEntity<?> edit(
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
    public ResponseEntity<?> delete(@PathVariable Long id){
        boolean gotDeleted = forumPostService.deleteForumPost(id);
        if (!gotDeleted){
            return ResponseEntity.badRequest().body("There was an error deleting this post");
        }
        return ResponseEntity.ok().build();
    }
}
