package is.hi.hbv501g.team_19_bank.Controller;

import is.hi.hbv501g.team_19_bank.Service.AccountService;
import is.hi.hbv501g.team_19_bank.Service.LoanService;
import is.hi.hbv501g.team_19_bank.Service.UserService;
import is.hi.hbv501g.team_19_bank.model.Account;
import is.hi.hbv501g.team_19_bank.model.Loan;
import is.hi.hbv501g.team_19_bank.model.LoanPaymentRequest;
import is.hi.hbv501g.team_19_bank.model.LoanRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@Controller // Kannski breyta þessu í @Controller ef við viljum skila HTML síðum
@RequestMapping("/loans")
public class LoanController {
    private final LoanService loanService;
    private final UserService userService;
    private final AccountService accountService;

    @Autowired
    public LoanController(LoanService loanService, UserService userService, AccountService accountService) {
        this.loanService = loanService;
        this.userService = userService;
        this.accountService = accountService;
    }


    @GetMapping("/user-loans")
    public Optional<Loan> getUserLoans(Authentication authentication) {
        String username = authentication.getName();
        return loanService.getLoansByAuthenticatedUser(username);
    }

    // Get a loan by ID
    @GetMapping("/{id}")
    public Optional<Loan> getLoanById(@PathVariable Long id) {
        return loanService.getLoanById(id);
    }

    // Delete a loan by ID
    @DeleteMapping("/{id}")
    public void deleteLoan(@PathVariable Long id) {
        loanService.deleteLoan(id);
    }

    @GetMapping
    public String form(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        try {
            // Fá ID og reikningsnúmer notanda
            Long userId = userService.getUserByUsernameWithAccounts(username)
                    .orElseThrow(() -> new RuntimeException("User not found")).getId();

            String userAccountNumber = userService.getUserAccount(userId).getAccountNumber();

            // Setja reikningsnúmerið í model-ið
            model.addAttribute("userAccountNumber", userAccountNumber);

        } catch (Exception e) {
            // Ef villa, þá áframsenda á login síðuna (ætti ekki að gerast ef innskráning tókst)
            return "redirect:/login";
        }

        model.addAttribute("loanRequest", new LoanRequest());
        return "loan";
    }

    @PostMapping
    public ResponseEntity<?> createLoan(@RequestBody @Valid LoanRequest req) {
        try {
            // Log the incoming request
            System.out.println("Loan request received: " + req);

            // Hardcoded loan giver account number
            // REMOVED: final String bankAccountNumber = "100200300"; // No longer needed here

            // Validate the loan giver account
            // MODIFIED: Use case-insensitive check against "bank"
            if (!req.getLoanGiverAccount().equalsIgnoreCase("bank")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                        "error", "Loan giver account must be the bank account."
                ));
            }

            // Get the authenticated user's username
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            if (username.equals("anonymousUser")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                        "error", "User is not authenticated."
                ));
            }
            System.out.println("Authenticated user: " + username);

            // Validate user and retrieve their account number
            Long userId = userService.getUserByUsernameWithAccounts(username)
                    .orElseThrow(() -> new RuntimeException("User not found")).getId();
            String userAccountNumber = userService.getUserAccount(userId).getAccountNumber();

            // Ensure the loan receiver account matches the user's account
            if (!req.getLoanReceiverAccount().equals(userAccountNumber)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                        "error", "Loan receiver account does not belong to the authenticated user."
                ));
            }

            // Process the loan
            Loan submittedLoan = loanService.loan(req);

            // Retrieve the confirmed loan
            Loan confirmedLoan = loanService.getLoanById(submittedLoan.getLoanId())
                    .orElse(submittedLoan);

            // --- FIX START: Check for REJECTION status (UC10 Failure) ---
            if (confirmedLoan.getStatus() == Loan.LoanStatus.REJECTED) {
                // Return a 400 Bad Request and detail the rejection
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                        "error", confirmedLoan.getFailureReason(),
                        "loan", confirmedLoan // Still include loan object for full context
                ));
            }
            // --- FIX END ---

            // Return the loan details (only runs if status is APPROVED/PENDING/COMPLETED)
            return ResponseEntity.ok(Map.of(
                    "message", "Loan created successfully",
                    "loan", confirmedLoan
            ));

        } catch (Exception e) {
            // Handle errors and return a bad request response
            System.out.println("Error during loan creation: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "error", e.getMessage()
            ));
        }
    }

    @PutMapping("/pay")
    public ResponseEntity<?> payLoan(@RequestBody @Valid LoanPaymentRequest request) {
        try {
            // Log the incoming request
            System.out.println("Loan payment request received: " + request);

            // Get the authenticated user's username
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            if (username.equals("anonymousUser")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                        "error", "User is not authenticated."
                ));
            }
            System.out.println("Authenticated user: " + username);

            // Fetch the loan by ID
            Loan loan = loanService.getLoanById(request.getLoanId())
                    .orElseThrow(() -> new RuntimeException("Loan not found with ID: " + request.getLoanId()));

            // Fetch the authenticated user's account
            Account userAccount = accountService.getAccountByAccountNumber(request.getPayerAccount())
                    .orElseThrow(() -> new RuntimeException("Account not found with account number: " + request.getPayerAccount()));

            // Process the payment
            Loan updatedLoan = loanService.pay(loan, userAccount);

            // Check if the loan was successfully paid
            if (updatedLoan.getStatus() == Loan.LoanStatus.PAID_OFF) {
                return ResponseEntity.ok(Map.of(
                        "message", "Loan payment successful",
                        "loan", updatedLoan
                ));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                        "error", "Loan payment failed. Check account balance or loan details."
                ));
            }

        } catch (Exception e) {
            // Handle errors and return a bad request response
            System.out.println("Error during loan payment: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "error", e.getMessage()
            ));
        }
    }
}
