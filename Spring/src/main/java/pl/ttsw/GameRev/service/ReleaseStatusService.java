package pl.ttsw.GameRev.service;

import org.springframework.stereotype.Service;
import pl.ttsw.GameRev.dto.ReleaseStatusDTO;
import pl.ttsw.GameRev.mapper.ReleaseStatusMapper;
import pl.ttsw.GameRev.model.ReleaseStatus;
import pl.ttsw.GameRev.repository.ReleaseStatusRepository;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReleaseStatusService {
    private final ReleaseStatusRepository releaseStatusRepository;
    private final ReleaseStatusMapper releaseStatusMapper;

    public ReleaseStatusService(ReleaseStatusRepository releaseStatusRepository, ReleaseStatusMapper releaseStatusMapper) {
        this.releaseStatusRepository = releaseStatusRepository;
        this.releaseStatusMapper = releaseStatusMapper;
    }


    public List<ReleaseStatusDTO> getAllReleaseStatuses() {
        List<ReleaseStatus> tags = releaseStatusRepository.findAll();
        return tags.stream().map(releaseStatusMapper::toDto).collect(Collectors.toList());
    }
}
