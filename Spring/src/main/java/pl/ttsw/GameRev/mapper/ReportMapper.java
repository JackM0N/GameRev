package pl.ttsw.GameRev.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import pl.ttsw.GameRev.dto.ReportDTO;
import pl.ttsw.GameRev.model.Report;

@Mapper(componentModel = "spring")
public interface ReportMapper {
    ReportMapper INSTANCE = Mappers.getMapper(ReportMapper.class);

    @Mapping(source = "userReview", target = "userReview")
    @Mapping(source = "user", target = "user")
    ReportDTO reportToReportDTO(Report report);

    @Mapping(source = "userReview", target = "userReview")
    @Mapping(source = "user", target = "user")
    Report reportDTOToReport(ReportDTO reportDTO);
}
