package dieterbaier.letterwright.usecases.service;

import static org.assertj.core.api.Assertions.assertThat;

import dieterbaier.letterwright.domain.model.UserId;
import dieterbaier.letterwright.domain.model.BusinessReference;
import dieterbaier.letterwright.domain.model.LetterAction;
import dieterbaier.letterwright.domain.model.Page;
import dieterbaier.letterwright.domain.model.PageRequest;
import dieterbaier.letterwright.domain.model.letter.Letter;
import dieterbaier.letterwright.usecases.in.ListResult;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.Test;

/**
 * Brücke zu {@code features/letter/letter-auflisten.feature}: ein Test je Szenario, benannt nach dem
 * Szenariotitel. Die ausgehenden Ports sind {@link Fakes}.
 */
class ListLettersServiceTest {

    private static final Instant MONDAY = Instant.parse("2026-11-16T09:00:00Z");
    private static final UserId ERIKA = new UserId("erika");
    private static final BusinessReference CUSTOMER_4711 = new BusinessReference("Kunde", "4711");
    private static final BusinessReference CUSTOMER_0815 = new BusinessReference("Kunde", "0815");
    private static final PageRequest FIRST_PAGE_OF_2 = new PageRequest(0, 2);

    private final Fakes.Host host = new Fakes.Host();
    private final Fakes.Documents documents = new Fakes.Documents();
    private final Fakes.LetterStore letter = new Fakes.LetterStore(new Fakes.Snapshots());
    private final SavedLetters saved = new SavedLetters(letter, documents);
    private final ListLettersService list = new ListLettersService(host, letter);

    // Grundlage: drei Schreiben zum Kunden 4711, an drei Tagen gespeichert; Erika darf sie ansehen.
    private final Letter oldest = saved.save(CUSTOMER_4711, MONDAY);
    private final Letter middle = saved.save(CUSTOMER_4711, MONDAY.plus(Duration.ofDays(1)));
    private final Letter newest = saved.save(CUSTOMER_4711, MONDAY.plus(Duration.ofDays(2)));

    {
        host.allow(ERIKA, LetterAction.VIEW, CUSTOMER_4711);
    }

    @Test
    void theListShowsTheLettersForTheReferencePageByPageNewestFirst() {
        var result = list.list(ERIKA, CUSTOMER_4711, FIRST_PAGE_OF_2);

        Page<Letter> page = ((ListResult.Found) result).page();
        assertThat(page.entries()).containsExactly(newest, middle);
        assertThat(page.total()).isEqualTo(3);
        assertThat(page.pageCount()).isEqualTo(2);
        assertThat(list.list(ERIKA, CUSTOMER_4711, new PageRequest(1, 2)))
                .as("die zweite Seite hat das älteste")
                .isEqualTo(new ListResult.Found(new Page<>(java.util.List.of(oldest), 3,
                        new PageRequest(1, 2))));
    }

    @Test
    void invisibleLettersCountNeitherInThePageNorInTheTotal() {
        // Given: zwei neuere Schreiben zu einem anderen Kunden; das neueste zum Kunden 4711 ist logisch gelöscht
        saved.save(CUSTOMER_0815, MONDAY.plus(Duration.ofDays(3)));
        saved.save(CUSTOMER_0815, MONDAY.plus(Duration.ofDays(4)));
        saved.deleteLogically(newest, MONDAY.plus(Duration.ofDays(5)));

        var result = list.list(ERIKA, CUSTOMER_4711, FIRST_PAGE_OF_2);

        // Then: nur die übrigen beiden; Gesamtzahl und Seitenzahl zählen nur sie
        Page<Letter> page = ((ListResult.Found) result).page();
        assertThat(page.entries()).containsExactly(middle, oldest);
        assertThat(page.entries()).allMatch(entry -> entry.reference().equals(CUSTOMER_4711))
                .allMatch(Letter::isVisible);
        assertThat(page.total()).isEqualTo(2);
        assertThat(page.pageCount()).isEqualTo(1);
    }

    @Test
    void aPageBeyondTheEndIsEmptyAndCountsTheVisibleLetters() {
        var result = list.list(ERIKA, CUSTOMER_4711, new PageRequest(2, 2));

        Page<Letter> page = ((ListResult.Found) result).page();
        assertThat(page.entries()).isEmpty();
        assertThat(page.total()).isEqualTo(3);
        assertThat(page.pageCount()).isEqualTo(2);
    }

    @Test
    void withoutPermissionToViewNothingIsRead() {
        host.deny(ERIKA, LetterAction.VIEW, CUSTOMER_4711);

        var result = list.list(ERIKA, CUSTOMER_4711, FIRST_PAGE_OF_2);

        assertThat(result).isEqualTo(new ListResult.NotAllowed());
        assertThat(letter.query).as("nichts gelesen oder gezählt").isZero();
    }
}
