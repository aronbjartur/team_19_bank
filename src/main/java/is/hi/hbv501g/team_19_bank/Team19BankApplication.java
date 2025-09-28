package is.hi.hbv501g.team_19_bank;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
public class Team19BankApplication {
    public static void main(String[] args) {
        SpringApplication.run(Team19BankApplication.class, args);
    }
}
