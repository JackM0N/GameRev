package pl.ttsw.GameRev.mapper;

import org.mapstruct.*;
import pl.ttsw.GameRev.dto.ForumRequestDTO;
import pl.ttsw.GameRev.model.ForumRequest;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface ForumRequestMapper {
    ForumRequest toEntity(ForumRequestDTO forumRequestDTO);

    ForumRequestDTO toDto(ForumRequest forumRequest);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    ForumRequest partialUpdate(ForumRequestDTO forumRequestDTO, @MappingTarget ForumRequest forumRequest);
}