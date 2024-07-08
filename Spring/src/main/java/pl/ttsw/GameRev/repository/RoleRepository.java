package pl.ttsw.GameRev.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.ttsw.GameRev.model.Role;

public interface RoleRepository extends JpaRepository<Role, Long> {
  public Role findByRoleName(String name);
}