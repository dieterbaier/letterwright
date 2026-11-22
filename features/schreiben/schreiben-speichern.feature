# language: de
# Status: proposed (KI-gestützt, wartet auf Review). Brücke zu den Tests:
# SchreibenSpeichernTest im Modul usecases-service, ein Test je Szenario,
# benannt nach dem Szenariotitel. Die Ports sind dort durch Attrappen ersetzt.
Funktionalität: Schreiben speichern
  Ein Sachbearbeiter speichert eine geprüfte Vorschau als Schreiben. Gespeichert
  wird genau das PDF, das aus dem Snapshot der Vorschau entsteht, und nur, wenn es
  konform ist (ADR-005, ADR-007). Die Bibliothek prüft die Berechtigung erneut und
  liest den Datenkontext erneut; hat er sich seit der Vorschau geändert, ist eine
  neue Vorschau nötig. Zu einem Snapshot gibt es höchstens ein Schreiben
  (ADR-021).

  Grundlage:
    Angenommen Erika hat vor 10 Minuten eine Vorschau der "Mahnung" zum Kunden 4711 erzeugt
    Und Erika darf zum Kunden 4711 aus der "Mahnung" ein Schreiben erzeugen
    Und der Host liefert zum Kunden 4711 weiter denselben Datenkontext

  Szenario: Aus einer unveränderten Vorschau wird das geprüfte PDF gespeichert
    Wenn Erika die Vorschau speichert
    Dann ist ein Schreiben gespeichert
    Und sein PDF entstand aus den Versionen und dem Datenkontext des Snapshots
    Und es trägt die Prüfsumme dieses PDFs

  Szenario: Wird die Berechtigung beim Speichern entzogen, wird nichts persistiert
    Angenommen Erika darf inzwischen aus der "Mahnung" kein Schreiben mehr erzeugen
    Wenn Erika die Vorschau speichert
    Dann ist das Speichern nicht erlaubt
    Und es wurde kein PDF erzeugt
    Und nichts ist gespeichert oder abgelegt

  Szenario: Ein nicht konformes PDF wird nicht gespeichert
    Angenommen die Konformitätsprüfung findet "PDF/UA-1: Bild ohne Alternativtext"
    Wenn Erika die Vorschau speichert
    Dann ist das PDF nicht konform mit dem Befund "PDF/UA-1: Bild ohne Alternativtext"
    Und nichts ist gespeichert oder abgelegt

  Szenario: Ein nicht prüfbares PDF wird nicht gespeichert
    Angenommen die Konformitätsprüfung kann das PDF nicht prüfen
    Wenn Erika die Vorschau speichert
    Dann schlägt das Speichern mit einem Fehler fehl
    Und nichts ist gespeichert oder abgelegt

  Szenario: Nach einer Änderung des Datenkontexts wird kein PDF erzeugt
    Angenommen der Host liefert zum Kunden 4711 inzwischen einen anderen Datenkontext
    Wenn Erika die Vorschau speichert
    Dann hat sich der Datenkontext geändert
    Und es wurde kein PDF erzeugt
    Und nichts ist gespeichert oder abgelegt

  Szenario: Nach einer neuen Vorschau gelingt das Speichern
    Angenommen der Host liefert zum Kunden 4711 inzwischen einen anderen Datenkontext
    Und Erika hat die Vorschau danach aktualisiert
    Wenn Erika die aktualisierte Vorschau speichert
    Dann ist ein Schreiben mit dem neuen Datenkontext gespeichert

  Szenario: Ein fremder Snapshot verhält sich wie ein nicht vorhandener
    Wenn Max die Vorschau von Erika speichert
    Dann ist der Snapshot unbekannt
    Und der Host wurde nicht gefragt

  Szenario: Ein abgelaufener Snapshot wird nicht gespeichert
    Angenommen seit der Vorschau sind 30 Minuten vergangen
    Wenn Erika die Vorschau speichert
    Dann ist der Snapshot abgelaufen
    Und der Host wurde nicht gefragt

  Szenario: Wiederholtes Speichern derselben Vorschau ergibt kein zweites Schreiben
    Angenommen Erika hat die Vorschau bereits gespeichert, aber die Antwort nicht erhalten
    Wenn Erika die Vorschau erneut speichert
    Dann erhält sie das bereits gespeicherte Schreiben
    Und es wurde kein weiteres PDF erzeugt

  Szenario: Gleichzeitiges Speichern derselben Vorschau ergibt ein Schreiben
    Angenommen eine zweite Anfrage zur selben Vorschau speichert ihr Schreiben, während diese ihr PDF erzeugt
    Wenn Erika die Vorschau speichert
    Dann erhält sie das Schreiben der zweiten Anfrage
    Und ihr eigenes PDF ist nicht mehr abgelegt

  Szenario: Eine neue Vorschau ergibt ein neues Schreiben
    Angenommen Erika hat die Vorschau bereits gespeichert
    Und Erika erzeugt eine neue Vorschau der "Mahnung" zum Kunden 4711
    Wenn Erika die neue Vorschau speichert
    Dann ist ein zweites Schreiben gespeichert
