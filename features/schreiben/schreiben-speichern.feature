# language: de
# Status: proposed (KI-gestützt, wartet auf Review). Brücke zu den Tests:
# SchreibenspeicherungTest im Modul usecases-service, ein Test je Szenario,
# benannt nach dem Szenariotitel. Die Ports sind dort durch Attrappen ersetzt.
Funktionalität: Schreiben speichern
  Ein Sachbearbeiter speichert eine geprüfte Vorschau als Schreiben. Gespeichert
  wird genau das PDF, das aus dem Snapshot der Vorschau entsteht, und nur, wenn es
  konform ist (ADR-005, ADR-007). Die Bibliothek prüft die Berechtigung erneut und
  liest den Datenkontext erneut; hat er sich seit der Vorschau geändert, ist eine
  neue Vorschau nötig. Aus einem Snapshot entsteht höchstens ein Schreiben: Das
  Speichern verbraucht ihn und gibt seinen Datenkontext frei (ADR-021).

  Grundlage:
    Angenommen Erika hat vor 10 Minuten eine Vorschau der "Mahnung" zum Kunden 4711 erzeugt
    Und Erika darf zum Kunden 4711 aus der "Mahnung" ein Schreiben erzeugen
    Und der Host liefert zum Kunden 4711 weiter denselben Datenkontext

  Szenario: Aus einer unveränderten Vorschau wird das geprüfte PDF gespeichert
    Wenn Erika die Vorschau speichert
    Dann ist ein Schreiben gespeichert
    Und sein PDF entstand aus den Versionen und dem Datenkontext des Snapshots
    Und es trägt die Prüfsumme dieses PDFs
    Und der Snapshot ist verbraucht und hält keinen Datenkontext mehr

  Szenario: Wird die Berechtigung beim Speichern entzogen, wird nichts persistiert
    Angenommen Erika darf inzwischen aus der "Mahnung" kein Schreiben mehr erzeugen
    Wenn Erika die Vorschau speichert
    Dann ist das Speichern nicht erlaubt
    Und es wurde kein PDF erzeugt
    Und nichts ist gespeichert oder abgelegt
    Und der Snapshot ist offen und behält seinen Datenkontext

  Szenario: Ein nicht konformes PDF wird nicht gespeichert
    Angenommen die Konformitätsprüfung findet "PDF/UA-1: Bild ohne Alternativtext"
    Wenn Erika die Vorschau speichert
    Dann ist das PDF nicht konform mit dem Befund "PDF/UA-1: Bild ohne Alternativtext"
    Und nichts ist gespeichert oder abgelegt
    Und der Snapshot ist offen und behält seinen Datenkontext

  Szenario: Ein nicht prüfbares PDF wird nicht gespeichert
    Angenommen die Konformitätsprüfung kann das PDF nicht prüfen
    Wenn Erika die Vorschau speichert
    Dann schlägt das Speichern mit einem Fehler fehl
    Und nichts ist gespeichert oder abgelegt
    Und der Snapshot ist offen und behält seinen Datenkontext

  Szenario: Nach einer Änderung des Datenkontexts wird kein PDF erzeugt
    Angenommen der Host liefert zum Kunden 4711 inzwischen einen anderen Datenkontext
    Wenn Erika die Vorschau speichert
    Dann hat sich der Datenkontext geändert
    Und es wurde kein PDF erzeugt
    Und nichts ist gespeichert oder abgelegt
    Und der Snapshot ist offen und behält seinen Datenkontext

  Szenario: Nach einer neuen Vorschau gelingt das Speichern
    Angenommen der Host liefert zum Kunden 4711 inzwischen einen anderen Datenkontext
    Und Erika hat die Vorschau danach aktualisiert
    Wenn Erika die aktualisierte Vorschau speichert
    Dann ist ein Schreiben mit dem neuen Datenkontext gespeichert

  Szenario: Ein fremder Snapshot verhält sich wie ein nicht vorhandener
    Wenn Max die Vorschau von Erika speichert
    Dann ist der Snapshot unbekannt
    Und der Host wurde nicht gefragt

  Szenario: Auch nach dem Speichern bleibt ein fremder Snapshot verborgen
    Angenommen Erika hat die Vorschau bereits gespeichert
    Wenn Max die Vorschau von Erika speichert
    Dann ist der Snapshot unbekannt

  Szenario: Ein abgelaufener Snapshot wird nicht gespeichert
    Angenommen seit der Vorschau sind 30 Minuten vergangen
    Wenn Erika die Vorschau speichert
    Dann ist der Snapshot abgelaufen
    Und der Host wurde nicht gefragt
    Und nichts ist gespeichert oder abgelegt
    Und der Snapshot ist offen und behält seinen Datenkontext

  Szenario: Wiederholtes Speichern derselben Vorschau ergibt kein zweites Schreiben
    Angenommen Erika hat die Vorschau bereits gespeichert, aber die Antwort nicht erhalten
    Wenn Erika die Vorschau erneut speichert
    Dann erhält sie das bereits gespeicherte Schreiben
    Und der Host wurde nicht erneut nach dem Datenkontext gefragt
    Und es wurde kein weiteres PDF erzeugt

  Szenario: Bleibt der Ausgang des Speicherns ungewiss, bleibt das PDF erhalten
    Angenommen die Persistenz speichert das Schreiben, aber ihre Bestätigung geht verloren
    Wenn Erika die Vorschau speichert
    Dann schlägt das Speichern mit einem Fehler fehl
    Und das PDF ist weiter abgelegt
    Wenn Erika die Vorschau erneut speichert
    Dann erhält sie das gespeicherte Schreiben mit genau diesem PDF

  Szenario: Speichert Erika, während sie die Vorschau aktualisiert, findet eine Wiederholung das Schreiben
    Angenommen Erika aktualisiert ihre Vorschau, und der bisherige Snapshot ist dabei schon gelesen
    Und bevor er gelöscht wird, speichert eine andere Anfrage diese Vorschau
    Wenn das Aktualisieren den bisherigen Snapshot löschen will
    Dann bleibt der verbrauchte Snapshot bestehen
    Und eine Wiederholung des Speicherns erhält das gespeicherte Schreiben

  Szenario: Ersetzt eine gleichzeitige Vorschau den Snapshot, wird nichts gespeichert
    Angenommen Erika ersetzt die Vorschau in einem anderen Tab, während sie speichert
    Wenn das Speichern den Snapshot verbrauchen will
    Dann ist der Snapshot unbekannt
    Und nichts ist gespeichert oder abgelegt

  Szenario: Gleichzeitiges Speichern derselben Vorschau ergibt ein Schreiben
    Angenommen eine zweite Anfrage zur selben Vorschau verbraucht den Snapshot, während diese ihr PDF erzeugt
    Wenn Erika die Vorschau speichert
    Dann erhält sie das Schreiben der zweiten Anfrage
    Und ihr eigenes PDF ist nicht mehr abgelegt

  Szenario: Eine neue Vorschau ergibt ein neues Schreiben
    Angenommen Erika hat die Vorschau bereits gespeichert
    Und Erika erzeugt eine neue Vorschau der "Mahnung" zum Kunden 4711
    Wenn Erika die neue Vorschau speichert
    Dann ist ein zweites Schreiben gespeichert

  Szenariogrundriss: Ist das Schreiben gelöscht, findet eine Wiederholung des Speicherns nichts mehr
    Angenommen Erika hat die Vorschau bereits gespeichert
    Und das Schreiben ist danach <Art> gelöscht worden
    Wenn Erika die Vorschau erneut speichert
    Dann ist der Snapshot unbekannt
    Und es wurde kein weiteres PDF erzeugt

    Beispiele:
      | Art     |
      | hart    |
      | logisch |

  Szenariogrundriss: Wird das Schreiben einer gleichzeitigen Anfrage gelöscht, bevor diese speichert, wird nichts gespeichert
    Angenommen eine zweite Anfrage zur selben Vorschau verbraucht den Snapshot, während diese ihr PDF erzeugt
    Und das Schreiben der zweiten Anfrage wird danach <Art> gelöscht
    Wenn Erika die Vorschau speichert
    Dann ist der Snapshot unbekannt
    Und ihr eigenes PDF ist nicht mehr abgelegt

    Beispiele:
      | Art     |
      | hart    |
      | logisch |
