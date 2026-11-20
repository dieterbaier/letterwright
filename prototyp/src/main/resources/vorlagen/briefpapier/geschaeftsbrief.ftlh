<#--
  Briefpapier „Geschäftsbrief“ nach der DOCX-Vorgabe src/examples/Geschaeftsbrief.docx.

  Alle Maße stammen aus der DOCX (Seitenränder, Rahmen, Tabstopps, Formatvorlagen) und aus den
  Grundlinien des Word-PDFs, gemessen mit dem Task `positionen`. Die Kommentare im CSS nennen die
  Grundlinie, auf die eine Angabe zielt. Seitenkoordinaten sind von der linken oberen Ecke
  gemessen; der Inhaltsbereich der ersten Seite beginnt bei x = 30 mm, y = 10 mm.

  Rechenregel für eine Zeile mit Schriftgröße g und Zeilenhöhe h (Arial: Oberlänge 0,905 g,
  Unterlänge 0,212 g): Grundlinie = Oberkante + (h − 1,117 g) / 2 + 0,905 g.
-->
<!DOCTYPE html>
<html lang="de">
<head>
<meta charset="UTF-8"/>
<title>${schreiben.betreff}</title>
<meta name="description" content="Geschäftsbrief: ${schreiben.betreff}"/>
<#-- openhtmltopdf übernimmt „subject“ als dc:description; ohne sie warnt es vor fehlender PDF/UA-Konformität -->
<meta name="subject" content="Geschäftsbrief: ${schreiben.betreff}"/>
<style>
  /* Die DOCX-Ränder sind links 30 mm und rechts 20 mm. openhtmltopdf schneidet aber alles ab,
     was über den Inhaltsbereich der Seite hinausragt, und Word setzt Falzmarken und den rechten
     Rahmen in den Rand. Darum sind die seitlichen Seitenränder 0, und die Inhaltsblöcke rücken
     ein. Nicht der Body: Paged.js verschiebt dessen Inhalt in eigene Seitencontainer. */
  /* Word setzt den zusätzlichen Zeilenabstand des 1,5-zeiligen Fließtexts unter die Schrift, CSS
     verteilt ihn je zur Hälfte darüber und darunter. Die Zeilenbox liegt in CSS daher 1,15 mm
     höher zum Text als in Word. Damit der Text oben auf Folgeseiten wie in Word steht, ist der
     obere Rand um diesen Betrag kleiner. Unten braucht es keinen Ausgleich: Word lässt den
     Zeilenabstand der letzten Zeile in den unteren Rand ragen (Grundlinie 274,2 mm im
     mehrseitigen Referenzschreiben). Eine Verschiebung der Absätze mit position: relative setzt
     openhtmltopdf über Seitenumbrüche nicht verlässlich um. */
  @page {
    size: A4;
    /* Folgeseiten: zweiter Abschnitt der DOCX, oben 1474 twips (26 mm), unten 1134 twips (20 mm) */
    margin: 24.85mm 0 20mm 0;
    @top-left {
      content: "Seite " counter(page) " von " counter(pages);
      font-family: "Arial Narrow";
      font-size: 9pt;
      line-height: 3.75mm;
      vertical-align: top;
      /* Grundlinie 17,1 mm */
      padding-top: 14.1mm;
      padding-left: 30mm;
      /* Paged.js zählt den Einzug in die Breite, openhtmltopdf nicht; 180 mm reicht für beide */
      width: 180mm;
    }
  }
  @page :first {
    /* Erste Seite: erster Abschnitt der DOCX, oben 567 twips. Der feste Bereich .seite1 bezieht
       sich auf diese Kante, deshalb hier ohne Verschiebung. */
    margin-top: 10mm;
    /* content: "" statt none: In Paged.js 0.4.3 blendet ein Randfeld mit content: none alle
       folgenden Randfelder derselben Seite aus, hier die Fußzeile. */
    @top-left { content: ""; }
    @bottom-left {
      content: element(fusszeile);
      vertical-align: top;
      padding-left: 30mm;
      /* Paged.js zählt den Einzug in die Breite, openhtmltopdf nicht; 180 mm reicht für beide */
      width: 180mm;
    }
  }

  html { font-family: "Arial"; font-size: 12pt; color: #000; }
  body { margin: 0; }
  .seite1, .schreiben { margin: 0 20mm 0 30mm; }
  p { margin: 0; }

  /* Bereich der ersten Seite mit fest positionierten Teilen; der Fließtext beginnt darunter. */
  .seite1 { position: relative; height: 128.3mm; }
  .seite1 > * { position: absolute; margin: 0; }

  /* Formatvorlage „Logo links“, 14 pt fett. Grundlinie 21,0 mm */
  .filiale { left: 0; top: 6.45mm; font-size: 14pt; font-weight: bold; line-height: 5.675mm; }

  /* Logo 29,2 mm × 23,75 mm, rechtsbündig, Oberkante 11,9 mm */
  .logo { right: 0; top: 1.9mm; width: 29.2mm; height: 23.75mm; }

  /* Formatvorlage „kleine Schrift“, Arial Narrow 7 pt. Grundlinie 46,5 mm */
  .ruecksendeangabe { left: 0; top: 34.14mm; font-family: "Arial Narrow"; font-size: 7pt; line-height: 3mm; }

  /* Formatvorlage „Anschrift“, Rahmen 5143 twips breit. Grundlinie der ersten Zeile 56,9 mm */
  .anschrift { left: 0; top: 43mm; width: 90.7mm; font-style: normal; line-height: 4.86mm; }

  /* Rahmen „Block rechts“ und „Block2 rechts“ mit 142 twips Einzug, Arial Narrow 9 pt */
  .infoblock { font-family: "Arial Narrow"; font-size: 9pt; }
  .infoblock p { margin-bottom: 6pt; }
  .infoblock .label { display: inline-block; width: 12.5mm; }
  /* x = 131,8 mm, Grundlinie der ersten Zeile 46,9 mm, Zeilenabstand 4,0 mm */
  .block1 { left: 101.8mm; top: 33.8mm; width: 60mm; line-height: 4mm; }
  .block1 p { margin-bottom: 2mm; }
  /* x = 132,0 mm, Grundlinie der ersten Zeile 86,9 mm, Zeilenabstand 3,75 mm */
  .block2 { left: 102mm; top: 73.9mm; width: 60mm; line-height: 3.75mm; }
  /* Formatvorlage „InfoBlock2 rechts“ ist um 2,5 pt hochgestellt; Word setzt die Zeilen dadurch 4,0 mm auseinander */
  .block2 p.bank { line-height: 3.95mm; margin-top: -0.1mm; }

  /* Falz- und Lochmarken der Kopfzeile der ersten Seite: 2 mm lang, x = 10,2 mm,
     y = 102,7 mm, 148,2 mm und 199,8 mm (gemessen im Word-PDF) */
  .marke { left: -19.8mm; width: 2mm; height: 0; border-top: 0.75pt solid #000; }
  .marke.falz-oben { top: 92.7mm; }
  .marke.loch { top: 138.2mm; }
  .marke.falz-unten { top: 189.8mm; }

  /* Formatvorlage „Betreffblock“: hängender Einzug 851 twips (15 mm).
     Grundlinie der ersten Zeile 142,2 mm, Zeilenabstand einzeilig Arial 12 */
  .betreffblock { margin-bottom: 13.6mm; }
  /* openhtmltopdf setzt negativen text-indent nicht um; daher negativer Rand am Label.
     line-height: 1 hält das Inline-Block-Label innerhalb der Zeile, sonst wächst der Zeilenabstand. */
  /* Der Betreff ist die Überschrift des Schreibens (h1) für Screenreader und PDF/UA, sieht aber
     aus wie die übrigen Zeilen des Betreffblocks. */
  .betreffblock h1 { font-size: inherit; font-weight: normal; margin: 0; }
  .betreffblock p, .betreffblock h1 { line-height: 4.86mm; padding-left: 15mm; }
  .betreffblock .label { display: inline-block; width: 15mm; margin-left: -15mm; line-height: 1; font-family: "Arial Narrow"; font-size: 9pt; }
  .betreffblock .wert-fett { font-weight: bold; }
  .betreffblock p.hinweis { font-family: "Arial Narrow"; font-size: 9pt; line-height: 3.8mm; }

  /* Formatvorlage „Standard“: Arial 12, 1,5-zeilig (1,5 × 1,1499 × 12 pt = 7,30 mm), 6 pt davor
     und danach, die Word zu 6 pt zwischen zwei Absätzen zusammenfasst. Hier nur als Abstand
     danach: Oben auf einer Folgeseite verwerfen Word und openhtmltopdf den Abstand davor,
     Paged.js nicht. keepLines: Absatz nicht über Seiten trennen; keine Absatzkontrolle.
     Zum Versatz der Zeilenbox gegenüber Word siehe @page. */
  .inhalt p { line-height: 7.3mm; margin: 0 0 6pt; page-break-inside: avoid; orphans: 1; widows: 1; }
  /* Formatvorlage „Unterzeichnen“: 600 twips davor */
  .inhalt p.gruss { margin-top: 10.6mm; }

  /* Fußzeile nur auf der ersten Seite. Grundlinie 286,3 mm, Tabstopp bei 105 mm */
  .fusszeile { position: running(fusszeile); font-family: "Arial Narrow"; font-size: 9pt; line-height: 3.75mm; padding-top: 6.33mm; }
  .fusszeile .links { display: inline-block; width: 105mm; }
</style>
</head>
<body>
  <footer class="fusszeile">
    <#-- ohne Leerraum zwischen den Spalten, sonst verschiebt ein Leerzeichen die rechte um 0,7 mm -->
    <span class="links">Öffnungszeiten: 09:00 – 15:00 Uhr</span><span>Homepage: www.superfirma.de</span>
  </footer>

  <div class="seite1">
    <p class="filiale">Filiale Musterstadt</p>
    <img class="logo" src="${logo}" alt="Logo der Filiale Musterstadt"/>

    <p class="ruecksendeangabe">Filiale Musterstadt, Postfach 00 00 00, 12345 Musterstadt</p>
    <address class="anschrift">
      <#list empfaenger.zeilen as zeile>${zeile}<#sep><br/></#sep></#list>
    </address>

    <div class="infoblock block1">
      <p>${absender.sachgebiet}<br/><strong>${absender.sachgebietHinweis}</strong></p>
      <p>Bearbeitet von:${absender.kuerzel}<br/>${absender.name}</p>
      <p>Gebäude:<br/>${absender.strasse}<br/>${absender.ort}</p>
    </div>
    <div class="infoblock block2">
      <p><span class="label">Telefon:</span>${absender.telefon}<br/><span class="label">Fax:</span>${absender.fax}<br/><span class="label">E-Mail:</span>${absender.email}</p>
      <p class="bank">Bankverbindung:<br/>IBAN DE00 0000 0000 0000 0000 00<br/>BIC XXXXXXXXXXX</p>
      <p><span class="label">Datum:</span>${schreiben.datum}</p>
    </div>

    <div class="marke falz-oben" aria-hidden="true"></div>
    <div class="marke loch" aria-hidden="true"></div>
    <div class="marke falz-unten" aria-hidden="true"></div>
  </div>

  <#-- div mit role="main" statt <main>: Paged.js klont das Element auf jeder Folgeseite, und die
       Vorschau entfernt die Rolle an den Fortsetzungen. Ein <main> behielte sie (drei Landmarks). -->
  <div class="schreiben" role="main">
    <div class="betreffblock">
      <h1><span class="label">Betreff</span><span class="wert-fett">${schreiben.betreff}</span></h1>
      <p><span class="label">Bezug</span>${schreiben.bezug}</p>
      <p><span class="label">Anlagen</span>${schreiben.anlagen}</p>
      <p><span class="label">GZ</span><span class="wert-fett">${schreiben.geschaeftszeichen}</span></p>
      <p class="hinweis">(bei Antwort bitte angeben)</p>
    </div>
    <div class="inhalt">
      ${inhalt?no_esc}
    </div>
  </div>
</body>
</html>
