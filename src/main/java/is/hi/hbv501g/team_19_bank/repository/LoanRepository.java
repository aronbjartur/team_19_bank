package is.hi.hbv501g.team_19_bank.repository;

import is.hi.hbv501g.team_19_bank.model.Loan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoanRepository extends JpaRepository<Loan, Long> {

    List<Loan> findAllByAuthenticatedUser(String authenticatedUser);

    List<Loan> findByStatusIn(List<Loan.LoanStatus> statuses);

    List<Loan> findByLoanGiverAccountAndStatus(String loanGiverAccount, Loan.LoanStatus status);

    List<Loan> findByLoanReceiverAccountOrLoanGiverAccount(String loanReceiverAccount, String loanGiverAccount);
}