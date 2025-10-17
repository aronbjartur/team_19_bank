package is.hi.hbv501g.team_19_bank.Service;

import is.hi.hbv501g.team_19_bank.model.Account;
import is.hi.hbv501g.team_19_bank.model.Transfer;
import is.hi.hbv501g.team_19_bank.model.TransferRequest;
import is.hi.hbv501g.team_19_bank.repository.AccountRepository;
import is.hi.hbv501g.team_19_bank.repository.TransferRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Slf4j
public class TransferService {

    private final AccountRepository accounts;
    private final TransferRepository transfers;

    public TransferService(AccountRepository accounts, TransferRepository transfers) {
        this.accounts = accounts;
        this.transfers = transfers;
    }

    public Optional<Transfer> getTransferById(Long id) {
        return transfers.findById(id);
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
        log.info("Attempting transfer: {} from {} to {}", req.getAmount(), req.getSourceAccount(), req.getDestinationAccount());

        if (req.getSourceAccount() == null || req.getDestinationAccount() == null) {
            return fail(req, "Source and destination accounts are required.");
        }
        if (req.getSourceAccount().equals(req.getDestinationAccount())) {
            return fail(req, "Source account cannot be same as destination account.");
        }
        if (req.getAmount() <= 0) {
            return fail(req, "Amount must be greater than 0 kr.");
        }

        Account from = accounts.findByAccountNumber(req.getSourceAccount()).orElse(null);
        Account to = accounts.findByAccountNumber(req.getDestinationAccount()).orElse(null);

        if (from == null || to == null) {
            return fail(req, "One or both account numbers are invalid or non-existent.");
        }

        if (from.getBalance() < req.getAmount()) {
            return fail(req, "Insufficient funds in the source account.");
        }

        if (from.getBalance() < req.getAmount()) {
            return fail(req, "Insufficient funds in the source account.");
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

        log.info("Transfer completed successfully. Status set to COMPLETED.");
        return transfers.save(t);
    }

    private Transfer fail(TransferRequest req, String reason) {
        Transfer t = new Transfer();
        t.setSourceAccount(req.getSourceAccount());
        t.setDestinationAccount(req.getDestinationAccount());
        t.setAmount(req.getAmount());
        t.setMemo(req.getMemo());
        t.setStatus(Transfer.Status.FAILED);
        t.setFailureReason(reason);

        log.warn("Transfer failed: {}", reason);
        return transfers.save(t);
    }
}
