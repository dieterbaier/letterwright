package dieterbaier.letterwright.adapter.in.api;

import dieterbaier.letterwright.api.SaveLetter;
import dieterbaier.letterwright.api.SaveResult;
import dieterbaier.letterwright.domain.model.UserId;
import dieterbaier.letterwright.domain.model.snapshot.SnapshotId;
import java.util.UUID;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Speichert über den Anwendungsfall {@code SaveLetter} in der Transaktion des Hosts (ADR-023).
 *
 * <p>Die Transaktion steuert der Adapter selbst mit {@code REQUIRED}, statt über {@code @Transactional}: Dann hängt die
 * Grenze nicht davon ab, dass der Aufruf durch einen Proxy läuft, und sie lässt sich ohne Spring-Kontext prüfen. Ein
 * Ergebnis kehrt regulär zurück und markiert die Transaktion deshalb nie zum Zurückrollen; nur eine Ausnahme tut das.
 * Auch die Eingaben werden erst innerhalb der Grenze zu Domänentypen: Eine ungültige Eingabe markiert die Transaktion
 * des Hosts wie jede andere Ausnahme, damit ein Host, der sie fängt, nicht halb committen kann.
 */
public final class ApiSaveLetter implements SaveLetter {

    private final dieterbaier.letterwright.usecases.in.SaveLetter useCase;
    private final TransactionTemplate transaction;

    public ApiSaveLetter(dieterbaier.letterwright.usecases.in.SaveLetter useCase,
            PlatformTransactionManager transactions) {
        this.useCase = useCase;
        this.transaction = new TransactionTemplate(transactions);
        this.transaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
    }

    @Override
    public SaveResult save(String userId, UUID snapshot) {
        return transaction.execute(status -> {
            UserId user = new UserId(userId);
            return Mapping.toApi(useCase.save(user, new SnapshotId(snapshot)), user);
        });
    }
}
