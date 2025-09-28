package Controller;

import is.hi.hbv501g.team_19_bank.model.Account;
import is.hi.hbv501g.team_19_bank.model.User;
import is.hi.hbv501g.team_19_bank.repository.AccountRepository;
import is.hi.hbv501g.team_19_bank.repository.UserRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/bank")
public class BankController {
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;


    public BankController(UserRepository userRepository, AccountRepository accountRepository) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
    }

    @PostMapping("/user")
    public User createUser(@RequestBody User user) {
        return userRepository.save(user);
    }

    @GetMapping("/users")
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @PostMapping("/account/{userId}")
    public Account createAccount(@PathVariable Long userId, @RequestBody Account account) {
        User user = userRepository.findById(userId).orElseThrow();
        account.setUser(user);
        return accountRepository.save(account);
    }

    @GetMapping("/accounts")
    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }
}
