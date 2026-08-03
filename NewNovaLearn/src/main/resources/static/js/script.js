document.addEventListener('DOMContentLoaded', () => {
    // Mobile menu toggle
    const menuToggle = document.querySelector('.menu-toggle');
    const bottomNav = document.querySelector('.bottom-nav');

    if (menuToggle) {
        menuToggle.addEventListener('click', () => {
            // Simple animation for the menu icon
            const icon = menuToggle.querySelector('i');
            if (icon.classList.contains('fa-bars')) {
                icon.classList.remove('fa-bars');
                icon.classList.add('fa-times');
            } else {
                icon.classList.remove('fa-times');
                icon.classList.add('fa-bars');
            }
        });
    }

    // Add subtle hover effects to cards
    const cards = document.querySelectorAll('.domain-card, .eco-card, .cta-card');
    cards.forEach(card => {
        // We'll rely on the animations.css CSS transitions for hover,
        // but this JS can remain for fallback or extra logic.
    });

    // ── Sticky Navbar Scroll Shadow ──
    const dashNav = document.querySelector('.dash-navbar');
    if (dashNav) {
        window.addEventListener('scroll', () => {
            if (window.scrollY > 10) {
                dashNav.classList.add('scrolled');
            } else {
                dashNav.classList.remove('scrolled');
            }
        });
    }

    // ── Button Ripple Effect ──
    const buttons = document.querySelectorAll('.btn-primary, .btn-block, .btn-verify');
    buttons.forEach(btn => {
        btn.addEventListener('mousedown', function (e) {
            // Remove any existing ripples
            const existingRipples = this.querySelectorAll('.btn-ripple-effect');
            existingRipples.forEach(r => r.remove());

            const ripple = document.createElement('span');
            ripple.classList.add('btn-ripple-effect');

            const rect = this.getBoundingClientRect();
            const x = e.clientX - rect.left;
            const y = e.clientY - rect.top;

            const maxDim = Math.max(rect.width, rect.height);
            ripple.style.width = ripple.style.height = `${maxDim}px`;
            ripple.style.left = `${x - maxDim / 2}px`;
            ripple.style.top = `${y - maxDim / 2}px`;

            this.style.position = 'relative';
            this.style.overflow = 'hidden';
            this.appendChild(ripple);

            // Clean up ripple after animation (0.55s)
            setTimeout(() => {
                ripple.remove();
            }, 600);
        });
    });
});
