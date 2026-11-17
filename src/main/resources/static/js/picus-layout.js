/* Layout condiviso PICUS: solo navigazione, nessun dato di richiesta nel browser. */
(() => {
  'use strict';
  function init() {
    const menuButton = document.getElementById('openMenu');
    const sidebar = document.getElementById('sidebar');
    const backdrop = document.getElementById('menuBackdrop');
    const collapseButton = document.getElementById('collapseMenu');
    const shell = document.getElementById('appShell');
    if (!menuButton || !sidebar || !backdrop || !collapseButton || !shell) return;

    const mobileMedia = window.matchMedia('(max-width: 768px)');
    function toggleMobileMenu(open) {
      if (!mobileMedia.matches) open = false;
      sidebar.classList.toggle('is-open', open);
      backdrop.hidden = !open;
      menuButton.setAttribute('aria-expanded', String(open));
      menuButton.setAttribute('aria-label', open ? 'Chiudi menu' : 'Apri menu');
      menuButton.title = open ? 'Chiudi menu' : 'Apri menu';
      menuButton.classList.toggle('is-active', open);
      const path = menuButton.querySelector('path');
      if (path) path.setAttribute('d', open ? 'M6 6l12 12M18 6L6 18' : 'M4 6h16M4 12h16M4 18h16');
      document.body.style.overflow = open ? 'hidden' : '';
      if (!open && mobileMedia.matches) menuButton.focus();
    }
    menuButton.addEventListener('click', () => toggleMobileMenu(!sidebar.classList.contains('is-open')));
    backdrop.addEventListener('click', () => toggleMobileMenu(false));
    document.addEventListener('keydown', e => {
      if (e.key === 'Escape' && sidebar.classList.contains('is-open')) toggleMobileMenu(false);
    });
    mobileMedia.addEventListener('change', () => toggleMobileMenu(false));
    collapseButton.addEventListener('click', () => {
      const collapsed = shell.classList.toggle('is-collapsed');
      collapseButton.setAttribute('aria-expanded', String(!collapsed));
      collapseButton.setAttribute('aria-label', collapsed ? 'Espandi menu laterale' : 'Comprimi menu laterale');
    });
  }
  document.addEventListener('DOMContentLoaded', init);
})();
