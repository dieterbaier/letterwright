// Prüft die Paged.js-Vorschau eines Schreibens in Chrome (Issue #3, ADR-014).
//
// Aufruf im Ordner prototyp/:  npm run pruefen -- <schreiben>
//
// 1. Host-Seite mit dem sandboxed iframe laden, auf die Fertigmeldung des iframe warten,
//    Konsolenmeldungen und CSP-Verstöße aus Host und iframe sammeln, Bildschirmfoto speichern.
// 2. Das Dokument des iframe allein laden, paginieren lassen und als PDF drucken. Dieses PDF
//    vergleicht `../gradlew vergleich` mit dem PDF von openhtmltopdf (QS-004).
import { chromium } from "playwright";
import { pathToFileURL } from "node:url";
import path from "node:path";

const schreiben = process.argv[2];
if (!schreiben) {
  console.error("Aufruf: npm run pruefen -- <schreiben>");
  process.exit(2);
}
const ausgabe = path.resolve("ausgabe");
const datei = (endung) => path.join(ausgabe, `${schreiben}${endung}`);

const browser = await chromium.launch({ channel: "chrome" });
let fehler = 0;

try {
  // 1. Host-Seite mit iframe
  const host = await browser.newPage({ viewport: { width: 1000, height: 900 } });
  const meldungen = [];
  host.on("console", (m) => meldungen.push(`[${m.type()}] ${m.text()}`));
  host.on("pageerror", (e) => meldungen.push(`[pageerror] ${e.message}`));
  await host.goto(pathToFileURL(datei("-vorschau.html")).href);
  await host.waitForFunction(
    () => document.getElementById("status").textContent.startsWith("Vorschau bereit"),
    null,
    { timeout: 60_000 },
  );
  const status = await host.textContent("#status");
  const hoehe = await host.$eval("#vorschau", (f) => f.style.height);
  const frame = host.frames().find((f) => f !== host.mainFrame());
  const seiten = await frame.$$eval(".pagedjs_page", (s) => s.length);
  // Zugriff des iframe auf den Host muss scheitern (eigene, opake Origin).
  const isoliert = await frame.evaluate(() => {
    try {
      return parent.document === undefined ? "kein Zugriff" : "ZUGRIFF MÖGLICH";
    } catch (e) {
      return "kein Zugriff (" + e.name + ")";
    }
  });
  await host.screenshot({ path: datei("-vorschau.png"), fullPage: true });
  console.log(`Host:     ${status}; iframe-Höhe ${hoehe}; ${seiten} Seiten im iframe`);
  console.log(`Sandbox:  Zugriff des iframe auf den Host: ${isoliert}`);
  const csp = meldungen.filter((m) => /Content Security Policy|Refused to/i.test(m));
  const andere = meldungen.filter((m) => !csp.includes(m) && !/^\[(log|info|debug)\]/.test(m));
  console.log(`CSP:      ${csp.length === 0 ? "keine Verstöße" : csp.length + " Verstöße"}`);
  csp.forEach((m) => console.log("  " + m));
  andere.forEach((m) => console.log("  " + m));
  fehler += csp.length + andere.length + (isoliert.startsWith("kein Zugriff") ? 0 : 1);

  // 2. Dokument allein paginieren und drucken
  const dok = await browser.newPage();
  await dok.goto(pathToFileURL(datei("-vorschau-dokument.html")).href);
  await dok.waitForFunction(() => window.vorschauFertig === true, null, { timeout: 60_000 });
  // Die Schriften müssen aus dem @font-face kommen, nicht aus dem System.
  const schriften = await dok.evaluate(async () => {
    await document.fonts.ready;
    return [...document.fonts].filter((f) => f.status === "loaded").map((f) => `${f.family} ${f.weight} ${f.style}`);
  });
  console.log(`Schriften geladen: ${schriften.join(", ")}`);
  await dok.pdf({ path: datei("-vorschau.pdf"), preferCSSPageSize: true, printBackground: true });
  console.log(`PDF der Vorschau: ${datei("-vorschau.pdf")}`);
} finally {
  await browser.close();
}
process.exit(fehler === 0 ? 0 : 1);
