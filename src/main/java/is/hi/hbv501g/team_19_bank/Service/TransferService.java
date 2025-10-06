package is.hi.hbv501g.team_19_bank.Service;

import is.hi.hbv501g.team_19_bank.model.Account;
import is.hi.hbv501g.team_19_bank.model.Transfer;
import is.hi.hbv501g.team_19_bank.model.TransferRequest;
import is.hi.hbv501g.team_19_bank.repository.AccountRepository;
import is.hi.hbv501g.team_19_bank.repository.TransferRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransferService {
    
    private final AccountRepository accounts;
    private final TransferRepository transfers;

    public TransferService(AccountRepository accounts, TransferRepository transfers){
        this.accounts = accounts;
        this.transfers = transfers;
    }

    /**
     * Reglur um transfer samkvæmt UC2
     * source != destination
     * Báðir reikningar verða að vera til
     * amount > 0
     * Nægilegur peningur í source reikning
     */
    @Transactional
    public Transfer transfer(TransferRequest req) {
        if (req.getSourceAccount() == null || req.getDestinationAccount() == null) {
            return fail(req, "Accounts are required");
        }
        if (req.getSourceAccount().equals(req.getDestinationAccount())) {
            return fail(req, "Source account cannot be same as destination account");
        }
        if (req.getAmount() <= 0) {
            return fail(req, "Amount cannot be 0");
        }

        Account from = accounts.findByAccountNumber(req.getSourceAccount()).orElse(null);
        Account to = accounts.findByAccountNumber(req.getDestinationAccount()).orElse(null);

        if(from == null || to == null) {
            return fail(req, "Invalid account(s)");
        }

        if(from.getBalance() < req.getAmount()){
            return fail(req, "Insufficient funds");
        }

        from.setBalance(from.getBalance() - req.getAmount());
        to.setBalance(to.getBalance() + req.getAmount());

        accounts.save(from);
        accounts.save(to);

        Transfer t = new Transfer();
        t.setSourceAccount(req.getSourceAccount());
        t.setDestinationAccount(req.getDestinationAccount());
        t.setAmount(req.getAmount());
        t.setMemo(req.getMemo());
        t.setStatus(Transfer.Status.COMPLETED);

        return transfers.save(t);
    }

    private Transfer fail(TransferRequest req, String reason){
        Transfer t = new Transfer();
        t.setSourceAccount(req.getSourceAccount());
        t.setDestinationAccount(req.getDestinationAccount());
        t.setAmount(req.getAmount());
        t.setMemo(req.getMemo());
        t.setStatus(Transfer.Status.FAILED);
        t.setFailureReason(reason);
        return transfers.save(t);
    }
}
