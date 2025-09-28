package Security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfigJava {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {

        return httpSecurity.formLogin(httpForm -> {
            httpForm
                    .loginPage("/login")
                    .permitAll();
            httpForm
                    .defaultSuccessUrl("/bank", true);
            httpForm
                    .failureUrl("/login?error=true");
        }).authorizeHttpRequests(registry -> {
            registry
                    .requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**", "/h2-console/**", "/signup/**", "/login/**", "/bank/**")
                    .permitAll();
            registry
                    .anyRequest()
                    .authenticated();
        }).build();
    }
}
