package dieterbaier.standardschreiben.spi;

import static dieterbaier.standardschreiben.spi.Pruefungen.lehntAb;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class BerechtigungsanfrageTest {

    private static final FachlicherBezug KUNDE = new FachlicherBezug("Kunde", "4711");
    private static final Standardschreibenkennung MAHNUNG =
            new Standardschreibenkennung(UUID.randomUUID(), "Mahnung");

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", " ", "\t"})
    void dieBenutzerkennungLehntFehlendeLeereUndBlankeWerteAb(String wert) {
        lehntAb(() -> Berechtigungsanfrage.zuSchreiben(wert, SchreibenAktion.ANSEHEN, KUNDE), "Die Benutzerkennung");
        lehntAb(() -> Berechtigungsanfrage.zumErzeugen(wert, KUNDE, MAHNUNG), "Die Benutzerkennung");
        lehntAb(() -> Berechtigungsanfrage.zuVorlagen(wert, VorlagenAktion.BEARBEITEN), "Die Benutzerkennung");
    }

    @Test
    void dieAktionIstPflicht() {
        lehntAb(() -> Berechtigungsanfrage.zuSchreiben("erika", null, KUNDE), "Die Aktion");
        lehntAb(() -> Berechtigungsanfrage.zuVorlagen("erika", null), "Die Aktion");
    }

    @Test
    void eineAnfrageZuSchreibenBrauchtEinenBezug() {
        lehntAb(() -> Berechtigungsanfrage.zuSchreiben("erika", SchreibenAktion.ANSEHEN, null), "Der fachliche Bezug");
        lehntAb(() -> Berechtigungsanfrage.zumErzeugen("erika", null, MAHNUNG), "Der fachliche Bezug");
        lehntAb(() -> Berechtigungsanfrage.zumErzeugen("erika", KUNDE, null), "Das Standardschreiben");
    }

    @Test
    void dieAnfrageSagtIhrerArtNachWasSieTraegt() {
        var schreiben = (Berechtigungsanfrage.ZuSchreiben) Berechtigungsanfrage.zuSchreiben("erika",
                SchreibenAktion.LOESCHEN, KUNDE);
        Berechtigungsanfrage vorlagen = Berechtigungsanfrage.zuVorlagen("erika", VorlagenAktion.VEROEFFENTLICHEN);

        assertThat(schreiben).isEqualTo(new Berechtigungsanfrage.ZuSchreiben("erika", SchreibenAktion.LOESCHEN, KUNDE));
        assertThat(schreiben.aktion()).isEqualTo(SchreibenAktion.LOESCHEN);
        assertThat(vorlagen).isEqualTo(new Berechtigungsanfrage.ZuVorlagen("erika", VorlagenAktion.VEROEFFENTLICHEN));
        assertThat(Berechtigungsanfrage.zumErzeugen("erika", KUNDE, MAHNUNG))
                .isEqualTo(new Berechtigungsanfrage.ZumErzeugen("erika", KUNDE, MAHNUNG));
    }
}
