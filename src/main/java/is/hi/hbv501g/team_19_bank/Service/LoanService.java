package is.hi.hbv501g.team_19_bank.Service;

import is.hi.hbv501g.team_19_bank.model.Account;
import is.hi.hbv501g.team_19_bank.model.BankUser;
import is.hi.hbv501g.team_19_bank.model.Loan;
import is.hi.hbv501g.team_19_bank.model.LoanRequest;
import is.hi.hbv501g.team_19_bank.repository.AccountRepository;
import is.hi.hbv501g.team_19_bank.repository.LoanRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class LoanService {
    private final LoanRepository loanRepository;
    private final UserService userService;
    private final AccountRepository accountRepository;

    // UC11: Define the maximum total outstanding loans the bank can handle
    private static final double MAX_BANK_LIABILITY = 500000000.00; // 500 Million kr

    // The Bank's actual account number (used for DB lookup)
    private static final String ANCHOR_ACCOUNT_NUMBER = "bank"; // CHANGED: Now using "bank"

    public LoanService(LoanRepository loanRepository, UserService userService, AccountRepository accountRepository) {
        this.loanRepository = loanRepository;
        this.userService = userService;
        this.accountRepository = accountRepository;
    }

    // UC11: New method to check if the bank has reached its total lending limit
    private boolean checkBankCapacity(double newLoanAmount) {
        // 1. Get all active loans (where status is APPROVED or PENDING/UNPAID)
        List<Loan> activeLoans = loanRepository.findByStatusIn(
                List.of(Loan.LoanStatus.APPROVED, Loan.LoanStatus.PENDING)
        );

        // 2. Calculate the total outstanding amount
        double currentTotalLiability = activeLoans.stream()
                .mapToDouble(Loan::getLoanAmount)
                .sum();

        // 3. Check if the new loan pushes the bank over the limit
        if (currentTotalLiability + newLoanAmount > MAX_BANK_LIABILITY) {
            System.out.println("UC11 Check: Bank capacity exceeded by new loan.");
            return false;
        }
        return true;
    }


    @Transactional
    public Loan loan(LoanRequest loanRequest) {
        // Get the currently authenticated user
        // Gæti þurft að eyða þessu
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        BankUser authenticatedUser = userService.getUserByName(username).orElseThrow(() -> new RuntimeException("User not found with username: " + username));
        log.info("Attempting loan: {} from {} to {}", loanRequest.getAmount(), loanRequest.getLoanGiverAccount(), loanRequest.getLoanReceiverAccount());

        if (loanRequest.getLoanGiverAccount() == null || loanRequest.getLoanReceiverAccount() == null) {
            return reject(loanRequest, "Bank lender and loan receiver accounts are required.");
        }

        // CHECK 1: Ensure Loan Giver is "Bank" (case-insensitive)
        if (!loanRequest.getLoanGiverAccount().equalsIgnoreCase("Bank")) {
            return reject(loanRequest, "Loan giver account must be the bank (identified by 'Bank').");
        }

        if (loanRequest.getLoanGiverAccount().equals(loanRequest.getLoanReceiverAccount())) {
            return reject(loanRequest, "Lender account cannot be same as loan receiver account.");
        }
        if (loanRequest.getAmount() <= 0) {
            return reject(loanRequest, "Amount must be greater than 0 kr.");
        }

        // UC11: Check Bank Capacity
        if (!checkBankCapacity(loanRequest.getAmount())) {
            return reject(loanRequest, "Bank has reached its maximum lending capacity. Try again later.");
        }

        // Use the actual account number to retrieve the bank account
        Account lender = accountRepository.findByAccountNumber(ANCHOR_ACCOUNT_NUMBER).orElse(null);
        Account receiver = accountRepository.findByAccountNumber(loanRequest.getLoanReceiverAccount()).orElse(null);

        // Determine maximum withdrawal amount based on credit score
        int creditScore = receiver.getUser().getCreditScore();
        System.out.println("Credit Score: " + creditScore);
        double maxWithdrawalLimit;

        if (creditScore >= 1 && creditScore <= 350) {
            maxWithdrawalLimit = 100000;
        } else if (creditScore >= 351 && creditScore <= 700) {
            maxWithdrawalLimit = 250000;
        } else if (creditScore >= 701 && creditScore <= 850) {
            maxWithdrawalLimit = 500000;
        } else {
            return reject(loanRequest, "Invalid credit score.");
        }
        // Check if the requested amount exceeds the maximum withdrawal limit
        if (loanRequest.getAmount() > maxWithdrawalLimit) {
            return reject(loanRequest, "Requested loan amount exceeds the maximum withdrawal limit of " + maxWithdrawalLimit + " kr for your credit score ( " + creditScore + " ).");
        }
        if (lender == null || receiver == null) {
            return reject(loanRequest, "One or both account numbers are invalid or non-existent.");
        }

        if (lender.getBalance() < loanRequest.getAmount()) {
            return reject(loanRequest, "Insufficient funds in bank account.");
        }

        lender.setBalance(lender.getBalance() - loanRequest.getAmount());
        receiver.setBalance(receiver.getBalance() + loanRequest.getAmount());

        accountRepository.save(lender);
        accountRepository.save(receiver);
        // Create a new Loan object
        Loan loan = new Loan();
        loan.setUser(authenticatedUser.getUsername());
        loan.setLoanAmount(loanRequest.getAmount());

        // Store the Giver as the identifier "Bank" for display purposes
        loan.setLoanGiverAccount("Bank");

        loan.setLoanReceiverAccount(loanRequest.getLoanReceiverAccount());
        loan.setMemo(loanRequest.getMemo());
        loan.setStatus(Loan.LoanStatus.PENDING);


        // Save the loan to the repository
        Loan savedLoan = loanRepository.save(loan);
        loan.setStatus(Loan.LoanStatus.APPROVED);
        log.info("Loan created with ID {} for user {}", savedLoan.getLoanId(), authenticatedUser.getUsername());

        return savedLoan;
    }

    private Loan reject(LoanRequest req, String reason) {
        Loan l = new Loan();

        // FIX: Ensure authenticated_user is set to avoid DB NULL constraint error
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            l.setUser(authentication.getName());
        }

        l.setLoanGiverAccount(req.getLoanGiverAccount());
        l.setLoanReceiverAccount(req.getLoanReceiverAccount());
        l.setLoanAmount(req.getAmount());
        l.setMemo(req.getMemo());
        l.setStatus(Loan.LoanStatus.REJECTED);
        l.setFailureReason(reason);

        log.warn("Loan request was rejected: {}", reason);
        return loanRepository.save(l);
    }

    // Gæti þurft að tékka á þessu
    public Loan pay(Loan loan, Account payerAccount) {
        // Check if the payer has enough balance to pay the loan
        if (payerAccount.getBalance() < loan.getLoanAmount()) {
            log.warn("Insufficient funds in account {} to pay loan ID {}", payerAccount.getAccountNumber(), loan.getLoanId());
            return loan;
        }

        // Get the bank's account (loan giver)
        Account bankAccount = accountRepository.findByAccountNumber(ANCHOR_ACCOUNT_NUMBER) // Uses the actual number
                .orElseThrow(() -> new RuntimeException("Bank account not found"));

        // Perform the payment
        payerAccount.setBalance(payerAccount.getBalance() - loan.getLoanAmount());
        bankAccount.setBalance(bankAccount.getBalance() + loan.getLoanAmount());

        // Update the loan status to PAID_OFF
        loan.setStatus(Loan.LoanStatus.PAID_OFF);

        // Save the updated accounts and loan
        accountRepository.save(payerAccount);
        accountRepository.save(bankAccount);
        loanRepository.save(loan);

        log.info("Loan ID {} has been paid by account {}", loan.getLoanId(), payerAccount.getAccountNumber());
        return loan;
    }

    // Fetch all loans
    public List<Loan> getAllLoans() {
        return loanRepository.findAll();
    }

    public Optional<Loan> getLoanById(Long id) {
        return loanRepository.findById(id);
    }

    // Delete a loan by its ID
    public void deleteLoan(Long id) {
        if (loanRepository.existsById(id)) {
            loanRepository.deleteById(id);
            log.info("Loan with ID {} has been deleted.", id);
        } else {
            log.warn("Loan with ID {} does not exist.", id);
        }
    }


    public Optional<Loan> getLoansByAuthenticatedUser(String authenticatedUser) {
        return loanRepository.findByAuthenticatedUser(authenticatedUser);
    }
}
