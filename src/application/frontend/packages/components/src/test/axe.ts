import axe from 'axe-core';

/** Impact levels that fail a test (ADR-011, QS-003). */
const BLOCKING_IMPACTS: readonly string[] = ['serious', 'critical'];

/**
 * Checks a rendered element with axe-core and throws as soon as there is a
 * violation of impact "serious" or "critical".
 *
 * Lighter violations ("minor", "moderate") let the test pass. Rules that jsdom
 * cannot evaluate (such as colour contrast) are reported by axe as incomplete,
 * not as violations; they need a check in a real browser.
 */
export async function checkAccessibility(element: Element): Promise<void> {
  const result = await axe.run(element, { resultTypes: ['violations'] });
  const blocking = result.violations.filter((violation) =>
    BLOCKING_IMPACTS.includes(violation.impact ?? ''),
  );
  if (blocking.length > 0) {
    const list = blocking
      .map((violation) => `- ${violation.id} (${violation.impact ?? 'unknown'}): ${violation.help}`)
      .join('\n');
    throw new Error(`Accessibility violated:\n${list}`);
  }
}
