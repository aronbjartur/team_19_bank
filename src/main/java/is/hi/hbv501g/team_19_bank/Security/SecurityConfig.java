package is.hi.hbv501g.team_19_bank.Security;

import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@AllArgsConstructor
@EnableWebSecurity
public class SecurityConfig {


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {

        return httpSecurity.csrf(AbstractHttpConfigurer::disable).formLogin(httpForm -> {
            httpForm
                    .loginPage("/login")
                    .permitAll();
            httpForm
                    .defaultSuccessUrl("/index");
        }).authorizeHttpRequests(registry -> {
            registry
                    .requestMatchers("/req/**", "/css/**", "/js/**", "/images/**", "/webjars/**", "/h2-console/**", "req/signup/**", "/login/**", "/bank/**", "/accounts/**", "/users/**")
                    .permitAll();
            registry.anyRequest().authenticated();
        }).build();
    }
}

