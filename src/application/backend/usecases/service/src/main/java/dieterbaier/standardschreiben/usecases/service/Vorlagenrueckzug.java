package dieterbaier.standardschreiben.usecases.service;

import dieterbaier.standardschreiben.domaene.modell.BenutzerKennung;
import dieterbaier.standardschreiben.domaene.modell.VorlagenAktion;
import dieterbaier.standardschreiben.domaene.modell.vorlage.Standardschreiben;
import dieterbaier.standardschreiben.domaene.modell.vorlage.VorlagenId;
import dieterbaier.standardschreiben.domaene.modell.vorlage.Vorlagenversion;
import dieterbaier.standardschreiben.usecases.in.Rueckzugsergebnis;
import dieterbaier.standardschreiben.usecases.in.VorlagenversionZurueckziehen;
import dieterbaier.standardschreiben.usecases.out.Berechtigungen;
import dieterbaier.standardschreiben.usecases.out.Ersetzungsausgang;
import dieterbaier.standardschreiben.usecases.out.VorlagenRepository;
import java.util.Optional;
import java.util.function.Supplier;

public final class Vorlagenrueckzug implements VorlagenversionZurueckziehen {

    private final VorlagenRepository vorlagen;
    private final Berechtigungen berechtigungen;

    public Vorlagenrueckzug(VorlagenRepository vorlagen, Berechtigungen berechtigungen) {
        this.vorlagen = vorlagen;
        this.berechtigungen = berechtigungen;
    }

    /** Zurückziehen gehört zum Veröffentlichen: Beides bestimmt, was in neue Schreiben eingeht. */
    @Override
    public Rueckzugsergebnis ziehZurueck(BenutzerKennung benutzer, VorlagenId vorlage) {
        if (!berechtigungen.darf(benutzer, VorlagenAktion.VEROEFFENTLICHEN)) {
            return new Rueckzugsergebnis.NichtErlaubt();
        }
        Optional<Standardschreiben> standardschreiben = vorlagen.standardschreiben(vorlage);
        if (standardschreiben.isPresent()) {
            Standardschreiben geladen = standardschreiben.get();
            return geladen.veroeffentlichteVersion()
                    .map(version -> ziehZurueck(version, () -> vorlagen.ersetze(geladen, geladen.zurueckziehen())))
                    .orElseGet(Rueckzugsergebnis.NichtsVeroeffentlicht::new);
        }
        return vorlagen.briefpapier(vorlage)
                .map(geladen -> geladen.veroeffentlichteVersion()
                        .map(version -> ziehZurueck(version, () -> vorlagen.ersetze(geladen, geladen.zurueckziehen())))
                        .orElseGet(Rueckzugsergebnis.NichtsVeroeffentlicht::new))
                .orElseGet(Rueckzugsergebnis.VorlageUnbekannt::new);
    }

    private static Rueckzugsergebnis ziehZurueck(Vorlagenversion veroeffentlicht, Supplier<Ersetzungsausgang> ersetze) {
        return switch (ersetze.get()) {
            case Ersetzungsausgang.Ersetzt ersetzt -> new Rueckzugsergebnis.Zurueckgezogen(veroeffentlicht.verweis());
            case Ersetzungsausgang.Veraltet veraltet -> new Rueckzugsergebnis.Veraltet();
        };
    }
}
