package is.hi.hbv501g.team_19_bank.Controller;

import is.hi.hbv501g.team_19_bank.Service.UserService;
import is.hi.hbv501g.team_19_bank.model.BankUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class ContentController {

    private final UserService userService;

    public ContentController(UserService userService) {
        this.userService = userService;
    }

    // Fer bara á index ef það auth, annars ertu reddirectaður á login    @GetMapping("/")
    public String index(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || "anonymousUser".equals(authentication.getName())) {
            return "redirect:/login";
        }

        String username = authentication.getName();

        BankUser user = userService.getUserByUsernameWithAccounts(username)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found in database."));

        // Bæta gögnum við Model-ið
        model.addAttribute("username", user.getUsername());

        // Vegna þess að við neyðum bara einn reikning, getum við sótt hann beint
        double balance = user.getAccounts().get(0).getBalance();
        String accountNumber = user.getAccounts().get(0).getAccountNumber();

        model.addAttribute("balance", String.format("%.2f", balance)); // Sýnir 0.00 í stað 0.0, breyttum þessu seinna í heilartölur
        model.addAttribute("accountNumber", accountNumber);
        model.addAttribute("creditScore", user.getCreditScore());

        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/signup")
    public String signup(Model model) {
        model.addAttribute("bankUser", new BankUser());
        return "signup";
    }

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
}
