package pl.ttsw.GameRev.service;

import org.springframework.stereotype.Service;
import pl.ttsw.GameRev.dto.TagDTO;
import pl.ttsw.GameRev.model.Tag;
import pl.ttsw.GameRev.repository.TagRepository;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TagService {
    private TagRepository tagRepository;

    public TagService(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    public List<TagDTO> getAllTags() {
        List<Tag> tags = tagRepository.findAll();
        return tags.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    private TagDTO mapToDTO(Tag tag) {
        TagDTO tagDTO = new TagDTO();
        tagDTO.setId(tag.getId());
        tagDTO.setTagName(tag.getTagName());
        tagDTO.setPriority(tag.getPriority());
        return tagDTO;
    }
}
