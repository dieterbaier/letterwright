package dieterbaier.letterwright.adapter.out.host;

import dieterbaier.letterwright.domain.model.UserId;
import dieterbaier.letterwright.domain.model.letter.DeletionDecision;
import dieterbaier.letterwright.domain.model.letter.Letter;
import dieterbaier.letterwright.spi.DeletionPolicy;
import dieterbaier.letterwright.usecases.out.DeletionRules;
import org.jspecify.annotations.Nullable;

/**
 * Asks the host's {@link DeletionPolicy}. The host lies outside the trust boundary: if it does not keep the SPI's null
 * contract, the adapter reports this as {@link HostContractViolated}.
 */
public final class HostDeletionRules implements DeletionRules {

    private final DeletionPolicy policy;

    public HostDeletionRules(DeletionPolicy policy) {
        this.policy = policy;
    }

    @Override
    public DeletionDecision decide(UserId user, Letter letter) {
        dieterbaier.letterwright.spi.@Nullable DeletionDecision answer =
                policy.decide(Mapping.deletionRequest(user, letter));
        if (answer == null) {
            throw new HostContractViolated("The DeletionPolicy of the host returned null instead of a decision");
        }
        return Mapping.toDomain(answer);
    }
}
