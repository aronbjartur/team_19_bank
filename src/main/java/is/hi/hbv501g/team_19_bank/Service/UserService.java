package is.hi.hbv501g.team_19_bank.Service;


import is.hi.hbv501g.team_19_bank.model.BankUser;
import is.hi.hbv501g.team_19_bank.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import org.springframework.security.crypto.password.PasswordEncoder; // -Ó

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class UserService implements UserDetailsService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder; // -Ó

    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<BankUser> user = userRepository.findByUsername(username);
        if (user.isPresent()) {
            var u = user.get();
            return User.builder().username(u.getUsername()).password(u.getPassword()).build();
        } else {
            throw new UsernameNotFoundException("User not found with username: " + username);
        }
    }


    // Create a new user
    public BankUser createUser(BankUser user) {
        Optional<BankUser> existingUser = userRepository.findByUsername(user.getUsername());
        if (user.getCreditScore() < 0 || user.getCreditScore() > 850) {
            throw new IllegalArgumentException("Credit score must be non-negative and not exceed 850");
        } else if (existingUser.isPresent()) {
            throw new IllegalArgumentException("Username already exists");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword())); // -Ó

        System.out.println("Creating user: " + user.getUsername());
        return userRepository.save(user);
    }

    // Get all users
    public List<BankUser> getAllUsers() {
        return userRepository.findAll();
    }

    // Get user by ID
    public Optional<BankUser> getUserById(Long id) {
        return userRepository.findById(id);
    }

    // Delete user by ID
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    /*// Find user by username (custom query from repository)
    public Optional<BankUser> getUserByUsername(String username) {
        return Optional.ofNullable(userRepository.findByUsername(username));
    }
    */
}
