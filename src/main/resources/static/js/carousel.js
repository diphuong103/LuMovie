/**
 * LUMOVIE — Carousel Engine
 * File: /static/js/carousel.js
 * Nhúng trước </body>: <script th:src="@{/js/carousel.js}"></script>
 *
 * Dùng chung cho tất cả carousel trên trang:
 *   trending | genre | latest | toprated
 *
 * Mỗi carousel cần các element trong DOM:
 *   #<id>-track   → flex container chứa các item
 *   #<id>-prev    → nút mũi tên trái
 *   #<id>-next    → nút mũi tên phải
 *   #<id>-dots    → container chứa dots indicator
 */

(function () {
    'use strict';

    /* ──────────────────────────────────────────────
       STATE — lưu trữ trạng thái từng carousel
    ────────────────────────────────────────────── */
    const carousels = {};

    /* ──────────────────────────────────────────────
       HELPER — số item hiển thị theo viewport
    ────────────────────────────────────────────── */
    function getVisibleCount() {
        const w = window.innerWidth;
        if (w <= 480)  return 2;
        if (w <= 768)  return 3;
        if (w <= 1024) return 4;
        if (w <= 1280) return 5;
        return 6;
    }

    /* ──────────────────────────────────────────────
       INIT — khởi tạo một carousel theo id
    ────────────────────────────────────────────── */
    function initCarousel(id) {
        const track   = document.getElementById(id + '-track');
        const dotsEl  = document.getElementById(id + '-dots');
        const prevBtn = document.getElementById(id + '-prev');
        const nextBtn = document.getElementById(id + '-next');

        if (!track) return; // carousel này không tồn tại trên trang, bỏ qua

        const items      = track.children;
        const totalItems = items.length;
        const state      = { current: 0 };
        carousels[id]    = state;

        /* số item tối đa có thể scroll đến */
        function maxIndex() {
            return Math.max(0, totalItems - getVisibleCount());
        }

        /* ── Build dots ── */
        function buildDots() {
            if (!dotsEl) return;
            dotsEl.innerHTML = '';
            const pages = maxIndex() + 1;

            if (pages <= 1) {
                dotsEl.style.display = 'none';
                return;
            }

            dotsEl.style.display = 'flex';
            for (let i = 0; i < pages; i++) {
                const dot = document.createElement('div');
                dot.className = 'carousel-dot' + (i === state.current ? ' active' : '');
                dot.addEventListener('click', function () { go(i); });
                dotsEl.appendChild(dot);
            }
        }

        /* ── Sync dots với vị trí hiện tại ── */
        function updateDots() {
            if (!dotsEl) return;
            Array.from(dotsEl.children).forEach(function (dot, i) {
                dot.classList.toggle('active', i === state.current);
            });
        }

        /* ── Di chuyển đến index ── */
        function go(index) {
            state.current = Math.max(0, Math.min(index, maxIndex()));

            const firstItem = items[0];
            if (!firstItem) return;

            /* Lấy width thực tế từ DOM để tính offset chính xác */
            const itemW = firstItem.getBoundingClientRect().width;
            const gap   = 16; /* khớp với CSS gap trong .carousel-track */

            track.style.transform = 'translateX(-' + (state.current * (itemW + gap)) + 'px)';

            updateDots();

            if (prevBtn) prevBtn.disabled = (state.current === 0);
            if (nextBtn) nextBtn.disabled = (state.current >= maxIndex());
        }

        /* ── Expose slide function ra ngoài ── */
        state.slide = function (direction) {
            go(state.current + direction);
        };

        /* ── Khởi tạo lần đầu ── */
        buildDots();
        go(0);

        /* ── Re-calculate khi resize cửa sổ ── */
        const ro = new ResizeObserver(function () {
            buildDots();
            go(Math.min(state.current, maxIndex()));
        });
        ro.observe(track.parentElement);
    }

    /* ──────────────────────────────────────────────
       PUBLIC API — gọi từ onclick trong HTML
       Ví dụ: onclick="LuCarousel.slide('trending', -1)"
    ────────────────────────────────────────────── */
    function slide(id, direction) {
        if (carousels[id]) {
            carousels[id].slide(direction);
        }
    }

    /* Expose ra global scope để dùng inline onclick */
    window.slide = slide;

    /* ──────────────────────────────────────────────
       BOOT — khởi tạo tất cả carousel khi DOM sẵn sàng
    ────────────────────────────────────────────── */
    document.addEventListener('DOMContentLoaded', function () {
        ['trending', 'genre', 'latest', 'toprated'].forEach(initCarousel);
    });

})();