package dieterbaier.letterwright.adapter.out.host;

import dieterbaier.letterwright.domain.model.UserId;
import dieterbaier.letterwright.domain.model.DataContext;
import dieterbaier.letterwright.domain.model.BusinessReference;
import dieterbaier.letterwright.domain.model.template.LetterTemplate;
import dieterbaier.letterwright.spi.PlaceholderDataProvider;
import dieterbaier.letterwright.usecases.out.PlaceholderDataSource;
import org.jspecify.annotations.Nullable;

/**
 * Fragt den {@link PlaceholderDataProvider} des Hosts und übernimmt seine Werte ins Domänenmodell. Der Host liegt
 * außerhalb der Vertrauensgrenze: Hält er den Vertrag des SPI nicht ein, meldet der Adapter das als
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
            throw new HostContractViolated("Der PlatzhalterdatenAnbieter des Hosts hat null statt eines Datenkontexts"
                    + " geliefert");
        }
        return Mapping.toDomain(answer);
    }
}
