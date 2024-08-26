package pl.ttsw.GameRev.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.ttsw.GameRev.dto.ForumCommentDTO;
import pl.ttsw.GameRev.dto.ForumPostDTO;
import pl.ttsw.GameRev.filter.ForumCommentFilter;
import pl.ttsw.GameRev.service.ForumCommentService;

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
            Pageable pageable){
        return ResponseEntity.ok(forumCommentService.getForumCommentsByPost(id, forumCommentFilter, pageable));
    }

    @PostMapping("/create")
    public ResponseEntity<ForumCommentDTO> create(@RequestBody ForumCommentDTO forumCommentDTO) {
        if (forumCommentDTO == null){
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(forumCommentService.createForumComment(forumCommentDTO));
    }

    @PutMapping("/edit/{id}")
    public ResponseEntity<ForumCommentDTO> edit(@PathVariable Long id, @RequestBody ForumCommentDTO forumCommentDTO) {
        if (forumCommentDTO == null){
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(forumCommentService.updateForumComment(id, forumCommentDTO));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id, @RequestParam(name = "isDeleted") Boolean isDeleted) {
        boolean gotDeleted = forumCommentService.deleteForumComment(id, isDeleted);
        if(!gotDeleted){
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok().build();
    }
}
