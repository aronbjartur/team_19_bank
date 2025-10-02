package is.hi.hbv501g.team_19_bank.Controller;


import is.hi.hbv501g.team_19_bank.Service.UserService;
import is.hi.hbv501g.team_19_bank.model.BankUser;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/users")
@RestController
public class BankUserController {

    private final UserService userService;

    public BankUserController(UserService userService) {
        this.userService = userService;
    }

    // Create new user (Virkar)
    @PostMapping
    public BankUser createUser(@RequestBody BankUser user) {
        return userService.createUser(user);
    }

    // Get all users
    @GetMapping
    public List<BankUser> getUsers() {
        return userService.getAllUsers();
    }

    // Get user by ID (Virkar)
    @GetMapping("/{id}")
    public BankUser getUser(@PathVariable Long id) {
        return userService.getUserById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // Delete user by ID (Virkar)
    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
    }
}
