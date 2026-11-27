package dieterbaier.letterwright.adapter.in.api;

import dieterbaier.letterwright.api.SaveLetter;
import dieterbaier.letterwright.api.SaveResult;
import dieterbaier.letterwright.domain.model.UserId;
import dieterbaier.letterwright.domain.model.snapshot.SnapshotId;
import java.util.UUID;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Saves via the use case {@code SaveLetter} in the host's transaction (ADR-023).
 *
 * <p>The adapter controls the transaction itself with {@code REQUIRED} instead of via {@code @Transactional}: that way
 * the boundary does not depend on the call going through a proxy, and it can be tested without a Spring context. A
 * result returns normally and therefore never marks the transaction for rollback; only an exception does. The inputs,
 * too, become domain types only inside the boundary: an invalid input marks the host's transaction like any other
 * exception, so that a host that catches it cannot commit halfway.
 */
public final class ApiSaveLetter implements SaveLetter {

    private final dieterbaier.letterwright.usecases.in.SaveLetter useCase;
    private final TransactionTemplate transaction;

    public ApiSaveLetter(dieterbaier.letterwright.usecases.in.SaveLetter useCase,
            PlatformTransactionManager transactions) {
        this.useCase = useCase;
        this.transaction = new TransactionTemplate(transactions);
        this.transaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
    }

    @Override
    public SaveResult save(String userId, UUID snapshot) {
        return transaction.execute(status -> {
            UserId user = new UserId(userId);
            return Mapping.toApi(useCase.save(user, new SnapshotId(snapshot)), user);
        });
    }
}
