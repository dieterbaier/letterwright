package dieterbaier.letterwright.domain.model;

import static dieterbaier.letterwright.domain.model.Checks.rejects;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class DataContextTest {

    private static final Map<String, Object> CUSTOMER = Map.of(
            "recipient", Map.of("name", "Erika Mustermann", "city", "Cologne"),
            "items", List.of("Fee 2026", "Reminder fee"));

    @Test
    void sameContentYieldsSameChecksumRegardlessOfKeyOrder() {
        Map<String, Object> forward = new LinkedHashMap<>();
        forward.put("a", "1");
        forward.put("b", "2");
        Map<String, Object> backward = new LinkedHashMap<>();
        backward.put("b", "2");
        backward.put("a", "1");

        assertThat(DataContext.from(forward).checksum()).isEqualTo(DataContext.from(backward).checksum());
        assertThat(DataContext.from(forward)).isEqualTo(DataContext.from(backward))
                .hasSameHashCodeAs(DataContext.from(backward));
    }

    @Test
    void everyChangeOfAValueChangesTheChecksum() {
        Checksum before = DataContext.from(CUSTOMER).checksum();

        Map<String, Object> other = new HashMap<>(CUSTOMER);
        other.put("recipient", Map.of("name", "Erika Mustermann", "city", "Bonn"));

        assertThat(DataContext.from(other).checksum()).isNotEqualTo(before);
    }

    @Test
    void theOrderInListsMatters() {
        DataContext ab = DataContext.from(Map.of("items", List.of("a", "b")));
        DataContext ba = DataContext.from(Map.of("items", List.of("b", "a")));

        assertThat(ab.checksum()).isNotEqualTo(ba.checksum());
        assertThat(ab).isNotEqualTo(ba);
    }

    @Test
    void theEncodingIsUnambiguousEvenWhenTextsLookLikeStructure() {
        // Without lengths and type tags these pairs would yield the same byte sequence.
        assertThat(DataContext.from(Map.of("a", "bc")).checksum())
                .isNotEqualTo(DataContext.from(Map.of("ab", "c")).checksum());
        assertThat(DataContext.from(Map.of("x", List.of("ab"))).checksum())
                .isNotEqualTo(DataContext.from(Map.of("x", List.of("a", "b"))).checksum());
        assertThat(DataContext.from(Map.of("x", "")).checksum())
                .isNotEqualTo(DataContext.from(Map.of("x", List.of())).checksum())
                .isNotEqualTo(DataContext.from(Map.of("x", Map.of())).checksum());
    }

    @Test
    void theChecksumIsStable() {
        // Pinned so that a change of the encoding is noticed: snapshots created before the change would otherwise count
        // as changed on saving. The value was recomputed independently of this implementation (Python, hashlib):
        // M,2 | T,"items" L,1 T,"a" | T,"name" T,"Erika"; lengths as int32 big-endian.
        assertThat(DataContext.from(Map.of("name", "Erika", "items", List.of("a"))).checksum().hex())
                .isEqualTo("9f283f547742e425341d6f0a592f249b049c0c65a567f337c47be24851e76fd8");
    }

    @Test
    void isDeeplyImmutableAndIndependentOfTheSource() {
        List<Object> items = new ArrayList<>(List.of("a"));
        Map<String, Object> source = new HashMap<>(Map.of("items", items, "sub", new HashMap<>(Map.of("k", "v"))));
        DataContext context = DataContext.from(source);

        items.add("b");
        source.put("new", "x");

        assertThat(context.values()).containsOnlyKeys("items", "sub");
        assertThat(context.values().get("items")).isEqualTo(List.of("a"));
        assertThatThrownBy(() -> context.values().put("new", "x")).isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> ((List<?>) context.values().get("items")).clear())
                .isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> ((Map<?, ?>) context.values().get("sub")).clear())
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void theValuesAreSortedByKey() {
        Map<String, Object> source = new LinkedHashMap<>();
        source.put("b", "2");
        source.put("a", "1");

        assertThat(DataContext.from(source).values().keySet()).containsExactly("a", "b");
    }

    @Test
    void rejectsOtherTypesAndNamesThePath() {
        rejects(() -> DataContext.from(Map.of("amount", Map.of("net", 12))), "amount.net");
        rejects(() -> DataContext.from(Map.of("amount", Map.of("net", 12))), "java.lang.Integer");
        rejects(() -> DataContext.from(Map.of("items", List.of("a", List.of(1)))), "items[1][0]");
    }

    @Test
    void rejectsMissingValues() {
        Map<String, Object> source = new HashMap<>();
        source.put("name", null);

        rejects(() -> DataContext.from(null), "values of the data context");
        rejects(() -> DataContext.from(source), "name");
        rejects(() -> DataContext.from(Map.of("items", Arrays.asList("a", null))), "items[1]");
    }

    @Test
    void rejectsKeysThatAreNotText() {
        rejects(() -> DataContext.from(Map.of("sub", Map.of(1, "x"))), "sub");
    }

    @Test
    void rejectsLoneSurrogatesInValuesAndNamesThePath() {
        // Without this check UTF-8 would encode the surrogate as '?', and both contexts would have the same checksum.
        rejects(() -> DataContext.from(Map.of("x", "\uD800")), "x");
        rejects(() -> DataContext.from(Map.of("items", List.of("a\uDC00b"))), "items[0]");
    }

    @Test
    void rejectsLoneSurrogatesInKeys() {
        rejects(() -> DataContext.from(Map.of("sub", Map.of("k\uD800", "x"))), "Key sub.k");
    }

    @Test
    void validSurrogatePairsAreAllowedAndDistinguishable() {
        DataContext emoji = DataContext.from(Map.of("greeting", "Hello \uD83D\uDE00"));
        DataContext questionMark = DataContext.from(Map.of("greeting", "Hello ?"));

        assertThat(emoji.values()).containsEntry("greeting", "Hello 😀");
        assertThat(emoji.checksum()).isNotEqualTo(questionMark.checksum());
    }

    @Test
    void extendedByAnotherWhoseValuesTakePrecedence() {
        DataContext letterhead = DataContext.from(Map.of("sender", "Muster GmbH", "subject", "General"));
        DataContext letterTemplate = DataContext.from(Map.of("subject", "Payment reminder", "amount", "12,00 €"));

        DataContext together = letterhead.extendedBy(letterTemplate);

        assertThat(together).isEqualTo(DataContext.from(
                Map.of("sender", "Muster GmbH", "subject", "Payment reminder", "amount", "12,00 €")));
        assertThat(together.values()).isUnmodifiable();
        assertThat(together.checksum()).isEqualTo(DataContext.from(together.values()).checksum());
        rejects(() -> letterhead.extendedBy(null), "preferred data context");
    }

    @Test
    void toStringShowsNoValues() {
        assertThat(DataContext.from(CUSTOMER).toString()).doesNotContain("Erika").contains("checksum=");
    }

    @Test
    void isEqualOnlyWithDataContexts() {
        assertThat(DataContext.from(CUSTOMER)).isNotEqualTo(CUSTOMER);
    }
}
