package is.hi.hbv501g.team_19_bank.repository;

import is.hi.hbv501g.team_19_bank.model.Loan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LoanRepository extends JpaRepository<Loan, Long> {
    Optional<Loan> findByUserId(Long userId);

    Optional<Loan> findByLoanStatus(Loan.LoanStatus status);
}
