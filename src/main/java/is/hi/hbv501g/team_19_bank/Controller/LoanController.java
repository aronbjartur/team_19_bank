package is.hi.hbv501g.team_19_bank.Controller;

import is.hi.hbv501g.team_19_bank.Service.AccountService;
import is.hi.hbv501g.team_19_bank.Service.LoanService;
import is.hi.hbv501g.team_19_bank.Service.UserService;
import is.hi.hbv501g.team_19_bank.model.Loan;
import is.hi.hbv501g.team_19_bank.model.LoanRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

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
    public String submit(@ModelAttribute("loanRequest") @Valid LoanRequest req,
                         BindingResult binding,
                         Model model) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        // Fá reikningsnúmerið aftur fyrir villumeðhöndlun
        Long userId = userService.getUserByUsernameWithAccounts(username)
                .orElseThrow(() -> new RuntimeException("User not found")).getId();
        String userAccountNumber = userService.getUserAccount(userId).getAccountNumber();

        model.addAttribute("userAccountNumber", userAccountNumber);

        if (binding.hasErrors()) {
            return "loan";
        }

        Loan submittedLoan = loanService.loan(req);

        Loan confirmedLoan = loanService.getLoanById(submittedLoan.getLoanId())
                .orElse(submittedLoan); // Fallback ef það finnst ekki.

        model.addAttribute("loan", confirmedLoan);
        return "loan_result";
    }
/**
 @PutMapping("/pay") public ResponseEntity<Loan> payLoan(@ModelAttribute("loanPaymentRequest") @Valid LoanPaymentRequest request,
 BindingResult binding,
 Model model) {
 Authentication auth = SecurityContextHolder.getContext().getAuthentication();
 String username = auth.getName();
 // Fetch the loan by ID
 Loan loan = loanService.getLoanById(request.getLoanId())
 .orElseThrow(() -> new RuntimeException("Loan not found with ID: " + request.getLoanId()));
 Long userId = userService.getUserByUsernameWithAccounts(username)
 .orElseThrow(() -> new RuntimeException("User not found")).getId();
 String userAccountNumber = userService.getUserAccount(userId).getAccountNumber();
 // Fetch the payer's account by account number
 Account payerAccount = accountService.getAccountByAccountNumber(userAccountNumber)
 .orElseThrow(() -> new RuntimeException("Account not found with account number: " + userAccountNumber));

 // Call the pay method in LoanService
 Loan updatedLoan = loanService.pay(loan, payerAccount);

 return ResponseEntity.ok(updatedLoan);
 }**/
}
