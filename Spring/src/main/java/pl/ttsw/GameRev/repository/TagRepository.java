package pl.ttsw.GameRev.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.ttsw.GameRev.model.Tag;
import java.util.List;

public interface TagRepository extends JpaRepository<Tag, Long> {
}