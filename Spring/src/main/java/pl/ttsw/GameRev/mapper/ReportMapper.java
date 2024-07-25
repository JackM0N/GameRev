package pl.ttsw.GameRev.mapper;

import org.mapstruct.*;
import pl.ttsw.GameRev.dto.ReportDTO;
import pl.ttsw.GameRev.model.Report;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ReportMapper {

    @Mapping(source = "userReview", target = "userReview")
    @Mapping(source = "user", target = "user")
    Report toEntity(ReportDTO reportDTO);

    @Mapping(source = "userReview", target = "userReview")
    @Mapping(source = "user", target = "user")
    ReportDTO toDto(Report report);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Report partialUpdate(ReportDTO reportDTO, @MappingTarget Report report);
}
