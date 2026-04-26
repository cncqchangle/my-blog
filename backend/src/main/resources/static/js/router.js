export class Router {
    constructor(appElement) {
        this.app = appElement;
        this.routes = {};
        window.addEventListener('hashchange', () => this.handleRoute());
    }

    addRoute(path, handler) {
        this.routes[path] = handler;
    }

    handleRoute() {
        const hash = window.location.hash || '#/';
        let matched = null;
        let params = {};

        // Simple param matching for routes like #/note/:id or #/user/:account
        for (const path in this.routes) {
            const regexPath = path.replace(/:\w+/g, '([^/]+)');
            const match = hash.match(new RegExp(`^${regexPath}$`));
            if (match) {
                matched = this.routes[path];
                const paramNames = (path.match(/:\w+/g) || []).map(p => p.slice(1));
                paramNames.forEach((name, i) => params[name] = match[i + 1]);
                break;
            }
        }

        if (matched) {
            matched(params);
        } else {
            console.error('No route matched', hash);
            window.location.hash = '#/';
        }
    }

    navigate(path) {
        window.location.hash = path;
    }
}
