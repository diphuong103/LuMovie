/**
 * LuMovie — HLS Player
 * Dùng HLS.js để phát stream m3u8
 * Tự động fallback về native HLS (Safari) nếu browser hỗ trợ
 */

class LuMoviePlayer {
  constructor(videoEl, options = {}) {
    this.video    = videoEl;
    this.hls      = null;
    this.options  = options;
    this.isReady  = false;

    // UI elements
    this.container   = videoEl.closest('.lm-player-wrap');
    this.playBtn     = this.container?.querySelector('.lm-play-btn');
    this.progressBar = this.container?.querySelector('.lm-progress-fill');
    this.progressWrap= this.container?.querySelector('.lm-progress-wrap');
    this.timeEl      = this.container?.querySelector('.lm-time');
    this.durationEl  = this.container?.querySelector('.lm-duration');
    this.volBtn      = this.container?.querySelector('.lm-vol-btn');
    this.volSlider   = this.container?.querySelector('.lm-vol-slider');
    this.fsBtn       = this.container?.querySelector('.lm-fs-btn');
    this.overlay     = this.container?.querySelector('.lm-overlay');
    this.spinner     = this.container?.querySelector('.lm-spinner');
    this.qualityMenu = this.container?.querySelector('.lm-quality-menu');
    this.qualityBtn  = this.container?.querySelector('.lm-quality-btn');
    this.speedBtn    = this.container?.querySelector('.lm-speed-btn');
    this.speedMenu   = this.container?.querySelector('.lm-speed-menu');
    this.controls    = this.container?.querySelector('.lm-controls');

    this._init();
  }

  _init() {
    this._bindControls();
    this._bindKeyboard();
    this._autoHideControls();
  }

  load(url) {
    if (!url) return;

    this._showSpinner(true);

    if (!url.includes('.m3u8')) {
      this._loadIframe(url);
      return;
    }

    if (Hls.isSupported()) {
      if (this.hls) { this.hls.destroy(); }

      this.hls = new Hls({
        maxBufferLength: 30,
        maxMaxBufferLength: 60,
        enableWorker: true,
      });

      this.hls.loadSource(url);
      this.hls.attachMedia(this.video);

      this.hls.on(Hls.Events.MANIFEST_PARSED, (event, data) => {
        this._showSpinner(false);
        this.isReady = true;
        this.video.play().catch(() => {});
        this._buildQualityMenu(data.levels);
      });

      this.hls.on(Hls.Events.ERROR, (event, data) => {
        if (data.fatal) {
          console.error('HLS fatal error:', data);
          this._showError('Không thể tải video. Vui lòng thử lại.');
        }
      });

    } else if (this.video.canPlayType('application/vnd.apple.mpegurl')) {
      // Safari native HLS
      this.video.src = url;
      this.video.addEventListener('loadedmetadata', () => {
        this._showSpinner(false);
        this.isReady = true;
        this.video.play().catch(() => {});
      });
    } else {
      this._showError('Trình duyệt không hỗ trợ phát video này.');
    }

    this._bindVideoEvents();
  }

  _bindVideoEvents() {
    const v = this.video;

    v.addEventListener('timeupdate', () => this._updateProgress());
    v.addEventListener('waiting',    () => this._showSpinner(true));
    v.addEventListener('playing',    () => {
      this._showSpinner(false);
      this._setPlayIcon(true);
    });
    v.addEventListener('pause',  () => this._setPlayIcon(false));
    v.addEventListener('ended',  () => {
      this._setPlayIcon(false);
      if (this.options.onEnded) this.options.onEnded();
    });
    v.addEventListener('volumechange', () => this._updateVolIcon());
    v.addEventListener('durationchange', () => {
      if (this.durationEl) {
        this.durationEl.textContent = this._fmt(v.duration);
      }
    });
  }

  _bindControls() {
    // Play/Pause
    this.playBtn?.addEventListener('click', () => this.togglePlay());
    this.overlay?.addEventListener('click', () => this.togglePlay());

    // Progress seek
    this.progressWrap?.addEventListener('click', e => {
      const rect = this.progressWrap.getBoundingClientRect();
      const pct  = (e.clientX - rect.left) / rect.width;
      if (this.video.duration) {
        this.video.currentTime = pct * this.video.duration;
      }
    });

    // Volume
    this.volBtn?.addEventListener('click', () => {
      this.video.muted = !this.video.muted;
    });

    this.volSlider?.addEventListener('input', e => {
      this.video.volume = e.target.value / 100;
      this.video.muted  = false;
    });

    // Fullscreen
    this.fsBtn?.addEventListener('click', () => this.toggleFullscreen());

    // Speed menu
    this.speedBtn?.addEventListener('click', e => {
      e.stopPropagation();
      this.speedMenu?.classList.toggle('visible');
      this.qualityMenu?.classList.remove('visible');
    });

    this.speedMenu?.querySelectorAll('[data-speed]').forEach(el => {
      el.addEventListener('click', () => {
        const spd = parseFloat(el.dataset.speed);
        this.video.playbackRate = spd;
        if (this.speedBtn) this.speedBtn.textContent = spd + 'x';
        this.speedMenu.classList.remove('visible');
      });
    });

    // Quality button
    this.qualityBtn?.addEventListener('click', e => {
      e.stopPropagation();
      this.qualityMenu?.classList.toggle('visible');
      this.speedMenu?.classList.remove('visible');
    });

    document.addEventListener('click', () => {
      this.qualityMenu?.classList.remove('visible');
      this.speedMenu?.classList.remove('visible');
    });

    // Fullscreen change
    document.addEventListener('fullscreenchange', () => {
      if (this.fsBtn) {
        this.fsBtn.textContent = document.fullscreenElement ? '⛶' : '⛶';
      }
    });
  }

  _buildQualityMenu(levels) {
    if (!this.qualityMenu || !levels || levels.length < 2) return;

    this.qualityMenu.innerHTML = '<div class="lm-menu-item active" data-level="-1">Auto</div>';
    levels.forEach((lv, i) => {
      const label = lv.height ? lv.height + 'p' : 'Level ' + i;
      const el    = document.createElement('div');
      el.className     = 'lm-menu-item';
      el.dataset.level = i;
      el.textContent   = label;
      el.addEventListener('click', () => {
        this.hls.currentLevel = i;
        this.qualityMenu.querySelectorAll('.lm-menu-item').forEach(x => x.classList.remove('active'));
        el.classList.add('active');
        this.qualityMenu.classList.remove('visible');
        if (this.qualityBtn) this.qualityBtn.textContent = '🎞 ' + label;
      });
      this.qualityMenu.appendChild(el);
    });

    // Auto level
    this.qualityMenu.querySelector('[data-level="-1"]').addEventListener('click', el => {
      this.hls.currentLevel = -1;
      this.qualityMenu.querySelectorAll('.lm-menu-item').forEach(x => x.classList.remove('active'));
      this.qualityMenu.querySelector('[data-level="-1"]').classList.add('active');
      this.qualityMenu.classList.remove('visible');
      if (this.qualityBtn) this.qualityBtn.textContent = '🎞 Auto';
    });

    this.qualityBtn?.classList.remove('hidden');
  }

  _bindKeyboard() {
    document.addEventListener('keydown', e => {
      if (['INPUT','TEXTAREA'].includes(e.target.tagName)) return;
      switch (e.key) {
        case ' ':
        case 'k':
          e.preventDefault();
          this.togglePlay();
          break;
        case 'ArrowRight':
          e.preventDefault();
          this.video.currentTime += e.shiftKey ? 30 : 5;
          this._showNudge('+' + (e.shiftKey ? 30 : 5) + 's');
          break;
        case 'ArrowLeft':
          e.preventDefault();
          this.video.currentTime -= e.shiftKey ? 30 : 5;
          this._showNudge('-' + (e.shiftKey ? 30 : 5) + 's');
          break;
        case 'ArrowUp':
          e.preventDefault();
          this.video.volume = Math.min(1, this.video.volume + 0.1);
          break;
        case 'ArrowDown':
          e.preventDefault();
          this.video.volume = Math.max(0, this.video.volume - 0.1);
          break;
        case 'f':
          e.preventDefault();
          this.toggleFullscreen();
          break;
        case 'm':
          e.preventDefault();
          this.video.muted = !this.video.muted;
          break;
      }
    });
  }

  _autoHideControls() {
    if (!this.controls) return;
    let timer;
    const show = () => {
      this.controls.classList.remove('hidden');
      clearTimeout(timer);
      timer = setTimeout(() => {
        if (!this.video.paused) this.controls.classList.add('hidden');
      }, 3000);
    };
    this.container?.addEventListener('mousemove', show);
    this.container?.addEventListener('touchstart', show);
    this.video.addEventListener('pause', () => {
      this.controls.classList.remove('hidden');
      clearTimeout(timer);
    });
  }

  togglePlay() {
    if (this.video.paused) {
      this.video.play();
    } else {
      this.video.pause();
    }
  }

  toggleFullscreen() {
    if (document.fullscreenElement) {
      document.exitFullscreen();
    } else {
      this.container?.requestFullscreen();
    }
  }

  _updateProgress() {
    const v   = this.video;
    const pct = v.duration ? (v.currentTime / v.duration) * 100 : 0;
    if (this.progressBar)  this.progressBar.style.width = pct + '%';
    if (this.timeEl)       this.timeEl.textContent = this._fmt(v.currentTime);

    // Save progress
    if (this._saveTimer) clearTimeout(this._saveTimer);
    this._saveTimer = setTimeout(() => {
      if (this.options.onProgress) {
        this.options.onProgress(v.currentTime, v.duration);
      }
    }, 5000);
  }

  _updateVolIcon() {
    if (!this.volBtn) return;
    this.volBtn.textContent = this.video.muted || this.video.volume === 0 ? '🔇' : '🔊';
    if (this.volSlider) this.volSlider.value = this.video.muted ? 0 : this.video.volume * 100;
  }

  _setPlayIcon(playing) {
    if (this.playBtn) this.playBtn.textContent = playing ? '⏸' : '▶';
  }

  _showSpinner(show) {
    if (this.spinner) this.spinner.style.display = show ? 'flex' : 'none';
  }

  _showNudge(text) {
    const el = this.container?.querySelector('.lm-nudge');
    if (!el) return;
    el.textContent = text;
    el.classList.add('show');
    setTimeout(() => el.classList.remove('show'), 800);
  }

  _showError(msg) {
    this._showSpinner(false);
    const el = this.container?.querySelector('.lm-error');
    if (el) { el.textContent = msg; el.style.display = 'flex'; }
  }

  _fmt(sec) {
    if (!sec || isNaN(sec)) return '0:00';
    const m = Math.floor(sec / 60);
    const s = Math.floor(sec % 60);
    return m + ':' + String(s).padStart(2, '0');
  }

  destroy() {
    if (this.hls) { this.hls.destroy(); this.hls = null; }
  }
}

// ── Auto-init nếu có element ──
window.addEventListener('DOMContentLoaded', () => {
  const wrap = document.querySelector('.lm-player-wrap');
  if (!wrap) return;

  const video = wrap.querySelector('video');
  const src   = video?.dataset.src;
  if (!video) return;

  window.luPlayer = new LuMoviePlayer(video, {
    onEnded: () => {
      // Tự động chuyển tập tiếp theo
      const nextBtn = document.querySelector('[data-next-ep]');
      if (nextBtn) {
        setTimeout(() => { window.location.href = nextBtn.href; }, 2000);
      }
    },
    onProgress: (currentTime, duration) => {
      // Lưu tiến trình xem
      const movieId   = document.body.dataset.movieId;
      const episodeId = document.body.dataset.episodeId;
      if (!movieId) return;
      const pct = Math.round((currentTime / duration) * 100);
      fetch('/api/history/progress', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ movieId, episodeId, progressPercent: pct })
      }).catch(() => {});
    }
  });

  if (src) luPlayer.load(src);
});

