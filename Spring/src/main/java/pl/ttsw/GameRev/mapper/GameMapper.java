package pl.ttsw.GameRev.mapper;

import org.mapstruct.*;
import org.mapstruct.factory.Mappers;
import pl.ttsw.GameRev.dto.GameDTO;
import pl.ttsw.GameRev.model.Game;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,componentModel = MappingConstants.ComponentModel.SPRING)
public interface GameMapper {
    @Mapping(source = "releaseStatus", target = "releaseStatus")
    @Mapping(source = "tags", target = "tags")
    Game toEntity(GameDTO gameDTO);

    @Mapping(source = "releaseStatus", target = "releaseStatus")
    @Mapping(source = "tags", target = "tags")
    GameDTO toDto(Game game);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Game partialUpdate(GameDTO gameDTO, @MappingTarget Game game);
}