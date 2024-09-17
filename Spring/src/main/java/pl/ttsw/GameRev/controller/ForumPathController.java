package pl.ttsw.GameRev.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.ttsw.GameRev.dto.ForumDTO;
import pl.ttsw.GameRev.model.Forum;
import pl.ttsw.GameRev.repository.ForumRepository;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/path")
@RequiredArgsConstructor
public class ForumPathController {
    private final ForumRepository forumRepository;

    @GetMapping("/{id}")
    public ResponseEntity<?> getForumPath(@PathVariable("id") Long id) {
        Forum forum = forumRepository.findById(id).orElse(null);

        if (forum == null) {
            return ResponseEntity.notFound().build();
        }

        List<ForumDTO> forumDTOS = new ArrayList<>();

        ForumDTO currentForumDTO = new ForumDTO();
        currentForumDTO.setId(forum.getId());
        currentForumDTO.setForumName(forum.getForumName());
        forumDTOS.add(currentForumDTO);

        while (forum.getParentForum() != null) {
            ForumDTO forumDTO = new ForumDTO();
            forumDTO.setId(forum.getParentForum().getId());
            forumDTO.setForumName(forum.getParentForum().getForumName());
            forumDTOS.add(forumDTO);
            forum = forum.getParentForum();
        }
        return ResponseEntity.ok(forumDTOS);
    }
}
