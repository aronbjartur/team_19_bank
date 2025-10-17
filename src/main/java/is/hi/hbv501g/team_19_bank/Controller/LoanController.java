package is.hi.hbv501g.team_19_bank.Controller;

import is.hi.hbv501g.team_19_bank.Service.AccountService;
import is.hi.hbv501g.team_19_bank.Service.LoanService;
import is.hi.hbv501g.team_19_bank.model.Loan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/loans")
public class LoanController {
    private final LoanService loanService;
    private final AccountService accountService;

    @Autowired
    public LoanController(LoanService loanService, AccountService accountService) {
        this.loanService = loanService;
        this.accountService = accountService;
    }

    // Get all loans
    @GetMapping
    public List<Loan> getAllLoans() {
        return loanService.getAllLoans();
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


}
