package is.hi.hbv501g.team_19_bank.repository;

import is.hi.hbv501g.team_19_bank.model.Loan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LoanRepository extends JpaRepository<Loan, Long> {

    Optional<Loan> findByAuthenticatedUser(String authenticatedUser);

    // Finder method required for UC11 logic (checking capacity)
    List<Loan> findByStatusIn(List<Loan.LoanStatus> statuses);
}
