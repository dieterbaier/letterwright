# language: de
# Status: proposed (KI-gestützt, wartet auf Review). Brücke zu den Tests: ein Test
# je Szenario, benannt nach dem Szenariotitel. Die ersten vier Szenarien prüft
# SchreibenspeicherungTest im Modul usecases-service gegen Attrappen der Ports,
# das letzte StandardschreibenAutoKonfigurationTest im Modul spring-boot-starter.
# Dass der Snapshot nach einer Ablehnung offen bleibt, leistet der Rollback an der
# äußeren Grenze; das prüft der REST-Endpunkt in #10.
Funktionalität: Rückruf nach dem Speichern
  Speichert ein Sachbearbeiter über die Oberfläche der Bibliothek, kann die
  Host-Anwendung im selben Commit eigene Daten ändern oder das Speichern mit einem
  Grund ablehnen (ADR-023). Dafür stellt sie optional den Rückruf NachDemSpeichern
  bereit. Er läuft nach den Datenbankänderungen der Bibliothek und vor dem Commit,
  und nur, wenn der aktuelle Aufruf das Schreiben anlegt. Über die Java-API gibt
  es keinen Rückruf.

  Grundlage:
    Angenommen Erika hat vor 10 Minuten eine Vorschau der "Mahnung" zum Kunden 4711 erzeugt
    Und Erika darf zum Kunden 4711 aus der "Mahnung" ein Schreiben erzeugen
    Und der Host liefert zum Kunden 4711 weiter denselben Datenkontext

  Szenario: Legt das Speichern ein Schreiben an, erhält der Host einen Rückruf
    Wenn Erika die Vorschau speichert
    Dann ist ein Schreiben gespeichert
    Und der Host erhielt genau einen Rückruf mit diesem Schreiben und Erika

  Szenario: Eine Wiederholung löst keinen weiteren Rückruf aus
    Angenommen Erika hat die Vorschau bereits gespeichert
    Wenn Erika die Vorschau erneut speichert
    Dann erhält sie das bereits gespeicherte Schreiben
    Und der Host erhielt keinen weiteren Rückruf

  Szenario: Der Host lehnt das neue Schreiben mit einem Grund ab
    Angenommen der Host lehnt jedes neue Schreiben ab mit dem Grund "Die Aufgabe ist bereits erledigt"
    Wenn Erika die Vorschau speichert
    Dann ist das Speichern vom Host abgelehnt mit dem Grund "Die Aufgabe ist bereits erledigt"

  Szenario: Ein Fehler im Rückruf ist ein technischer Fehler
    Angenommen der Rückruf des Hosts scheitert mit einem Fehler
    Wenn Erika die Vorschau speichert
    Dann schlägt das Speichern mit diesem Fehler fehl
    Und die Bibliothek hat den Rückruf nicht wiederholt

  Szenario: Ohne Rückruf des Hosts wird gespeichert wie bisher
    Angenommen die Host-Anwendung stellt keinen Rückruf bereit
    Wenn Erika die Vorschau speichert
    Dann wird das Schreiben ohne Rückruf angenommen
