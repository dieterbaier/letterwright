package dieterbaier.standardschreiben.usecases.service;

import dieterbaier.standardschreiben.domaene.modell.BenutzerKennung;
import dieterbaier.standardschreiben.domaene.modell.VorlagenAktion;
import dieterbaier.standardschreiben.domaene.modell.vorlage.Briefpapier;
import dieterbaier.standardschreiben.domaene.modell.vorlage.Standardschreiben;
import dieterbaier.standardschreiben.domaene.modell.vorlage.VorlagenId;
import dieterbaier.standardschreiben.domaene.modell.vorlage.Vorlagenversion;
import dieterbaier.standardschreiben.usecases.in.Veroeffentlichungsergebnis;
import dieterbaier.standardschreiben.usecases.in.VorlagenversionVeroeffentlichen;
import dieterbaier.standardschreiben.usecases.out.Berechtigungen;
import dieterbaier.standardschreiben.usecases.out.Ersetzungsausgang;
import dieterbaier.standardschreiben.usecases.out.Konformitaet;
import dieterbaier.standardschreiben.usecases.out.KonformitaetsPruefer;
import dieterbaier.standardschreiben.usecases.out.Renderer;
import dieterbaier.standardschreiben.usecases.out.VorlagenRepository;
import java.util.Optional;
import java.util.function.Supplier;

public final class Vorlagenveroeffentlichung implements VorlagenversionVeroeffentlichen {

    private final VorlagenRepository vorlagen;
    private final Berechtigungen berechtigungen;
    private final Renderer renderer;
    private final KonformitaetsPruefer pruefer;

    public Vorlagenveroeffentlichung(VorlagenRepository vorlagen, Berechtigungen berechtigungen, Renderer renderer,
            KonformitaetsPruefer pruefer) {
        this.vorlagen = vorlagen;
        this.berechtigungen = berechtigungen;
        this.renderer = renderer;
        this.pruefer = pruefer;
    }

    @Override
    public Veroeffentlichungsergebnis veroeffentliche(BenutzerKennung benutzer, VorlagenId vorlage) {
        if (!berechtigungen.darf(benutzer, VorlagenAktion.VEROEFFENTLICHEN)) {
            return new Veroeffentlichungsergebnis.NichtErlaubt();
        }
        Optional<Standardschreiben> standardschreiben = vorlagen.standardschreiben(vorlage);
        if (standardschreiben.isPresent()) {
            return veroeffentliche(standardschreiben.get());
        }
        return vorlagen.briefpapier(vorlage)
                .map(this::veroeffentliche)
                .orElseGet(Veroeffentlichungsergebnis.VorlageUnbekannt::new);
    }

    private Veroeffentlichungsergebnis veroeffentliche(Standardschreiben geladen) {
        Optional<Vorlagenversion> entwurf = geladen.entwurf();
        if (entwurf.isEmpty()) {
            return new Veroeffentlichungsergebnis.KeinEntwurf();
        }
        if (!entwurf.get().istVeroeffentlichbar()) {
            return new Veroeffentlichungsergebnis.EntwurfOhneInhalt();
        }
        Optional<Vorlagenversion> briefpapier = vorlagen.briefpapier(geladen.briefpapier())
                .flatMap(Briefpapier::veroeffentlichteVersion);
        if (briefpapier.isEmpty()) {
            return new Veroeffentlichungsergebnis.BriefpapierNichtVeroeffentlicht();
        }
        var beispieldaten = briefpapier.get().beispieldaten().ergaenztUm(entwurf.get().beispieldaten());
        byte[] probe = renderer.erzeugePdf(briefpapier.get(), entwurf.get(), beispieldaten);
        return wennKonform(probe, entwurf.get(),
                () -> vorlagen.ersetze(geladen, geladen.veroeffentlicheEntwurf()));
    }

    private Veroeffentlichungsergebnis veroeffentliche(Briefpapier geladen) {
        Optional<Vorlagenversion> entwurf = geladen.entwurf();
        if (entwurf.isEmpty()) {
            return new Veroeffentlichungsergebnis.KeinEntwurf();
        }
        if (!entwurf.get().istVeroeffentlichbar()) {
            return new Veroeffentlichungsergebnis.EntwurfOhneInhalt();
        }
        byte[] probe = renderer.erzeugeProbePdf(entwurf.get(), entwurf.get().beispieldaten());
        return wennKonform(probe, entwurf.get(),
                () -> vorlagen.ersetze(geladen, geladen.veroeffentlicheEntwurf()));
    }

    /** Kann der Prüfer nicht prüfen, wirft er; dann wird ebenfalls nichts ersetzt (fail-closed, ADR-005). */
    private Veroeffentlichungsergebnis wennKonform(
            byte[] probe, Vorlagenversion entwurf, Supplier<Ersetzungsausgang> ersetze) {
        if (pruefer.pruefe(probe) instanceof Konformitaet.NichtKonform nichtKonform) {
            return new Veroeffentlichungsergebnis.NichtKonform(nichtKonform.befunde());
        }
        return switch (ersetze.get()) {
            case Ersetzungsausgang.Ersetzt ersetzt -> new Veroeffentlichungsergebnis.Veroeffentlicht(entwurf.verweis());
            case Ersetzungsausgang.Veraltet veraltet -> new Veroeffentlichungsergebnis.Veraltet();
        };
    }
}
