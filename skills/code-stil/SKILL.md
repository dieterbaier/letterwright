---
name: code-stil
description: "Write and review Java code in standardschreiben in the project's code style: validation only in the types, illegal states made unrepresentable with sealed types, no null outside what the type allows, static factory methods, one test class per production class with its own contract, and Javadoc only for the why or the SPI contract. Use when writing, changing or reviewing backend code or tests, when tempted to add a parameter check, a boolean flag or a nullable field, or when writing Javadoc."
---

# Code-Stil

Dieser Skill erweitert `skills/implement-issue-workflow/SKILL.md` aus dem Toolkit
(`tools/architecture-knowledge-toolkit/`). Lies zuerst diesen, dann die Schritte
hier.

Die Regeln und ihre Begründung stehen in der Architekturdokumentation, nicht hier:

- `src/docs/arc42/doc-04000-solution-strategy.adoc`, Abschnitt „Code-Stil“
  (auch die Testkonvention)
- ADR-018 (DDD-Rollen und Selbstvalidierung), ADR-019 (Validierung in den
  Typen und Null-Vertrag) und ADR-011 (Qualitätsgates, Linting) unter
  `src/docs/arc42/09-architecture-decisions/`
- Querschnittskonzept „Fachmodell“ in `src/docs/arc42/doc-08000-crosscutting-concepts.adoc`

Was der Build durchsetzt, steht hier nicht: Modulgrenzen und Paketregeln
(ArchUnit), Linting (Error Prone), den Null-Vertrag (NullAway), Abdeckung (JaCoCo) und den
Kompilier-Klassenpfad des Hosts (`host-vertragstest`). Lass `./gradlew check`
laufen; was dort scheitert, ist ein Fehler, keine Stilfrage.

## Beim Schreiben

1. **Eine Prüfung gehört in den Typ.** Willst du einen Parameter prüfen, frag, ob
   du in einem Konstruktor oder einer Fabrikmethode bist. Nur dort steht `Pruefe`
   (Domäne) oder `Pflicht` (SPI). In Anwendungsfällen, Ports, Adaptern und
   Abfragemethoden prüfst du nicht.
2. **Ein Flag oder ein optionales Feld ist ein fehlender Typ.** Hängt an einem
   `boolean` oder einem `@Nullable`-Feld, welche anderen Werte gültig sind, entwirf
   stattdessen einen Sealed Type mit einem Record je Fall. Vorbild:
   `Berechtigungsanfrage` mit `ZuVorlagen`, `ZuSchreiben` und `ZumErzeugen`.
3. **Eine fachliche Ablehnung ist ein Ergebnis.** Gibt ein Anwendungsfall
   erwartbar „nicht erlaubt“, „unbekannt“ o. Ä. zurück, ist das ein Fall seines
   Sealed-Ergebnistyps (Vorbild `Vorschauergebnis`), keine Ausnahme. Unterscheide
   die Fälle mit `switch` ohne `default`. Technische Fehler bleiben Ausnahmen.
4. **Kein `null` als Ergebnis.** Darf ein Ergebnis fehlen, gib ein `Optional`
   zurück. `Optional` ist kein Feld und kein Parameter.
5. **Die Grenze bleibt geprüft.** Werte vom Host (SPI-Rückgaben) sind nicht
   vertrauenswürdig. Prüfe sie in `adapter-host` und melde Verstöße als
   `HostVertragVerletzt`.
6. **Neue Objekte über Fabrikmethoden** mit sprechendem Namen: `anlegen`,
   `ausSnapshot`, `neu()`, `wiederherstellen`.
7. **Zu jeder Klasse mit eigenem Verhalten ihre Testklasse.** `Foo` bekommt
   `FooTest` im selben Paket, keine Sammelklasse für mehrere Klassen. Ein
   Anwendungsfall wird in der Testklasse seiner Implementierung geprüft, zugleich
   die Brücke zur Feature-Datei. Baue erwartete Werte im Test selbst auf, nicht
   mit dem Mapper oder der Fabrik, die der Produktionscode benutzt.
8. **Namen aus dem Glossar** (`src/docs/arc42/doc-12000-glossary.adoc`). Ein neuer
   Fachbegriff kommt zuerst ins Glossar.

## Ausnahmen vom Linting

Meldet Error Prone einen Befund, behebe ihn. Ist er an dieser Stelle falsch,
schalte genau diese Prüfung an der kleinsten möglichen Stelle ab, mit
`@SuppressWarnings("<Prüfung>")` und einem Kommentar daneben, der begründet,
warum (ADR-011). Eine Prüfung im ganzen Build abzuschalten, entscheidet der
Owner.

## Javadoc

- **SPI (`spi`):** vollständig. Ein Host-Entwickler muss die Schnittstelle allein
  aus der Javadoc richtig benutzen können. Paketweite Regeln wie den Null-Vertrag
  einmal in `package-info.java`, nicht an jeder Methode.
- **Sonst:** nur, wenn sie ein Warum oder eine Regel erklärt, die die Signatur
  nicht ausdrücken kann. Wiederholt ein Kommentar den Methodennamen, lösch ihn.
- Kein `@throws` für eine Prüfung, die ein Typ überflüssig machen könnte. Mach
  zuerst den Typ (Punkt 2).

## Beim Review

Prüfe jede geänderte Datei gegen die Punkte oben. Melde einen Verstoß mit dem
Verweis auf die Regel in der Lösungsstrategie oder dem ADR, nicht auf diesen Skill.
