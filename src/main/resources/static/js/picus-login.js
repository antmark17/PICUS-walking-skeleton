/* PICUS — solo controllo visivo della password. L'autenticazione e' gestita dal server. */
'use strict';
document.addEventListener('DOMContentLoaded', () => {
  const password = document.getElementById('password');
  const toggle = document.getElementById('togglePassword');
  if (!password || !toggle) return;
  toggle.addEventListener('click', () => {
    const visible = password.type === 'password';
    password.type = visible ? 'text' : 'password';
    toggle.setAttribute('aria-pressed', String(visible));
    toggle.setAttribute('aria-label', visible ? 'Nascondi password' : 'Mostra password');
    password.focus();
  });
});
