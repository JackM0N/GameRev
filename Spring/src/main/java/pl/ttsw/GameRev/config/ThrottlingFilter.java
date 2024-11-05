package pl.ttsw.GameRev.config;

import jakarta.servlet.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.io.IOException;

@Component
public class ThrottlingFilter implements Filter {
    @Value("${throttling.enabled}")
    private boolean throttlingEnabled;

    @Value("${throttling.delay:2000}") // Default delay is 2000ms if not set
    private long delay;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (throttlingEnabled) {
            try {
                Thread.sleep(delay); // Only apply delay if throttling is enabled
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        chain.doFilter(request, response);
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void destroy() {}
}
