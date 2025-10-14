package is.hi.hbv501g.team_19_bank.Controller;

import is.hi.hbv501g.team_19_bank.Service.TransferService;
import is.hi.hbv501g.team_19_bank.Service.UserService;
import is.hi.hbv501g.team_19_bank.model.Transfer;
import is.hi.hbv501g.team_19_bank.model.TransferRequest;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

@Controller
@RequestMapping("/transfer")
public class TransferController {

    private final TransferService transferService;
    private final UserService userService;

    @Autowired
    public TransferController(TransferService transferService, UserService userService) {
        this.transferService = transferService;
        this.userService = userService;
    }

    @GetMapping
    public String form(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        try {
            // Fá ID og reikningsnúmer notanda
            Long userId = userService.getUserByUsernameWithAccounts(username)
                    .orElseThrow(() -> new RuntimeException("User not found")).getId();

            String userAccountNumber = userService.getUserAccount(userId).getAccountNumber();

            // Setja reikningsnúmerið í model-ið
            model.addAttribute("userAccountNumber", userAccountNumber);

        } catch (Exception e) {
            // Ef villa, þá áframsenda á login síðuna (ætti ekki að gerast ef innskráning tókst)
            return "redirect:/login";
        }

        model.addAttribute("transferRequest", new TransferRequest());
        return "transfer";
    }

    @PostMapping
    public String submit(@ModelAttribute("transferRequest") @Valid TransferRequest req,
                         BindingResult binding,
                         Model model) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        // Fá reikningsnúmerið aftur fyrir villumeðhöndlun
        Long userId = userService.getUserByUsernameWithAccounts(username)
                .orElseThrow(() -> new RuntimeException("User not found")).getId();
        String userAccountNumber = userService.getUserAccount(userId).getAccountNumber();

        model.addAttribute("userAccountNumber", userAccountNumber);

        if (binding.hasErrors()) {
            return "transfer";
        }

        Transfer submittedTransfer = transferService.transfer(req);

        Transfer confirmedTransfer = transferService.getTransferById(submittedTransfer.getId())
                .orElse(submittedTransfer); // Fallback ef það finnst ekki.

        model.addAttribute("transfer", confirmedTransfer);
        return "transfer_result";
    }
}
