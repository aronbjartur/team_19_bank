package is.hi.hbv501g.team_19_bank.Controller;

import is.hi.hbv501g.team_19_bank.Service.UserService;
import is.hi.hbv501g.team_19_bank.model.Account;
import is.hi.hbv501g.team_19_bank.model.BankUser;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/users")
@RestController
public class BankUserController {

    private final UserService userService;

    public BankUserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BankUser createUser(@RequestBody BankUser user) {
        return userService.createUser(user);
    }

    @GetMapping
    public List<BankUser> getUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/{id}")
    public BankUser getUser(@PathVariable Long id) {
        return userService.getUserById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @GetMapping("/{id}/account/balance")
    public Double getAccountBalance(@PathVariable Long id) {
        return userService.getUserAccountBalance(id);
    }

    @GetMapping("/{id}/account")
    public Account getUserAccount(@PathVariable Long id) {
        return userService.getUserAccount(id);
    }

    // Delete user by ID (Virkar)
    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
    }
}
