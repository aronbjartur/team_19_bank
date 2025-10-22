package is.hi.hbv501g.team_19_bank.Controller;

import is.hi.hbv501g.team_19_bank.Service.AccountService;
import is.hi.hbv501g.team_19_bank.Service.UserService;
import is.hi.hbv501g.team_19_bank.model.BankUser;
import is.hi.hbv501g.team_19_bank.model.LoginRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class ContentController {
    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final AccountService accountService;

    public ContentController(UserService userService, AccountService accountService, AuthenticationManager authenticationManager) {
        this.userService = userService;
        this.accountService = accountService;
        this.authenticationManager = authenticationManager;
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


    // Þetta virkar með postman
    @PostMapping("/signup")
    public ResponseEntity<String> registerUser(@RequestBody BankUser user) {
        try {
            userService.createUser(user);
            return ResponseEntity.ok("User registered successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
    /*
    @PostMapping("/signup")
    public String registerUser(@ModelAttribute("bankUser") BankUser user, BindingResult result) {
        if (result.hasErrors()) {
            return "signup";
        }

        try {
            userService.createUser(user);
        } catch (IllegalArgumentException e) {
            result.rejectValue("username", "user.exists", e.getMessage());
            return "signup";
        }

        return "redirect:/login";
    }

      */

    /**
     * // Fer bara á index ef það auth, annars ertu reddirectaður á login
     *
     * @GetMapping("/") public String index(Model model) {
     * Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
     * <p>
     * if (authentication == null || "anonymousUser".equals(authentication.getName())) {
     * return "redirect:/login";
     * }
     * <p>
     * String username = authentication.getName();
     * <p>
     * BankUser user = userService.getUserByUsernameWithAccounts(username)
     * .orElseThrow(() -> new RuntimeException("Authenticated user not found in database."));
     * <p>
     * // Bæta gögnum við Model-ið
     * model.addAttribute("username", user.getUsername());
     * <p>
     * // Vegna þess að við neyðum bara einn reikning, getum við sótt hann beint
     * double balance = user.getAccounts().get(0).getBalance();
     * String accountNumber = user.getAccounts().get(0).getAccountNumber();
     * <p>
     * model.addAttribute("balance", String.format("%.2f", balance)); // Sýnir 0.00 í stað 0.0, breyttum þessu seinna í heilartölur
     * model.addAttribute("accountNumber", accountNumber);
     * model.addAttribute("creditScore", user.getCreditScore());
     * <p>
     * return "index";
     * }
     * @GetMapping("/login") public String login() {
     * return "login";
     * }
     * @GetMapping("/signup") public String signup(Model model) {
     * model.addAttribute("bankUser", new BankUser());
     * return "signup";
     * }
     */

}
