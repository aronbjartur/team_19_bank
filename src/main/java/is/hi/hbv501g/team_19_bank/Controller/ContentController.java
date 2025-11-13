package is.hi.hbv501g.team_19_bank.Controller;

import is.hi.hbv501g.team_19_bank.Security.TokenBlacklist;
import is.hi.hbv501g.team_19_bank.Service.AccountService;
import is.hi.hbv501g.team_19_bank.Service.UserService;
import is.hi.hbv501g.team_19_bank.model.BankUser;
import is.hi.hbv501g.team_19_bank.model.LoginRequest;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class ContentController {
    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final AccountService accountService;
    private final TokenBlacklist tokenBlacklist;

    public ContentController(UserService userService, AccountService accountService, AuthenticationManager authenticationManager, TokenBlacklist tokenBlacklist) {
        this.userService = userService;
        this.accountService = accountService;
        this.authenticationManager = authenticationManager;
        this.tokenBlacklist = tokenBlacklist;
    }

    // Virkar með postman
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginRequest loginRequest) {
        System.out.println("Login request received: " + loginRequest);
        try {
            String token = userService.authenticate(
                    loginRequest.getUsername(),
                    loginRequest.getPassword()
            );
            // Log the generated token
            System.out.println("Generated Token: " + token);

            return ResponseEntity.ok(Map.of(
                    "message", "Login successful",
                    "token", token
            ));

        } catch (IllegalArgumentException e) {
            System.out.println("Authentication failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "error", e.getMessage()
            ));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser(HttpServletRequest request) {
        String authorizationHeader = request.getHeader("Authorization");

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String token = authorizationHeader.substring(7);
            tokenBlacklist.addToken(token);
            System.out.println("Token added to blacklist: " + token);
            return ResponseEntity.ok(Map.of(
                    "message", "Logout successful. Token has been invalidated."
            ));
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "error", "No valid token provided."
        ));
    }


    // Þetta virkar með postman
    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@RequestBody BankUser user) {
        try {
            userService.createUser(user);
            return ResponseEntity.ok(Map.of("message", "User registered successfully",
                    "Accountnumber", user.getAccounts().get(0).getAccountNumber()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    // NÝTT: Endpoint til að eyða notandareikningi
    @PostMapping("/delete-account")
    public String deleteAccount() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        boolean deleted = userService.deleteUserByUsername(username);

        if (deleted) {
            // Eyðing tókst - Vísar á útskráningu, sem vísar á login
            return "redirect:/logout";
        } else {
            // Eyðing mistókst (balance ekki 0). Redirecta á heimasíðu til að sýna villu.
            return "redirect:/?deleteError=true";
        }
    }


}
