package is.hi.hbv501g.team_19_bank.Security;

import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@AllArgsConstructor
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {

        return httpSecurity.csrf(AbstractHttpConfigurer::disable)
                .headers(headers -> headers.frameOptions().disable()).
                formLogin(httpForm -> {
                    httpForm
                            .loginPage("/login")
                            .permitAll();
                    httpForm
                            .defaultSuccessUrl("/", true);
                }).authorizeHttpRequests(registry -> {
                    registry
                            // tók út req -AB
                            .requestMatchers("/", "/login", "/signup", "/css/**", "/js/**", "/images/**", "/webjars/**", "/h2-console/**", "/users*", "/transfers*", "/loans").permitAll();
                    registry.anyRequest().authenticated();
                }).build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    } // Ó
}
