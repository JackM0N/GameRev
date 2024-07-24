package pl.ttsw.GameRev.mapper;

import org.mapstruct.*;
import org.mapstruct.factory.Mappers;
import pl.ttsw.GameRev.dto.TagDTO;
import pl.ttsw.GameRev.model.Tag;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface TagMapper {
    Tag toEntity(TagDTO tagDTO);

    TagDTO toDto(Tag tag);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Tag partialUpdate(TagDTO tagDTO, @MappingTarget Tag tag);
}