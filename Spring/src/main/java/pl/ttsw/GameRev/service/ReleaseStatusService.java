package pl.ttsw.GameRev.service;

import org.springframework.stereotype.Service;
import pl.ttsw.GameRev.dto.ReleaseStatusDTO;
import pl.ttsw.GameRev.model.ReleaseStatus;
import pl.ttsw.GameRev.repository.ReleaseStatusRepository;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReleaseStatusService {
    private ReleaseStatusRepository releaseStatusRepository;

    public ReleaseStatusService(ReleaseStatusRepository releaseStatusRepository) {
        this.releaseStatusRepository = releaseStatusRepository;
    }


    public List<ReleaseStatusDTO> getAllReleaseStatuses() {
        List<ReleaseStatus> tags = releaseStatusRepository.findAll();
        return tags.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    private ReleaseStatusDTO mapToDTO(ReleaseStatus releaseStatus) {
        ReleaseStatusDTO releaseStatusDTO = new ReleaseStatusDTO();
        releaseStatusDTO.setId(releaseStatus.getId());
        releaseStatusDTO.setStatusName(releaseStatus.getStatusName());
        return releaseStatusDTO;
    }
}
