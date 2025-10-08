package is.hi.hbv501g.team_19_bank.Controller;

import is.hi.hbv501g.team_19_bank.Service.UserService;
import is.hi.hbv501g.team_19_bank.model.BankUser;
import org.springframework.security.core.Authentication;
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

    // Fer bara á index ef það auth, annars ertu reddirectaður á login
    @GetMapping("/")
    public String index(Model model, Authentication authentication) {
        if (authentication == null || "anonymousUser".equals(authentication.getName())) {
            return "redirect:/login";
        }


        model.addAttribute("username", authentication.getName());
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    // signup
    @GetMapping("/signup")
    public String signup(Model model) {
        model.addAttribute("bankUser", new BankUser());
        return "signup";
    }

    // höndlar form submission
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

        // til login ef signup gekk
        return "redirect:/login";
    }
}
