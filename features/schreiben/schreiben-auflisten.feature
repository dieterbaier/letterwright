# language: de
# Status: proposed (KI-gestützt, wartet auf Review). Brücke zu den Tests:
# SchreibenAuflistenTest im Modul usecases-service, ein Test je Szenario,
# benannt nach dem Szenariotitel. Die Ports sind dort durch Attrappen ersetzt.
# Gefiltert, sortiert und gezählt wird im SchreibenRepository; die Attrappe
# hält dessen Vertrag ein. Den Nachweis für die Datenbank führt der Adapter (#6).
Funktionalität: Schreiben auflisten
  Ein Sachbearbeiter sieht die gespeicherten Schreiben zu einem fachlichen Bezug,
  seitenweise und die neuesten zuerst. Ob er sie sehen darf, entscheidet der Host
  je Bezug. Was er nicht sehen darf, zählt weder in die Seite noch in die
  Gesamtzahl (QS-009).

  Grundlage:
    Angenommen zum Kunden 4711 sind 3 Schreiben gespeichert
    Und Erika darf die Schreiben zum Kunden 4711 ansehen

  Szenario: Die Übersicht zeigt die Schreiben zum Bezug seitenweise, die neuesten zuerst
    Wenn Erika die erste Seite mit 2 Einträgen zum Kunden 4711 abruft
    Dann sieht sie die 2 neuesten Schreiben, das neueste zuerst
    Und die Gesamtzahl ist 3 auf 2 Seiten

  Szenario: Nicht sichtbare Schreiben zählen weder in die Seite noch in die Gesamtzahl
    Angenommen zum Kunden 0815 sind 2 Schreiben gespeichert
    Und das neueste Schreiben zum Kunden 4711 ist logisch gelöscht
    Wenn Erika die erste Seite mit 2 Einträgen zum Kunden 4711 abruft
    Dann sieht sie die 2 übrigen Schreiben zum Kunden 4711
    Und die Gesamtzahl ist 2 auf 1 Seite

  Szenario: Ohne Berechtigung zum Ansehen wird nichts gelesen
    Angenommen Erika darf die Schreiben zum Kunden 4711 nicht ansehen
    Wenn Erika die erste Seite mit 2 Einträgen zum Kunden 4711 abruft
    Dann ist die Übersicht nicht erlaubt
    Und es wurden keine Schreiben gelesen oder gezählt
