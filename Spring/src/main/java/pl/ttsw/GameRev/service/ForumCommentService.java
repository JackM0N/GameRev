package pl.ttsw.GameRev.service;

import org.springframework.stereotype.Service;
import pl.ttsw.GameRev.dto.ForumCommentDTO;
import pl.ttsw.GameRev.repository.ForumCommentRepository;

@Service
public class ForumCommentService {
    private final ForumCommentRepository forumCommentRepository;

    public ForumCommentService(ForumCommentRepository forumCommentRepository) {
        this.forumCommentRepository = forumCommentRepository;
    }

    public ForumCommentDTO getForumCommentsByPost(Long id) {
        return null;
    }
}
