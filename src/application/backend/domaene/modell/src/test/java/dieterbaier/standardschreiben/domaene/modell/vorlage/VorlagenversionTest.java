package dieterbaier.standardschreiben.domaene.modell.vorlage;

import static dieterbaier.standardschreiben.domaene.modell.Pruefungen.lehntAb;
import static org.assertj.core.api.Assertions.assertThat;

import dieterbaier.standardschreiben.domaene.modell.Datenkontext;
import java.util.Map;
import org.junit.jupiter.api.Test;

class VorlagenversionTest {

    private static final Datenkontext LEER = Datenkontext.aus(Map.of());

    private final VorlagenId id = VorlagenId.neu();

    private static Vorlagenversion version(VorlagenId vorlage, int nummer, Vorlagenzustand zustand) {
        return Vorlagenversion.wiederherstellen(vorlage, new Versionsnummer(nummer), zustand, "<p>" + nummer + "</p>", LEER);
    }

    @Test
    void pflichtattributeDerVersion() {
        Versionsnummer eins = Versionsnummer.ERSTE;
        Vorlagenzustand entwurf = Vorlagenzustand.ENTWURF;

        lehntAb(() -> Vorlagenversion.wiederherstellen(null, eins, entwurf, "", LEER), "Vorlage der Version");
        lehntAb(() -> Vorlagenversion.wiederherstellen(id, null, entwurf, "", LEER), "Versionsnummer");
        lehntAb(() -> Vorlagenversion.wiederherstellen(id, eins, null, "", LEER), "Zustand der Version");
        lehntAb(() -> Vorlagenversion.wiederherstellen(id, eins, entwurf, null, LEER), "Inhalt der Version");
        lehntAb(() -> Vorlagenversion.wiederherstellen(id, eins, entwurf, "", null), "Beispieldaten der Version");
        for (Vorlagenzustand freigegeben : new Vorlagenzustand[] {Vorlagenzustand.VEROEFFENTLICHT,
                Vorlagenzustand.ABGELOEST, Vorlagenzustand.ZURUECKGEZOGEN}) {
            lehntAb(() -> Vorlagenversion.wiederherstellen(id, eins, freigegeben, " ", LEER),
                    "Inhalt einer freigegebenen");
        }
    }

    @Test
    void veroeffentlichbarIstNurEinEntwurfMitInhalt() {
        assertThat(version(id, 1, Vorlagenzustand.ENTWURF).istVeroeffentlichbar()).isTrue();
        assertThat(Vorlagenversion.wiederherstellen(id, Versionsnummer.ERSTE, Vorlagenzustand.ENTWURF, " \n", LEER)
                .istVeroeffentlichbar()).as("nur Leerraum").isFalse();
        assertThat(version(id, 1, Vorlagenzustand.VEROEFFENTLICHT).istVeroeffentlichbar()).isFalse();
        assertThat(version(id, 1, Vorlagenzustand.ABGELOEST).istVeroeffentlichbar()).isFalse();
    }

    @Test
    void gleichheitDerVersionenUeberVorlageUndNummer() {
        Vorlagenversion entwurf = version(id, 1, Vorlagenzustand.ENTWURF);
        Vorlagenversion veroeffentlicht = version(id, 1, Vorlagenzustand.VEROEFFENTLICHT);

        assertThat(veroeffentlicht).isEqualTo(entwurf).hasSameHashCodeAs(entwurf);
        assertThat(version(id, 2, Vorlagenzustand.ENTWURF)).isNotEqualTo(entwurf);
        assertThat(version(VorlagenId.neu(), 1, Vorlagenzustand.ENTWURF)).isNotEqualTo(entwurf);
        assertThat(entwurf).isNotEqualTo(entwurf.verweis());
        assertThat(entwurf.toString()).contains("nummer=1", "ENTWURF").doesNotContain("<p>");
    }
}
