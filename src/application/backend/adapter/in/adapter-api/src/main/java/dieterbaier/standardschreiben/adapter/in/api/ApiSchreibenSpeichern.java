package dieterbaier.standardschreiben.adapter.in.api;

import dieterbaier.standardschreiben.api.SchreibenSpeichern;
import dieterbaier.standardschreiben.api.Speicherergebnis;
import dieterbaier.standardschreiben.domaene.modell.BenutzerKennung;
import dieterbaier.standardschreiben.domaene.modell.snapshot.SnapshotId;
import java.util.UUID;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Speichert über den Anwendungsfall {@code SchreibenSpeichern} in der Transaktion des Hosts (ADR-023).
 *
 * <p>Die Transaktion steuert der Adapter selbst mit {@code REQUIRED}, statt über {@code @Transactional}: Dann hängt die
 * Grenze nicht davon ab, dass der Aufruf durch einen Proxy läuft, und sie lässt sich ohne Spring-Kontext prüfen. Ein
 * Ergebnis kehrt regulär zurück und markiert die Transaktion deshalb nie zum Zurückrollen; nur eine Ausnahme tut das.
 */
public final class ApiSchreibenSpeichern implements SchreibenSpeichern {

    private final dieterbaier.standardschreiben.usecases.in.SchreibenSpeichern anwendungsfall;
    private final TransactionTemplate transaktion;

    public ApiSchreibenSpeichern(dieterbaier.standardschreiben.usecases.in.SchreibenSpeichern anwendungsfall,
            PlatformTransactionManager transaktionen) {
        this.anwendungsfall = anwendungsfall;
        this.transaktion = new TransactionTemplate(transaktionen);
        this.transaktion.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
    }

    @Override
    public Speicherergebnis speichere(String benutzerkennung, UUID snapshot) {
        BenutzerKennung benutzer = new BenutzerKennung(benutzerkennung);
        SnapshotId id = new SnapshotId(snapshot);
        return transaktion.execute(status -> Abbildung.zurApi(anwendungsfall.speichere(benutzer, id), benutzer));
    }
}
