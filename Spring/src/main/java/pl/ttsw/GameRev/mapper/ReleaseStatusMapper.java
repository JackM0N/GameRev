package pl.ttsw.GameRev.mapper;

import org.mapstruct.*;
import pl.ttsw.GameRev.dto.ReleaseStatusDTO;
import pl.ttsw.GameRev.model.ReleaseStatus;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface ReleaseStatusMapper {
    ReleaseStatus toEntity(ReleaseStatusDTO releaseStatusDTO);

    ReleaseStatusDTO toDto(ReleaseStatus releaseStatus);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    ReleaseStatus partialUpdate(ReleaseStatusDTO releaseStatusDTO, @MappingTarget ReleaseStatus releaseStatus);
}