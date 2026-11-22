package dieterbaier.standardschreiben.usecases.service;

import dieterbaier.standardschreiben.domaene.modell.BenutzerKennung;
import dieterbaier.standardschreiben.domaene.modell.Pruefsumme;
import dieterbaier.standardschreiben.domaene.modell.SchreibenAktion;
import dieterbaier.standardschreiben.domaene.modell.schreiben.Schreiben;
import dieterbaier.standardschreiben.domaene.modell.schreiben.SchreibenId;
import dieterbaier.standardschreiben.usecases.in.Downloadergebnis;
import dieterbaier.standardschreiben.usecases.in.SchreibenHerunterladen;
import dieterbaier.standardschreiben.usecases.out.Berechtigungen;
import dieterbaier.standardschreiben.usecases.out.DokumentAblage;
import dieterbaier.standardschreiben.usecases.out.SchreibenRepository;

/**
 * Ein nicht vorhandenes, ein logisch gelöschtes und ein nicht erlaubtes Schreiben ergeben dasselbe {@code Unbekannt}
 * (QS-009). Das PDF liefert es nur mit der Prüfsumme vom Speichern aus; weicht sie ab, ist der Bestand defekt, und es
 * liefert nichts (QS-008).
 */
public final class Schreibenabruf implements SchreibenHerunterladen {

    private final SchreibenRepository schreiben;
    private final Berechtigungen berechtigungen;
    private final DokumentAblage dokumente;

    public Schreibenabruf(SchreibenRepository schreiben, Berechtigungen berechtigungen, DokumentAblage dokumente) {
        this.schreiben = schreiben;
        this.berechtigungen = berechtigungen;
        this.dokumente = dokumente;
    }

    @Override
    public Downloadergebnis lade(BenutzerKennung benutzer, SchreibenId id) {
        return schreiben.lade(id)
                .filter(Schreiben::istSichtbar)
                .filter(gefunden -> berechtigungen.darf(benutzer, SchreibenAktion.HERUNTERLADEN, gefunden.bezug()))
                .<Downloadergebnis>map(this::liefere)
                .orElseGet(Downloadergebnis.Unbekannt::new);
    }

    private Downloadergebnis.Geliefert liefere(Schreiben gefunden) {
        byte[] pdf = dokumente.lies(gefunden.dokument());
        if (!Pruefsumme.ueber(pdf).equals(gefunden.pdfPruefsumme())) {
            throw new IllegalStateException(
                    "Das PDF von Schreiben " + gefunden.id().wert() + " weicht von der gespeicherten Prüfsumme ab");
        }
        return new Downloadergebnis.Geliefert(gefunden, pdf);
    }
}
