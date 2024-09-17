package pl.ttsw.GameRev.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pl.ttsw.GameRev.dto.ForumCommentDTO;
import pl.ttsw.GameRev.dto.ForumPostDTO;
import pl.ttsw.GameRev.filter.ForumCommentFilter;
import pl.ttsw.GameRev.service.ForumCommentService;
import java.io.IOException;

@RestController
@RequestMapping("/post")
@RequiredArgsConstructor
public class ForumCommentController {
    private final ForumCommentService forumCommentService;

    @GetMapping("/origin/{id}")
    public ResponseEntity<ForumPostDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(forumCommentService.getOriginalPost(id));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Page<ForumCommentDTO>> findById(
            @PathVariable Long id,
            ForumCommentFilter forumCommentFilter,
            Pageable pageable
    ) {
        return ResponseEntity.ok(forumCommentService.getForumCommentsByPost(id, forumCommentFilter, pageable));
    }

    @GetMapping("/picture/{id}")
    public ResponseEntity<byte[]> getPicture(@PathVariable Long id) {
        try {
            byte[] image = forumCommentService.getCommentPicture(id);
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_JPEG)
                    .body(image);
        } catch (IOException e) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
    }

    @PostMapping("/create")
    public ResponseEntity<ForumCommentDTO> create(
            @RequestParam(value = "comment") String commentJson,
            @RequestParam(value = "picture", required = false) MultipartFile picture) throws IOException
    {
        ObjectMapper mapper = JsonMapper.builder()
                .addModule(new JavaTimeModule())
                .build();

        ForumCommentDTO request = mapper.readValue(commentJson, ForumCommentDTO.class);
        ForumCommentDTO comment = forumCommentService.createForumComment(request, picture);

        if (comment == null){
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(comment);
    }

    @PutMapping("/edit/{id}")
    public ResponseEntity<ForumCommentDTO> edit(
            @PathVariable Long id,
            @RequestParam(value = "comment") String commentJson,
            @RequestParam(value = "picture") MultipartFile picture) throws IOException
    {
        ObjectMapper objectMapper = JsonMapper.builder()
                .addModule(new JavaTimeModule())
                .build();

        ForumCommentDTO request = objectMapper.readValue(commentJson, ForumCommentDTO.class);
        return ResponseEntity.ok(forumCommentService.updateForumComment(id, request, picture));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id, @RequestParam(name = "isDeleted") Boolean isDeleted) {
        boolean gotDeleted = forumCommentService.deleteForumComment(id, isDeleted);
        if (!gotDeleted) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok().build();
    }
}
