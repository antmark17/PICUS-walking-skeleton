/* PICUS Banco — ricerca e paginazione locali; conferma di un POST vero verso G2. */
'use strict';
document.addEventListener('DOMContentLoaded', () => {
  const dialog = document.getElementById('bancoConfirmDialog');
  const cancel = document.getElementById('bancoCancelProcessing');
  const confirm = document.getElementById('bancoConfirmProcessing');
  let pendingForm = null;
  if (dialog && cancel && confirm && typeof dialog.showModal === 'function') {
    document.querySelectorAll('form.js-confirm-start').forEach(form => {
      form.addEventListener('submit', event => {
        event.preventDefault();
        pendingForm = form;
        dialog.showModal();
      });
    });
    cancel.addEventListener('click', () => dialog.close());
    dialog.addEventListener('click', event => { if (event.target === dialog) dialog.close(); });
    dialog.addEventListener('close', () => { pendingForm?.querySelector('button')?.focus(); });
    confirm.addEventListener('click', () => {
      if (!pendingForm) return;
      const form = pendingForm;
      pendingForm = null;
      confirm.disabled = true;
      form.submit(); // Invia il form vero con token CSRF, senza fetch/sessionStorage.
    });
  }

  const search = document.getElementById('bancoSearchRequests');
  const status = document.getElementById('bancoStatusFilter');
  if (!search || !status) return; // Nella pagina dettaglio esiste soltanto il popup.
  const rows = [...document.querySelectorAll('#bancoRequestsBody .request-row')];
  const cards = [...document.querySelectorAll('#bancoMobileCards .request-card')];
  const perPage = 6;
  let page = 1;
  const byId = id => document.getElementById(id);
  const reset = byId('bancoResetFilters');
  const empty = byId('bancoEmptyState');
  const count = byId('bancoResultCount');
  const range = byId('bancoRangeCount');
  const current = byId('bancoPageNumber');
  const previous = byId('bancoPreviousPage');
  const next = byId('bancoNextPage');
  const matches = el => {
    const q = search.value.trim().toLocaleLowerCase('it');
    return (status.value === 'all' || el.dataset.status === status.value)
      && (el.dataset.search || '').toLocaleLowerCase('it').includes(q);
  };
  function render() {
    const matching = rows.filter(matches);
    const total = matching.length;
    const pages = Math.max(1, Math.ceil(total / perPage));
    page = Math.min(page, pages);
    const start = (page - 1) * perPage;
    rows.forEach(r => { r.hidden = true; });
    cards.forEach(c => { c.hidden = true; });
    matching.slice(start, start + perPage).forEach(r => { r.hidden = false; });
    cards.filter(matches).slice(start, start + perPage).forEach(c => { c.hidden = false; });
    count.textContent = `${total} ${total === 1 ? 'richiesta' : 'richieste'}`;
    range.textContent = total ? `${start + 1}–${Math.min(start + perPage, total)} di ${total} richieste` : '0 richieste';
    current.textContent = `${page} / ${pages}`;
    previous.disabled = page <= 1;
    next.disabled = page >= pages;
    reset.hidden = !search.value && status.value === 'all';
    empty.hidden = total !== 0;
  }
  search.addEventListener('input', () => { page = 1; render(); });
  status.addEventListener('change', () => { page = 1; render(); });
  reset.addEventListener('click', () => { search.value = ''; status.value = 'all'; page = 1; render(); search.focus(); });
  previous.addEventListener('click', () => { if (page > 1) { page--; render(); } });
  next.addEventListener('click', () => { page++; render(); });
  render();
});
