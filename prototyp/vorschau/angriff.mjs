// Prüft das bösartige Testschreiben in der Vorschau (ADR-014): Kein Angriff darf Code ausführen,
// einen Dialog öffnen oder eine Anfrage nach außen stellen. Ein erlaubter Link öffnet einen neuen
// Tab und navigiert das iframe nicht.
//
// Aufruf im Ordner prototyp/:
//   ../gradlew run --args="boesartig" && npm run angriff -- boesartig
// Gegenprobe ohne Bereinigung; dann muss allein die CSP blockieren, und ihre Verstöße werden sichtbar:
//   BEREINIGUNG=aus ../gradlew run --args="boesartig" && npm run angriff -- boesartig --gegenprobe
import { chromium } from "playwright";
import { pathToFileURL } from "node:url";
import path from "node:path";

const schreiben = process.argv[2];
const gegenprobe = process.argv.includes("--gegenprobe");
if (!schreiben) {
  console.error("Aufruf: npm run angriff -- <schreiben> [--gegenprobe]");
  process.exit(2);
}

const browser = await chromium.launch({ channel: "chrome" });
const ausgefuehrt = [];
const dialoge = [];
const anfragen = [];
const csp = [];
const navigationen = [];
const neueTabs = [];
let fehler = 0;
try {
  const page = await browser.newPage();
  page.on("console", (m) => {
    if (m.text().includes("ANGRIFF")) ausgefuehrt.push(m.text());
    if (/Content Security Policy|Refused to/i.test(m.text())) csp.push(m.text());
  });
  page.on("dialog", async (d) => { dialoge.push(d.message()); await d.dismiss(); });
  // Navigiert das iframe weg vom Schreiben (z. B. per meta refresh), zeigt es fremden Inhalt.
  page.on("framenavigated", (f) => {
    if (f !== page.mainFrame() && !/^(about:srcdoc|file:)/.test(f.url())) navigationen.push(f.url());
  });
  // Nichts verlässt den Rechner: Jede Anfrage außer file: und data: wird protokolliert und abgebrochen.
  // Neue Tabs (Links aus dem Schreiben) im selben Kontext: Anfragen dort ebenfalls abbrechen.
  await page.context().route("**/*", (route) => {
    const url = route.request().url();
    if (url.startsWith("file:") || url.startsWith("data:")) return route.continue();
    anfragen.push(url);
    // Anfragen aus einem anderen Tab als der Vorschau stammen von einem geöffneten Link.
    // Bei der ersten Navigation eines neuen Tabs kennt Playwright den Frame noch nicht.
    let herkunft = null;
    try { herkunft = route.request().frame().page(); } catch { herkunft = null; }
    if (herkunft !== page) neueTabs.push(url);
    return route.abort();
  });
  await page.goto(pathToFileURL(path.resolve("ausgabe", `${schreiben}-vorschau.html`)).href);
  await page.waitForFunction(() => document.getElementById("status").textContent.startsWith("Vorschau bereit"), null, { timeout: 60_000 });
  await page.waitForTimeout(1500);
  const frame = page.frames().find((f) => f !== page.mainFrame());

  // Was im Dokument des iframe ankommt
  const leer = { skripte: -1, eventhandler: [], links: [], eingebettet: [], externeBilder: [] };
  const dom = await frame.evaluate(() => ({
    skripte: document.scripts.length,
    eventhandler: [...document.querySelectorAll("*")].flatMap((e) => [...e.attributes].filter((a) => a.name.startsWith("on")).map((a) => `${e.localName}[${a.name}]`)),
    links: [...document.querySelectorAll("a[href]")].map((a) => a.getAttribute("href")),
    eingebettet: [...document.querySelectorAll("iframe, object, embed, svg, form, base, meta[http-equiv=refresh]")].map((e) => e.localName),
    externeBilder: [...document.querySelectorAll("img[src]")].map((i) => i.getAttribute("src")).filter((s) => !s.startsWith("data:")),
  })).catch((e) => { console.log(`Dokument im iframe nicht auswertbar: ${e.message.split("\n")[0]}`); return leer; });
  console.log(`Dokument im iframe: ${dom.skripte} Skripte (Paged.js und Konfiguration: 2)`);
  console.log(`  Eventhandler: ${dom.eventhandler.join(", ") || "keine"}`);
  console.log(`  Links: ${dom.links.join(", ") || "keine"}`);
  console.log(`  Eingebettete oder aktive Elemente: ${dom.eingebettet.join(", ") || "keine"}`);
  console.log(`  Bilder ohne data:-Quelle: ${dom.externeBilder.length}`);

  // Bedienen: über jeden Absatz fahren, jeden Link anklicken (Navigation im iframe verhindern wir nicht)
  for (const p of await frame.locator(".inhalt p, .inhalt img").all()) {
    await p.hover({ force: true }).catch(() => {});
    await p.click({ force: true }).catch(() => {});
  }
  const vorKlicks = navigationen.length;
  for (const a of await frame.locator("a").all()) {
    await a.click({ force: true, noWaitAfter: true, timeout: 2000 }).catch(() => {});
  }
  await page.waitForTimeout(1500);
  // Ein Link darf das iframe nicht navigieren; erlaubte Links öffnen einen neuen Tab.
  const ohneKlick = navigationen.slice(0, vorKlicks);
  const durchKlick = navigationen.slice(vorKlicks);

  console.log(`Ausgeführt: ${ausgefuehrt.length ? ausgefuehrt.join(" | ") : "nichts"}`);
  console.log(`Dialoge: ${dialoge.length ? dialoge.join(" | ") : "keine"}`);
  console.log(`Anfragen nach außen: ${anfragen.length ? anfragen.join(", ") : "keine"}`);
  console.log(`Navigationen des iframe ohne Klick: ${ohneKlick.length ? ohneKlick.join(", ") : "keine"}`);
  console.log(`Navigationen des iframe durch Klick: ${durchKlick.length ? durchKlick.join(", ") : "keine"}`);
  console.log(`Neue Tabs: ${[...new Set(neueTabs)].join(", ") || "keine"}`);
  console.log(`CSP-Verstöße: ${csp.length}`);
  csp.slice(0, 6).forEach((m) => console.log("  " + m.slice(0, 140)));

  fehler += ausgefuehrt.length + dialoge.length;
  if (gegenprobe) {
    // Ohne Bereinigung kommen die Angriffe im Browser an; die CSP muss den Code sichtbar blockieren.
    if (csp.length === 0) { console.log("Gegenprobe: keine CSP-Verstöße, die Angriffe kamen gar nicht an?"); fehler++; }
    // Eine Weiterleitung per meta refresh hält die CSP nicht auf; das kann nur die Bereinigung.
    if (ohneKlick.length) console.log("Ohne Bereinigung erwartet: Das iframe wurde weitergeleitet (meta refresh); die CSP verhindert das nicht.");
  } else {
    // Anfragen an angriff.invalid kommen nur von Angriffen; erlaubt.invalid ist der erlaubte Link.
    fehler += ohneKlick.length + durchKlick.length + anfragen.filter((u) => u.includes("angriff.invalid")).length;
    // Der erlaubte Link muss als neuer Tab ankommen.
    if (!neueTabs.some((u) => u.startsWith("https://erlaubt.invalid"))) { console.log("Erlaubter Link hat keinen neuen Tab geöffnet."); fehler++; }
    fehler += dom.skripte === 2 ? 0 : 1;
    fehler += dom.eventhandler.length + dom.eingebettet.length + dom.externeBilder.length;
    fehler += dom.links.filter((h) => !/^(https?:|mailto:|tel:|#)/i.test(h)).length;
  }
} finally {
  await browser.close();
}
console.log(fehler === 0 ? (gegenprobe ? "\nGegenprobe: Die CSP allein hat jeden Code blockiert." : "\nKein Angriff erfolgreich.") : `\n${fehler} Befunde.`);
process.exit(fehler === 0 ? 0 : 1);
