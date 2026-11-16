import axe from 'axe-core';

/** Schweregrade, die einen Test scheitern lassen (ADR-011, QS-003). */
const BLOCKIERENDE_SCHWEREGRADE: readonly string[] = ['serious', 'critical'];

/**
 * Prüft ein gerendertes Element mit axe-core und wirft einen Fehler, sobald ein
 * Verstoß der Stufe „serious“ oder „critical“ vorliegt.
 *
 * Leichtere Verstöße („minor“, „moderate“) lassen den Test bestehen. Regeln, die
 * jsdom nicht auswerten kann (etwa Farbkontraste), meldet axe als unvollständig,
 * nicht als Verstoß; sie brauchen eine Prüfung im echten Browser.
 */
export async function pruefeBarrierefreiheit(element: Element): Promise<void> {
  const ergebnis = await axe.run(element, { resultTypes: ['violations'] });
  const blockierend = ergebnis.violations.filter((verstoss) =>
    BLOCKIERENDE_SCHWEREGRADE.includes(verstoss.impact ?? ''),
  );
  if (blockierend.length > 0) {
    const liste = blockierend
      .map((verstoss) => `- ${verstoss.id} (${verstoss.impact ?? 'unbekannt'}): ${verstoss.help}`)
      .join('\n');
    throw new Error(`Barrierefreiheit verletzt:\n${liste}`);
  }
}
