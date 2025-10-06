package is.hi.hbv501g.team_19_bank.model;
import jakarta.persistence.*;
import lombok.Data;
import java.time.Instant;

@Entity
@Table(name = "transfers")
@Data
public class Transfer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String sourceAccount;

    @Column(nullable = false)
    private String destinationAccount;

    @Column(nullable = false)
    private double amount;

    private String memo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.COMPLETED;

    private String failureReason = "";

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public enum Status { COMPLETED, FAILED }
}
