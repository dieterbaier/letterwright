package dieterbaier.letterwright.adapter.out.host;

import dieterbaier.letterwright.domain.model.UserId;
import dieterbaier.letterwright.domain.model.letter.DeletionDecision;
import dieterbaier.letterwright.domain.model.letter.Letter;
import dieterbaier.letterwright.spi.DeletionPolicy;
import dieterbaier.letterwright.usecases.out.DeletionRules;
import org.jspecify.annotations.Nullable;

/**
 * Fragt die {@link DeletionPolicy} des Hosts. Der Host liegt außerhalb der Vertrauensgrenze: Hält er den Null-Vertrag
 * des SPI nicht ein, meldet der Adapter das als {@link HostContractViolated}.
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
            throw new HostContractViolated("Die Loeschrichtlinie des Hosts hat null statt einer Entscheidung geliefert");
        }
        return Mapping.toDomain(answer);
    }
}
