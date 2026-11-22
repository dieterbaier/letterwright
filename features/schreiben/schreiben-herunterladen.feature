# language: de
# Status: proposed (KI-gestützt, wartet auf Review). Brücke zu den Tests:
# SchreibenabrufTest im Modul usecases-service, ein Test je Szenario,
# benannt nach dem Szenariotitel. Die Ports sind dort durch Attrappen ersetzt.
Funktionalität: Schreiben herunterladen
  Ein Sachbearbeiter lädt das PDF eines gespeicherten Schreibens herunter. Er
  bekommt genau das PDF, das beim Speichern geprüft wurde (ADR-007, QS-008). Ein
  Schreiben, das er nicht herunterladen darf, gibt es für ihn nicht (QS-009).

  Grundlage:
    Angenommen zum Kunden 4711 ist ein Schreiben gespeichert
    Und Erika darf die Schreiben zum Kunden 4711 herunterladen

  Szenario: Der Download liefert das unveränderte PDF
    Wenn Erika das Schreiben herunterlädt
    Dann erhält sie das PDF mit der Prüfsumme vom Speichern

  Szenario: Ein nicht erlaubtes Schreiben verhält sich wie ein nicht vorhandenes
    Angenommen Max darf die Schreiben zum Kunden 4711 nicht herunterladen
    Wenn Max das Schreiben herunterlädt
    Dann ist das Schreiben unbekannt
    Und es wurde kein PDF gelesen

  Szenario: Ein unbekanntes Schreiben gibt es nicht
    Wenn Erika ein Schreiben mit unbekannter ID herunterlädt
    Dann ist das Schreiben unbekannt
    Und der Host wurde nicht gefragt

  Szenario: Ein logisch gelöschtes Schreiben gibt es nicht
    Angenommen das Schreiben ist logisch gelöscht
    Wenn Erika das Schreiben herunterlädt
    Dann ist das Schreiben unbekannt
    Und der Host wurde nicht gefragt
    Und es wurde kein PDF gelesen

  Szenario: Ein verändertes PDF wird nicht ausgeliefert
    Angenommen die Dokumentablage liefert andere Bytes als beim Speichern
    Wenn Erika das Schreiben herunterlädt
    Dann schlägt der Download mit einem Fehler fehl
