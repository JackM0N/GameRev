package pl.ttsw.GameRev.service;

import org.springframework.stereotype.Service;
import pl.ttsw.GameRev.dto.TagDTO;
import pl.ttsw.GameRev.mapper.TagMapper;
import pl.ttsw.GameRev.model.Tag;
import pl.ttsw.GameRev.repository.TagRepository;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TagService {
    private TagRepository tagRepository;
    private TagMapper tagMapper;

    public TagService(TagRepository tagRepository , TagMapper tagMapper) {
        this.tagRepository = tagRepository;
        this.tagMapper = tagMapper;
    }

    public List<TagDTO> getAllTags() {
        List<Tag> tags = tagRepository.findAll();
        return tags.stream().map(tagMapper::toDto).collect(Collectors.toList());
    }
}
