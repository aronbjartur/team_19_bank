package is.hi.hbv501g.team_19_bank.Service;

import is.hi.hbv501g.team_19_bank.Security.JwtUtil;
import is.hi.hbv501g.team_19_bank.model.Account;
import is.hi.hbv501g.team_19_bank.model.BankUser;
import is.hi.hbv501g.team_19_bank.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private JwtUtil jwtUtil;

    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<BankUser> user = userRepository.findByUsername(username);
        if (user.isPresent()) {
            var u = user.get();
            // String accountNumber = u.getAccounts().isEmpty() ? "N/A" : u.getAccounts().get(0).getAccountNumber(); // This line is not needed here

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
            // Þessi lína neyðir seinnilegan reikninga-listann til að hlaðast
            user.getAccounts().size();
        }
        return userOpt;
    }

    public String authenticate(String username, String unhashedPassword) {
        BankUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (passwordEncoder.matches(unhashedPassword, user.getPassword())) {
            return jwtUtil.generateToken(username);
        } else {
            throw new IllegalArgumentException("Invalid credentials");
        }
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

    // NÝTT: Skilar true ef eytt, false ef balance er ekki 0
    @Transactional
    public boolean deleteUserByUsername(String username) {
        Optional<BankUser> userOpt = getUserByUsernameWithAccounts(username);

        if (userOpt.isEmpty()) {
            // Notandi fannst ekki (tæknilega séð eytt)
            return true;
        }

        BankUser user = userOpt.get();

        // Athuga hvort allir reikningar séu 0 (Við vitum að það er bara einn)
        // **Mikilvægt:** TotalBalance þarf að vera nákvæmlega 0.0 til að eyða.
        if (user.getTotalBalance() > 0.0) {
            // Reikningurinn er ekki tómur - Ekki eyða
            return false;
        }

        // Eyða notanda (CascadeType.ALL sér um að eyða reikningnum)
        userRepository.delete(user);
        return true;
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

    public void changePassword(BankUser user, String oldPassword, String newPassword) {
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new IllegalArgumentException("Old password is incorrect.");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
}
