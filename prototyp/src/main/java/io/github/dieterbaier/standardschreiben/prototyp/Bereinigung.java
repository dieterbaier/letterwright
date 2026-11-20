package io.github.dieterbaier.standardschreiben.prototyp;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Comment;
import org.jsoup.nodes.DataNode;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Entities;
import org.jsoup.nodes.Node;

/**
 * HTML-Bereinigung nach Allowlist (ADR-014). Läuft auf dem fertig zusammengeführten Dokument,
 * bevor PDF und Vorschau entstehen. Platzhalterwerte sind schon beim Zusammenführen escaped
 * (ADR-008); die Bereinigung fängt ab, was Vorlagen selbst mitbringen oder was eine Vorlage mit
 * {@code ?no_esc} aus den Daten durchlässt.
 *
 * <p>Erlaubt ist nur, was ein Schreiben braucht. Alles andere fliegt heraus, und jede Entfernung
 * wird als Befund gemeldet:
 * <ul>
 * <li>Elemente außerhalb der Allowlist. Aktive und einbettende Elemente (Skripte, Frames, Objekte,
 * Formulare, SVG) mitsamt Inhalt, alle anderen unter Erhalt ihres Inhalts.</li>
 * <li>Attribute außerhalb der Allowlist, damit alle Eventhandler.</li>
 * <li>URLs außer {@code http}, {@code https}, {@code mailto}, {@code tel} und Fragmenten in Links
 * und außer {@code data:}-Bildern in {@code img}. Externe Ressourcen lädt kein Renderer.</li>
 * <li>In CSS: {@code @import}, {@code url()} außer {@code data:}-Bildern und -Schriften,
 * {@code expression()} und {@code javascript:}.</li>
 * <li>Kommentare.</li>
 * </ul>
 */
final class Bereinigung {

    record Ergebnis(String html, List<String> befunde) {
    }

    private static final Set<String> STRUKTUR = Set.of("html", "head", "body");

    /** Nur im Kopf erlaubt. */
    private static final Set<String> KOPF = Set.of("title", "meta", "style");

    private static final Set<String> ELEMENTE = Set.of(
            "div", "p", "span", "br", "strong", "em", "b", "i", "u", "s", "sub", "sup", "small",
            "h1", "h2", "h3", "h4", "h5", "h6", "ul", "ol", "li", "dl", "dt", "dd",
            "table", "caption", "colgroup", "col", "thead", "tbody", "tfoot", "tr", "th", "td",
            "img", "a", "address", "header", "footer", "main", "section", "article", "aside",
            "blockquote", "hr", "figure", "figcaption");

    /** Werden samt Inhalt entfernt: aktive, einbettende oder interaktive Elemente. */
    private static final Set<String> MIT_INHALT = Set.of(
            "script", "noscript", "template", "iframe", "frame", "frameset", "object", "embed",
            "applet", "svg", "math", "canvas", "audio", "video", "source", "track", "picture",
            "form", "input", "button", "select", "textarea", "option", "link", "base", "style",
            "title", "meta", "dialog", "portal");

    private static final Set<String> GLOBALE_ATTRIBUTE = Set.of(
            "class", "id", "lang", "dir", "title", "role", "style",
            "aria-hidden", "aria-label", "aria-labelledby", "aria-describedby");

    private static final Map<String, Set<String>> ATTRIBUTE = Map.of(
            "img", Set.of("src", "alt", "width", "height"),
            "a", Set.of("href"),
            "td", Set.of("colspan", "rowspan", "headers"),
            "th", Set.of("colspan", "rowspan", "headers", "scope"),
            "ol", Set.of("start"),
            "meta", Set.of("charset", "name", "content"),
            "col", Set.of("span"),
            "colgroup", Set.of("span"));

    private static final Pattern LINK = Pattern.compile("^(https?://|mailto:|tel:|#)[^\\s]*$", Pattern.CASE_INSENSITIVE);
    private static final Pattern DATA_BILD = Pattern.compile("^data:image/(png|jpeg|gif|webp);base64,[a-z0-9+/=]+$", Pattern.CASE_INSENSITIVE);
    private static final Pattern CSS_URL = Pattern.compile("url\\(\\s*(['\"]?)(.*?)\\1\\s*\\)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern CSS_DATA = Pattern.compile("^data:(image/(png|jpeg|gif|webp)|font/(ttf|otf|woff2?));base64,", Pattern.CASE_INSENSITIVE);
    private static final Pattern CSS_IMPORT = Pattern.compile("@import[^;]*;?", Pattern.CASE_INSENSITIVE);
    private static final Pattern CSS_AKTIV = Pattern.compile("expression\\s*\\(|javascript\\s*:|-moz-binding|behavior\\s*:", Pattern.CASE_INSENSITIVE);

    static Ergebnis bereinige(String html) {
        List<String> befunde = new ArrayList<>();
        Document dokument = Jsoup.parse(html);
        bereinigeKnoten(dokument, befunde);
        // XHTML-Ausgabe, weil openhtmltopdf wohlgeformtes XML erwartet. Ohne Pretty-Print, sonst
        // ändert zusätzlicher Leerraum das Layout (z. B. zwischen den Spalten der Fußzeile).
        dokument.outputSettings()
                .syntax(Document.OutputSettings.Syntax.xml)
                .escapeMode(Entities.EscapeMode.xhtml)
                .prettyPrint(false)
                .charset("UTF-8");
        return new Ergebnis(dokument.outerHtml(), befunde);
    }

    private static void bereinigeKnoten(Node knoten, List<String> befunde) {
        for (Node kind : new ArrayList<>(knoten.childNodes())) {
            if (kind instanceof Comment c) {
                befunde.add("Kommentar entfernt" + kurz(c.getData()));
                kind.remove();
                continue;
            }
            if (!(kind instanceof Element e)) {
                continue;
            }
            String name = e.normalName();
            boolean imKopf = e.parents().stream().anyMatch(p -> p.normalName().equals("head"));
            boolean erlaubt = STRUKTUR.contains(name) || ELEMENTE.contains(name) && !imKopf
                    || KOPF.contains(name) && imKopf;
            if (!erlaubt) {
                if (MIT_INHALT.contains(name)) {
                    befunde.add("Element <" + name + "> samt Inhalt entfernt" + kurz(e.outerHtml()));
                    e.remove();
                    continue;
                }
                befunde.add("Element <" + name + "> entfernt, Inhalt behalten");
                bereinigeKnoten(e, befunde);
                e.unwrap();
                continue;
            }
            bereinigeAttribute(e, befunde);
            if (name.equals("a") && !e.hasAttr("href")) {
                // Ein Link ohne Ziel wäre im PDF eine leere Link-Annotation; nur der Text bleibt.
                bereinigeKnoten(e, befunde);
                e.unwrap();
                continue;
            }
            if (name.equals("style")) {
                for (DataNode d : e.dataNodes()) {
                    d.setWholeData(bereinigeCss(d.getWholeData(), "<style>", befunde));
                }
            }
            bereinigeKnoten(e, befunde);
        }
    }

    private static void bereinigeAttribute(Element e, List<String> befunde) {
        String name = e.normalName();
        Set<String> erlaubt = ATTRIBUTE.getOrDefault(name, Set.of());
        for (Attribute a : new ArrayList<>(e.attributes().asList())) {
            String attr = a.getKey().toLowerCase(Locale.ROOT);
            if (!GLOBALE_ATTRIBUTE.contains(attr) && !erlaubt.contains(attr)) {
                befunde.add("Attribut " + attr + " an <" + name + "> entfernt" + kurz(a.getValue()));
                e.removeAttr(a.getKey());
                continue;
            }
            // Steuer- und Leerzeichen entfernen, bevor das Schema geprüft wird (java\tscript:)
            String wert = a.getValue().replaceAll("[\\x00-\\x20]", "");
            if (attr.equals("href") && !LINK.matcher(wert).matches()) {
                befunde.add("Link an <a> entfernt" + kurz(a.getValue()));
                e.removeAttr(a.getKey());
            } else if (attr.equals("src") && !DATA_BILD.matcher(wert).matches()) {
                befunde.add("Bildquelle an <img> entfernt (nur data:-Bilder erlaubt)" + kurz(a.getValue()));
                e.removeAttr(a.getKey());
            } else if (attr.equals("style")) {
                String css = bereinigeCss(a.getValue(), "style an <" + name + ">", befunde);
                if (css.isBlank()) {
                    e.removeAttr(a.getKey());
                } else {
                    e.attr("style", css);
                }
            }
        }
    }

    private static String bereinigeCss(String css, String ort, List<String> befunde) {
        String ergebnis = css;
        Matcher importe = CSS_IMPORT.matcher(ergebnis);
        while (importe.find()) {
            befunde.add("CSS: @import in " + ort + " entfernt" + kurz(importe.group()));
        }
        ergebnis = CSS_IMPORT.matcher(ergebnis).replaceAll("");

        Matcher url = CSS_URL.matcher(ergebnis);
        StringBuilder sb = new StringBuilder();
        while (url.find()) {
            String ziel = url.group(2).replaceAll("[\\x00-\\x20]", "");
            if (CSS_DATA.matcher(ziel).find()) {
                url.appendReplacement(sb, Matcher.quoteReplacement(url.group()));
            } else {
                befunde.add("CSS: url() in " + ort + " entfernt" + kurz(url.group()));
                url.appendReplacement(sb, "none");
            }
        }
        url.appendTail(sb);
        ergebnis = sb.toString();

        // Aktiver Inhalt lässt sich nicht sicher herausschneiden; die ganze Stilangabe fällt weg.
        Matcher aktiv = CSS_AKTIV.matcher(ergebnis);
        if (aktiv.find()) {
            befunde.add("CSS: aktiver Inhalt, Stilangabe in " + ort + " ganz entfernt" + kurz(aktiv.group()));
            return "";
        }
        return ergebnis;
    }

    private static String kurz(String text) {
        String t = text.strip().replaceAll("\\s+", " ");
        if (t.length() > 80) {
            t = t.substring(0, 77) + "...";
        }
        return t.isEmpty() ? "" : ": " + t;
    }

    private Bereinigung() {
    }
}
