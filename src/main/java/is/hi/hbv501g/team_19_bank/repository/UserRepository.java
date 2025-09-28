package is.hi.hbv501g.team_19_bank.repository;

import is.hi.hbv501g.team_19_bank.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);
}
