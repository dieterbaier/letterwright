package dieterbaier.standardschreiben.usecases.service;

import static org.assertj.core.api.Assertions.assertThat;

import dieterbaier.standardschreiben.domaene.modell.BenutzerKennung;
import dieterbaier.standardschreiben.domaene.modell.FachlicherBezug;
import dieterbaier.standardschreiben.domaene.modell.SchreibenAktion;
import dieterbaier.standardschreiben.domaene.modell.Seite;
import dieterbaier.standardschreiben.domaene.modell.Seitenanfrage;
import dieterbaier.standardschreiben.domaene.modell.schreiben.Schreiben;
import dieterbaier.standardschreiben.usecases.in.Uebersichtsergebnis;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.Test;

/**
 * Brücke zu {@code features/schreiben/schreiben-auflisten.feature}: ein Test je Szenario, benannt nach dem
 * Szenariotitel. Die ausgehenden Ports sind {@link Attrappen}.
 */
class SchreibenuebersichtTest {

    private static final Instant MONTAG = Instant.parse("2026-11-16T09:00:00Z");
    private static final BenutzerKennung ERIKA = new BenutzerKennung("erika");
    private static final FachlicherBezug KUNDE_4711 = new FachlicherBezug("Kunde", "4711");
    private static final FachlicherBezug KUNDE_0815 = new FachlicherBezug("Kunde", "0815");
    private static final Seitenanfrage ERSTE_SEITE_MIT_2 = new Seitenanfrage(0, 2);

    private final Attrappen.Host host = new Attrappen.Host();
    private final Attrappen.Dokumente dokumente = new Attrappen.Dokumente();
    private final Attrappen.SchreibenAblage schreiben = new Attrappen.SchreibenAblage(new Attrappen.Snapshots());
    private final GespeicherteSchreiben gespeicherte = new GespeicherteSchreiben(schreiben, dokumente);
    private final Schreibenuebersicht uebersicht = new Schreibenuebersicht(host, schreiben);

    // Grundlage: drei Schreiben zum Kunden 4711, an drei Tagen gespeichert; Erika darf sie ansehen.
    private final Schreiben aeltestes = gespeicherte.speichere(KUNDE_4711, MONTAG);
    private final Schreiben mittleres = gespeicherte.speichere(KUNDE_4711, MONTAG.plus(Duration.ofDays(1)));
    private final Schreiben neuestes = gespeicherte.speichere(KUNDE_4711, MONTAG.plus(Duration.ofDays(2)));

    {
        host.erlaube(ERIKA, SchreibenAktion.ANSEHEN, KUNDE_4711);
    }

    @Test
    void dieUebersichtZeigtDieSchreibenZumBezugSeitenweiseDieNeuestenZuerst() {
        var ergebnis = uebersicht.liste(ERIKA, KUNDE_4711, ERSTE_SEITE_MIT_2);

        Seite<Schreiben> seite = ((Uebersichtsergebnis.Gefunden) ergebnis).seite();
        assertThat(seite.eintraege()).containsExactly(neuestes, mittleres);
        assertThat(seite.gesamtzahl()).isEqualTo(3);
        assertThat(seite.anzahlSeiten()).isEqualTo(2);
        assertThat(uebersicht.liste(ERIKA, KUNDE_4711, new Seitenanfrage(1, 2)))
                .as("die zweite Seite hat das älteste")
                .isEqualTo(new Uebersichtsergebnis.Gefunden(new Seite<>(java.util.List.of(aeltestes), 3,
                        new Seitenanfrage(1, 2))));
    }

    @Test
    void nichtSichtbareSchreibenZaehlenWederInDieSeiteNochInDieGesamtzahl() {
        // Given: zwei neuere Schreiben zu einem anderen Kunden; das neueste zum Kunden 4711 ist logisch gelöscht
        gespeicherte.speichere(KUNDE_0815, MONTAG.plus(Duration.ofDays(3)));
        gespeicherte.speichere(KUNDE_0815, MONTAG.plus(Duration.ofDays(4)));
        gespeicherte.loescheLogisch(neuestes, MONTAG.plus(Duration.ofDays(5)));

        var ergebnis = uebersicht.liste(ERIKA, KUNDE_4711, ERSTE_SEITE_MIT_2);

        // Then: nur die übrigen beiden; Gesamtzahl und Seitenzahl zählen nur sie
        Seite<Schreiben> seite = ((Uebersichtsergebnis.Gefunden) ergebnis).seite();
        assertThat(seite.eintraege()).containsExactly(mittleres, aeltestes);
        assertThat(seite.eintraege()).allMatch(eintrag -> eintrag.bezug().equals(KUNDE_4711))
                .allMatch(Schreiben::istSichtbar);
        assertThat(seite.gesamtzahl()).isEqualTo(2);
        assertThat(seite.anzahlSeiten()).isEqualTo(1);
    }

    @Test
    void ohneBerechtigungZumAnsehenWirdNichtsGelesen() {
        host.verbiete(ERIKA, SchreibenAktion.ANSEHEN, KUNDE_4711);

        var ergebnis = uebersicht.liste(ERIKA, KUNDE_4711, ERSTE_SEITE_MIT_2);

        assertThat(ergebnis).isEqualTo(new Uebersichtsergebnis.NichtErlaubt());
        assertThat(schreiben.abfragen).as("nichts gelesen oder gezählt").isZero();
    }
}
