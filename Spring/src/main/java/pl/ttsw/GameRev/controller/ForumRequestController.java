package pl.ttsw.GameRev.controller;

import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class ForumRequestController {
    private final ForumRequestService forumRequestService;

    @GetMapping("/list")
    public ResponseEntity<Page<ForumRequestDTO>> getAllForumRequests(
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
    public ResponseEntity<Page<ForumRequestDTO>> getAllForumRequestsByOwner(
            @RequestParam(value = "approved", required = false) Boolean approved,
            Pageable pageable
    ) {
      Page<ForumRequestDTO> forumRequestDTOS = forumRequestService.getAllForumRequestsByOwner(approved, pageable);
        return ResponseEntity.ok(forumRequestService.getAllForumRequestsByOwner(approved, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ForumRequestDTO> getForumRequestById(@PathVariable Long id) {
        return ResponseEntity.ok(forumRequestService.getForumRequestById(id));
    }

    @PostMapping("/create")
    public ResponseEntity<ForumRequestDTO> createForumRequest(@RequestBody ForumRequestDTO forumRequestDTO) throws BadRequestException {
        if (forumRequestDTO == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(forumRequestService.createForumRequest(forumRequestDTO));
    }

    @PutMapping("/edit/{id}")
    public ResponseEntity<ForumRequestDTO> updateForumRequest(@RequestBody ForumRequestDTO forumRequestDTO, @PathVariable Long id) {
        if (forumRequestDTO == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(forumRequestService.updateForumRequest(id, forumRequestDTO));
    }

    @PutMapping("/approve/{id}")
    public ResponseEntity<ForumRequestDTO> approveForumRequest(@PathVariable Long id,
                                                 @RequestParam(value = "approved") Boolean approved) {
        if (id == null || approved == null) {
            return ResponseEntity.badRequest().build();
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
