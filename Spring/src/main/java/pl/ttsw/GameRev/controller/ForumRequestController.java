package pl.ttsw.GameRev.controller;

import org.apache.coyote.BadRequestException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.ttsw.GameRev.dto.ForumRequestDTO;
import pl.ttsw.GameRev.service.ForumRequestService;

@RestController
@RequestMapping("/forum-request")
public class ForumRequestController {
    private final ForumRequestService forumRequestService;

    public ForumRequestController(ForumRequestService forumRequestService) {
        this.forumRequestService = forumRequestService;
    }

    @GetMapping("/list")
    public ResponseEntity<?> getAllForumRequests(
            @RequestParam(value = "approved", required = false) Boolean approved,
            Pageable pageable
    ) {
        Page<ForumRequestDTO> forumRequestDTOS = forumRequestService.getAllForumRequests(approved, pageable);
        if (forumRequestDTOS.getTotalElements() == 0 || forumRequestDTOS.getContent().isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return ResponseEntity.ok(forumRequestDTOS);
    }

    @GetMapping("/own-requests")
    public ResponseEntity<?> getAllForumRequestsByOwner(
            @RequestParam(value = "approved", required = false) Boolean approved,
            Pageable pageable
    ) {
      Page<ForumRequestDTO> forumRequestDTOS = forumRequestService.getAllForumRequestsByOwner(approved, pageable);
        if (forumRequestDTOS.getTotalElements() == 0 || forumRequestDTOS.getContent().isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return ResponseEntity.ok(forumRequestDTOS);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getForumRequestById(@PathVariable Long id) {
        ForumRequestDTO forumRequestDTO = forumRequestService.getForumRequestById(id);
        if (forumRequestDTO == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(forumRequestDTO);
    }

    @PostMapping("/create")
    public ResponseEntity<?> createForumRequest(@RequestBody ForumRequestDTO forumRequestDTO) throws BadRequestException {
        if (forumRequestDTO == null) {
            return ResponseEntity.badRequest().body("There was an error creating this forum request");
        }
        return ResponseEntity.ok(forumRequestService.createForumRequest(forumRequestDTO));
    }

    @PutMapping("/edit/{id}")
    public ResponseEntity<?> updateForumRequest(@RequestBody ForumRequestDTO forumRequestDTO, @PathVariable Long id) {
        if (forumRequestDTO == null) {
            return ResponseEntity.badRequest().body("There was an error editing this forum request");
        }
        return ResponseEntity.ok(forumRequestService.updateForumRequest(id, forumRequestDTO));
    }

    @PutMapping("/approve/{id}")
    public ResponseEntity<?> approveForumRequest(@PathVariable Long id,
                                                 @RequestParam(value = "approved") Boolean approved) throws BadRequestException {
        if (id == null || approved == null) {
            return ResponseEntity.badRequest().body("There was an error approving the request");
        }
        return ResponseEntity.ok(forumRequestService.approveForumRequest(id, approved));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteForumRequest(@PathVariable Long id) {
        boolean deleted = forumRequestService.deleteForumRequest(id);
        if (!deleted) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return ResponseEntity.ok().build();
    }
}
