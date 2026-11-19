// Klicklogik für die exportierte Bestellmaske (Wireframe-Demo).
// Elemente werden über die data-pencil-id/-name-Attribute aus dem pen.dev-Export gefunden.
(() => {
  const C = { accent: '#0E7C5A', soft: '#E3F2EC', surface: '#FFFFFF', bg: '#F4F5F2', line: '#E1E3DE',
    off: '#B8BDB5', ok: '#8FD9BD', warn: '#FDBA74', err: '#C2410C', input: '#C9CDC6' };
  const $ = (sel, root = document) => root.querySelector(sel);
  const $$ = (sel, root = document) => [...root.querySelectorAll(sel)];
  const byName = (name, root) => $(`[data-pencil-name="${name}"]`, root);
  const byPrefix = (prefix, root) => $$(`[data-pencil-name^="${prefix}"]`, root);
  const text = el => el.textContent.trim();
  const euro = n => n.toLocaleString('de-DE') + ' €';
  const parseEuro = s => Number(s.replace(/[^\d]/g, ''));

  const style = (el, s) => Object.assign(el.style, s);
  const border = (el, w, c) => style(el, { borderStyle: 'solid', borderWidth: w + 'px', borderColor: c });

  // ---- Modelle --------------------------------------------------------------
  const cars = byPrefix('Car – ').map(el => {
    const info = byName('Info', el);
    const texts = $$('div', info).filter(d => !d.children.length);
    const specs = $$('div', byName('Specs', el)).filter(d => !d.children.length).map(text);
    return {
      el,
      name: text(texts[0]),
      sub: text(texts[1]),
      specs,
      price: parseEuro(text(byName('Price', el))),
      radio: byName('Radio', el),
      img: byName('Car Image', el).style.backgroundImage,
    };
  });

  // ---- Extras ---------------------------------------------------------------
  const checkSvg = $('[data-pencil-name="Checkbox"] svg')?.cloneNode(true);
  const extras = byPrefix('Extra – ').map(el => {
    const leaf = $$('div', el).filter(d => !d.children.length && d.textContent.trim());
    return {
      el,
      name: el.dataset.pencilName.replace('Extra – ', ''),
      price: parseEuro(text(leaf[leaf.length - 1])),
      box: byName('Checkbox', el),
      icon: byName('Icon', el),
      on: !!$('[data-pencil-name="Checkbox"] svg', el),
    };
  });

  // ---- Zusammenfassung ------------------------------------------------------
  const summary = byName('Summary Card');
  const head = byName('Head', summary);
  const headTexts = $$('div', head).filter(d => !d.children.length);
  const lines = byName('Lines', summary);
  const baseRow = byName('Grundpreis', lines);
  const transferRow = byName('Überführung & Zulassung', lines);
  const totalEl = $('[data-pencil-name="Total"] > div:last-child', summary);
  const checklist = $$(':scope > div', byName('Checklist', summary));
  const okIcon = $('svg', checklist[0]).cloneNode(true);
  const warnIcon = $('svg', checklist[2]).cloneNode(true);
  const summaryImg = byName('Summary Image', summary);

  let selected = cars.findIndex(c => c.radio.style.borderWidth === '7px' || /7px/.test(c.radio.getAttribute('style')));
  if (selected < 0) selected = 0;

  function setCheck(item, okState, label) {
    const svg = $('svg', item);
    svg.replaceWith((okState ? okIcon : warnIcon).cloneNode(true));
    const t = $$('div', item).find(d => !d.children.length);
    t.textContent = label;
    t.style.color = okState ? C.ok : C.warn;
  }

  function render() {
    cars.forEach((c, i) => {
      const on = i === selected;
      style(c.el, { backgroundColor: on ? C.soft : C.surface, cursor: 'pointer' });
      border(c.el, on ? 2 : 1, on ? C.accent : C.line);
      border(c.radio, on ? 7 : 1.5, on ? C.accent : C.off);
    });
    extras.forEach(x => {
      style(x.el, { backgroundColor: x.on ? C.soft : C.surface, cursor: 'pointer' });
      border(x.el, x.on ? 2 : 1, x.on ? C.accent : C.line);
      style(x.box, { backgroundColor: x.on ? C.accent : C.surface });
      border(x.box, 1.5, x.on ? C.accent : C.off);
      const svg = $('svg', x.box);
      if (x.on && !svg && checkSvg) x.box.appendChild(checkSvg.cloneNode(true));
      if (!x.on && svg) svg.remove();
      if (x.icon) x.icon.style.backgroundColor = x.on ? C.surface : C.bg;
    });

    const car = cars[selected];
    headTexts[1].textContent = car.name;
    const range = (car.specs[0] || '').replace(' Reichweite', '');
    headTexts[2].textContent = [car.sub.split(' · ')[0], range, car.specs[1]].filter(Boolean).join(' · ');
    if (summaryImg && car.img) summaryImg.style.backgroundImage = car.img;

    $$(':scope > div', lines).filter(r => r !== baseRow && r !== transferRow).forEach(r => r.remove());
    $(':scope > div:last-child', baseRow).textContent = euro(car.price);
    const chosen = extras.filter(x => x.on);
    chosen.forEach(x => {
      const row = baseRow.cloneNode(true);
      row.dataset.pencilName = x.name;
      $(':scope > div:first-child', row).textContent = x.name;
      $(':scope > div:last-child', row).textContent = euro(x.price);
      lines.insertBefore(row, transferRow);
    });
    totalEl.textContent = euro(car.price + chosen.reduce((s, x) => s + x.price, 0));

    setCheck(checklist[0], true, 'Modell gewählt');
    setCheck(checklist[1], true, chosen.length === 1 ? '1 Extra gewählt'
      : chosen.length ? `${chosen.length} Extras gewählt` : 'Keine Extras gewählt');
    const mailOk = emailValid();
    setCheck(checklist[2], mailOk, mailOk ? 'E-Mail-Adresse gültig' : 'E-Mail-Adresse prüfen');
  }

  cars.forEach((c, i) => c.el.addEventListener('click', () => { selected = i; render(); }));
  extras.forEach(x => x.el.addEventListener('click', () => { x.on = !x.on; render(); }));

  // ---- Eingabefelder --------------------------------------------------------
  let emailInput, emailField;
  byPrefix('Field – ').forEach(field => {
    const box = byName('Input', field);
    const valueEl = $$('div', box).find(d => !d.children.length);
    if (!valueEl) return;
    const input = document.createElement('input');
    input.value = text(valueEl);
    input.setAttribute('aria-label', field.dataset.pencilName.replace('Field – ', ''));
    style(input, { border: 0, outline: 0, background: 'transparent', font: 'inherit', color: 'inherit', width: '100%', padding: 0 });
    valueEl.textContent = '';
    style(valueEl, { flex: '1 1 0', whiteSpace: 'normal' });
    valueEl.appendChild(input);
    box.style.cursor = 'text';
    box.addEventListener('click', () => input.focus());
    input.addEventListener('focus', () => { if (box !== emailField?.box || emailValid()) border(box, 2, C.accent); });
    input.addEventListener('blur', () => { if (box !== emailField?.box || emailValid()) border(box, 1, C.input); });
    if (/E-Mail/.test(field.dataset.pencilName)) {
      emailInput = input;
      emailField = { field, box, icon: $('svg', box), msg: $(':scope > div:last-child', field), shadow: box.style.boxShadow };
      input.addEventListener('input', () => { showEmailState(); render(); });
    }
  });

  function emailValid() {
    return !emailInput || /^[^\s@]+@[^\s@]+\.[^\s@]{2,}$/.test(emailInput.value.trim());
  }
  function showEmailState() {
    if (!emailField) return;
    const ok = emailValid();
    const focused = document.activeElement === emailInput;
    border(emailField.box, ok ? (focused ? 2 : 1) : 2, ok ? (focused ? C.accent : C.input) : C.err);
    emailField.box.style.boxShadow = ok ? 'none' : emailField.shadow;
    if (emailField.icon) emailField.icon.style.display = ok ? 'none' : '';
    if (emailField.msg && emailField.msg !== emailField.box) emailField.msg.style.display = ok ? 'none' : '';
  }

  // ---- Navigation -----------------------------------------------------------
  const button = byName('Primary Button');
  button.style.cursor = 'pointer';
  button.addEventListener('click', () => {
    if (!emailValid()) {
      emailField.field.scrollIntoView({ behavior: 'smooth', block: 'center' });
      emailInput.focus();
      showEmailState();
      return;
    }
    location.href = 'index.html#danke';
  });
  const login = byName('Menüpunkt Anmelden');
  if (login) { login.style.cursor = 'pointer'; login.addEventListener('click', () => { location.href = 'index.html#login'; }); }

  // ---- Demo-Leiste ----------------------------------------------------------
  const bar = document.createElement('div');
  bar.innerHTML = '<b style="color:#fff;margin-right:8px">Klick-Demo</b>' +
    '<a href="bestellmaske.html" style="color:#8fe0c0;text-decoration:none">Bestellmaske (Kunde)</a>' +
    '<a href="index.html#login" style="color:#8fe0c0;text-decoration:none">Admin-Login</a>' +
    '<span style="margin-left:auto;opacity:.7">Modell, Extras und Felder sind bedienbar</span>';
  style(bar, { position: 'sticky', top: 0, zIndex: 10, display: 'flex', gap: '16px', alignItems: 'center',
    padding: '10px 20px', background: '#14181F', color: '#c9cdd4', font: '13px Inter, system-ui, sans-serif' });
  document.body.prepend(bar);
  style(document.body, { display: 'flex', flexDirection: 'column', alignItems: 'center' });
  bar.style.alignSelf = 'stretch';

  // Exportierter Header ist content-box und dadurch breiter als 1440 px.
  const header = byName('Header');
  if (header) header.style.boxSizing = 'border-box';

  render();
  showEmailState();
})();
