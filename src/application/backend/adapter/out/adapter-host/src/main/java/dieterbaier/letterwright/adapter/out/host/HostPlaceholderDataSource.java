package dieterbaier.letterwright.adapter.out.host;

import dieterbaier.letterwright.domain.model.UserId;
import dieterbaier.letterwright.domain.model.DataContext;
import dieterbaier.letterwright.domain.model.BusinessReference;
import dieterbaier.letterwright.domain.model.template.LetterTemplate;
import dieterbaier.letterwright.spi.PlaceholderDataProvider;
import dieterbaier.letterwright.usecases.out.PlaceholderDataSource;
import org.jspecify.annotations.Nullable;

/**
 * Asks the host's {@link PlaceholderDataProvider} and takes its values over into the domain model. The host lies
 * outside the trust boundary: if it does not keep the SPI's contract, the adapter reports this as
 * {@link HostContractViolated}.
 */
public final class HostPlaceholderDataSource implements PlaceholderDataSource {

    private final PlaceholderDataProvider provider;

    public HostPlaceholderDataSource(PlaceholderDataProvider provider) {
        this.provider = provider;
    }

    @Override
    public DataContext readDataContext(
            UserId user, BusinessReference reference, LetterTemplate letterTemplate) {
        dieterbaier.letterwright.spi.@Nullable DataContext answer =
                provider.dataContext(Mapping.dataRequest(user, reference, letterTemplate));
        if (answer == null) {
            throw new HostContractViolated("The PlaceholderDataProvider of the host returned null instead of a"
                    + " DataContext");
        }
        return Mapping.toDomain(answer);
    }
}
