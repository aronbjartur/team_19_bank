package is.hi.hbv501g.team_19_bank.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "loans")
@Data
public class Loan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long loanId;
    @Column(nullable = false)
    private Double loanAmount;
    @Column(nullable = false)
    private LocalDate startDate;
    @Column(nullable = false)
    private Double interestRate;
    @Column(nullable = false)
    private String loanGiverAccount;
    @Column(nullable = false)
    private String loanReceiverAccount;
    @Column(nullable = false)
    private String authenticatedUser;
    private String memo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LoanStatus status = LoanStatus.APPROVED;

    private String failureReason = "";

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public void setUser(BankUser authenticatedUser) {
        this.authenticatedUser = authenticatedUser.getUsername();
    }

    public enum LoanStatus {
        APPROVED,
        REJECTED,
        PENDING,
        PAID_OFF
    }

    public Long getLoanId() {
        return loanId;
    }

    public void setLoanId(Long loanId) {
        this.loanId = loanId;
    }

    public Double getLoanAmount() {
        return loanAmount;
    }

    public void setLoanAmount(Double loanAmount) {
        this.loanAmount = loanAmount;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public Double getInterestRate() {
        return interestRate;
    }

    public void setInterestRate(Double interestRate) {
        this.interestRate = interestRate;
    }


    public LoanStatus getStatus() {
        return status;
    }

    public void setStatus(LoanStatus status) {
        this.status = status;
    }

    public String getLoanGiverAccount() {
        return loanGiverAccount;
    }

    public void setLoanGiverAccount(String loanGiverAccount) {
        this.loanGiverAccount = loanGiverAccount;
    }

    public String getLoanReceiverAccount() {
        return loanReceiverAccount;
    }

    public void setLoanReceiverAccount(String loanReceiverAccount) {
        this.loanReceiverAccount = loanReceiverAccount;
    }
}
