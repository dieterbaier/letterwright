package dieterbaier.standardschreiben.usecases.service;

import dieterbaier.standardschreiben.domaene.modell.FachlicherBezug;
import dieterbaier.standardschreiben.domaene.modell.Pruefsumme;
import dieterbaier.standardschreiben.domaene.modell.schreiben.Schreiben;
import dieterbaier.standardschreiben.domaene.modell.schreiben.SchreibenId;
import dieterbaier.standardschreiben.domaene.modell.schreiben.Schreibenzustand;
import dieterbaier.standardschreiben.domaene.modell.snapshot.SnapshotId;
import dieterbaier.standardschreiben.domaene.modell.vorlage.Versionsnummer;
import dieterbaier.standardschreiben.domaene.modell.vorlage.Versionsverweis;
import dieterbaier.standardschreiben.domaene.modell.vorlage.VorlagenId;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

/** Legt Schreiben samt PDF in den Attrappen ab, als wären sie früher gespeichert worden. */
final class GespeicherteSchreiben {

    private final Attrappen.SchreibenAblage schreiben;
    private final Attrappen.Dokumente dokumente;
    private final Versionsverweis briefpapier = new Versionsverweis(VorlagenId.neu(), Versionsnummer.ERSTE);
    private final Versionsverweis mahnung = new Versionsverweis(VorlagenId.neu(), Versionsnummer.ERSTE);

    GespeicherteSchreiben(Attrappen.SchreibenAblage schreiben, Attrappen.Dokumente dokumente) {
        this.schreiben = schreiben;
        this.dokumente = dokumente;
    }

    Schreiben speichere(FachlicherBezug bezug, Instant erstellt) {
        byte[] pdf = ("%PDF " + bezug.id() + " " + erstellt).getBytes(StandardCharsets.UTF_8);
        return schreiben.ablegen(Schreiben.wiederherstellen(SchreibenId.neu(), SnapshotId.neu(), bezug, briefpapier,
                mahnung, dokumente.lege(pdf), Pruefsumme.ueber(pdf), erstellt, new Schreibenzustand.Sichtbar()));
    }

    Schreiben loescheLogisch(Schreiben vorhandenes, Instant am) {
        Schreiben geloescht = vorhandenes.logischGeloescht(am);
        schreiben.ersetze(geloescht);
        return geloescht;
    }
}
