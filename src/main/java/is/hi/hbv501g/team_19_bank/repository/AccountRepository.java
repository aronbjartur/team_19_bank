package is.hi.hbv501g.team_19_bank.repository;

import is.hi.hbv501g.team_19_bank.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByAccountNumber(String accountNumber);

}
