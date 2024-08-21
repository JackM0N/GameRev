package pl.ttsw.GameRev.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.ttsw.GameRev.dto.ForumCommentDTO;
import pl.ttsw.GameRev.dto.ForumPostDTO;
import pl.ttsw.GameRev.service.ForumCommentService;

@RestController
@RequestMapping("/post")
public class ForumCommentController {
    private final ForumCommentService forumCommentService;

    public ForumCommentController(ForumCommentService forumCommentService) {
        this.forumCommentService = forumCommentService;
    }

    @GetMapping("/origin/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        ForumPostDTO forumPostDTO = forumCommentService.getOriginalPost(id);
        if (forumPostDTO == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(forumPostDTO);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> findById(
            @PathVariable Long id,
            @RequestParam(value = "userId", required = false)  Long userId,
            @RequestParam(value = "searchText", required = false)  String searchText,
            Pageable pageable){
        Page<ForumCommentDTO> forumCommentDTOS = forumCommentService.getForumCommentsByPost(id, userId, searchText, pageable);
        if(forumCommentDTOS.isEmpty()){
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return ResponseEntity.ok(forumCommentDTOS);
    }

    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestBody ForumCommentDTO forumCommentDTO) {
        if (forumCommentDTO == null){
            return ResponseEntity.badRequest().body("There was an error creating this comment");
        }
        return ResponseEntity.ok(forumCommentService.createForumComment(forumCommentDTO));
    }

    @PutMapping("/edit/{id}")
    public ResponseEntity<?> edit(@PathVariable Long id, @RequestBody ForumCommentDTO forumCommentDTO) {
        if (forumCommentDTO == null){
            return ResponseEntity.badRequest().body("There was an error editing this comment");
        }
        return ResponseEntity.ok(forumCommentService.updateForumComment(id, forumCommentDTO));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        boolean gotDeleted = forumCommentService.deleteForumComment(id);
        if(!gotDeleted){
            return ResponseEntity.badRequest().body("There was an error deleting this comment");
        }
        return ResponseEntity.ok().build();
    }
}
