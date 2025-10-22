package is.hi.hbv501g.team_19_bank.Controller;

import is.hi.hbv501g.team_19_bank.Service.TransferService;
import is.hi.hbv501g.team_19_bank.Service.UserService;
import is.hi.hbv501g.team_19_bank.model.Transfer;
import is.hi.hbv501g.team_19_bank.model.TransferRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/transfer")
public class TransferController {

    private final TransferService transferService;
    private final UserService userService;

    @Autowired
    public TransferController(TransferService transferService, UserService userService) {
        this.transferService = transferService;
        this.userService = userService;
    }

    /**
     * @GetMapping public String form(Model model) {
     * Authentication auth = SecurityContextHolder.getContext().getAuthentication();
     * String username = auth.getName();
     * <p>
     * try {
     * // Fá ID og reikningsnúmer notanda
     * Long userId = userService.getUserByUsernameWithAccounts(username)
     * .orElseThrow(() -> new RuntimeException("User not found")).getId();
     * <p>
     * String userAccountNumber = userService.getUserAccount(userId).getAccountNumber();
     * <p>
     * // Setja reikningsnúmerið í model-ið
     * model.addAttribute("userAccountNumber", userAccountNumber);
     * <p>
     * } catch (Exception e) {
     * // Ef villa, þá áframsenda á login síðuna (ætti ekki að gerast ef innskráning tókst)
     * return "redirect:/login";
     * }
     * <p>
     * model.addAttribute("transferRequest", new TransferRequest());
     * return "transfer";
     * }
     * @PostMapping public String submit(@ModelAttribute("transferRequest") @Valid TransferRequest req,
     * BindingResult binding,
     * Model model) {
     * <p>
     * Authentication auth = SecurityContextHolder.getContext().getAuthentication();
     * String username = auth.getName();
     * <p>
     * // Fá reikningsnúmerið aftur fyrir villumeðhöndlun
     * Long userId = userService.getUserByUsernameWithAccounts(username)
     * .orElseThrow(() -> new RuntimeException("User not found")).getId();
     * String userAccountNumber = userService.getUserAccount(userId).getAccountNumber();
     * <p>
     * model.addAttribute("userAccountNumber", userAccountNumber);
     * <p>
     * if (binding.hasErrors()) {
     * return "transfer";
     * }
     * <p>
     * Transfer submittedTransfer = transferService.transfer(req);
     * <p>
     * Transfer confirmedTransfer = transferService.getTransferById(submittedTransfer.getId())
     * .orElse(submittedTransfer); // Fallback ef það finnst ekki.
     * <p>
     * model.addAttribute("transfer", confirmedTransfer);
     * return "transfer_result";
     * }
     */
    @PostMapping
    public ResponseEntity<?> createTransfer(@RequestBody @Valid TransferRequest req) {
        try {
            // Log the incoming request
            System.out.println("Transfer request received: " + req);

            // Get the authenticated user's username
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            if (username.equals("anonymousUser")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                        "error", "User is not authenticated."
                ));
            }
            System.out.println("Authenticated user: " + username);

            // Validate user and retrieve their account number
            Long userId = userService.getUserByUsernameWithAccounts(username)
                    .orElseThrow(() -> new RuntimeException("User not found")).getId();
            String userAccountNumber = userService.getUserAccount(userId).getAccountNumber();

            // Ensure the source account matches the user's account
            // Breyti aftur í source account til að passa við UC
            if (!req.getSourceAccount().equals(userAccountNumber)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                        "error", "Source account does not belong to the authenticated user."
                ));
            }

            // Perform the transfer
            Transfer submittedTransfer = transferService.transfer(req);

            // Retrieve the confirmed transfer
            Transfer confirmedTransfer = transferService.getTransferById(submittedTransfer.getId())
                    .orElse(submittedTransfer);

            // Return the transfer details
            return ResponseEntity.ok(Map.of(
                    "message", "Transfer successful",
                    "transfer", confirmedTransfer
            ));

        } catch (Exception e) {
            // Handle errors and return a bad request response
            System.out.println("Error during transfer: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "error", e.getMessage()
            ));
        }
    }
}
