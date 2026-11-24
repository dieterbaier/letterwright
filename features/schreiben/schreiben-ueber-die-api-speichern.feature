# language: de
# Status: proposed (KI-gestützt, wartet auf Review). Brücke zu den Tests:
# ApiSchreibenSpeichernTest im Modul adapter-api, ein Test je Szenario, benannt
# nach dem Szenariotitel. Der Anwendungsfall ist dort eine Attrappe, seine Regeln
# prüft schreiben-speichern.feature. Der Transaktionsmanager ist ein echter
# AbstractPlatformTransactionManager ohne Datenbank.
Funktionalität: Schreiben über die Java-API speichern
  Ein Host speichert ein Schreiben in seinem eigenen Anwendungsfall, etwa beim
  Abschließen einer Aufgabe, und committet seine eigenen Änderungen gemeinsam mit
  dem Schreiben (ADR-023). Er erhält das Ergebnis als Rückgabewert und sieht dabei
  weder das Domänenmodell noch die internen Ports der Bibliothek. Es gelten die
  Regeln aus schreiben-speichern.feature: dieselbe Berechtigungsprüfung und
  höchstens ein Schreiben je Snapshot (ADR-021).

  Grundlage:
    Angenommen Erika hat eine Vorschau der "Mahnung" in Version 2 zum Kunden 4711 erzeugt

  Szenario: Der Host erhält die Identifikatoren des gespeicherten Schreibens
    Wenn der Host die Vorschau für Erika über die API speichert
    Dann ist das Schreiben gespeichert
    Und das Ergebnis nennt die ID des Schreibens, den Kunden 4711, die "Mahnung" in Version 2, Erika und den Zeitpunkt der Erstellung

  Szenario: Eine Wiederholung erhält das bereits gespeicherte Schreiben
    Angenommen das Schreiben aus der Vorschau ist bereits gespeichert
    Wenn der Host die Vorschau für Erika erneut über die API speichert
    Dann erhält er das bereits gespeicherte Schreiben mit denselben Identifikatoren

  Szenariogrundriss: Eine fachliche Ablehnung kommt als Ergebnis zurück
    Angenommen der Anwendungsfall lehnt das Speichern ab: <Ablehnung>
    Wenn der Host die Vorschau für Erika über die API speichert
    Dann erhält er die Ablehnung <Ablehnung>

    Beispiele:
      | Ablehnung             |
      | SnapshotUnbekannt     |
      | SnapshotAbgelaufen    |
      | NichtErlaubt          |
      | DatenkontextGeaendert |
      | NichtKonform          |

  Szenario: Das Speichern tritt der Transaktion des Hosts bei
    Angenommen der Host hat eine Transaktion eröffnet
    Wenn der Host die Vorschau für Erika über die API speichert
    Dann läuft das Speichern in der Transaktion des Hosts
    Und es wurde keine weitere Transaktion eröffnet

  Szenario: Ohne Transaktion des Hosts eröffnet das Speichern eine eigene
    Wenn der Host die Vorschau für Erika ohne eigene Transaktion über die API speichert
    Dann lief das Speichern in einer eigenen Transaktion, die committet wurde

  Szenario: Eine fachliche Ablehnung lässt die Transaktion des Hosts committen
    Angenommen der Host hat eine Transaktion eröffnet
    Und der Anwendungsfall lehnt das Speichern ab: NichtErlaubt
    Wenn der Host die Vorschau für Erika über die API speichert
    Und der Host seine Transaktion committet
    Dann ist die Transaktion des Hosts committet und nicht zurückgerollt

  Szenario: Ein technischer Fehler rollt die Transaktion des Hosts zurück
    Angenommen der Host hat eine Transaktion eröffnet
    Und der Anwendungsfall scheitert mit einem technischen Fehler
    Wenn der Host die Vorschau für Erika über die API speichert
    Dann erhält der Host den Fehler
    Und die Transaktion des Hosts kann nur noch zurückgerollt werden

  Szenario: Eine ungültige Benutzerkennung rollt die Transaktion des Hosts zurück
    Angenommen der Host hat eine Transaktion eröffnet
    Wenn der Host die Vorschau mit einer leeren Benutzerkennung über die API speichert
    Dann erhält der Host einen Fehler des Aufrufers
    Und der Anwendungsfall wurde nicht aufgerufen
    Und die Transaktion des Hosts kann nur noch zurückgerollt werden

  Szenario: Rollt der Host zurück, rollt das Speichern mit zurück
    Angenommen der Host hat eine Transaktion eröffnet
    Wenn der Host die Vorschau für Erika über die API speichert
    Und der Host seine Transaktion zurückrollt
    Dann ist nichts committet
