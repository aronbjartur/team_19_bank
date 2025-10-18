package is.hi.hbv501g.team_19_bank.model;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LoanPaymentRequest {
    @NotBlank(message = "Payer account number is required.")
    private String payerAccount;

    @DecimalMin(value = "0.01", message = "Amount must be greater than 0.")
    private double amount;

    @NotBlank(message = "Lender account number is required.")
    private String lenderAccount;
    @NotBlank(message = "Loan ID is required.")
    private Long loanId;
    @Size(max = 140)
    private String memo;
}
