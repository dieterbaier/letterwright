package dieterbaier.letterwright.domain.model.snapshot;

import static dieterbaier.letterwright.domain.model.Checks.rejects;
import static org.assertj.core.api.Assertions.assertThat;

import dieterbaier.letterwright.domain.model.DataContext;
import dieterbaier.letterwright.domain.model.TestData;
import java.util.Map;
import org.junit.jupiter.api.Test;

class SnapshotStateTest {

    private final Snapshot snapshot = TestData.snapshot();

    @Test
    void detectsAChangedDataContext() {
        var open = (SnapshotState.Open) snapshot.state();

        assertThat(open.isUnchanged(DataContext.from(Map.of("name", "Erika")))).isTrue();
        assertThat(open.isUnchanged(DataContext.from(Map.of("name", "Max")))).isFalse();
    }

    @Test
    void anOpenSnapshotHoldsADataContext() {
        rejects(() -> new SnapshotState.Open(null), "Datenkontext des Snapshots");
    }
}
