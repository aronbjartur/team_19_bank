package is.hi.hbv501g.team_19_bank.model;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class TransferRequest {
    @NotBlank
    private String sourceAccount;

    @NotBlank
    private String destinationAccount;

    @DecimalMin(value= "0.01", message = "Amount must be > 0")
    private double amount;

    @Size(max = 140)
    private String memo;
}
