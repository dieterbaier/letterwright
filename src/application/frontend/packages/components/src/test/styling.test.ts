import { createGenerator } from 'unocss';
import { describe, expect, it } from 'vitest';
import configuration, { CLASS_PREFIX, CSS_LAYER } from '../../uno.config';

/**
 * Returns the CSS text that is not in the package's layer: without comments,
 * without the declaration `@layer <layer>;` and without all blocks `@layer <layer>{…}`.
 */
function outsideTheLayer(css: string): string {
  const withoutComments = css.replace(/\/\*.*?\*\//gs, '');
  const head = `@layer ${CSS_LAYER}{`;
  let rest = '';
  let i = 0;
  while (i < withoutComments.length) {
    if (withoutComments.startsWith(head, i)) {
      let depth = 1;
      i += head.length;
      while (i < withoutComments.length && depth > 0) {
        if (withoutComments[i] === '{') depth++;
        if (withoutComments[i] === '}') depth--;
        i++;
      }
    } else {
      rest += withoutComments.charAt(i);
      i++;
    }
  }
  return rest.replace(`@layer ${CSS_LAYER};`, '').trim();
}

/** Checks that the UnoCSS configuration follows ADR-013. */
describe('UnoCSS configuration according to ADR-013', () => {
  async function generate(classes: string): Promise<string> {
    const generator = await createGenerator(configuration);
    const { css } = await generator.generate(classes, { preflights: true });
    return css;
  }

  it('generates classes only with the package prefix', async () => {
    const css = await generate(`${CLASS_PREFIX}p-2 p-4`);

    expect(css).toContain(`.${CLASS_PREFIX}p-2`);
    expect(css).not.toMatch(/\.p-4\b/);
  });

  it('puts all styles into its own CSS layer', async () => {
    const css = await generate(`${CLASS_PREFIX}p-2`);

    expect(css).toContain(`@layer ${CSS_LAYER}{`);
    expect(outsideTheLayer(css)).toBe('');
  });

  it('contains no global reset', async () => {
    const css = await generate(`${CLASS_PREFIX}p-2`);

    expect(css).not.toMatch(/(^|[\s,}])(\*|html|body|::before|::after)\s*[,{]/);
  });
});
