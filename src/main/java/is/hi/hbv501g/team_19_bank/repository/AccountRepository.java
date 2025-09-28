package is.hi.hbv501g.team_19_bank.repository;

import is.hi.hbv501g.team_19_bank.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, Long> {
    Account findByAccountNumber(String accountNumber);
}
