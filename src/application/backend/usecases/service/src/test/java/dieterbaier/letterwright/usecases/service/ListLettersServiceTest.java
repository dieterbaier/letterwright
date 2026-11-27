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
 * Bridge to {@code features/letters/list-letters.feature}: one test per scenario, named after the
 * scenario title. The outbound ports are {@link Fakes}.
 */
class ListLettersServiceTest {

    private static final Instant MONDAY = Instant.parse("2026-11-16T09:00:00Z");
    private static final UserId ERIKA = new UserId("erika");
    private static final BusinessReference CUSTOMER_4711 = new BusinessReference("Customer", "4711");
    private static final BusinessReference CUSTOMER_0815 = new BusinessReference("Customer", "0815");
    private static final PageRequest FIRST_PAGE_OF_2 = new PageRequest(0, 2);

    private final Fakes.Host host = new Fakes.Host();
    private final Fakes.Documents documents = new Fakes.Documents();
    private final Fakes.LetterStore letters = new Fakes.LetterStore(new Fakes.Snapshots());
    private final SavedLetters saved = new SavedLetters(letters, documents);
    private final ListLettersService list = new ListLettersService(host, letters);

    // Background: three letters for customer 4711, saved on three days; Erika may view them.
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
                .as("the second page has the oldest")
                .isEqualTo(new ListResult.Found(new Page<>(java.util.List.of(oldest), 3,
                        new PageRequest(1, 2))));
    }

    @Test
    void invisibleLettersCountNeitherInThePageNorInTheTotal() {
        // Given: two newer letters for another customer; the newest for customer 4711 is logically deleted
        saved.save(CUSTOMER_0815, MONDAY.plus(Duration.ofDays(3)));
        saved.save(CUSTOMER_0815, MONDAY.plus(Duration.ofDays(4)));
        saved.deleteLogically(newest, MONDAY.plus(Duration.ofDays(5)));

        var result = list.list(ERIKA, CUSTOMER_4711, FIRST_PAGE_OF_2);

        // Then: only the other two; the total and the page count count only them
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
        assertThat(letters.query).as("nothing read or counted").isZero();
    }
}
