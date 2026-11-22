package dieterbaier.standardschreiben.usecases.service;

import dieterbaier.standardschreiben.domaene.modell.BenutzerKennung;
import dieterbaier.standardschreiben.domaene.modell.SchreibenAktion;
import dieterbaier.standardschreiben.domaene.modell.schreiben.Loeschentscheidung;
import dieterbaier.standardschreiben.domaene.modell.schreiben.Schreiben;
import dieterbaier.standardschreiben.domaene.modell.schreiben.SchreibenId;
import dieterbaier.standardschreiben.usecases.in.Loeschergebnis;
import dieterbaier.standardschreiben.usecases.in.SchreibenLoeschen;
import dieterbaier.standardschreiben.usecases.out.Berechtigungen;
import dieterbaier.standardschreiben.usecases.out.DokumentAblage;
import dieterbaier.standardschreiben.usecases.out.Loeschregeln;
import dieterbaier.standardschreiben.usecases.out.SchreibenRepository;
import java.time.Clock;

/**
 * Fragt die Löschrichtlinie erst, wenn die Berechtigung erteilt ist, wie es das SPI zusagt. Ein nicht vorhandenes, ein
 * logisch gelöschtes und ein nicht erlaubtes Schreiben ergeben dasselbe {@code Unbekannt} (QS-009).
 *
 * <p>Hart gelöscht wird erst das Schreiben, dann sein PDF. Scheitert das Entfernen des PDFs, bleibt es ohne Schreiben
 * liegen; umgekehrt bliebe ein Schreiben, dessen PDF fehlt.
 */
public final class Schreibenloeschung implements SchreibenLoeschen {

    private final SchreibenRepository schreiben;
    private final Berechtigungen berechtigungen;
    private final Loeschregeln loeschregeln;
    private final DokumentAblage dokumente;
    private final Clock uhr;

    public Schreibenloeschung(SchreibenRepository schreiben, Berechtigungen berechtigungen, Loeschregeln loeschregeln,
            DokumentAblage dokumente, Clock uhr) {
        this.schreiben = schreiben;
        this.berechtigungen = berechtigungen;
        this.loeschregeln = loeschregeln;
        this.dokumente = dokumente;
        this.uhr = uhr;
    }

    @Override
    public Loeschergebnis loesche(BenutzerKennung benutzer, SchreibenId id) {
        return schreiben.lade(id)
                .filter(Schreiben::istSichtbar)
                .filter(gefunden -> berechtigungen.darf(benutzer, SchreibenAktion.LOESCHEN, gefunden.bezug()))
                .map(gefunden -> loesche(benutzer, gefunden))
                .orElseGet(Loeschergebnis.Unbekannt::new);
    }

    private Loeschergebnis loesche(BenutzerKennung benutzer, Schreiben gefunden) {
        return switch (loeschregeln.entscheide(benutzer, gefunden)) {
            case Loeschentscheidung.Hart hart -> {
                schreiben.entferne(gefunden.id());
                dokumente.entferne(gefunden.dokument());
                yield new Loeschergebnis.Geloescht();
            }
            case Loeschentscheidung.Logisch logisch -> {
                schreiben.ersetze(gefunden.logischGeloescht(uhr.instant()));
                yield new Loeschergebnis.Geloescht();
            }
            case Loeschentscheidung.Gesperrt gesperrt -> new Loeschergebnis.Gesperrt(gesperrt.grund());
        };
    }
}
