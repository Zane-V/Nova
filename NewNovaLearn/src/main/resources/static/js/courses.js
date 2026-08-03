document.addEventListener('DOMContentLoaded', () => {

    // ---- Filter Pills ----
    const pills = document.querySelectorAll('.pill');
    const cards = document.querySelectorAll('.course-card');
    const emptyState = document.getElementById('emptyState');

    function filterCards(filter) {
        let visibleCount = 0;
        cards.forEach(card => {
            const category = card.getAttribute('data-category');
            const matches = filter === 'all' || category === filter;
            if (matches) {
                card.classList.remove('hidden');
                visibleCount++;
            } else {
                card.classList.add('hidden');
            }
        });
        emptyState.style.display = visibleCount === 0 ? 'block' : 'none';
    }

    pills.forEach(pill => {
        pill.addEventListener('click', () => {
            pills.forEach(p => p.classList.remove('active'));
            pill.classList.add('active');
            filterCards(pill.getAttribute('data-filter'));
        });
    });

    // ---- Search ----
    const searchInput = document.getElementById('courseSearch');
    const searchBtn = document.querySelector('.search-btn');

    function performSearch() {
        const query = searchInput.value.trim().toLowerCase();
        let visibleCount = 0;

        // reset filter pills to 'all' when searching
        pills.forEach(p => p.classList.remove('active'));
        document.querySelector('.pill[data-filter="all"]').classList.add('active');

        cards.forEach(card => {
            const title = card.querySelector('h3').textContent.toLowerCase();
            const tag   = card.querySelector('.card-tag').textContent.toLowerCase();
            const matches = query === '' || title.includes(query) || tag.includes(query);
            if (matches) {
                card.classList.remove('hidden');
                visibleCount++;
            } else {
                card.classList.add('hidden');
            }
        });

        emptyState.style.display = visibleCount === 0 ? 'block' : 'none';
    }

    searchInput.addEventListener('input', performSearch);
    searchBtn.addEventListener('click', performSearch);
    searchInput.addEventListener('keydown', (e) => {
        if (e.key === 'Enter') performSearch();
    });

    // ---- Hover lift on cards (touch-friendly) ----
    cards.forEach(card => {
        card.addEventListener('mouseenter', () => {
            card.style.transition = 'transform 0.3s ease, box-shadow 0.3s ease';
        });
    });
});
