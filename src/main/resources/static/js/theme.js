// Theme Management
(function () {
    'use strict';

    const THEME_KEY = 'olsheets-theme';
    const THEME_LIGHT = 'light';
    const THEME_DARK = 'dark';

    // Get saved theme or default to dark
    function getSavedTheme() {
        return localStorage.getItem(THEME_KEY) || THEME_DARK;
    }

    // Apply theme to document
    function applyTheme(theme) {
        if (theme === THEME_LIGHT) {
            document.documentElement.setAttribute('data-theme', 'light');
        } else {
            document.documentElement.removeAttribute('data-theme');
        }
        localStorage.setItem(THEME_KEY, theme);
    }

    // Toggle between themes
    function toggleTheme() {
        const currentTheme = getSavedTheme();
        const newTheme = currentTheme === THEME_LIGHT ? THEME_DARK : THEME_LIGHT;
        applyTheme(newTheme);
        updateThemeIcon(newTheme);
    }

    // Update theme toggle icon
    function updateThemeIcon(theme) {
        const toggleBtn = document.getElementById('theme-toggle');
        if (toggleBtn) {
            toggleBtn.innerHTML = theme === THEME_LIGHT ? '🌙' : '☀️';
            toggleBtn.setAttribute('aria-label', `Switch to ${theme === THEME_LIGHT ? 'dark' : 'light'} mode`);
        }
    }

    // Initialize theme on page load
    function initTheme() {
        const savedTheme = getSavedTheme();
        applyTheme(savedTheme);

        // Wait for DOM to be ready
        if (document.readyState === 'loading') {
            document.addEventListener('DOMContentLoaded', () => updateThemeIcon(savedTheme));
        } else {
            updateThemeIcon(savedTheme);
        }
    }

    // Expose toggle function globally
    window.toggleTheme = toggleTheme;

    // Initialize immediately
    initTheme();
})();
