package pl.ttsw.GameRev.controller;

import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.ttsw.GameRev.dto.ForumDTO;
import pl.ttsw.GameRev.filter.ForumFilter;
import pl.ttsw.GameRev.service.ForumService;

@RestController
@RequestMapping("/forum")
@RequiredArgsConstructor
public class ForumController {
    private final ForumService forumService;

    @GetMapping("")
    public ResponseEntity<Page<ForumDTO>> getForums(
            ForumFilter forumFilter,
            Pageable pageable){
        return ResponseEntity.ok(forumService.getForum(1L, forumFilter, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Page<ForumDTO>> getForum(
            @PathVariable Long id,
            ForumFilter forumFilter,
            Pageable pageable) {
        return ResponseEntity.ok(forumService.getForum(id, forumFilter, pageable));
    }

    @PostMapping("/create")
    public ResponseEntity<ForumDTO> createForum(@RequestBody ForumDTO forumDTO) throws BadRequestException {
        if (forumDTO == null){
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(forumService.createForum(forumDTO));
    }

    @PutMapping("/edit/{id}")
    public ResponseEntity<ForumDTO> editForum(@PathVariable Long id, @RequestBody ForumDTO forumDTO) throws BadRequestException {
        if (forumDTO == null){
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(forumService.updateForum(id,forumDTO));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteForum(@PathVariable Long id, @RequestParam(name = "isDeleted") Boolean isDeleted) throws BadRequestException {
        boolean gotDeleted = forumService.deleteForum(id, isDeleted);
        if(!gotDeleted){
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok().build();
    }
}
