package dieterbaier.letterwright.adapter.out.host;

import dieterbaier.letterwright.domain.model.UserId;
import dieterbaier.letterwright.domain.model.letter.DocumentReference;
import dieterbaier.letterwright.domain.model.BusinessReference;
import dieterbaier.letterwright.domain.model.Checksum;
import dieterbaier.letterwright.domain.model.letter.LetterId;
import dieterbaier.letterwright.domain.model.letter.LetterState;
import dieterbaier.letterwright.domain.model.letter.Letter;
import dieterbaier.letterwright.domain.model.template.LetterTemplate;
import dieterbaier.letterwright.domain.model.template.VersionNumber;
import dieterbaier.letterwright.domain.model.template.VersionReference;
import dieterbaier.letterwright.domain.model.template.TemplateId;
import dieterbaier.letterwright.domain.model.snapshot.SnapshotId;
import dieterbaier.letterwright.spi.LetterTemplateId;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

/** Shared test data of the adapter tests. */
final class TestData {

    static final UserId ERIKA = new UserId("erika");
    static final BusinessReference CUSTOMER = new BusinessReference("Customer", "4711");
    static final Instant CREATED = Instant.parse("2026-11-21T10:00:00Z");
    static final LetterTemplate REMINDER =
            LetterTemplate.create(TemplateId.generate(), "Payment reminder", TemplateId.generate(), "Customer", "Dear {{name}}");
    static final Letter LETTER = Letter.restore(LetterId.generate(), SnapshotId.generate(), CUSTOMER,
            new VersionReference(TemplateId.generate(), VersionNumber.FIRST),
            new VersionReference(REMINDER.id(), VersionNumber.FIRST), new DocumentReference("store/1.pdf"),
            Checksum.of("pdf".getBytes(StandardCharsets.UTF_8)), CREATED, new LetterState.Visible());

    /**
     * This is how user, reference and payment reminder look in the SPI. Built by hand and not via {@link Mapping}, so
     * that an error in the mapping becomes visible in the adapter tests.
     */
    static final String ERIKA_IN_SPI = "erika";
    static final dieterbaier.letterwright.spi.BusinessReference CUSTOMER_IN_SPI =
            new dieterbaier.letterwright.spi.BusinessReference("Customer", "4711");
    static final LetterTemplateId REMINDER_IN_SPI =
            new LetterTemplateId(REMINDER.id().value(), "Payment reminder");

    private TestData() {
    }

    /** A host that does not keep the null contract. */
    @SuppressWarnings({"NullAway", "unchecked"})
    static <T> T nullFromHost() {
        return (T) null;
    }
}
