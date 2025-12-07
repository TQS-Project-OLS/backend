document.addEventListener('DOMContentLoaded', () => {
    const navHTML = `
        <div class="nav-container">
            <a href="index.html" class="nav-brand">🎸 OLSHEETS</a>
            <div class="nav-links">
                <a href="index.html" class="nav-link">Discover</a>
                <a href="rent-up.html" class="nav-link">Rent Up</a>
                <a href="my-bookings.html" class="nav-link">My Bookings</a>
                <a href="manage.html" class="nav-link">Manage</a>
                <button id="theme-toggle" class="theme-toggle" onclick="toggleTheme()" aria-label="Toggle theme">☀️</button>
            </div>
        </div>
    `;

    const nav = document.createElement('nav');
    nav.className = 'navbar';
    nav.innerHTML = navHTML;

    document.body.insertBefore(nav, document.body.firstChild);

    // Set active link
    const currentPath = window.location.pathname.split('/').pop() || 'index.html';
    const links = document.querySelectorAll('.nav-link');
    links.forEach(link => {
        if (link.getAttribute('href') === currentPath) {
            link.classList.add('active');
        }
    });
});
