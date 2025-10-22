package is.hi.hbv501g.team_19_bank.model;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LoanRequest {

    @NotBlank
    private String loanGiverAccount;

    @NotBlank
    private String loanReceiverAccount;

    @DecimalMin(value = "0.01", message = "Amount must be > 0")
    private double amount;

    @Size(max = 140)
    private String memo;
    
}
