// Prüft die Barrierefreiheit der Paged.js-Vorschau nach den Prüfkriterien aus ADR-014 (QS-003).
//
// Aufruf im Ordner prototyp/:  npm run barrierefreiheit -- <schreiben>
//
// Automatisch: axe-core auf der Host-Seite und im Dokument des iframe, Accessibility-Baum des
// iframe, Tab-Reihenfolge, „Aktualisieren“ mit Fokus und Statusmeldung, Darstellung bei 200 %.
// Die Prüfung mit NVDA und VoiceOver ersetzt das nicht.
import { chromium } from "playwright";
import { pathToFileURL } from "node:url";
import { readFile, writeFile } from "node:fs/promises";
import path from "node:path";

const schreiben = process.argv[2];
if (!schreiben) {
  console.error("Aufruf: npm run barrierefreiheit -- <schreiben>");
  process.exit(2);
}
const hostUrl = pathToFileURL(path.resolve("ausgabe", `${schreiben}-vorschau.html`)).href;
const axeQuelle = await readFile(path.resolve("node_modules/axe-core/axe.min.js"), "utf8");
let fehler = 0;

const browser = await chromium.launch({ channel: "chrome" });
try {
  const page = await browser.newPage({ viewport: { width: 1280, height: 900 } });
  await ladeHost(page);
  const frame = page.frames().find((f) => f !== page.mainFrame());

  // 1. axe-core. Playwright wertet über das DevTools-Protokoll aus; die CSP des iframe gilt dafür nicht.
  console.log("== axe-core (WCAG 2.2 A/AA)");
  fehler += await axe("Host", page.mainFrame(), { iframes: false });
  fehler += await axe("iframe", frame, {});

  // 2. Was ein Screenreader im iframe bekommt
  console.log("== Accessibility-Baum des iframe");
  const baum = await frame.locator("body").ariaSnapshot();
  const baumDatei = path.resolve("ausgabe", `${schreiben}-vorschau-aria.yaml`);
  await writeFile(baumDatei, baum);
  const zeilen = baum.split("\n");
  console.log(`  ${zeilen.length} Zeilen, gespeichert in ${path.relative(process.cwd(), baumDatei)}`);
  const landmarks = zeilen.filter((z) => /^\s*- (main|contentinfo|banner|complementary|navigation|region)\b/.test(z));
  console.log(`  Landmarks: ${landmarks.map((z) => z.trim().split(/[: ]/)[1]).join(", ") || "keine"}`);
  for (const text of ["Seite \\d+ von \\d+", "Öffnungszeiten", "Im Auftrag"]) {
    const n = zeilen.filter((z) => new RegExp(text).test(z)).length;
    console.log(`  „${text}“: ${n}x`);
  }
  const reihenfolge = ["Filiale Musterstadt", "Arial 12", "Sachgebiet", "Betreff", "Sehr geehrte", "Im Auftrag"]
    .map((t) => [t, zeilen.findIndex((z) => z.includes(t))]);
  const geordnet = reihenfolge.every(([, i], k) => i >= 0 && (k === 0 || i > reihenfolge[k - 1][1]));
  console.log(`  Lesereihenfolge ${reihenfolge.map(([t]) => t).join(" → ")}: ${geordnet ? "eingehalten" : "VERLETZT"}`);
  fehler += geordnet ? 0 : 1;

  // 3. Tastatur: Tab-Reihenfolge ab Seitenanfang, inklusive Rückweg aus dem iframe
  console.log("== Tastatur");
  await page.mouse.click(5, 5);
  const schritte = [];
  for (let i = 0; i < 6; i++) {
    await page.keyboard.press("Tab");
    schritte.push(await fokus(page, frame));
  }
  console.log(`  Tab: ${schritte.join(" → ")}`);
  const zurueck = [];
  for (let i = 0; i < 3; i++) {
    await page.keyboard.press("Shift+Tab");
    zurueck.push(await fokus(page, frame));
  }
  console.log(`  Shift+Tab: ${zurueck.join(" → ")}`);
  // Fokusfalle: Tab verlässt das iframe nicht mehr.
  const falle = schritte.slice(-3).every((s) => s.startsWith("iframe"));
  console.log(`  Fokusfalle: ${falle ? "JA" : "nein"}`);
  fehler += falle ? 1 : 0;

  // 4. Aktualisieren: Fokus bleibt, Status wird über aria-live angesagt
  console.log("== Aktualisieren");
  await page.evaluate(() => {
    window.statusVerlauf = [];
    new MutationObserver(() => window.statusVerlauf.push(document.getElementById("status").textContent))
      .observe(document.getElementById("status"), { childList: true, characterData: true, subtree: true });
  });
  await page.focus("#aktualisieren");
  await page.keyboard.press("Enter");
  await page.waitForFunction(() => window.statusVerlauf.some((s) => s.startsWith("Vorschau bereit")), null, { timeout: 60_000 });
  const verlauf = await page.evaluate(() => window.statusVerlauf);
  const nachher = await fokus(page, page.frames().find((f) => f !== page.mainFrame()));
  console.log(`  Statusmeldungen: ${verlauf.map((s) => `„${s}“`).join(" → ")}`);
  console.log(`  Fokus danach: ${nachher}`);
  fehler += nachher === "button#aktualisieren" ? 0 : 1;

  // 5. Zoom 200 %: 1280 px Fensterbreite entsprechen 640 CSS-Pixeln
  console.log("== Zoom 200 %");
  const zoom = await browser.newPage({ viewport: { width: 640, height: 450 }, deviceScaleFactor: 2 });
  await ladeHost(zoom);
  const zoomFrame = zoom.frames().find((f) => f !== zoom.mainFrame());
  const host = await zoom.evaluate(() => ({
    seite: document.documentElement.scrollWidth, fenster: document.documentElement.clientWidth,
    iframe: Math.round(document.getElementById("vorschau").getBoundingClientRect().width),
  }));
  const innen = await zoomFrame.evaluate(() => ({
    inhalt: document.documentElement.scrollWidth, sichtbar: document.documentElement.clientWidth,
    overflowX: getComputedStyle(document.documentElement).overflowX,
  }));
  console.log(`  Host: Seitenbreite ${host.seite} px bei ${host.fenster} px Fenster, iframe ${host.iframe} px breit`);
  console.log(`  iframe: Inhalt ${innen.inhalt} px bei ${innen.sichtbar} px sichtbar, overflow-x ${innen.overflowX}`);
  await zoom.screenshot({ path: path.resolve("ausgabe", `${schreiben}-vorschau-zoom200.png`) });
} finally {
  await browser.close();
}
console.log(fehler === 0 ? "\nKeine automatisch erkannten Verstöße." : `\n${fehler} automatisch erkannte Verstöße.`);
process.exit(fehler === 0 ? 0 : 1);

async function ladeHost(page) {
  await page.goto(hostUrl);
  await page.waitForFunction(() => document.getElementById("status").textContent.startsWith("Vorschau bereit"), null, { timeout: 60_000 });
}

async function axe(name, frame, optionen) {
  await frame.evaluate(axeQuelle);
  const ergebnis = await frame.evaluate((opt) => window.axe.run(document, {
    ...opt,
    runOnly: { type: "tag", values: ["wcag2a", "wcag2aa", "wcag21a", "wcag21aa", "wcag22aa", "best-practice"] },
    resultTypes: ["violations"],
  }), optionen);
  const schwer = ergebnis.violations.filter((v) => v.impact === "serious" || v.impact === "critical");
  console.log(`  ${name}: ${ergebnis.violations.length} Verstöße, davon ${schwer.length} serious/critical`);
  for (const v of ergebnis.violations) {
    console.log(`    [${v.impact}] ${v.id} (${v.nodes.length}x): ${v.help}`);
    v.nodes.slice(0, 2).forEach((n) => console.log(`        ${n.target.join(" ")}  ${n.html.slice(0, 100)}`));
  }
  return schwer.length;
}

async function fokus(page, frame) {
  const host = await page.evaluate(() => {
    // Ohne Fokus im Dokument liegt er in der Browser-Oberfläche; activeElement ist dann body.
    if (!document.hasFocus()) return "Browser";
    const e = document.activeElement;
    return e ? e.tagName.toLowerCase() + (e.id ? "#" + e.id : "") : "kein";
  });
  if (host !== "iframe#vorschau") return host;
  const innen = await frame.evaluate(() => {
    const e = document.activeElement;
    return e && e !== document.body ? e.tagName.toLowerCase() + (e.className ? "." + e.className.split(" ")[0] : "") : "body";
  });
  return `iframe(${innen})`;
}
