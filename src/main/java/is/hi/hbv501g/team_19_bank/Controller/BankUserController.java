package is.hi.hbv501g.team_19_bank.Controller;

import is.hi.hbv501g.team_19_bank.Service.TransferService;
import is.hi.hbv501g.team_19_bank.Service.UserService;
import is.hi.hbv501g.team_19_bank.model.Account;
import is.hi.hbv501g.team_19_bank.model.BankUser;
import is.hi.hbv501g.team_19_bank.model.ChangePasswordRequest;
import is.hi.hbv501g.team_19_bank.model.Transfer;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RequestMapping("/users")
@RestController
public class BankUserController {

    private final UserService userService;
    private final TransferService transferService;

    public BankUserController(UserService userService, TransferService transferService) {
        this.userService = userService;
        this.transferService = transferService;
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

    @GetMapping("/me/id")
    public ResponseEntity<?> getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        // Find user by username
        BankUser user = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return ResponseEntity.ok(Map.of("id", user.getId()));
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

    @PatchMapping("/{id}/change-password")
    public ResponseEntity<?> changePassword(@PathVariable Long id, @RequestBody ChangePasswordRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        // Ensure the authenticated user matches the user ID being updated
        BankUser user = userService.getUserById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.getUsername().equals(username)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                    "error", "You can only change your own password."
            ));
        }
        try {
            userService.changePassword(user, request.getOldPassword(), request.getNewPassword());
            return ResponseEntity.ok(Map.of(
                    "message", "Password changed successfully."
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "error", e.getMessage()
            ));
        }
    }

    @GetMapping("/{id}/transactions")
    public ResponseEntity<?> getUserTransactions(@PathVariable Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        // Ensure the authenticated user matches the user ID being accessed
        BankUser user = userService.getUserById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.getUsername().equals(username)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                    "error", "You can only view your own transactions."
            ));
        }

        // Retrieve transactions for the user's account
        Account account = userService.getUserAccount(id);
        String accountNumber = account.getAccountNumber();
        List<Transfer> transactions = transferService.getTransactionsByAccountNumber(accountNumber);

        return ResponseEntity.ok(transactions);
    }

}
