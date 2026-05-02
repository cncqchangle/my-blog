/**
 * Main Application Controller - Classic Blog Version
 */
marked.setOptions({
    breaks: true
});

const App = {
    state: {
        currentUser: null,
        homeData: null,
        currentNote: null
    },

    async init() {
        console.log('App initializing...');
        this.bindGlobalEvents();
        Router.init();
    },

    bindGlobalEvents() {
        // Delegate clicks on dynamic elements
        document.addEventListener('click', async (e) => {
            // Handle Navigation Items (Active State)
            const navItem = e.target.closest('.nav-item');
            if (navItem) {
                document.querySelectorAll('.nav-item').forEach(l => l.classList.remove('active'));
                navItem.classList.add('active');
                return;
            }

            // Article Card Click
            const articleItem = e.target.closest('.article-item');
            if (articleItem && articleItem.dataset.id && !e.target.closest('a') && !e.target.closest('button')) {
                const id = articleItem.dataset.id;
                Router.navigate(`#note/${id}`);
                return;
            }

            // Action Buttons
            if (e.target.closest('#btn-logout-nav')) {
                await this.handleLogout();
                return;
            }

            if (e.target.closest('#btn-add-folder-side')) {
                this.showAddFolderModal();
                return;
            }

            if (e.target.closest('#btn-create-note-empty') || e.target.closest('#btn-create-note-bottom') || e.target.closest('#btn-create-note-nav')) {
                const hash = window.location.hash;
                if (hash.startsWith('#folder/')) {
                    const folderId = hash.split('/')[1];
                    Router.navigate(`#note/new?folderId=${folderId}`);
                } else {
                    Router.navigate('#note/new');
                }
                return;
            }

            if (e.target.closest('#btn-edit-note')) {
                Router.navigate(`#note/edit/${this.state.currentNote.noteId}`);
                return;
            }

            // Global search result click
            const searchResult = e.target.closest('.search-dropdown-item');
            if (searchResult) {
                const account = searchResult.dataset.account;
                Router.navigate(`#user/${account}/home`);
                document.getElementById('global-search').value = '';
                document.getElementById('global-search-results').classList.add('hidden');
                return;
            }

            // Hide search results when clicking outside
            if (!e.target.closest('.search-bar')) {
                const results = document.getElementById('global-search-results');
                if (results) results.classList.add('hidden');
            }

            // Rename Folder
            const btnRenameFolder = e.target.closest('.btn-rename-folder');
            if (btnRenameFolder) {
                e.preventDefault();
                const folderId = btnRenameFolder.dataset.id;
                const oldName = btnRenameFolder.dataset.name;
                const newName = prompt('输入新的分类名称：', oldName);
                if (newName && newName.trim() !== '' && newName !== oldName) {
                    try {
                        await API.renameFolder(folderId, newName.trim());
                        UI.showToast('分类已重命名');
                        await this.refreshHomeData();
                        this.reRenderCurrentView();
                    } catch (error) {
                        console.error('Rename folder failed:', error);
                    }
                }
                return;
            }

            // Delete Folder
            const btnDeleteFolder = e.target.closest('.btn-delete-folder');
            if (btnDeleteFolder) {
                e.preventDefault();
                const folderId = btnDeleteFolder.dataset.id;
                if (confirm('确定要删除该分类及其下的所有笔记吗？此操作不可恢复。')) {
                    try {
                        await API.deleteFolder(folderId);
                        UI.showToast('分类已删除');
                        await this.refreshHomeData();
                        const currentHash = window.location.hash;
                        if (currentHash === `#folder/${folderId}`) {
                            Router.navigate('#home');
                        } else {
                            // Re-render current view to update sidebar and potentially note list
                            this.reRenderCurrentView();
                        }
                    } catch (err) {
                        UI.showToast(err.message, 'error');
                    }
                }
                return;
            }

            // Delete Note (List and Detail)
            const btnDeleteNote = e.target.closest('.btn-delete-note') || e.target.closest('#btn-delete-note-detail');
            if (btnDeleteNote) {
                e.preventDefault();
                const noteId = btnDeleteNote.dataset.id;
                if (confirm('确定要删除这篇笔记吗？')) {
                    try {
                        await API.deleteNote(noteId);
                        UI.showToast('笔记已删除');
                        await this.refreshHomeData();
                        const currentHash = window.location.hash;
                        if (currentHash.startsWith(`#note/${noteId}`)) {
                            Router.navigate('#home');
                        } else {
                            this.reRenderCurrentView();
                        }
                    } catch (err) {
                        UI.showToast(err.message, 'error');
                    }
                }
                return;
            }
        });

        // Global Search Logic
        const globalSearch = document.getElementById('global-search');
        const globalResults = document.getElementById('global-search-results');
        if (globalSearch) {
            let debounceTimer;
            globalSearch.oninput = () => {
                clearTimeout(debounceTimer);
                const q = globalSearch.value.trim();
                if (!q) {
                    globalResults.classList.add('hidden');
                    return;
                }

                debounceTimer = setTimeout(async () => {
                    try {
                        const data = await API.searchUsers(q);
                        if (data.results.length === 0) {
                            globalResults.innerHTML = '<div style="padding: 15px; text-align: center; color: #999; font-size: 14px;">未找到用户</div>';
                        } else {
                            globalResults.innerHTML = data.results.map(user => `
                                <div class="search-dropdown-item" data-account="${user.account}">
                                    <div class="avatar">${user.account.charAt(0).toUpperCase()}</div>
                                    <div class="info">
                                        <div class="name">${user.account}</div>
                                        <div class="meta">查看博主主页</div>
                                    </div>
                                </div>
                            `).join('');
                        }
                        globalResults.classList.remove('hidden');
                    } catch (err) {
                        console.error('Search error:', err);
                    }
                }, 300);
            };
        }
    },

    reRenderCurrentView() {
        const hash = window.location.hash || '#home';
        if (hash === '#home') {
            UI.renderHome(this.state.homeData, this.state.currentUser);
        } else if (hash.startsWith('#folder/')) {
            const folderId = hash.split('/')[1];
            UI.renderFolderView(folderId, this.state.homeData, this.state.currentUser);
        }
    },

    /**
     * View Controllers
     */

    async showLogin() {
        UI.renderLogin();
        const form = document.getElementById('login-form');
        if (form) {
            form.onsubmit = async (e) => {
                e.preventDefault();
                const formData = new FormData(form);
                try {
                    const user = await API.login(formData.get('account'), formData.get('password'));
                    this.state.currentUser = user;
                    UI.showToast(`欢迎回来, ${user.account}`);
                    Router.navigate('#home');
                } catch (err) {
                    UI.showToast(err.message, 'error');
                }
            };
        }
    },

    async showRegister() {
        UI.renderRegister();
        const form = document.getElementById('register-form');
        if (form) {
            form.onsubmit = async (e) => {
                e.preventDefault();
                const formData = new FormData(form);
                try {
                    const user = await API.register(formData.get('account'), formData.get('password'));
                    this.state.currentUser = user;
                    UI.showToast(`账号创建成功，欢迎加入 ${user.account}`);
                    Router.navigate('#home');
                } catch (err) {
                    UI.showToast(err.message, 'error');
                }
            };
        }
    },

    async showHome() {
        try {
            const data = await API.getMeHome();
            this.state.homeData = data;
            this.state.currentUser = { account: data.ownerAccount };
            UI.renderHome(data, this.state.currentUser);
        } catch (err) {
            Router.navigate('#login');
        }
    },

    async showFolder(folderId) {
        if (!this.state.homeData) {
            await this.refreshHomeData();
        }
        UI.renderFolderView(folderId, this.state.homeData, this.state.currentUser);
    },

    async showNote(noteId) {
        try {
            if (!this.state.currentUser) {
                const homeData = await API.getMeHome();
                this.state.currentUser = { account: homeData.ownerAccount };
            }
            const note = await API.getNote(noteId);
            this.state.currentNote = note;
            UI.render(UI.templates.noteDetail(note));
            UI.updateNavbar(this.state.currentUser.account);
        } catch (err) {
            UI.showToast(err.message, 'error');
            Router.navigate('#home');
        }
    },

    async showNoteEditor(noteId = null, folderId = null) {
        if (!this.state.homeData) await this.refreshHomeData();
        if (!this.state.currentUser) {
            const homeData = await API.getMeHome();
            this.state.currentUser = { account: homeData.ownerAccount };
        }
        
        let note = null;
        if (noteId) {
            try {
                note = await API.getNote(noteId);
            } catch (err) {
                UI.showToast('无法加载文章进行编辑', 'error');
                return;
            }
        }

        UI.render(UI.templates.noteEditor(note, this.state.homeData.folders, folderId), false);
        this.initEditorEvents(note);
    },

    /**
     * Logic & Helpers
     */

    async refreshHomeData() {
        try {
            const data = await API.getMeHome();
            this.state.homeData = data;
            this.state.currentUser = { account: data.ownerAccount };
        } catch (err) {
            Router.navigate('#login');
        }
    },

    async handleLogout() {
        await API.logout();
        this.state.currentUser = null;
        this.state.homeData = null;
        UI.showToast('已安全退出登录');
        Router.navigate('#login');
    },

    showAddFolderModal() {
        UI.showModal(`
            <div style="padding: 10px;">
                <h3 style="margin-bottom: 20px;">新建分类</h3>
                <div class="auth-form">
                    <div class="input-group">
                        <label>分类名称</label>
                        <input type="text" id="new-folder-name" placeholder="请输入分类名称..." autofocus>
                    </div>
                </div>
                <div style="display: flex; justify-content: flex-end; gap: 10px; margin-top: 20px;">
                    <button class="btn-secondary" onclick="UI.hideModal()">取消</button>
                    <button id="confirm-add-folder" class="btn-primary">确认创建</button>
                </div>
            </div>
        `);

        document.getElementById('confirm-add-folder').onclick = async () => {
            const name = document.getElementById('new-folder-name').value;
            if (!name) return;
            try {
                await API.createFolder(name);
                UI.hideModal();
                UI.showToast('分类创建成功');
                await this.refreshHomeData();
                UI.updateSidebar(this.state.homeData);
            } catch (err) {
                UI.showToast(err.message, 'error');
            }
        };
    },

    initEditorEvents(existingNote) {
        const markdownInput = document.getElementById('editor-markdown');
        const previewArea = document.getElementById('editor-preview');
        const titleInput = document.getElementById('editor-title');
        const folderSelect = document.getElementById('editor-folder');
        const coverInput = document.getElementById('editor-cover');
        const saveBtn = document.getElementById('btn-save-note');

        // Initialize CodeMirror
        const cm = CodeMirror.fromTextArea(markdownInput, {
            mode: 'markdown',
            theme: 'monokai',
            lineWrapping: true,
            viewportMargin: Infinity
        });

        const updatePreview = () => {
            const raw = cm.getValue();
            const cleanHtml = DOMPurify.sanitize(marked.parse(raw));
            previewArea.innerHTML = cleanHtml;
            previewArea.querySelectorAll('pre code').forEach(el => hljs.highlightElement(el));
        };

        cm.on('change', updatePreview);
        updatePreview();

        const updateCoverPreview = () => {
            const previewArea = document.getElementById('cover-preview');
            
            if (coverInput.files && coverInput.files[0]) {
                const reader = new FileReader();
                reader.onload = (e) => {
                    previewArea.innerHTML = `<img src="${e.target.result}" style="width: 100%; border-radius: 4px;">`;
                };
                reader.readAsDataURL(coverInput.files[0]);
                return;
            }

            if (titleInput.value.trim()) {
                previewArea.innerHTML = `
                    <div class="cover-auto-hint">
                        <i class="ph ph-magic-wand"></i>
                        <span>系统将根据该标题自动生成封面</span>
                    </div>
                `;
                return;
            }

            if (existingNote && existingNote.coverImageUrl) {
                previewArea.innerHTML = `<img src="${existingNote.coverImageUrl}" style="width: 100%; border-radius: 4px;">`;
                return;
            }

            previewArea.innerHTML = '';
        };

        titleInput.oninput = updateCoverPreview;
        coverInput.onchange = updateCoverPreview;
        updateCoverPreview();

        saveBtn.onclick = async () => {
            if (!titleInput.value) { UI.showToast('请输入标题', 'error'); return; }
            if (!folderSelect.value) { UI.showToast('请选择文章分类', 'error'); return; }
            
            const formData = new FormData();
            formData.append('title', titleInput.value);
            formData.append('markdownContent', cm.getValue());
            formData.append('folderId', folderSelect.value);
            if (coverInput.files[0]) {
                formData.append('coverImage', coverInput.files[0]);
            }

            try {
                saveBtn.disabled = true;
                saveBtn.textContent = '发布中...';
                
                let result;
                if (existingNote) {
                    result = await API.updateNote(existingNote.noteId, formData);
                } else {
                    result = await API.createNote(formData);
                }
                
                UI.showToast('文章已成功发布');
                Router.navigate(`#note/${result.noteId}`);
                await this.refreshHomeData();
            } catch (err) {
                UI.showToast(err.message, 'error');
                saveBtn.disabled = false;
                saveBtn.textContent = existingNote ? '保存修改' : '发布文章';
            }
        };

        // Quick Add Folder
        const btnQuickAdd = document.getElementById('btn-quick-add-folder');
        if (btnQuickAdd) {
            btnQuickAdd.onclick = async () => {
                UI.showModal(`
                    <div style="padding: 10px;">
                        <h3 style="margin-bottom: 20px;">新建分类</h3>
                        <div class="auth-form">
                            <div class="input-group">
                                <label>分类名称</label>
                                <input type="text" id="new-folder-name" placeholder="请输入分类名称..." autofocus>
                            </div>
                        </div>
                        <div style="display: flex; justify-content: flex-end; gap: 10px; margin-top: 20px;">
                            <button class="btn-secondary" onclick="UI.hideModal()">取消</button>
                            <button id="confirm-quick-add-folder" class="btn-primary">确认创建</button>
                        </div>
                    </div>
                `);

                document.getElementById('confirm-quick-add-folder').onclick = async () => {
                    const name = document.getElementById('new-folder-name').value;
                    if (!name) return;
                    try {
                        const newFolder = await API.createFolder(name);
                        UI.hideModal();
                        UI.showToast('分类创建成功');
                        await this.refreshHomeData();
                        UI.updateSidebar(this.state.homeData);
                        
                        // Update the dropdown in current editor
                        const currentFolders = this.state.homeData.folders;
                        const placeholder = `<option value="" disabled>请选择文章分类...</option>`;
                        folderSelect.innerHTML = placeholder + currentFolders.map(f => `
                            <option value="${f.folderId}" ${f.folderId == newFolder.folderId ? 'selected' : ''}>${f.folderName}</option>
                        `).join('');
                    } catch (err) {
                        UI.showToast(err.message, 'error');
                    }
                };
            };
        }
    },

    async showSearch() {
        UI.render(UI.templates.searchPage());
        if (!this.state.currentUser) {
            const homeData = await API.getMeHome();
            this.state.currentUser = { account: homeData.ownerAccount };
        }
        UI.updateNavbar(this.state.currentUser.account);

        const input = document.getElementById('user-search-input');
        const resultsDiv = document.getElementById('search-results');
        
        let debounceTimer;
        input.oninput = () => {
            clearTimeout(debounceTimer);
            debounceTimer = setTimeout(async () => {
                const q = input.value.trim();
                if (!q) return;
                
                try {
                    const data = await API.searchUsers(q);
                    if (data.results.length === 0) {
                        resultsDiv.innerHTML = `<div style="text-align: center; color: #999; padding: 40px;">未找到匹配的用户</div>`;
                    } else {
                        resultsDiv.innerHTML = data.results.map(user => `
                            <div class="article-item" style="cursor: pointer; border-radius: 4px;" onclick="location.hash='#user/${user.account}/home'">
                                <div class="user-card-top" style="margin-bottom: 0;">
                                    <div class="user-card-avatar" style="width: 40px; height: 40px; font-size: 16px;">${user.account.charAt(0).toUpperCase()}</div>
                                    <div>
                                        <div class="user-card-name" style="font-size: 16px;">${user.account}</div>
                                        <div style="font-size: 12px; color: #999;">点击访问其公开博客</div>
                                    </div>
                                </div>
                            </div>
                        `).join('');
                    }
                } catch (err) {}
            }, 300);
        };
    },

    async showUserHome(account) {
        try {
            if (!this.state.currentUser) {
                const myHomeData = await API.getMeHome();
                this.state.currentUser = { account: myHomeData.ownerAccount };
            }

            const data = await API.getUserHome(account);
            this.state.homeData = data; 
            
            UI.renderHome(data, this.state.currentUser);
            
            if (!data.isViewerOwner) {
                const addBtn = document.getElementById('btn-add-folder-side');
                if (addBtn) addBtn.style.display = 'none';
            }
        } catch (err) {
            UI.showToast('无法加载用户主页', 'error');
            Router.navigate('#home');
        }
    }
};

// Start the app
App.init();
