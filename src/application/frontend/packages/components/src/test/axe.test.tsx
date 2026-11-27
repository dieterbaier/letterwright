import { cleanup, render } from '@testing-library/react';
import { afterEach, describe, expect, it } from 'vitest';
import { checkAccessibility } from './axe';

afterEach(cleanup);

describe('checkAccessibility', () => {
  it('reports an image without alternative text as an error', async () => {
    const { container } = render(<img src="logo.png" />);

    await expect(checkAccessibility(container)).rejects.toThrow(/image-alt \(critical\)/);
  });

  it('lets accessible markup pass', async () => {
    const { container } = render(<img src="logo.png" alt="Logo of the authority" />);

    await expect(checkAccessibility(container)).resolves.toBeUndefined();
  });

  it('lets violations below "serious" pass', async () => {
    const { container } = render(
      <main>
        <h1>Title</h1>
        <h3>Skipped level</h3>
      </main>,
    );

    await expect(checkAccessibility(container)).resolves.toBeUndefined();
  });
});
