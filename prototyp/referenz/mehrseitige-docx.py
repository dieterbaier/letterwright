"""Baut aus der DOCX-Vorgabe ein mehrseitiges Referenzschreiben für den Export mit Word.

Ersetzt den Absatz mit dem Ausfüllhinweis durch die Absätze aus mehrseitig-absaetze.txt,
jeweils in der Formatvorlage „Standard“. Aufruf im Ordner prototyp/:

    python3 -I referenz/mehrseitige-docx.py ../src/examples/Geschaeftsbrief.docx \
        referenz/mehrseitig-absaetze.txt <ziel.docx>
"""
import html
import re
import sys
import zipfile

quelle, absaetze_datei, ziel = sys.argv[1:]
with open(absaetze_datei, encoding="utf-8") as f:
    absaetze = [zeile.strip() for zeile in f if zeile.strip()]

with zipfile.ZipFile(quelle) as zin, zipfile.ZipFile(ziel, "w", zipfile.ZIP_DEFLATED) as zout:
    for eintrag in zin.infolist():
        daten = zin.read(eintrag.filename)
        if eintrag.filename == "word/document.xml":
            xml = daten.decode("utf-8")
            treffer = [p for p in re.findall(r"<w:p[ >].*?</w:p>", xml, re.S)
                       if "barrierefreie Bereitstellung" in "".join(re.findall(r"<w:t[^>]*>([^<]*)", p))]
            assert len(treffer) == 1, "Absatz mit dem Ausfüllhinweis nicht eindeutig gefunden"
            neu = "".join(f'<w:p><w:r><w:t xml:space="preserve">{html.escape(t, quote=False)}</w:t></w:r></w:p>'
                          for t in absaetze)
            daten = xml.replace(treffer[0], neu).encode("utf-8")
        zout.writestr(eintrag, daten)
