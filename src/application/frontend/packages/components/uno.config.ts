import { defineConfig } from 'unocss';
import presetWind3 from '@unocss/preset-wind3';

/** Prefix of all utility classes of the package. Part of the public contract (ADR-013). */
export const CLASS_PREFIX = 'lw-';

/** CSS layer that holds all styles of the package. Host styles outside it take precedence (ADR-013). */
export const CSS_LAYER = 'letterwright';

/**
 * UnoCSS according to ADR-013: its own class prefix, no global reset (no preflights)
 * and all generated styles in their own CSS layer.
 */
export default defineConfig({
  presets: [presetWind3({ prefix: CLASS_PREFIX, preflight: false })],
  outputToCssLayers: {
    cssLayerName: () => CSS_LAYER,
  },
});
