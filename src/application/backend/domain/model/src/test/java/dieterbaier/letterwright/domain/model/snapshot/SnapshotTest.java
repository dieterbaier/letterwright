package dieterbaier.letterwright.domain.model.snapshot;

import static dieterbaier.letterwright.domain.model.Checks.rejects;
import static dieterbaier.letterwright.domain.model.TestData.LETTERHEAD_V1;
import static dieterbaier.letterwright.domain.model.TestData.DATA_CONTEXT;
import static dieterbaier.letterwright.domain.model.TestData.NOW;
import static dieterbaier.letterwright.domain.model.TestData.CUSTOMER;
import static dieterbaier.letterwright.domain.model.TestData.CLERK;
import static dieterbaier.letterwright.domain.model.TestData.LETTER_TEMPLATE_V3;
import static org.assertj.core.api.Assertions.assertThat;

import dieterbaier.letterwright.domain.model.UserId;
import dieterbaier.letterwright.domain.model.DataContext;
import dieterbaier.letterwright.domain.model.BusinessReference;
import dieterbaier.letterwright.domain.model.TestData;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class SnapshotTest {

    private final Snapshot snapshot = TestData.snapshot();

    @Test
    void expiresThirtyMinutesAfterCreation() {
        assertThat(snapshot.expiry()).isEqualTo(NOW.plus(Duration.ofMinutes(30)));
        assertThat(snapshot.isExpired(snapshot.expiry().minusMillis(1))).isFalse();
        assertThat(snapshot.isExpired(snapshot.expiry())).isTrue();
    }

    @Test
    void bindsUserReferenceAndBothTemplateVersions() {
        assertThat(snapshot.user()).isEqualTo(CLERK);
        assertThat(snapshot.reference()).isEqualTo(CUSTOMER);
        assertThat(snapshot.letterhead()).isEqualTo(LETTERHEAD_V1);
        assertThat(snapshot.letterTemplate()).isEqualTo(LETTER_TEMPLATE_V3);
        assertThat(snapshot.state()).isEqualTo(new SnapshotState.Open(DATA_CONTEXT));
    }

    @Test
    void isUsableForTheSameUserAndReference() {
        assertThat(snapshot.isUsableFor(CLERK, CUSTOMER, NOW)).isTrue();
    }

    @Test
    void anotherUserCannotUseIt() {
        assertThat(snapshot.isUsableFor(new UserId("max"), CUSTOMER, NOW)).isFalse();
    }

    @Test
    void itCannotBeUsedForAnotherReference() {
        assertThat(snapshot.isUsableFor(CLERK, new BusinessReference("Kunde", "4712"), NOW))
                .isFalse();
    }

    @Test
    void afterExpiryItCannotBeUsed() {
        assertThat(snapshot.isUsableFor(CLERK, CUSTOMER, snapshot.expiry())).isFalse();
    }

    @Test
    void aConsumedSnapshotNoLongerHoldsADataContext() {
        Snapshot consumed = snapshot.consumed();

        assertThat(consumed.state()).isEqualTo(new SnapshotState.Consumed());
        assertThat(consumed).isEqualTo(snapshot);
        assertThat(consumed.user()).isEqualTo(CLERK);
        assertThat(consumed.reference()).isEqualTo(CUSTOMER);
        assertThat(consumed.letterTemplate()).isEqualTo(LETTER_TEMPLATE_V3);
        assertThat(consumed.expiry()).isEqualTo(snapshot.expiry());
    }

    @Test
    void aSnapshotIsConsumedOnlyOnce() {
        Snapshot consumed = snapshot.consumed();

        org.assertj.core.api.Assertions.assertThatIllegalStateException().isThrownBy(consumed::consumed)
                .withMessageContaining("schon ein Schreiben");
    }

    @Test
    void requiredAttributesOnCreation() {
        SnapshotId id = SnapshotId.generate();

        rejects(() -> Snapshot.create(null, CLERK, CUSTOMER, LETTERHEAD_V1, LETTER_TEMPLATE_V3,
                DATA_CONTEXT, NOW), "ID des Snapshots");
        rejects(() -> Snapshot.create(id, null, CUSTOMER, LETTERHEAD_V1, LETTER_TEMPLATE_V3, DATA_CONTEXT, NOW),
                "Benutzer des Snapshots");
        rejects(() -> Snapshot.create(id, CLERK, null, LETTERHEAD_V1, LETTER_TEMPLATE_V3, DATA_CONTEXT,
                NOW), "fachliche Bezug des Snapshots");
        rejects(() -> Snapshot.create(id, CLERK, CUSTOMER, null, LETTER_TEMPLATE_V3, DATA_CONTEXT, NOW),
                "Letterhead-Version");
        rejects(() -> Snapshot.create(id, CLERK, CUSTOMER, LETTERHEAD_V1, null, DATA_CONTEXT, NOW),
                "LetterTemplate-Version");
        rejects(() -> Snapshot.create(id, CLERK, CUSTOMER, LETTERHEAD_V1, LETTER_TEMPLATE_V3, null, NOW),
                "Datenkontext des Snapshots");
        rejects(() -> Snapshot.create(id, CLERK, CUSTOMER, LETTERHEAD_V1, LETTER_TEMPLATE_V3, DATA_CONTEXT,
                null), "Zeitpunkt der Anlage");
        rejects(() -> Snapshot.restore(id, CLERK, CUSTOMER, LETTERHEAD_V1, LETTER_TEMPLATE_V3,
                new SnapshotState.Open(DATA_CONTEXT), null), "Ablaufzeitpunkt");
        rejects(() -> Snapshot.restore(id, CLERK, CUSTOMER, LETTERHEAD_V1, LETTER_TEMPLATE_V3, null,
                NOW), "Zustand des Snapshots");
    }

    @Test
    void restoreKeepsTheExpiry() {
        Instant expiry = NOW.plusSeconds(5);

        Snapshot loaded = Snapshot.restore(snapshot.id(), CLERK, CUSTOMER, LETTERHEAD_V1,
                LETTER_TEMPLATE_V3, new SnapshotState.Consumed(), expiry);

        assertThat(loaded.expiry()).isEqualTo(expiry);
        assertThat(loaded).as("gleich über die ID").isEqualTo(snapshot).hasSameHashCodeAs(snapshot);
    }

    @Test
    void equalityByIdentityAndToStringWithoutData() {
        assertThat(TestData.snapshot()).isNotEqualTo(snapshot);
        assertThat(snapshot).isNotEqualTo(snapshot.id());
        assertThat(snapshot.toString()).contains(snapshot.id().value().toString()).doesNotContain("Erika");
    }
}
