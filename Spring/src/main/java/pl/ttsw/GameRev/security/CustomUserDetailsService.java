package pl.ttsw.GameRev.security;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import pl.ttsw.GameRev.model.Role;
import pl.ttsw.GameRev.model.WebsiteUser;
import pl.ttsw.GameRev.repository.WebsiteUserRepository;

import java.util.Optional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final WebsiteUserRepository websiteUserRepository;

    public CustomUserDetailsService(WebsiteUserRepository websiteUserRepository) {
        this.websiteUserRepository = websiteUserRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<WebsiteUser> user = Optional.ofNullable(websiteUserRepository.findByUsername(username));
        if (user.isPresent()) {
            var websiteUser = user.get();
            return User.builder()
                    .username(websiteUser.getUsername())
                    .password(websiteUser.getPassword())
                    .roles(websiteUser.getRoles().stream().map(Role::getRoleName).toArray(String[]::new))
                    .build();
        }else {
            throw new UsernameNotFoundException("User "+username+" not found");
        }
    }
}
