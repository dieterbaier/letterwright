package dieterbaier.standardschreiben.domaene.modell;

/** Verweis auf das in der Dokumentablage abgelegte PDF eines Schreibens. Was er bedeutet, weiß nur die Ablage. */
public record Dokumentverweis(String wert) {

    public Dokumentverweis {
        Pruefe.nichtLeer(wert, "Der Dokumentverweis");
    }
}
