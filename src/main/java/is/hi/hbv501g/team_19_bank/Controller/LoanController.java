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

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
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
    @ResponseBody
    public List<Loan> getUserLoans(Authentication authentication) {
        String username = authentication.getName();
        return loanService.getLoansByAuthenticatedUser(username);
    }

    @GetMapping("/my")
    @ResponseBody
    public ResponseEntity<?> getMyLoans(Authentication authentication) {
        try {
            String username = authentication.getName();
            List<Loan> loans = loanService.getLoansVisibleToUser(username);
            return ResponseEntity.ok(loans);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "error", e.getMessage()
            ));
        }
    }

    @GetMapping("/pending-approval")
    @ResponseBody
    public ResponseEntity<?> getPendingApprovalLoans(Authentication authentication) {
        try {
            String username = authentication.getName();
            List<Loan> loans = loanService.getPendingApprovalLoans(username);
            return ResponseEntity.ok(loans);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "error", e.getMessage()
            ));
        }
    }

    @GetMapping("/{id}")
    @ResponseBody
    public Optional<Loan> getLoanById(@PathVariable Long id) {
        return loanService.getLoanById(id);
    }

    @DeleteMapping("/{id}")
    @ResponseBody
    public void deleteLoan(@PathVariable Long id) {
        loanService.deleteLoan(id);
    }

    @GetMapping
    public String form(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        try {
            Long userId = userService.getUserByUsernameWithAccounts(username)
                    .orElseThrow(() -> new RuntimeException("User not found")).getId();

            String userAccountNumber = userService.getUserAccount(userId).getAccountNumber();
            model.addAttribute("userAccountNumber", userAccountNumber);

        } catch (Exception e) {
            return "redirect:/login";
        }

        model.addAttribute("loanRequest", new LoanRequest());
        return "loan";
    }

    @PostMapping
    @ResponseBody
    public ResponseEntity<?> createLoan(@RequestBody @Valid LoanRequest req) {
        try {
            System.out.println("Loan request received: " + req);

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            if (username.equals("anonymousUser")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                        "error", "User is not authenticated."
                ));
            }

            Long userId = userService.getUserByUsernameWithAccounts(username)
                    .orElseThrow(() -> new RuntimeException("User not found")).getId();
            String userAccountNumber = userService.getUserAccount(userId).getAccountNumber();

            if (!req.getLoanReceiverAccount().equals(userAccountNumber)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                        "error", "Loan receiver account does not belong to the authenticated user."
                ));
            }

            Loan submittedLoan = loanService.loan(req);
            Loan confirmedLoan = loanService.getLoanById(submittedLoan.getLoanId()).orElse(submittedLoan);

            if (confirmedLoan.getStatus() == Loan.LoanStatus.REJECTED) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                        "error", confirmedLoan.getFailureReason(),
                        "loan", confirmedLoan
                ));
            }

            return ResponseEntity.ok(Map.of(
                    "message", confirmedLoan.getStatus() == Loan.LoanStatus.PENDING
                            ? "Loan request created and is waiting for lender approval"
                            : "Loan created successfully",
                    "loan", confirmedLoan
            ));

        } catch (Exception e) {
            System.out.println("Error during loan creation: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "error", e.getMessage()
            ));
        }
    }

    @PutMapping("/{id}/approve")
    @ResponseBody
    public ResponseEntity<?> approveLoan(
            @PathVariable Long id,
            @RequestParam(required = false) String dueAt
    ) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            if (username.equals("anonymousUser")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                        "error", "User is not authenticated."
                ));
            }

            Instant parsedDueAt = null;
            if (dueAt != null && !dueAt.isBlank()) {
                parsedDueAt = Instant.parse(dueAt);
            }

            Loan updatedLoan = loanService.approveLoan(id, username, parsedDueAt);

            return ResponseEntity.ok(Map.of(
                    "message", "Loan approved successfully",
                    "loan", updatedLoan
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "error", e.getMessage()
            ));
        }
    }

    @PutMapping("/{id}/reject")
    @ResponseBody
    public ResponseEntity<?> rejectLoan(@PathVariable Long id) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            if (username.equals("anonymousUser")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                        "error", "User is not authenticated."
                ));
            }

            Loan updatedLoan = loanService.rejectLoan(id, username);

            return ResponseEntity.ok(Map.of(
                    "message", "Loan rejected successfully",
                    "loan", updatedLoan
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "error", e.getMessage()
            ));
        }
    }

    @PutMapping("/pay")
    @ResponseBody
    public ResponseEntity<?> payLoan(@RequestBody @Valid LoanPaymentRequest request) {
        try {
            System.out.println("Loan payment request received: " + request);

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            if (username.equals("anonymousUser")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                        "error", "User is not authenticated."
                ));
            }

            Loan loan = loanService.getLoanById(request.getLoanId())
                    .orElseThrow(() -> new RuntimeException("Loan not found with ID: " + request.getLoanId()));

            Account userAccount = accountService.getAccountByAccountNumber(request.getPayerAccount())
                    .orElseThrow(() -> new RuntimeException("Account not found with account number: " + request.getPayerAccount()));

            Loan updatedLoan = loanService.pay(loan, userAccount, request.getAmount(), username);

            String message = updatedLoan.getStatus() == Loan.LoanStatus.PAID_OFF
                    ? "Loan fully paid off"
                    : "Loan payment successful";

            return ResponseEntity.ok(Map.of(
                    "message", message,
                    "loan", updatedLoan
            ));

        } catch (Exception e) {
            System.out.println("Error during loan payment: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "error", e.getMessage()
            ));
        }
    }
}