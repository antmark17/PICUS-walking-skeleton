/* Filtra e impagina soltanto righe e card già renderizzate da Thymeleaf.
   Non usa sessionStorage o richieste dimostrative. */
(() => {
  'use strict';
  const PER_PAGE = 6;
  function init() {
    const search = document.getElementById('searchRequests');
    const filter = document.getElementById('statusFilter');
    const tbody = document.getElementById('requestsBody');
    const cards = document.getElementById('mobileCards');
    const resultCount = document.getElementById('resultCount');
    const rangeCount = document.getElementById('rangeCount');
    const pageNumber = document.getElementById('pageNumber');
    const previous = document.getElementById('previousPage');
    const next = document.getElementById('nextPage');
    const empty = document.getElementById('emptyState');
    const reset = document.getElementById('resetFilters');
    if (![search, filter, tbody, cards, resultCount, rangeCount, pageNumber, previous, next, empty, reset].every(Boolean)) return;

    const rows = Array.from(tbody.querySelectorAll('.request-row'));
    const cardsById = new Map(Array.from(cards.querySelectorAll('.request-card')).map(card => [card.dataset.id, card]));
    let currentPage = 1;
    function render() {
      const query = search.value.toLocaleLowerCase('it').trim();
      const status = filter.value;
      const matches = rows.filter(row => (status === 'all' || row.dataset.status === status) &&
        (!query || (row.dataset.search || '').toLocaleLowerCase('it').includes(query)));
      const count = matches.length;
      const pages = Math.max(1, Math.ceil(count / PER_PAGE));
      currentPage = Math.min(currentPage, pages);
      const start = (currentPage - 1) * PER_PAGE;
      const shownIds = new Set(matches.slice(start, start + PER_PAGE).map(row => row.dataset.id));
      for (const row of rows) row.hidden = !shownIds.has(row.dataset.id);
      for (const [id, card] of cardsById) card.hidden = !shownIds.has(id);
      resultCount.textContent = `${count} ${count === 1 ? 'richiesta' : 'richieste'}`;
      rangeCount.textContent = count ? `${start + 1}–${Math.min(start + PER_PAGE, count)} di ${count} richieste` : '0 richieste';
      pageNumber.textContent = `${currentPage} / ${pages}`;
      previous.disabled = currentPage <= 1;
      next.disabled = currentPage >= pages;
      empty.hidden = count !== 0;
      reset.hidden = !search.value && status === 'all';
    }
    search.addEventListener('input', () => { currentPage = 1; render(); });
    filter.addEventListener('change', () => { currentPage = 1; render(); });
    reset.addEventListener('click', () => {
      search.value = '';
      filter.value = 'all';
      currentPage = 1;
      render();
      search.focus();
    });
    previous.addEventListener('click', () => { if (currentPage > 1) { currentPage--; render(); } });
    next.addEventListener('click', () => { currentPage++; render(); });
    render();
  }
  document.addEventListener('DOMContentLoaded', init);
})();
