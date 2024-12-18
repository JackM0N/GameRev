package pl.ttsw.GameRev.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final CustomUserDetailsService userDetails;
    private final JWTAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(CustomUserDetailsService userDetails, JWTAuthenticationFilter jwtAuthenticationFilter) {
        this.userDetails = userDetails;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/critics-reviews/id/**", "/critics-reviews/edit/**",
                                "/critics-reviews/review/**", "/critics-reviews/create",
                                "/critics-reviews/delete/**", "/critics-reviews/list",
                                "/critics-reviews/id/**")
                        .hasAnyRole("Critic", "Admin")

                        .requestMatchers("/user/edit-profile/**", "/library", "/post/create", "/post/edit/",
                                "/post/delete/", "/forum-post/create", "/forum-post/delete/**",
                                "/forum-post/edit/**", "/forum-request/own-requests", "/forum-request/edit/**",
                                "/forum-request/delete/**")
                        .authenticated()

                        .requestMatchers("/user/ban", "/reports/**", "/users-reviews/admin/**", "/games/create",
                                "/games/edit/**", "/games/delete/**", "/user/edit/**", "/user/delete/**",
                                "/user/roles/**", "/forum/create", "/forum/delete/**", "/forum/edit/**",
                                "/forum-request/list", "/forum-request/**", "/forum-request/create",
                                "/forum-request/approve/**")
                        .hasRole("Admin")

                        .requestMatchers("/login/**", "/register/**", "/games/**","/tags/**",
                                "/release-statuses/**","/users-reviews/**", "/user/list", "/user/account/**",
                                "/user/**", "/library/**", "/password-reset/**", "/critics-reviews/**",
                                "/forum-post/**","/forum/**", "/post/**", "/path/**", "/forum/moderators/**",
                                "/forum-post/picture/**")
                        .permitAll()

                        .anyRequest()
                        .authenticated())
                .userDetailsService(userDetails)
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .httpBasic(Customizer.withDefaults())
                .build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}
