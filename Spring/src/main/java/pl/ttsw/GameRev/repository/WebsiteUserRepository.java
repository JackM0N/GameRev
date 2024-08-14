package pl.ttsw.GameRev.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import pl.ttsw.GameRev.model.WebsiteUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

@Repository
public interface WebsiteUserRepository extends JpaRepository<WebsiteUser, Long>, JpaSpecificationExecutor<WebsiteUser> {
    Optional<WebsiteUser> findByUsername(String username);
    Optional<WebsiteUser> findByUsernameOrEmail(String username, String email);
    Optional<WebsiteUser> findByNickname(String nickname);
    Optional<WebsiteUser> findByEmail(String email);
    Page<WebsiteUser> findAll(Pageable pageable);
}
