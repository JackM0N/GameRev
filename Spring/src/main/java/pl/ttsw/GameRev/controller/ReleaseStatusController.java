package pl.ttsw.GameRev.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.ttsw.GameRev.dto.ReleaseStatusDTO;
import pl.ttsw.GameRev.model.ReleaseStatus;
import pl.ttsw.GameRev.service.ReleaseStatusService;

import java.util.List;

@RestController
@RequestMapping("/release-statuses")
public class ReleaseStatusController {
    private ReleaseStatusService releaseStatusService;

    public ReleaseStatusController(ReleaseStatusService releaseStatusService) {
        this.releaseStatusService = releaseStatusService;
    }

    @GetMapping
    public ResponseEntity<?> getReleaseStatuses() {
        List<ReleaseStatusDTO> tags = releaseStatusService.getAllReleaseStatuses();
        if (tags == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(tags);
    }
}
