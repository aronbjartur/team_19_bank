package is.hi.hbv501g.team_19_bank.Controller;

import is.hi.hbv501g.team_19_bank.Service.TransferService;
import is.hi.hbv501g.team_19_bank.model.Transfer;
import is.hi.hbv501g.team_19_bank.model.TransferRequest;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/transfer")
public class TransferController {

    private final TransferService service;

    public TransferController(TransferService service) {
        this.service = service;
    }

    @GetMapping
    public String form(Model model) {
        model.addAttribute("transferRequest", new TransferRequest());
        return "transfer"; 
    }

    @PostMapping
    public String submit(@ModelAttribute("transferRequest") @Valid TransferRequest req,
                         BindingResult binding,
                         Model model) {

        if (binding.hasErrors()) {
            return "transfer";
        }

        Transfer t = service.transfer(req);
        model.addAttribute("transfer", t);
        return "transfer_result";
    }
}
