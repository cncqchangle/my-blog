/**
 * Router Service - Handles navigation and hash changes
 */
const Router = {
    routes: {
        '#login': () => App.showLogin(),
        '#register': () => App.showRegister(),
        '#home': () => App.showHome(),
        '#folder/(\\d+)': (id) => App.showFolder(id),
        '#note/(\\d+)': (id) => App.showNote(id),
        '#note/new(?:\\?folderId=(\\d+))?': (folderId) => App.showNoteEditor(null, folderId),
        '#note/edit/(\\d+)': (id) => App.showNoteEditor(id),
        '#search': () => App.showSearch(),
        '#user/([^/]+)/home': (account) => App.showUserHome(account)
    },

    init() {
        window.addEventListener('hashchange', () => this.handleRoute());
        this.handleRoute();
    },

    navigate(hash) {
        window.location.hash = hash;
    },

    handleRoute() {
        const hash = window.location.hash || '#home';
        
        for (const [pattern, handler] of Object.entries(this.routes)) {
            const regex = new RegExp(`^${pattern}$`);
            const match = hash.match(regex);
            
            if (match) {
                const args = match.slice(1);
                handler(...args);
                return;
            }
        }
        
        // Default route
        this.navigate('#home');
    }
};
