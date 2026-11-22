package dieterbaier.standardschreiben.usecases.service;

import static org.assertj.core.api.Assertions.assertThat;

import dieterbaier.standardschreiben.domaene.modell.BenutzerKennung;
import dieterbaier.standardschreiben.domaene.modell.VorlagenAktion;
import dieterbaier.standardschreiben.domaene.modell.vorlage.Briefpapier;
import dieterbaier.standardschreiben.domaene.modell.vorlage.Standardschreiben;
import dieterbaier.standardschreiben.domaene.modell.vorlage.Versionsnummer;
import dieterbaier.standardschreiben.domaene.modell.vorlage.Versionsverweis;
import dieterbaier.standardschreiben.domaene.modell.vorlage.VorlagenId;
import dieterbaier.standardschreiben.domaene.modell.vorlage.Vorlagenversion;
import dieterbaier.standardschreiben.domaene.modell.vorlage.Vorlagenzustand;
import dieterbaier.standardschreiben.usecases.in.Rueckzugsergebnis;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Brücke zu {@code features/vorlagen/vorlagenversion-zurueckziehen.feature}: ein Test je Szenario, benannt nach dem
 * Szenariotitel. Die ausgehenden Ports sind {@link Attrappen}.
 */
class VorlagenrueckzugTest {

    private static final BenutzerKennung VERA = new BenutzerKennung("vera");
    private static final BenutzerKennung MAX = new BenutzerKennung("max");

    private final Attrappen.Vorlagen vorlagen = new Attrappen.Vorlagen();
    private final Attrappen.Host host = new Attrappen.Host();
    private final Vorlagenrueckzug rueckzug = new Vorlagenrueckzug(vorlagen, host);

    // Grundlage: Briefpapier und Standardschreiben je mit veröffentlichter Version 1 und Entwurf 2; Vera darf
    // veröffentlichen.
    private final Briefpapier briefpapier = vorlagen.ablegen(Briefpapier.anlegenMitStandardtyp(VorlagenId.neu(),
            "Standard", "<header/>").veroeffentlicheEntwurf().neuerEntwurf("<header>2</header>"));
    private final Standardschreiben mahnung = vorlagen.ablegen(Standardschreiben.anlegen(VorlagenId.neu(), "Mahnung",
            briefpapier.id(), "Kunde", "<p>1</p>").veroeffentlicheEntwurf().neuerEntwurf("<p>2</p>"));

    {
        host.duerfenVeroeffentlichen.add(VERA);
    }

    @ParameterizedTest(name = "{0}")
    @ValueSource(strings = {"ein Standardschreiben", "ein Briefpapier"})
    void dieVeroeffentlichteVersionWirdOhneNachfolgerZurueckgezogen(String vorlage) {
        VorlagenId id = id(vorlage);

        var ergebnis = rueckzug.ziehZurueck(VERA, id);

        assertThat(ergebnis).isEqualTo(
                new Rueckzugsergebnis.Zurueckgezogen(new Versionsverweis(id, Versionsnummer.ERSTE)));
        assertThat(versionen(vorlage)).extracting(Vorlagenversion::zustand)
                .containsExactly(Vorlagenzustand.ZURUECKGEZOGEN, Vorlagenzustand.ENTWURF);
        assertThat(versionen(vorlage).getLast().inhalt()).as("der Entwurf bleibt unverändert")
                .isEqualTo(vorlage.contains("Brief") ? "<header>2</header>" : "<p>2</p>");
        assertThat(host.vorlagenAktionAngefragt).containsExactly(VorlagenAktion.VEROEFFENTLICHEN);
    }

    @Test
    void ohneBerechtigungWirdNichtsZurueckgezogen() {
        var ergebnis = rueckzug.ziehZurueck(MAX, mahnung.id());

        assertThat(ergebnis).isEqualTo(new Rueckzugsergebnis.NichtErlaubt());
        assertThat(vorlagen.ersetzungen).isZero();
    }

    @ParameterizedTest(name = "{0}")
    @ValueSource(strings = {"ein Standardschreiben", "ein Briefpapier"})
    void ohneVeroeffentlichteVersionGibtEsNichtsZurueckzuziehen(String vorlage) {
        vorlagen.ablegen(Briefpapier.anlegenMitStandardtyp(briefpapier.id(), "Standard", "<header/>"));
        vorlagen.ablegen(Standardschreiben.anlegen(mahnung.id(), "Mahnung", briefpapier.id(), "Kunde", "<p>1</p>"));

        var ergebnis = rueckzug.ziehZurueck(VERA, id(vorlage));

        assertThat(ergebnis).isEqualTo(new Rueckzugsergebnis.NichtsVeroeffentlicht());
        assertThat(vorlagen.ersetzungen).isZero();
    }

    @Test
    void eineUnbekannteVorlageWirdGemeldet() {
        assertThat(rueckzug.ziehZurueck(VERA, VorlagenId.neu())).isEqualTo(new Rueckzugsergebnis.VorlageUnbekannt());
    }

    @Test
    void hatEinAndererDieVorlageInzwischenGeaendertWirdNichtsZurueckgezogen() {
        vorlagen.zuvor = () -> vorlagen.ablegen(mahnung.veroeffentlicheEntwurf());

        var ergebnis = rueckzug.ziehZurueck(VERA, mahnung.id());

        assertThat(ergebnis).isEqualTo(new Rueckzugsergebnis.Veraltet());
        assertThat(versionen("ein Standardschreiben")).extracting(Vorlagenversion::zustand)
                .containsExactly(Vorlagenzustand.ABGELOEST, Vorlagenzustand.VEROEFFENTLICHT);
    }

    private VorlagenId id(String vorlage) {
        return vorlage.contains("Brief") ? briefpapier.id() : mahnung.id();
    }

    private List<Vorlagenversion> versionen(String vorlage) {
        return vorlage.contains("Brief")
                ? vorlagen.briefpapier(briefpapier.id()).orElseThrow().versionen()
                : vorlagen.standardschreiben(mahnung.id()).orElseThrow().versionen();
    }
}
