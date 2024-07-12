package pl.ttsw.GameRev.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.ttsw.GameRev.model.WebsiteUser;

@Repository
public interface WebsiteUserRepository extends JpaRepository<WebsiteUser, Integer> {
    WebsiteUser findByUsername(String username);
    WebsiteUser findByUsernameOrEmail(String username, String email);
    WebsiteUser findByNickname(String nickname);
}
