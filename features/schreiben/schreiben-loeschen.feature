# language: de
# Status: proposed (KI-gestützt, wartet auf Review). Brücke zu den Tests:
# SchreibenloeschungTest im Modul usecases-service, ein Test je Szenario,
# benannt nach dem Szenariotitel. Die Ports sind dort durch Attrappen ersetzt.
Funktionalität: Schreiben löschen
  Ein Sachbearbeiter löscht ein gespeichertes Schreiben. Ob und wie gelöscht
  wird, entscheidet die Löschrichtlinie des Hosts (RB-10, ADR-007): hart mit dem
  PDF, logisch unter Erhalt des PDFs, oder gar nicht. Die Bibliothek fragt sie erst,
  wenn der Host das Löschen erlaubt hat. Ein Schreiben, das der Benutzer nicht
  löschen darf, gibt es für ihn nicht (QS-009).

  Grundlage:
    Angenommen zum Kunden 4711 ist ein Schreiben gespeichert
    Und Erika darf die Schreiben zum Kunden 4711 löschen

  Szenario: Hart gelöscht sind Schreiben und PDF entfernt
    Angenommen die Löschrichtlinie entscheidet "hart"
    Wenn Erika das Schreiben löscht
    Dann ist das Schreiben gelöscht
    Und das Schreiben ist entfernt
    Und das PDF ist entfernt

  Szenario: Logisch gelöscht bleibt das PDF, das Schreiben ist nicht mehr sichtbar
    Angenommen die Löschrichtlinie entscheidet "logisch"
    Wenn Erika das Schreiben löscht
    Dann ist das Schreiben gelöscht
    Und das Schreiben ist als logisch gelöscht gespeichert
    Und das PDF ist weiter abgelegt
    Und das Schreiben erscheint nicht mehr in der Übersicht

  Szenario: Eine gesperrte Löschung nennt den Grund
    Angenommen die Löschrichtlinie sperrt mit dem Grund "Aufbewahrungsfrist bis 2036"
    Wenn Erika das Schreiben löscht
    Dann ist das Löschen gesperrt mit dem Grund "Aufbewahrungsfrist bis 2036"
    Und das Schreiben ist unverändert

  Szenario: Ein nicht erlaubtes Schreiben verhält sich wie ein nicht vorhandenes
    Angenommen Max darf die Schreiben zum Kunden 4711 nicht löschen
    Wenn Max das Schreiben löscht
    Dann ist das Schreiben unbekannt
    Und die Löschrichtlinie wurde nicht gefragt
    Und das Schreiben ist unverändert

  Szenario: Ein unbekanntes Schreiben gibt es nicht
    Wenn Erika ein Schreiben mit unbekannter ID löscht
    Dann ist das Schreiben unbekannt
    Und der Host wurde nicht gefragt

  Szenario: Ein logisch gelöschtes Schreiben wird nicht erneut gelöscht
    Angenommen das Schreiben ist logisch gelöscht
    Wenn Erika das Schreiben löscht
    Dann ist das Schreiben unbekannt
    Und der Host wurde nicht gefragt
