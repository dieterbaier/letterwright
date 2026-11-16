import { defineConfig } from 'unocss';
import presetWind3 from '@unocss/preset-wind3';

/** Präfix für alle Utility-Klassen des Pakets. Teil des öffentlichen Vertrags (ADR-013). */
export const KLASSEN_PRAEFIX = 'ssw-';

/** CSS-Ebene, in der alle Styles des Pakets liegen. Host-Styles außerhalb haben Vorrang (ADR-013). */
export const CSS_EBENE = 'standardschreiben';

/**
 * UnoCSS nach ADR-013: eigenes Klassenpräfix, kein globales Reset (keine Preflights)
 * und alle erzeugten Styles in einer eigenen CSS-Ebene.
 */
export default defineConfig({
  presets: [presetWind3({ prefix: KLASSEN_PRAEFIX, preflight: false })],
  outputToCssLayers: {
    cssLayerName: () => CSS_EBENE,
  },
});
