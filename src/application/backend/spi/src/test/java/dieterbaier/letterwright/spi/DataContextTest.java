package dieterbaier.letterwright.spi;

import static dieterbaier.letterwright.spi.Checks.rejects;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class DataContextTest {

    @Test
    void theDataContextIsAnImmutableCopy() {
        Map<String, Object> values = new HashMap<>();
        values.put("name", "Erika");
        DataContext context = new DataContext(values);
        values.put("betrag", "12,00 €");

        assertThat(context.values()).containsOnlyKeys("name");
        assertThatThrownBy(() -> context.values().clear()).isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void changesToTheHostsNestedCollectionsHaveNoEffect() {
        List<Object> customers = new ArrayList<>(List.of("Erika"));
        Map<String, Object> address = new HashMap<>(Map.of("ort", "Berlin"));
        DataContext context = new DataContext(Map.of("customers", customers, "address", address));
        int hashBefore = context.hashCode();

        customers.add("Max");
        address.put("ort", "Hamburg");

        assertThat(context.values().get("customers")).isEqualTo(List.of("Erika"));
        assertThat(context.values().get("address")).isEqualTo(Map.of("ort", "Berlin"));
        assertThat(context.hashCode()).isEqualTo(hashBefore);
    }

    @Test
    void nestedCollectionsCannotBeChangedViaTheAccessor() {
        DataContext context = new DataContext(Map.of("items", List.of(new HashMap<>(Map.of("betrag", "12,00 €")))));

        List<?> items = (List<?>) context.values().get("items");
        Map<?, ?> first = (Map<?, ?>) items.get(0);

        assertThatThrownBy(items::clear).isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(first::clear).isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void invalidValuesAreKeptForTheLibrarysCheck() {
        Map<String, Object> values = new HashMap<>();
        values.put("missing", null);
        values.put("zahl", 12);

        Map<String, ?> adopted = new DataContext(values).values();

        assertThat(adopted.containsKey("missing")).isTrue();
        assertThat(adopted.get("missing")).isNull();
        assertThat(adopted.get("zahl")).isEqualTo(12);
    }

    @Test
    void toStringRevealsNoValues() {
        DataContext context = new DataContext(
                Map.of("name", "Erika Mustermann", "konten", List.of(Map.of("iban", "DE02120300000000202051"))));

        assertThat(context.toString())
                .doesNotContain("Erika", "Mustermann", "DE02120300000000202051", "iban")
                .isEqualTo("Datenkontext[2 Werte]");
    }

    @Test
    void nestedValuesAreKept() {
        DataContext context = new DataContext(Map.of("items", List.of(Map.of("betrag", "12,00 €"))));

        assertThat(context.values().get("items")).isEqualTo(List.of(Map.of("betrag", "12,00 €")));
    }

    @Test
    void theValuesAreRequired() {
        rejects(() -> new DataContext(null), "Die Werte des Datenkontexts");
    }
}
