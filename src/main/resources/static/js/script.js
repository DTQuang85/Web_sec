function showToast(message, type) {
    const toastEl = document.getElementById('appToast');
    if (!toastEl || !window.bootstrap) return;
    const body = toastEl.querySelector('.toast-body');
    if (!body) return;
    body.textContent = message;
    toastEl.classList.remove('text-bg-success', 'text-bg-danger', 'text-bg-warning');
    toastEl.classList.add(type === 'error' ? 'text-bg-danger' : 'text-bg-success');
    new bootstrap.Toast(toastEl).show();
}

function setupProductViewToggle() {
    const gridButton = document.getElementById('gridViewBtn');
    const listButton = document.getElementById('listViewBtn');
    const container = document.getElementById('productGrid');
    if (!gridButton || !listButton || !container) return;

    gridButton.addEventListener('click', function () {
        container.classList.remove('list-mode');
    });

    listButton.addEventListener('click', function () {
        container.classList.add('list-mode');
    });
}

function setupCategoryFilter() {
    const categoryButtons = document.querySelectorAll('[data-category]');
    const keywordInput = document.getElementById('keywordInput');
    categoryButtons.forEach(function (button) {
        button.addEventListener('click', function () {
            const category = button.getAttribute('data-category');
            if (keywordInput && category) {
                keywordInput.value = category.split('|')[0];
                const form = document.getElementById('searchForm');
                if (form) form.submit();
            }
        });
    });
}

function setupTooltips() {
    if (!window.bootstrap) return;
    const triggers = document.querySelectorAll('[data-bs-toggle="tooltip"]');
    triggers.forEach(function (el) {
        new bootstrap.Tooltip(el);
    });
}

function setupLoadingOverlay() {
    const overlay = document.getElementById('loadingOverlay');
    if (!overlay) return;

    document.querySelectorAll('form').forEach(function (form) {
        form.addEventListener('submit', function () {
            overlay.classList.remove('d-none');
        });
    });

    window.addEventListener('pageshow', function () {
        overlay.classList.add('d-none');
    });
}

function setupHeaderScrollEffect() {
    const header = document.querySelector('.fixed-header, .site-header');
    if (!header) return;

    window.addEventListener('scroll', function () {
        if (window.scrollY > 50) {
            header.classList.add('scrolled');
        } else {
            header.classList.remove('scrolled');
        }
    });
}

function setupHeroBannerCarousel() {
    if (!window.bootstrap) return;
    const carouselEl = document.getElementById('heroBannerCarousel');
    if (!carouselEl) return;

    const carousel = bootstrap.Carousel.getOrCreateInstance(carouselEl, {
        interval: 2800,
        ride: 'carousel',
        pause: false,
        wrap: true,
        touch: true
    });
    carousel.cycle();
}

function setupProductCardClick() {
    const clickableCards = document.querySelectorAll('.product-card-clickable[data-detail-url]');
    if (!clickableCards.length) return;

    clickableCards.forEach(function (card) {
        card.addEventListener('click', function (event) {
            // Keep default behavior for real interactive controls inside card.
            if (event.target.closest('a, button, input, select, textarea, label, form')) {
                return;
            }

            const detailUrl = card.getAttribute('data-detail-url');
            if (detailUrl) {
                window.location.href = detailUrl;
            }
        });
    });
}

document.addEventListener('DOMContentLoaded', function () {
    setupProductViewToggle();
    setupCategoryFilter();
    setupTooltips();
    setupLoadingOverlay();
    setupHeaderScrollEffect();
    setupHeroBannerCarousel();
    setupProductCardClick();
});
