package pl.ttsw.GameRev.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.ttsw.GameRev.model.Role;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
  Role findByRoleName(String name);
}
