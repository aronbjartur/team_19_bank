package is.hi.hbv501g.team_19_bank.Service;

import is.hi.hbv501g.team_19_bank.ExceptionHandling.UserNotFoundException;
import is.hi.hbv501g.team_19_bank.model.Account;
import is.hi.hbv501g.team_19_bank.repository.AccountRepository;
import is.hi.hbv501g.team_19_bank.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    @Autowired
    private UserRepository userRepository;

    // Constructor Injection (best practice)
    public AccountService(AccountRepository accountRepository, UserRepository userRepository) {
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
    }

    // Create a new account
    public Account createAccount(Account account) {
        // Check if the user exists in the users table
        if (account.getUser() == null || !userRepository.existsById(account.getUser().getId())) {
            throw new UserNotFoundException("User not found with ID: " + (account.getUser() != null ? account.getUser().getId() : "null"));
        }
        return accountRepository.save(account);
    }

    // Get all accounts
    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }

    // Get account by ID
    public Optional<Account> getAccountById(Long id) {
        return accountRepository.findById(id);
    }

    // Delete account by ID
    public void deleteAccount(Long id) {
        accountRepository.deleteById(id);
    }

    /**
     // Find account by username (custom query from repository)
     public Optional<Account> getAccountByUsername(String username) {
     return Optional.ofNullable(accountRepository.findByUsername(username));
     }
     **/
}
