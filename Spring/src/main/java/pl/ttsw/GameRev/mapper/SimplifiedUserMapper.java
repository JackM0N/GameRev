package pl.ttsw.GameRev.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import pl.ttsw.GameRev.dto.SimplifiedUserDTO;
import pl.ttsw.GameRev.dto.WebsiteUserDTO;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface SimplifiedUserMapper {
    @Mapping(target = "id", source = "id")
    @Mapping(target = "profilepic", source = "profilepic")
    @Mapping(target = "nickname", source = "nickname")
    @Mapping(target = "lastActionDate", source = "lastActionDate")
    @Mapping(target = "description", source = "description")
    @Mapping(target = "joinDate", source = "joinDate")
    SimplifiedUserDTO toSimplifiedDto(WebsiteUserDTO websiteUserDTO);
}
