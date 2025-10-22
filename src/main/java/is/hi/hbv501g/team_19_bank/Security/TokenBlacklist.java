package is.hi.hbv501g.team_19_bank.Security;

import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class TokenBlacklist {

    private final Set<String> blacklist = new HashSet<>();

    // Add a token to the blacklist
    public void addToken(String token) {
        blacklist.add(token);
    }

    // Check if a token is blacklisted
    public boolean isTokenBlacklisted(String token) {
        return blacklist.contains(token);
    }
}
