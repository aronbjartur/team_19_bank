package is.hi.hbv501g.team_19_bank.Service;

import is.hi.hbv501g.team_19_bank.model.Account;
import is.hi.hbv501g.team_19_bank.model.BankUser;
import is.hi.hbv501g.team_19_bank.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class UserService implements UserDetailsService {

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder; // -Ó
    private final AccountService accountService;

    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<BankUser> user = userRepository.findByUsername(username);
        if (user.isPresent()) {
            var u = user.get();
            String accountNumber = u.getAccounts().isEmpty() ? "N/A" : u.getAccounts().get(0).getAccountNumber();

            return User.builder()
                    .username(u.getUsername())
                    .password(u.getPassword())
                    .roles("USER")
                    .build();
        } else {
            throw new UsernameNotFoundException("User not found with username: " + username);
        }
    }

    @Transactional
    public Optional<BankUser> getUserByUsernameWithAccounts(String username) {
        Optional<BankUser> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            BankUser user = userOpt.get();
            user.getAccounts().size();
        }
        return userOpt;
    }

    @Transactional
    public BankUser createUser(BankUser user) {
        Optional<BankUser> existingUser = userRepository.findByUsername(user.getUsername());

        if (user.getCreditScore() < 0 || user.getCreditScore() > 850) {
            throw new IllegalArgumentException("Credit score must be non-negative and not exceed 850");
        } else if (existingUser.isPresent()) {
            throw new IllegalArgumentException("Username already exists");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        BankUser savedUser = userRepository.save(user);
        Account newAccount = accountService.createDefaultAccountForUser(savedUser);
        savedUser.getAccounts().add(newAccount);

        System.out.println("Creating user: " + savedUser.getUsername() + " with account: " + newAccount.getAccountNumber());
        return savedUser;
    }

    // Get all users
    public List<BankUser> getAllUsers() {
        return userRepository.findAll();
    }

    // Get user by ID
    public Optional<BankUser> getUserById(Long id) {
        return userRepository.findById(id);
    }

    // Get user by name
    public Optional<BankUser> getUserByName(String name) {
        return userRepository.findByUsername(name);
    }

    @Transactional
    public Account getUserAccount(Long id) {
        BankUser user = getUserById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getAccounts().isEmpty()) {
            throw new RuntimeException("User has no associated bank account.");
        }

        return user.getAccounts().get(0);
    }

    public Double getUserAccountBalance(Long id) {
        return getUserAccount(id).getBalance();
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
}
