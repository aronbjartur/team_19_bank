package is.hi.hbv501g.team_19_bank.Service;

import is.hi.hbv501g.team_19_bank.model.Account;
import is.hi.hbv501g.team_19_bank.model.BankUser;
import is.hi.hbv501g.team_19_bank.repository.AccountRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;


    // Býr til nýjan reikning fyrir notanda
    public Account createDefaultAccountForUser(BankUser user) {
        Account newAccount = new Account();

        // byrjar a 0 kr
        newAccount.setBalance(0.0);
        newAccount.setUser(user); // Tengir reikninginn við notandann

        // Býr til sérstakt reikningsnúmer
        String accountNumber = generateUniqueAccountNumber();
        newAccount.setAccountNumber(accountNumber);

        return accountRepository.save(newAccount); // Vistar og gefur reikning
    }


    // Býr til einfalt, sérstakt reikningsnúmer
    private String generateUniqueAccountNumber() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();
    }

    public Optional<Account> getAccountByAccountNumber(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber);
    }
}
