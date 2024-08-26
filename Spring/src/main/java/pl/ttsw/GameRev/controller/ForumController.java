package pl.ttsw.GameRev.controller;

import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.ttsw.GameRev.dto.ForumDTO;
import pl.ttsw.GameRev.service.ForumService;

@RestController
@RequestMapping("/forum")
@RequiredArgsConstructor
public class ForumController {
    private final ForumService forumService;

    @GetMapping("")
    public ResponseEntity<?> getForums(
            @RequestParam(value = "gameId", required = false) Long gameId,
            @RequestParam(value = "searchText", required = false) String searchText,
            Pageable pageable){
        Page<ForumDTO> forumDTOS = forumService.getForum(1L, gameId, searchText, pageable);
        if(forumDTOS == null || forumDTOS.getTotalElements() == 0){
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return ResponseEntity.ok(forumDTOS);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getForum(
            @PathVariable Long id,
            @RequestParam(value = "gameId", required = false) Long gameId,
            @RequestParam(value = "searchText", required = false) String searchText,
            Pageable pageable) {
        Page<ForumDTO> forumDTOS = forumService.getForum(id, gameId, searchText, pageable);
        if(forumDTOS == null || forumDTOS.getTotalElements() == 0){
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return ResponseEntity.ok(forumDTOS);
    }

    @PostMapping("/create")
    public ResponseEntity<?> createForum(@RequestBody ForumDTO forumDTO) throws BadRequestException {
        if (forumDTO == null){
            return ResponseEntity.badRequest().body("There was an error creating this forum");
        }
        return ResponseEntity.ok(forumService.createForum(forumDTO));
    }

    @PutMapping("/edit/{id}")
    public ResponseEntity<?> editForum(@PathVariable Long id, @RequestBody ForumDTO forumDTO) throws BadRequestException {
        if (forumDTO == null){
            return ResponseEntity.badRequest().body("There was an error editing this forum");
        }
        return ResponseEntity.ok(forumService.updateForum(id,forumDTO));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteForum(@PathVariable Long id) throws BadRequestException {
        boolean gotDeleted = forumService.deleteForum(id);
        if(!gotDeleted){
            return ResponseEntity.badRequest().body("There was an error deleting this forum");
        }
        return ResponseEntity.ok().build();
    }
}
