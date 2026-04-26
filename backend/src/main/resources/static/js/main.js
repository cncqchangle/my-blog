import { auth, blog } from './api.js';
import { Router } from './router.js';

const app = document.getElementById('app');
const router = new Router(app);

let currentUser = null;
let currentHomeData = null; // Store folders and notes tree

// --- UI Components ---

function renderHeader() {
    return `
        <header>
            <div class="logo" onclick="window.location.hash='#/'">Minimal Blog</div>
            <div class="search-container">
                <input type="text" class="search-input" placeholder="Search users..." id="user-search">
                <div class="search-results hidden" id="search-results"></div>
            </div>
            <div class="user-nav">
                <div class="avatar" id="user-avatar">
                    <svg viewBox="0 0 24 24"><path d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z"/></svg>
                    <div class="dropdown">
                        <div class="dropdown-item" id="logout-btn">Logout</div>
                    </div>
                </div>
            </div>
        </header>
    `;
}

function initHeaderEvents() {
    const searchInput = document.getElementById('user-search');
    const resultsContainer = document.getElementById('search-results');
    const logoutBtn = document.getElementById('logout-btn');

    let debounceTimer;
    searchInput.addEventListener('input', (e) => {
        clearTimeout(debounceTimer);
        const q = e.target.value.trim();
        if (!q) {
            resultsContainer.classList.add('hidden');
            return;
        }

        debounceTimer = setTimeout(async () => {
            try {
                const data = await blog.searchUsers(q);
                if (data.results.length > 0) {
                    resultsContainer.innerHTML = data.results.map(user => `
                        <div class="search-result-item" data-account="${user.account}">
                            ${user.account}
                        </div>
                    `).join('');
                    resultsContainer.classList.remove('hidden');
                } else {
                    resultsContainer.innerHTML = '<div class="search-result-item">No users found</div>';
                    resultsContainer.classList.remove('hidden');
                }
            } catch (err) {
                console.error(err);
            }
        }, 300);
    });

    resultsContainer.addEventListener('click', (e) => {
        const item = e.target.closest('.search-result-item');
        if (item && item.dataset.account) {
            router.navigate(`#/user/${item.dataset.account}`);
            resultsContainer.classList.add('hidden');
            searchInput.value = '';
        }
    });

    logoutBtn.addEventListener('click', async () => {
        await auth.logout();
        currentUser = null;
        router.navigate('#/login');
    });

    // Close search results on click outside
    document.addEventListener('click', (e) => {
        if (!e.target.closest('.search-container')) {
            resultsContainer.classList.add('hidden');
        }
    });
}

function renderSidebar(homeData, activeNoteId = null) {
    return `
        <aside class="sidebar">
            <div class="sidebar-content">
                ${homeData.folders.map(folder => `
                    <div class="sidebar-folder">
                        <div class="sidebar-folder-header" onclick="this.nextElementSibling.classList.toggle('hidden')">
                            ${folder.folderName} (${folder.noteCount})
                        </div>
                        <div class="sidebar-folder-notes">
                            ${folder.notes.map(note => `
                                <div class="sidebar-note-item ${note.noteId == activeNoteId ? 'active' : ''}" 
                                     onclick="window.location.hash='#/note/${note.noteId}'">
                                    ${note.title}
                                </div>
                            `).join('')}
                        </div>
                    </div>
                `).join('')}
            </div>
        </aside>
    `;
}

// --- Views ---

async function loginView() {
    app.innerHTML = `
        <div class="auth-container">
            <div class="auth-card">
                <h1 class="auth-title" id="auth-title">Login</h1>
                <form id="auth-form">
                    <div class="form-group">
                        <label>Account</label>
                        <input type="text" name="account" class="input-field" placeholder="Enter your account" required>
                    </div>
                    <div class="form-group">
                        <label>Password</label>
                        <input type="password" name="password" class="input-field" placeholder="Enter your password" required>
                    </div>
                    <div id="auth-error" class="hidden" style="color: var(--error-color); font-size: 0.875rem; margin-bottom: 16px;"></div>
                    <button type="submit" class="btn" id="auth-submit">Sign In</button>
                </form>
                <div class="auth-switch" id="auth-switch-text">
                    Don't have an account? <span id="toggle-auth">Sign Up</span>
                </div>
            </div>
        </div>
    `;

    const form = document.getElementById('auth-form');
    const title = document.getElementById('auth-title');
    const submitBtn = document.getElementById('auth-submit');
    const toggleBtn = document.getElementById('toggle-auth');
    const errorMsg = document.getElementById('auth-error');
    const switchText = document.getElementById('auth-switch-text');

    let isLogin = true;

    toggleBtn.onclick = () => {
        isLogin = !isLogin;
        title.textContent = isLogin ? 'Login' : 'Create Account';
        submitBtn.textContent = isLogin ? 'Sign In' : 'Register';
        switchText.innerHTML = isLogin 
            ? "Don't have an account? <span id='toggle-auth'>Sign Up</span>"
            : "Already have an account? <span id='toggle-auth'>Sign In</span>";
        // Re-bind toggle event
        document.getElementById('toggle-auth').onclick = toggleBtn.onclick;
        errorMsg.classList.add('hidden');
    };

    form.onsubmit = async (e) => {
        e.preventDefault();
        submitBtn.disabled = true;
        errorMsg.classList.add('hidden');
        
        const account = form.account.value;
        const password = form.password.value;

        try {
            if (isLogin) {
                await auth.login(account, password);
            } else {
                await auth.register(account, password);
            }
            const data = await blog.getHome();
            currentUser = data.ownerAccount;
            router.navigate('#/');
        } catch (err) {
            errorMsg.textContent = err.message;
            errorMsg.classList.remove('hidden');
        } finally {
            submitBtn.disabled = false;
        }
    };
}

async function homeView(params) {
    const account = params.account || null;
    try {
        const data = await blog.getHome(account);
        currentHomeData = data;
        if (!account) currentUser = data.ownerAccount;

        app.innerHTML = `
            ${renderHeader()}
            <main class="main-content container">
                <div class="section-header">
                    <h1 class="section-title">${data.isViewerOwner ? 'My Blog' : data.ownerAccount + "'s Blog"}</h1>
                    ${data.isViewerOwner ? '<button class="btn" id="new-folder-btn" style="width: auto; padding: 8px 20px;">New Folder</button>' : ''}
                </div>
                <div class="folder-grid">
                    ${data.folders.map(folder => `
                        <div class="folder-card" data-id="${folder.folderId}">
                            <div class="folder-name">${folder.folderName}</div>
                            <div class="folder-info">${folder.noteCount} notes</div>
                        </div>
                    `).join('')}
                </div>
            </main>
        `;

        initHeaderEvents();

        if (data.isViewerOwner) {
            document.getElementById('new-folder-btn').onclick = () => showNewFolderModal();
        }

        app.addEventListener('click', (e) => {
            const card = e.target.closest('.folder-card');
            if (card) {
                const folderId = card.dataset.id;
                router.navigate(`#/folder/${folderId}${account ? '?user=' + account : ''}`);
            }
        });

    } catch (err) {
        if (err.status === 401) router.navigate('#/login');
        else console.error(err);
    }
}

async function folderView(params) {
    const folderId = params.id;
    const urlParams = new URLSearchParams(window.location.hash.split('?')[1]);
    const account = urlParams.get('user');

    try {
        const data = await blog.getHome(account);
        currentHomeData = data;
        const folder = data.folders.find(f => f.folderId == folderId);
        
        app.innerHTML = `
            ${renderHeader()}
            <main class="main-content container">
                <div class="section-header">
                    <div>
                        <div style="font-size: 0.875rem; color: var(--text-secondary); cursor: pointer;" onclick="window.location.hash='${account ? '#/user/'+account : '#/'}'">← Back to Home</div>
                        <h1 class="section-title" style="margin-top: 8px;">${folder.folderName}</h1>
                    </div>
                    ${data.isViewerOwner ? `
                        <button class="btn" id="new-note-btn" style="width: auto; padding: 8px 20px;">New Note</button>
                    ` : ''}
                </div>
                <div class="note-list-grid">
                    ${folder.notes.map(note => `
                        <div class="note-card" onclick="window.location.hash='#/note/${note.noteId}'">
                            <div class="note-card-img">${note.title[0].toUpperCase()}</div>
                            <div class="note-card-title">${note.title}</div>
                            <div class="note-card-date">Updated ${new Date(note.updatedAt).toLocaleDateString()}</div>
                        </div>
                    `).join('')}
                </div>
            </main>
        `;

        initHeaderEvents();

        if (data.isViewerOwner) {
            document.getElementById('new-note-btn').onclick = () => showNewNoteModal(folderId);
        }

    } catch (err) {
        console.error(err);
    }
}

async function noteView(params) {
    const noteId = params.id;
    try {
        const note = await blog.getNote(noteId);
        if (!currentHomeData || currentHomeData.ownerAccount !== note.authorAccount) {
            currentHomeData = await blog.getHome(note.authorAccount);
        }

        app.innerHTML = `
            ${renderHeader()}
            <div class="sidebar-layout">
                ${renderSidebar(currentHomeData, noteId)}
                <main class="content-area">
                    <div class="note-header">
                        <h1 class="note-title">${note.title}</h1>
                        <div class="note-meta">By ${note.authorAccount} • Last updated ${new Date(note.updatedAt).toLocaleDateString()}</div>
                    </div>
                    
                    ${note.isEditable ? `
                        <div class="note-actions">
                            <button class="btn-secondary active" id="view-mode-btn">View</button>
                            <button class="btn-secondary" id="edit-mode-btn">Edit</button>
                        </div>
                    ` : ''}

                    <div id="note-display" class="note-content">
                        ${note.renderedHtml}
                    </div>

                    <div id="note-editor" class="hidden">
                        <textarea class="editor-textarea" id="markdown-input">${note.markdownContent}</textarea>
                        <div style="margin-top: 24px;">
                            <button class="btn" id="save-note-btn" style="width: auto; padding: 10px 32px;">Save Changes</button>
                        </div>
                    </div>
                </main>
            </div>
        `;

        initHeaderEvents();

        if (note.isEditable) {
            const viewBtn = document.getElementById('view-mode-btn');
            const editBtn = document.getElementById('edit-mode-btn');
            const display = document.getElementById('note-display');
            const editor = document.getElementById('note-editor');
            const markdownInput = document.getElementById('markdown-input');
            const saveBtn = document.getElementById('save-note-btn');

            viewBtn.onclick = () => {
                viewBtn.classList.add('active');
                editBtn.classList.remove('active');
                display.classList.remove('hidden');
                editor.classList.add('hidden');
            };

            editBtn.onclick = () => {
                editBtn.classList.add('active');
                viewBtn.classList.remove('active');
                editor.classList.remove('hidden');
                display.classList.add('hidden');
            };

            saveBtn.onclick = async () => {
                saveBtn.disabled = true;
                try {
                    const res = await blog.updateNote(noteId, {
                        title: note.title,
                        markdownContent: markdownInput.value,
                        folderId: note.folderId
                    });
                    display.innerHTML = res.renderedHtml;
                    viewBtn.onclick();
                } catch (err) {
                    alert(err.message);
                } finally {
                    saveBtn.disabled = false;
                }
            };
        }

    } catch (err) {
        console.error(err);
    }
}

// --- Modals ---

function showNewFolderModal() {
    const modal = document.createElement('div');
    modal.className = 'modal-overlay';
    modal.innerHTML = `
        <div class="modal-content">
            <h2 class="modal-title">New Folder</h2>
            <div class="form-group">
                <label>Folder Name</label>
                <input type="text" id="new-folder-name" class="input-field" placeholder="e.g. Personal, Tech" autofocus>
            </div>
            <div class="modal-actions">
                <button class="btn-secondary" onclick="this.closest('.modal-overlay').remove()">Cancel</button>
                <button class="btn" id="confirm-new-folder" style="width: auto; padding: 8px 24px;">Create</button>
            </div>
        </div>
    `;
    document.body.appendChild(modal);

    document.getElementById('confirm-new-folder').onclick = async () => {
        const name = document.getElementById('new-folder-name').value.trim();
        if (!name) return;
        try {
            await blog.createFolder(name);
            modal.remove();
            homeView({});
        } catch (err) {
            alert(err.message);
        }
    };
}

function showNewNoteModal(folderId) {
    const modal = document.createElement('div');
    modal.className = 'modal-overlay';
    modal.innerHTML = `
        <div class="modal-content">
            <h2 class="modal-title">New Note</h2>
            <div class="form-group">
                <label>Title</label>
                <input type="text" id="new-note-title" class="input-field" placeholder="Note title" autofocus>
            </div>
            <div class="modal-actions">
                <button class="btn-secondary" onclick="this.closest('.modal-overlay').remove()">Cancel</button>
                <button class="btn" id="confirm-new-note" style="width: auto; padding: 8px 24px;">Create</button>
            </div>
        </div>
    `;
    document.body.appendChild(modal);

    document.getElementById('confirm-new-note').onclick = async () => {
        const title = document.getElementById('new-note-title').value.trim();
        if (!title) return;
        try {
            const note = await blog.createNote({
                title,
                markdownContent: '# ' + title,
                folderId: parseInt(folderId)
            });
            modal.remove();
            router.navigate(`#/note/${note.noteId}`);
        } catch (err) {
            alert(err.message);
        }
    };
}

// --- App Initialization ---

router.addRoute('#/login', loginView);
router.addRoute('#/', homeView);
router.addRoute('#/user/:account', homeView);
router.addRoute('#/folder/:id', folderView);
router.addRoute('#/note/:id', noteView);

// Initial auth check
(async () => {
    try {
        const data = await blog.getHome();
        currentUser = data.ownerAccount;
        router.handleRoute();
    } catch (err) {
        if (err.status === 401) {
            router.navigate('#/login');
            router.handleRoute();
        } else {
            console.error('Init failed', err);
        }
    }
})();
