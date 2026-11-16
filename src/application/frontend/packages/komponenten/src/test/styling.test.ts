import { createGenerator } from 'unocss';
import { describe, expect, it } from 'vitest';
import konfiguration, { CSS_EBENE, KLASSEN_PRAEFIX } from '../../uno.config';

/**
 * Liefert den CSS-Text, der nicht in der Ebene des Pakets liegt: ohne Kommentare,
 * ohne die Deklaration `@layer <ebene>;` und ohne alle Blöcke `@layer <ebene>{…}`.
 */
function ausserhalbDerEbene(css: string): string {
  const ohneKommentare = css.replace(/\/\*.*?\*\//gs, '');
  const kopf = `@layer ${CSS_EBENE}{`;
  let rest = '';
  let i = 0;
  while (i < ohneKommentare.length) {
    if (ohneKommentare.startsWith(kopf, i)) {
      let tiefe = 1;
      i += kopf.length;
      while (i < ohneKommentare.length && tiefe > 0) {
        if (ohneKommentare[i] === '{') tiefe++;
        if (ohneKommentare[i] === '}') tiefe--;
        i++;
      }
    } else {
      rest += ohneKommentare.charAt(i);
      i++;
    }
  }
  return rest.replace(`@layer ${CSS_EBENE};`, '').trim();
}

/** Prüft, dass die UnoCSS-Konfiguration die Vorgaben aus ADR-013 einhält. */
describe('UnoCSS-Konfiguration nach ADR-013', () => {
  async function erzeuge(klassen: string): Promise<string> {
    const generator = await createGenerator(konfiguration);
    const { css } = await generator.generate(klassen, { preflights: true });
    return css;
  }

  it('erzeugt Klassen nur mit dem Präfix des Pakets', async () => {
    const css = await erzeuge(`${KLASSEN_PRAEFIX}p-2 p-4`);

    expect(css).toContain(`.${KLASSEN_PRAEFIX}p-2`);
    expect(css).not.toMatch(/\.p-4\b/);
  });

  it('legt alle Styles in die eigene CSS-Ebene', async () => {
    const css = await erzeuge(`${KLASSEN_PRAEFIX}p-2`);

    expect(css).toContain(`@layer ${CSS_EBENE}{`);
    expect(ausserhalbDerEbene(css)).toBe('');
  });

  it('enthält kein globales Reset', async () => {
    const css = await erzeuge(`${KLASSEN_PRAEFIX}p-2`);

    expect(css).not.toMatch(/(^|[\s,}])(\*|html|body|::before|::after)\s*[,{]/);
  });
});
