# language: de
# Status: proposed (KI-gestützt, wartet auf Review). Brücke zu den Tests:
# VorschauErzeugenTest im Modul usecases-service, ein Test je Szenario,
# benannt nach dem Szenariotitel. Die Ports sind dort durch Attrappen ersetzt.
# Ablage nach dem Vorschlag in #46 (features/<bereich>/).
Funktionalität: Vorschau erzeugen
  Ein Sachbearbeiter wählt zu einem fachlichen Bezug ein Standardschreiben und
  sieht eine Vorschau, bevor er das Schreiben speichert. Die Bibliothek prüft
  zuerst selbst, ob das Standardschreiben zum Bezug passt (ADR-020), und fragt
  dann den Host nach der Berechtigung. Erst mit ihr prüft sie, ob es
  veröffentlichte Versionen gibt, und erst dann liefert der Host den
  Datenkontext. Jede Vorschau hält ihren Stand in einem Snapshot fest, der
  30 Minuten verwendbar ist (ADR-007).

  Grundlage:
    Angenommen das Standardschreiben "Mahnung" gehört zum Bezugstyp "Kunde"
    Und seine neueste veröffentlichte Version ist 2, und es hat einen Entwurf 3
    Und sein Briefpapier hat die veröffentlichte Version 1
    Und der Host liefert zum Kunden 4711 den Datenkontext "name" = "Erika Mustermann GmbH"

  Szenario: Mit Berechtigung entsteht eine Vorschau mit Snapshot
    Angenommen Erika darf zum Kunden 4711 aus der "Mahnung" ein Schreiben erzeugen
    Wenn Erika die Vorschau der "Mahnung" zum Kunden 4711 erzeugt
    Dann zeigt die Vorschau "Erika Mustermann GmbH"
    Und ein Snapshot ist gespeichert, gebunden an Erika, den Kunden 4711, Version 1 des Briefpapiers und Version 2 der "Mahnung"
    Und der Snapshot läuft in 30 Minuten ab

  Szenario: Ohne Berechtigung wird kein Datenkontext angefragt
    Angenommen Erika darf zum Kunden 4711 aus der "Mahnung" kein Schreiben erzeugen
    Wenn Erika die Vorschau der "Mahnung" zum Kunden 4711 erzeugt
    Dann ist die Vorschau nicht erlaubt
    Und der Host wurde nicht nach dem Datenkontext gefragt
    Und nichts wurde gerendert
    Und kein Snapshot ist gespeichert

  Szenario: Zu einem Bezug anderen Typs wird der Host nicht gefragt
    Wenn Erika die Vorschau der "Mahnung" zum Lieferanten 0815 erzeugt
    Dann passt das Standardschreiben nicht zum Bezug
    Und der Host wurde weder nach der Berechtigung noch nach dem Datenkontext gefragt
    Und kein Snapshot ist gespeichert

  Szenario: Zu einem unbekannten Standardschreiben gibt es keine Vorschau
    Wenn Erika die Vorschau eines unbekannten Standardschreibens zum Kunden 4711 erzeugt
    Dann ist das Standardschreiben unbekannt
    Und der Host wurde weder nach der Berechtigung noch nach dem Datenkontext gefragt

  Szenariogrundriss: Ohne veröffentlichte Version gibt es keine Vorschau
    Angenommen <vorlage> hat keine veröffentlichte Version
    Und Erika darf zum Kunden 4711 aus der "Mahnung" ein Schreiben erzeugen
    Wenn Erika die Vorschau der "Mahnung" zum Kunden 4711 erzeugt
    Dann hat die Vorschau keine veröffentlichte Version
    Und der Host wurde nach der Berechtigung, aber nicht nach dem Datenkontext gefragt
    Und kein Snapshot ist gespeichert

    Beispiele:
      | vorlage                |
      | das Standardschreiben  |
      | das Briefpapier        |

  Szenariogrundriss: Ohne Berechtigung bleibt verborgen, ob eine Version veröffentlicht ist
    Angenommen <vorlage> hat keine veröffentlichte Version
    Und Erika darf zum Kunden 4711 aus der "Mahnung" kein Schreiben erzeugen
    Wenn Erika die Vorschau der "Mahnung" zum Kunden 4711 erzeugt
    Dann ist die Vorschau nicht erlaubt
    Und der Host wurde nicht nach dem Datenkontext gefragt
    Und nichts wurde gerendert
    Und kein Snapshot ist gespeichert

    Beispiele:
      | vorlage                |
      | das Standardschreiben  |
      | das Briefpapier        |

  Szenario: Eine aktualisierte Vorschau ersetzt den eigenen Snapshot
    Angenommen Erika darf zum Kunden 4711 aus der "Mahnung" ein Schreiben erzeugen
    Und Erika hat vor 20 Minuten eine Vorschau der "Mahnung" zum Kunden 4711 erzeugt
    Wenn Erika diese Vorschau aktualisiert
    Dann ist der bisherige Snapshot gelöscht
    Und der neue Snapshot läuft in 30 Minuten ab

  Szenario: Ein fremder Snapshot verhält sich wie ein nicht vorhandener
    Angenommen Erika darf zum Kunden 4711 aus der "Mahnung" ein Schreiben erzeugen
    Und Max hat eine Vorschau der "Mahnung" zum Kunden 4711 erzeugt
    Wenn Erika eine Vorschau aktualisiert und dabei den Snapshot von Max angibt
    Dann entsteht für Erika eine neue Vorschau
    Und der Snapshot von Max ist unverändert gespeichert
