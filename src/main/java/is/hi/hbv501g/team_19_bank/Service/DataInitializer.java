package is.hi.hbv501g.team_19_bank.Service;
// þannig að 100200300 hefur milljón alltaf, alltaf hægt að taka pening þaðan

import is.hi.hbv501g.team_19_bank.model.Account;
import is.hi.hbv501g.team_19_bank.repository.AccountRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class DataInitializer implements CommandLineRunner {

    private final AccountRepository accountRepository;

    public DataInitializer(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        final String TEST_ACCOUNT_NUMBER = "100200300";
        final double STARTING_BALANCE = 1000000.00;

        Optional<Account> existingAccount = accountRepository.findByAccountNumber(TEST_ACCOUNT_NUMBER);

        if (existingAccount.isEmpty()) {
            Account anchorAccount = new Account();
            anchorAccount.setAccountNumber(TEST_ACCOUNT_NUMBER);
            anchorAccount.setBalance(STARTING_BALANCE);
            // NOTE: This test account does not belong to a BankUser

            accountRepository.save(anchorAccount);
            System.out.println("TEST DATA: Anchor Account " + TEST_ACCOUNT_NUMBER + " created with kr " + STARTING_BALANCE);
        } else {
            System.out.println("TEST DATA: Anchor Account " + TEST_ACCOUNT_NUMBER + " already exists.");
        }
    }
}
