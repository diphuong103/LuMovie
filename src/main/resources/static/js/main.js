// ═══════════════════════════════════════
// LuMovie — main.js
// ═══════════════════════════════════════

document.addEventListener('DOMContentLoaded', () => {

  // ── Active nav link ──
  const path = window.location.pathname;
  document.querySelectorAll('.nav-links a').forEach(a => {
    if (a.getAttribute('href') === path) a.classList.add('active');
  });

  // ── Navbar scroll effect ──
  const navbar = document.querySelector('.navbar');
  if (navbar) {
    window.addEventListener('scroll', () => {
      navbar.style.background = window.scrollY > 60
        ? 'rgba(10,10,14,0.98)'
        : 'linear-gradient(180deg,rgba(10,10,14,0.97) 0%,rgba(10,10,14,0) 100%)';
    });
  }

  // ── Auto-hide alerts ──
  document.querySelectorAll('.alert').forEach(el => {
    setTimeout(() => {
      el.style.transition = 'opacity 0.5s';
      el.style.opacity = '0';
      setTimeout(() => el.remove(), 500);
    }, 4000);
  });

  // ── CSRF helper for fetch ──
  window.getCsrf = () => {
    const meta = document.querySelector('meta[name="_csrf"]');
    const header = document.querySelector('meta[name="_csrf_header"]');
    return meta && header ? { header: header.content, token: meta.content } : null;
  };

  // ── Watchlist toggle (AJAX) ──
  document.querySelectorAll('[data-watchlist]').forEach(btn => {
    btn.addEventListener('click', async (e) => {
      e.preventDefault();
      const movieId = btn.dataset.movieId;
      const csrf = window.getCsrf();
      if (!csrf) { window.location.href = '/auth/login'; return; }

      try {
        const res = await fetch('/api/watchlist/toggle', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json', [csrf.header]: csrf.token },
          body: JSON.stringify({ movieId })
        });
        const data = await res.json();
        if (data.inWatchlist) {
          btn.textContent = '❤️ Đã Lưu';
          btn.style.color = '#e8b84b';
        } else {
          btn.textContent = '🤍 Lưu Phim';
          btn.style.color = '';
        }
      } catch { console.error('Watchlist error'); }
    });
  });

  // ── Image lazy load error fallback ──
  document.querySelectorAll('img').forEach(img => {
    img.addEventListener('error', function() {
      this.style.display = 'none';
    });
  });

  // ── Smooth scroll for anchor links ──
  document.querySelectorAll('a[href^="#"]').forEach(a => {
    a.addEventListener('click', e => {
      const target = document.querySelector(a.getAttribute('href'));
      if (target) { e.preventDefault(); target.scrollIntoView({ behavior: 'smooth' }); }
    });
  });

  // ── Search shortcut (Ctrl+K / /) ──
  document.addEventListener('keydown', e => {
    if ((e.ctrlKey && e.key === 'k') || (e.key === '/' && !['INPUT','TEXTAREA'].includes(document.activeElement.tagName))) {
      e.preventDefault();
      window.location.href = '/search';
    }
  });
});

// ── Toast notification ──
window.showToast = (message, type = 'info') => {
  const toast = document.createElement('div');
  const colors = { success: '#28d878', error: '#e84055', info: '#e8b84b' };
  toast.style.cssText = `
    position:fixed;bottom:24px;right:24px;z-index:9999;
    padding:14px 20px;border-radius:10px;font-size:0.85rem;
    background:#141419;border:1px solid ${colors[type]}40;color:${colors[type]};
    box-shadow:0 8px 32px rgba(0,0,0,0.5);
    animation:fadeUp 0.3s ease;max-width:320px;
    font-family:'DM Sans',sans-serif;
  `;
  toast.textContent = message;
  document.body.appendChild(toast);
  setTimeout(() => {
    toast.style.opacity = '0';
    toast.style.transition = 'opacity 0.3s';
    setTimeout(() => toast.remove(), 300);
  }, 3000);
};
