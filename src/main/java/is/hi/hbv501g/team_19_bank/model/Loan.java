package is.hi.hbv501g.team_19_bank.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;

@Entity
@Table(name = "loans")
@Data
public class Loan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long loanId;

    @Column(nullable = false)
    private Double loanAmount;
    // Gæti þurft að breyta þessu

    @Column(nullable = false)
    private String loanGiverAccount;

    @Column(nullable = false)
    private String loanReceiverAccount;
    // Gæti þurft að eyða þessu
    @Column(nullable = false)
    private String authenticatedUser;

    private String memo;


    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private LoanStatus status = LoanStatus.PENDING;

    private String failureReason = "";
    private Double interestRateAfterApproval = 1.05;


    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();


    public enum LoanStatus {
        APPROVED,
        REJECTED,
        PENDING,
        PAID_OFF
    }

    public void setUser(String authenticatedUser) {
        this.authenticatedUser = authenticatedUser;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

    public Double getInterestRateAfterApproval() {
        return interestRateAfterApproval;
    }

    public void setInterestRateAfterApproval(Double interestRateAfterApproval) {
        this.interestRateAfterApproval = interestRateAfterApproval;
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
