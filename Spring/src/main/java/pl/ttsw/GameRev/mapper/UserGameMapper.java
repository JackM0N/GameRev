package pl.ttsw.GameRev.mapper;

import org.mapstruct.*;
import pl.ttsw.GameRev.dto.UserGameDTO;
import pl.ttsw.GameRev.model.UserGame;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserGameMapper {
    UserGame toEntity(UserGameDTO userGameDTO);

    UserGameDTO toDto(UserGame userGame);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    UserGame partialUpdate(UserGameDTO userGameDTO, @MappingTarget UserGame userGame);
}