package dieterbaier.letterwright.domain.model.snapshot;

import static dieterbaier.letterwright.domain.model.Checks.rejects;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class SnapshotIdTest {

    @Test
    void theSnapshotIdRequiresAValueAndIsNewlyAssigned() {
        rejects(() -> new SnapshotId(null), "SnapshotId");
        assertThat(SnapshotId.generate()).isNotEqualTo(SnapshotId.generate());
    }
}
