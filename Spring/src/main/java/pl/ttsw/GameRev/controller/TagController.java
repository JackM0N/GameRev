package pl.ttsw.GameRev.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.ttsw.GameRev.dto.TagDTO;
import pl.ttsw.GameRev.service.TagService;

import java.util.List;

@RestController
@RequestMapping("/tags")
public class TagController {
    private final TagService tagService;

    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    @GetMapping
    public ResponseEntity<?> getAllTags() {
        List<TagDTO> tags = tagService.getAllTags();
        if (tags == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(tags);
    }
}
