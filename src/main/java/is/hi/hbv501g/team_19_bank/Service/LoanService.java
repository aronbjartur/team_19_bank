package is.hi.hbv501g.team_19_bank.Service;

import is.hi.hbv501g.team_19_bank.model.Account;
import is.hi.hbv501g.team_19_bank.model.BankUser;
import is.hi.hbv501g.team_19_bank.model.Loan;
import is.hi.hbv501g.team_19_bank.model.LoanRequest;
import is.hi.hbv501g.team_19_bank.model.Transfer;
import is.hi.hbv501g.team_19_bank.repository.AccountRepository;
import is.hi.hbv501g.team_19_bank.repository.LoanRepository;
import is.hi.hbv501g.team_19_bank.repository.TransferRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class LoanService {
    private final LoanRepository loanRepository;
    private final UserService userService;
    private final AccountRepository accountRepository;
    private final TransferRepository transferRepository;

    private static final double MAX_BANK_LIABILITY = 500000000.00;
    private static final String BANK_ACCOUNT_NUMBER = "bank";

    public LoanService(
            LoanRepository loanRepository,
            UserService userService,
            AccountRepository accountRepository,
            TransferRepository transferRepository
    ) {
        this.loanRepository = loanRepository;
        this.userService = userService;
        this.accountRepository = accountRepository;
        this.transferRepository = transferRepository;
    }

    private boolean isBankAccount(String accountNumber) {
        return accountNumber != null && accountNumber.equalsIgnoreCase(BANK_ACCOUNT_NUMBER);
    }

    private String normalizeLenderAccount(String accountNumber) {
        return isBankAccount(accountNumber) ? BANK_ACCOUNT_NUMBER : accountNumber;
    }

    private Account getAuthenticatedUserPrimaryAccount(String username) {
        Long userId = userService.getUserByUsernameWithAccounts(username)
                .orElseThrow(() -> new RuntimeException("User not found"))
                .getId();
        return userService.getUserAccount(userId);
    }

    private boolean accountBelongsToUser(String accountNumber, String username) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account not found: " + accountNumber));

        return account.getUser() != null && account.getUser().getUsername().equals(username);
    }

    private void validateLoanAmountAgainstCreditScore(Account receiver, double requestedAmount) {
        int creditScore = receiver.getUser().getCreditScore();
        double maxWithdrawalLimit;

        if (creditScore >= 1 && creditScore <= 350) {
            maxWithdrawalLimit = 100000;
        } else if (creditScore >= 351 && creditScore <= 700) {
            maxWithdrawalLimit = 250000;
        } else if (creditScore >= 701 && creditScore <= 850) {
            maxWithdrawalLimit = 500000;
        } else {
            throw new RuntimeException("Invalid credit score.");
        }

        if (requestedAmount > maxWithdrawalLimit) {
            throw new RuntimeException(
                    "Requested loan amount exceeds the maximum withdrawal limit of "
                            + maxWithdrawalLimit
                            + " kr for your credit score (" + creditScore + ")."
            );
        }
    }

    private boolean checkBankCapacity(double newLoanAmount) {
        List<Loan> activeLoans = loanRepository.findByStatusIn(
                List.of(Loan.LoanStatus.APPROVED)
        );

        double currentTotalLiability = activeLoans.stream()
                .filter(loan -> isBankAccount(loan.getLoanGiverAccount()))
                .mapToDouble(loan -> loan.getRemainingAmount() != null ? loan.getRemainingAmount() : loan.getLoanAmount())
                .sum();

        if (currentTotalLiability + newLoanAmount > MAX_BANK_LIABILITY) {
            log.warn("UC11 Check: Bank capacity exceeded by new loan.");
            return false;
        }

        return true;
    }

    private void createTransferRecord(String source, String destination, double amount, String memo, Long loanId) {
        Transfer t = new Transfer();
        t.setSourceAccount(source);
        t.setDestinationAccount(destination);
        t.setAmount(amount);
        t.setMemo(memo);
        t.setLoanId(loanId);
        t.setStatus(Transfer.Status.COMPLETED);
        transferRepository.save(t);
    }

    private void applyOverduePenaltyIfNeeded(Loan loan) {
        if (loan == null) return;
        if (loan.getStatus() != Loan.LoanStatus.APPROVED) return;
        if (loan.getDueAt() == null) return;
        if (loan.getPenaltyAppliedAt() != null) return;
        if (loan.getRemainingAmount() == null || loan.getRemainingAmount() <= 0) return;
        if (!Instant.now().isAfter(loan.getDueAt())) return;

        double multiplier = loan.getInterestRateAfterApproval() == null ? 1.05 : loan.getInterestRateAfterApproval();
        loan.setRemainingAmount(loan.getRemainingAmount() * multiplier);
        loan.setPenaltyAppliedAt(Instant.now());
        loanRepository.save(loan);

        log.info("Applied one-time overdue penalty to loan {}", loan.getLoanId());
    }

    @Transactional
    public Loan loan(LoanRequest loanRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        BankUser authenticatedUser = userService.getUserByName(username)
                .orElseThrow(() -> new RuntimeException("User not found with username: " + username));

        log.info("Attempting loan: {} from {} to {}", loanRequest.getAmount(),
                loanRequest.getLoanGiverAccount(), loanRequest.getLoanReceiverAccount());

        if (loanRequest.getLoanGiverAccount() == null || loanRequest.getLoanReceiverAccount() == null) {
            return reject(loanRequest, "Loan giver and receiver accounts are required.");
        }

        if (loanRequest.getLoanGiverAccount().equals(loanRequest.getLoanReceiverAccount())) {
            return reject(loanRequest, "Lender account cannot be same as loan receiver account.");
        }

        if (loanRequest.getAmount() <= 0) {
            return reject(loanRequest, "Amount must be greater than 0 kr.");
        }

        Account receiver = accountRepository.findByAccountNumber(loanRequest.getLoanReceiverAccount()).orElse(null);
        if (receiver == null) {
            return reject(loanRequest, "Loan receiver account does not exist.");
        }

        if (receiver.getUser() == null || !receiver.getUser().getUsername().equals(username)) {
            return reject(loanRequest, "Loan receiver account must belong to authenticated user.");
        }

        validateLoanAmountAgainstCreditScore(receiver, loanRequest.getAmount());

        String normalizedLender = normalizeLenderAccount(loanRequest.getLoanGiverAccount());

        Loan loan = new Loan();
        loan.setUser(authenticatedUser.getUsername());
        loan.setLoanAmount(loanRequest.getAmount());
        loan.setRemainingAmount(loanRequest.getAmount());
        loan.setLoanGiverAccount(normalizedLender);
        loan.setLoanReceiverAccount(loanRequest.getLoanReceiverAccount());
        loan.setMemo(loanRequest.getMemo());

        if (isBankAccount(normalizedLender)) {
            if (!checkBankCapacity(loanRequest.getAmount())) {
                return reject(loanRequest, "Bank has reached its maximum lending capacity. Try again later.");
            }

            Account lender = accountRepository.findByAccountNumber(BANK_ACCOUNT_NUMBER).orElse(null);
            if (lender == null) {
                return reject(loanRequest, "Bank account not found.");
            }

            if (lender.getBalance() < loanRequest.getAmount()) {
                return reject(loanRequest, "Insufficient funds in bank account.");
            }

            lender.setBalance(lender.getBalance() - loanRequest.getAmount());
            receiver.setBalance(receiver.getBalance() + loanRequest.getAmount());

            accountRepository.save(lender);
            accountRepository.save(receiver);

            loan.setStatus(Loan.LoanStatus.APPROVED);
            loan.setApprovedAt(Instant.now());
            loan.setDueAt(Instant.now().plus(30, ChronoUnit.DAYS));

            Loan savedLoan = loanRepository.save(loan);

            createTransferRecord(
                    lender.getAccountNumber(),
                    receiver.getAccountNumber(),
                    loanRequest.getAmount(),
                    "Loan disbursement (bank)",
                    savedLoan.getLoanId()
            );

            log.info("Bank loan created with ID {} for user {}", savedLoan.getLoanId(), authenticatedUser.getUsername());
            return savedLoan;
        }

        Account lender = accountRepository.findByAccountNumber(normalizedLender).orElse(null);
        if (lender == null) {
            return reject(loanRequest, "Loan giver account does not exist.");
        }

        if (lender.getUser() == null) {
            return reject(loanRequest, "Loan giver account is invalid.");
        }

        loan.setStatus(Loan.LoanStatus.PENDING);

        Loan savedLoan = loanRepository.save(loan);
        log.info("User-to-user loan request created with ID {} for borrower {}", savedLoan.getLoanId(), authenticatedUser.getUsername());
        return savedLoan;
    }

    @Transactional
    public Loan approveLoan(Long loanId, String username, Instant dueAt) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found with ID: " + loanId));

        if (loan.getStatus() != Loan.LoanStatus.PENDING) {
            throw new RuntimeException("Only pending loans can be approved.");
        }

        if (isBankAccount(loan.getLoanGiverAccount())) {
            throw new RuntimeException("Bank loans are approved immediately and cannot be approved manually.");
        }

        if (!accountBelongsToUser(loan.getLoanGiverAccount(), username)) {
            throw new RuntimeException("You are not allowed to approve this loan.");
        }

        Account lender = accountRepository.findByAccountNumber(loan.getLoanGiverAccount())
                .orElseThrow(() -> new RuntimeException("Lender account not found."));
        Account receiver = accountRepository.findByAccountNumber(loan.getLoanReceiverAccount())
                .orElseThrow(() -> new RuntimeException("Receiver account not found."));

        validateLoanAmountAgainstCreditScore(receiver, loan.getLoanAmount());

        if (lender.getBalance() < loan.getLoanAmount()) {
            throw new RuntimeException("Insufficient funds in lender account.");
        }

        Instant resolvedDueAt = dueAt != null ? dueAt : Instant.now().plus(30, ChronoUnit.DAYS);
        if (!resolvedDueAt.isAfter(Instant.now())) {
            throw new RuntimeException("Due date must be in the future.");
        }

        lender.setBalance(lender.getBalance() - loan.getLoanAmount());
        receiver.setBalance(receiver.getBalance() + loan.getLoanAmount());

        accountRepository.save(lender);
        accountRepository.save(receiver);

        loan.setStatus(Loan.LoanStatus.APPROVED);
        loan.setApprovedAt(Instant.now());
        loan.setDueAt(resolvedDueAt);
        loan.setRemainingAmount(
                loan.getRemainingAmount() == null ? loan.getLoanAmount() : loan.getRemainingAmount()
        );

        Loan savedLoan = loanRepository.save(loan);

        createTransferRecord(
                lender.getAccountNumber(),
                receiver.getAccountNumber(),
                loan.getLoanAmount(),
                "Loan disbursement #" + savedLoan.getLoanId(),
                savedLoan.getLoanId()
        );

        return savedLoan;
    }

    @Transactional
    public Loan rejectLoan(Long loanId, String username) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found with ID: " + loanId));

        if (loan.getStatus() != Loan.LoanStatus.PENDING) {
            throw new RuntimeException("Only pending loans can be rejected.");
        }

        if (isBankAccount(loan.getLoanGiverAccount())) {
            throw new RuntimeException("Bank loans cannot be manually rejected.");
        }

        if (!accountBelongsToUser(loan.getLoanGiverAccount(), username)) {
            throw new RuntimeException("You are not allowed to reject this loan.");
        }

        loan.setStatus(Loan.LoanStatus.REJECTED);
        loan.setFailureReason("Rejected by lender");

        return loanRepository.save(loan);
    }

    private Loan reject(LoanRequest req, String reason) {
        Loan l = new Loan();

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            l.setUser(authentication.getName());
        }

        l.setLoanGiverAccount(normalizeLenderAccount(req.getLoanGiverAccount()));
        l.setLoanReceiverAccount(req.getLoanReceiverAccount());
        l.setLoanAmount(req.getAmount());
        l.setRemainingAmount(req.getAmount());
        l.setMemo(req.getMemo());
        l.setStatus(Loan.LoanStatus.REJECTED);
        l.setFailureReason(reason);

        log.warn("Loan request was rejected: {}", reason);
        return loanRepository.save(l);
    }

    @Transactional
    public Loan pay(Loan loan, Account payerAccount, double amount, String username) {
        if (loan.getStatus() != Loan.LoanStatus.APPROVED) {
            throw new RuntimeException("Only approved loans can be paid.");
        }

        if (!payerAccount.getAccountNumber().equals(loan.getLoanReceiverAccount())) {
            throw new RuntimeException("Only the borrower account can pay this loan.");
        }

        if (payerAccount.getUser() == null || !payerAccount.getUser().getUsername().equals(username)) {
            throw new RuntimeException("Payer account does not belong to authenticated user.");
        }

        applyOverduePenaltyIfNeeded(loan);

        if (amount <= 0) {
            throw new RuntimeException("Payment amount must be greater than 0.");
        }

        double remaining = loan.getRemainingAmount() == null ? loan.getLoanAmount() : loan.getRemainingAmount();
        if (remaining <= 0) {
            loan.setRemainingAmount(0.0);
            loan.setStatus(Loan.LoanStatus.PAID_OFF);
            loan.setPaidOffAt(Instant.now());
            return loanRepository.save(loan);
        }

        double paymentAmount = Math.min(amount, remaining);

        if (payerAccount.getBalance() < paymentAmount) {
            throw new RuntimeException("Insufficient funds in account to make this payment.");
        }

        Account lenderAccount = accountRepository.findByAccountNumber(loan.getLoanGiverAccount())
                .orElseThrow(() -> new RuntimeException("Lender account not found."));

        payerAccount.setBalance(payerAccount.getBalance() - paymentAmount);
        lenderAccount.setBalance(lenderAccount.getBalance() + paymentAmount);

        accountRepository.save(payerAccount);
        accountRepository.save(lenderAccount);

        loan.setRemainingAmount(remaining - paymentAmount);

        if (loan.getRemainingAmount() <= 0.000001) {
            loan.setRemainingAmount(0.0);
            loan.setStatus(Loan.LoanStatus.PAID_OFF);
            loan.setPaidOffAt(Instant.now());
        }

        Loan savedLoan = loanRepository.save(loan);

        createTransferRecord(
                payerAccount.getAccountNumber(),
                lenderAccount.getAccountNumber(),
                paymentAmount,
                "Loan repayment #" + savedLoan.getLoanId(),
                savedLoan.getLoanId()
        );

        log.info("Loan ID {} received payment of {} from account {}", savedLoan.getLoanId(), paymentAmount, payerAccount.getAccountNumber());
        return savedLoan;
    }

    public List<Loan> getAllLoans() {
        List<Loan> loans = loanRepository.findAll();
        loans.forEach(this::applyOverduePenaltyIfNeeded);
        return loans;
    }

    public Optional<Loan> getLoanById(Long id) {
        Optional<Loan> loan = loanRepository.findById(id);
        loan.ifPresent(this::applyOverduePenaltyIfNeeded);
        return loan;
    }

    public List<Loan> getLoansVisibleToUser(String username) {
        Account account = getAuthenticatedUserPrimaryAccount(username);
        List<Loan> loans = loanRepository.findByLoanReceiverAccountOrLoanGiverAccount(
                account.getAccountNumber(),
                account.getAccountNumber()
        );
        loans.forEach(this::applyOverduePenaltyIfNeeded);
        return loans;
    }

    public List<Loan> getPendingApprovalLoans(String username) {
        Account account = getAuthenticatedUserPrimaryAccount(username);
        List<Loan> loans = loanRepository.findByLoanGiverAccountAndStatus(
                account.getAccountNumber(),
                Loan.LoanStatus.PENDING
        );
        loans.forEach(this::applyOverduePenaltyIfNeeded);
        return loans;
    }

    public void deleteLoan(Long id) {
        if (loanRepository.existsById(id)) {
            loanRepository.deleteById(id);
            log.info("Loan with ID {} has been deleted.", id);
        } else {
            log.warn("Loan with ID {} does not exist.", id);
        }
    }

    public List<Loan> getLoansByAuthenticatedUser(String authenticatedUser) {
        List<Loan> loans = loanRepository.findAllByAuthenticatedUser(authenticatedUser);
        loans.forEach(this::applyOverduePenaltyIfNeeded);
        return loans;
    }
}