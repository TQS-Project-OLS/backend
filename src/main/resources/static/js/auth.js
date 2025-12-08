// Authentication Helper Functions
(function () {
    'use strict';

    const TOKEN_KEY = 'olsheets-token';
    const USERNAME_KEY = 'olsheets-username';
    const NAME_KEY = 'olsheets-name';

    // Get JWT token from localStorage
    window.getToken = function () {
        return localStorage.getItem(TOKEN_KEY);
    };

    // Set JWT token in localStorage
    window.setToken = function (token, username, name) {
        localStorage.setItem(TOKEN_KEY, token);
        localStorage.setItem(USERNAME_KEY, username);
        localStorage.setItem(NAME_KEY, name);
    };

    // Remove JWT token from localStorage
    window.removeToken = function () {
        localStorage.removeItem(TOKEN_KEY);
        localStorage.removeItem(USERNAME_KEY);
        localStorage.removeItem(NAME_KEY);
    };

    // Check if user is authenticated
    window.isAuthenticated = function () {
        return !!getToken();
    };

    // Get current username
    window.getCurrentUsername = function () {
        return localStorage.getItem(USERNAME_KEY);
    };

    // Get current user's name
    window.getCurrentName = function () {
        return localStorage.getItem(NAME_KEY);
    };

    // Logout function
    window.logout = function () {
        removeToken();
        window.location.href = 'login.html';
    };

    // Fetch with authentication header
    window.authenticatedFetch = function (url, options = {}) {
        const token = getToken();

        if (!token) {
            window.location.href = 'login.html';
            return Promise.reject(new Error('Not authenticated'));
        }

        // Add Authorization header
        options.headers = options.headers || {};
        options.headers['Authorization'] = `Bearer ${token}`;

        return fetch(url, options);
    };

    // Redirect to login if not authenticated (for protected pages)
    window.requireAuth = function () {
        if (!isAuthenticated()) {
            window.location.href = 'login.html';
            return false;
        }
        return true;
    };

    // Auto-redirect to index if already authenticated (for login/signup pages)
    window.redirectIfAuthenticated = function () {
        if (isAuthenticated()) {
            window.location.href = 'index.html';
        }
    };
})();
