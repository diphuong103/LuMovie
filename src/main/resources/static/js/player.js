// ═══════════════════════════════════════
// LuMovie — player.js
// ═══════════════════════════════════════

document.addEventListener('DOMContentLoaded', () => {
  const video = document.getElementById('mainPlayer');
  if (!video) return;

  // ── Save & restore progress ──
  const movieSlug = document.body.dataset.movieSlug || window.location.pathname.split('/')[2];
  const epNum = new URLSearchParams(window.location.search).get('ep') || '0';
  const storageKey = `lumovie_progress_${movieSlug}_${epNum}`;

  // Restore saved position
  const saved = parseFloat(localStorage.getItem(storageKey));
  if (saved && saved > 10) {
    video.addEventListener('loadedmetadata', () => {
      if (saved < video.duration - 30) {
        video.currentTime = saved;
        window.showToast && window.showToast(`▶ Tiếp tục từ ${formatTime(saved)}`, 'info');
      }
    });
  }

  // Save progress every 5s
  setInterval(() => {
    if (!video.paused && video.currentTime > 0) {
      localStorage.setItem(storageKey, video.currentTime);
      // POST progress to server if logged in
      saveProgressToServer(video.currentTime, video.duration);
    }
  }, 5000);

  // Mark watched when 90% complete
  video.addEventListener('timeupdate', () => {
    if (video.duration > 0 && video.currentTime / video.duration > 0.9) {
      localStorage.setItem(storageKey + '_watched', 'true');
    }
  });

  // ── Keyboard shortcuts ──
  document.addEventListener('keydown', (e) => {
    if (['INPUT','TEXTAREA'].includes(document.activeElement.tagName)) return;

    switch(e.key) {
      case ' ':
      case 'k':
        e.preventDefault();
        video.paused ? video.play() : video.pause();
        break;
      case 'ArrowRight':
        e.preventDefault();
        video.currentTime = Math.min(video.currentTime + (e.shiftKey ? 30 : 5), video.duration);
        break;
      case 'ArrowLeft':
        e.preventDefault();
        video.currentTime = Math.max(video.currentTime - (e.shiftKey ? 30 : 5), 0);
        break;
      case 'ArrowUp':
        e.preventDefault();
        video.volume = Math.min(video.volume + 0.1, 1);
        break;
      case 'ArrowDown':
        e.preventDefault();
        video.volume = Math.max(video.volume - 0.1, 0);
        break;
      case 'f':
        e.preventDefault();
        document.fullscreenElement ? document.exitFullscreen() : video.requestFullscreen();
        break;
      case 'm':
        e.preventDefault();
        video.muted = !video.muted;
        break;
    }
  });

  // ── Playback speed ──
  window.setSpeed = (speed) => {
    video.playbackRate = speed;
    window.showToast && window.showToast(`Tốc độ: ${speed}x`, 'info');
  };
});

function formatTime(seconds) {
  const m = Math.floor(seconds / 60);
  const s = Math.floor(seconds % 60);
  return `${m}:${s.toString().padStart(2, '0')}`;
}

async function saveProgressToServer(currentTime, duration) {
  if (!duration) return;
  const percent = Math.round((currentTime / duration) * 100);
  const movieId = document.querySelector('[data-movie-id]')?.dataset.movieId;
  const episodeId = document.querySelector('[data-episode-id]')?.dataset.episodeId;
  if (!movieId) return;

  const csrf = window.getCsrf ? window.getCsrf() : null;
  if (!csrf) return;

  try {
    await fetch('/api/history/progress', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', [csrf.header]: csrf.token },
      body: JSON.stringify({ movieId, episodeId, progressPercent: percent, watchedSeconds: Math.floor(currentTime) })
    });
  } catch { /* silent */ }
}
