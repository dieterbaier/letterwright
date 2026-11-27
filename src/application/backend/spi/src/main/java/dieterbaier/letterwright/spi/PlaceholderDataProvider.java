package dieterbaier.letterwright.spi;

/**
 * Delivers the values for the placeholders of a letter template. The host implements this interface as a bean.
 *
 * <p>The library calls it when the preview is created and again when the letter is saved. If the values change in
 * between, it demands a new preview (ADR-007). The same data should therefore lead to the same values.
 */
public interface PlaceholderDataProvider {

    /** The already formatted values. If there are no values, an empty data context. */
    DataContext dataContext(DataRequest request);
}
