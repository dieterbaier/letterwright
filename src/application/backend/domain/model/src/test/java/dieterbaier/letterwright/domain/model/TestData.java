package dieterbaier.letterwright.domain.model;

import dieterbaier.letterwright.domain.model.snapshot.Snapshot;
import dieterbaier.letterwright.domain.model.snapshot.SnapshotId;
import dieterbaier.letterwright.domain.model.template.VersionNumber;
import dieterbaier.letterwright.domain.model.template.VersionReference;
import dieterbaier.letterwright.domain.model.template.TemplateId;
import java.time.Instant;
import java.util.Map;

/** Gemeinsame Testdaten der Modelltests. Keine Testklasse greift auf eine andere zu, nur auf diese hier. */
public final class TestData {

    public static final Instant NOW = Instant.parse("2026-11-21T10:00:00Z");
    public static final UserId CLERK = new UserId("erika");
    public static final BusinessReference CUSTOMER = new BusinessReference("Kunde", "4711");
    public static final VersionReference LETTERHEAD_V1 = new VersionReference(TemplateId.generate(), VersionNumber.FIRST);
    public static final VersionReference LETTER_TEMPLATE_V3 = new VersionReference(TemplateId.generate(), new VersionNumber(3));
    public static final DataContext DATA_CONTEXT = DataContext.from(Map.of("name", "Erika"));

    private TestData() {
    }

    public static Snapshot snapshot() {
        return Snapshot.create(SnapshotId.generate(), CLERK, CUSTOMER, LETTERHEAD_V1, LETTER_TEMPLATE_V3,
                DATA_CONTEXT, NOW);
    }
}
