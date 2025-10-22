package is.hi.hbv501g.team_19_bank.Security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // Exclude specific endpoints from the filter
        String path = request.getRequestURI();
        return path.startsWith("/login") || path.startsWith("/signup") || path.startsWith("/h2-console");
    }

    // Þetta er að sía fyrir request eftir login, athugar hvort þau innihaldi gilt JWT token
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String authorizationHeader = request.getHeader("Authorization");

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String token = authorizationHeader.substring(7);
            System.out.println("Extracted Token: " + token);
            String username = jwtUtil.validateTokenAndGetUsername(token);
            System.out.println("Extracted Username: " + username);

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                username,
                                null,
                                Collections.emptyList() // Add roles/authorities if needed
                        );
                SecurityContextHolder.getContext().setAuthentication(authentication);
                System.out.println("SecurityContext Updated: " + SecurityContextHolder.getContext().getAuthentication());
            } else {
                System.out.println("Authentication not set. Username is null or already authenticated.");
            }
        } else {
            System.out.println("Authorization header is missing or does not start with 'Bearer '.");
        }

        filterChain.doFilter(request, response);
    }
}
