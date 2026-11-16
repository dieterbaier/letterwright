import { cleanup, render } from '@testing-library/react';
import { afterEach, describe, expect, it } from 'vitest';
import { pruefeBarrierefreiheit } from './axe';

afterEach(cleanup);

describe('pruefeBarrierefreiheit', () => {
  it('meldet ein Bild ohne Alternativtext als Fehler', async () => {
    const { container } = render(<img src="logo.png" />);

    await expect(pruefeBarrierefreiheit(container)).rejects.toThrow(/image-alt \(critical\)/);
  });

  it('lässt zugängliches Markup bestehen', async () => {
    const { container } = render(<img src="logo.png" alt="Logo der Behörde" />);

    await expect(pruefeBarrierefreiheit(container)).resolves.toBeUndefined();
  });

  it('lässt Verstöße unterhalb von „serious“ bestehen', async () => {
    const { container } = render(
      <main>
        <h1>Titel</h1>
        <h3>Übersprungene Ebene</h3>
      </main>,
    );

    await expect(pruefeBarrierefreiheit(container)).resolves.toBeUndefined();
  });
});
