/**
 * UI Component & Rendering Service - Classic Blog Version
 */
const UI = {
    // Core Elements
    app: document.getElementById('app'),
    navbar: document.getElementById('navbar'),
    mainLayout: document.getElementById('main-layout'),
    viewContainer: document.getElementById('view-container'),
    sidebar: document.getElementById('sidebar'),
    modalContainer: document.getElementById('modal-container'),
    modalContent: document.getElementById('modal-content'),
    toastContainer: document.getElementById('toast-container'),

    /**
     * HTML Templates
     */
    templates: {
        loginPage: () => `
            <div class="auth-page">
                <div class="auth-card">
                    <div class="auth-logo">
                        <i class="ph-fill ph-article"></i> MyBlog
                    </div>
                    <form id="login-form" class="auth-form">
                        <div class="input-group">
                            <label>账号</label>
                            <input type="text" name="account" placeholder="请输入您的账号" required>
                        </div>
                        <div class="input-group">
                            <label>密码</label>
                            <input type="password" name="password" placeholder="请输入密码" required>
                        </div>
                        <button type="submit" class="btn-auth">登录系统</button>
                    </form>
                    <div style="margin-top: 20px; font-size: 13px; color: #999;">
                        还没有账号？<a href="#register" style="color: var(--primary-color);">立即注册</a>
                    </div>
                </div>
            </div>
        `,

        registerPage: () => `
            <div class="auth-page">
                <div class="auth-card">
                    <div class="auth-logo">
                        <i class="ph-fill ph-article"></i> MyBlog
                    </div>
                    <form id="register-form" class="auth-form">
                        <div class="input-group">
                            <label>设置账号</label>
                            <input type="text" name="account" placeholder="请设置您的账号" required>
                        </div>
                        <div class="input-group">
                            <label>设置密码</label>
                            <input type="password" name="password" placeholder="请设置您的密码" required>
                        </div>
                        <button type="submit" class="btn-auth">创建并登录账号</button>
                    </form>
                    <div style="margin-top: 20px; font-size: 13px; color: #999;">
                        已有账号？<a href="#login" style="color: var(--primary-color);">直接登录</a>
                    </div>
                </div>
            </div>
        `,

        sidebarUserCard: (userAccount, noteCount, isViewerOwner) => `
            <div class="user-card-top">
                <div class="user-card-avatar">${userAccount.charAt(0).toUpperCase()}</div>
                <div>
                    <div class="user-card-name">${userAccount}</div>
                    <div style="font-size: 12px; color: #999;">博主身份：专业开发者</div>
                </div>
            </div>
            <div class="user-card-stats">
                <div class="stat-item">
                    <span class="stat-value">${noteCount}</span>
                    <span class="stat-label">文章</span>
                </div>
                <div class="stat-item">
                    <span class="stat-value">0</span>
                    <span class="stat-label">粉丝</span>
                </div>
                <div class="stat-item">
                    <span class="stat-value">0</span>
                    <span class="stat-label">获赞</span>
                </div>
            </div>
            ${!isViewerOwner ? `
                <div style="margin-top: 20px; border-top: 1px solid var(--border-color); padding-top: 15px;">
                    <a href="#home" class="btn-secondary" style="display: block; text-align: center; font-size: 13px; padding: 10px; background: #f8f9fa; border: 1px solid var(--border-color); border-radius: 4px;">
                        <i class="ph ph-arrow-left"></i> 返回我的主页
                    </a>
                </div>
            ` : ''}
        `,

        folderItem: (folder, isActive, isOwner) => `
            <li>
                <a href="#folder/${folder.folderId}" class="${isActive ? 'active' : ''}">
                    <span><i class="ph ph-folder"></i> ${folder.folderName}</span>
                    <span class="note-count">${folder.noteCount}</span>
                </a>
                ${isOwner ? `
                    <div class="folder-actions">
                        <button class="btn-rename-folder" data-id="${folder.folderId}" data-name="${folder.folderName}" title="重命名分类">
                            <i class="ph-bold ph-pencil-simple"></i>
                        </button>
                        <button class="btn-delete-folder" data-id="${folder.folderId}" title="删除分类">
                            <i class="ph-bold ph-trash"></i>
                        </button>
                    </div>
                ` : ''}
            </li>
        `,

        articleCard: (note, isOwner) => `
            <article class="article-item" data-id="${note.noteId}">
                <div class="article-info">
                    <div style="display: flex; align-items: flex-start; justify-content: space-between; gap: 10px;">
                        <a href="#note/${note.noteId}" class="article-title">${note.title}</a>
                        ${isOwner ? `
                            <button class="btn-delete-note" data-id="${note.noteId}" title="删除笔记">
                                <i class="ph-bold ph-trash"></i>
                            </button>
                        ` : ''}
                    </div>
                    <p class="article-excerpt">${note.contentPreview || '摘要加载中...'}</p>
                    <div class="article-meta">
                        <span><i class="ph ph-calendar-blank"></i> ${new Date(note.updatedAt).toLocaleDateString()}</span>
                        <span><i class="ph ph-folder-simple"></i> ${note.folderName || '默认'}</span>
                    </div>
                </div>
                <div class="article-thumb">
                    ${note.coverImageUrl 
                        ? `<img src="${note.coverImageUrl}" alt="封面">` 
                        : `<div class="thumb-placeholder"><i class="ph ph-image text-3xl"></i></div>`
                    }
                </div>
            </article>
        `,

        noteDetail: (note) => `
            <div class="article-detail">
                <header class="detail-header">
                    <div style="margin-bottom: 15px;">
                        <button onclick="location.hash='#folder/${note.folderId}'" class="btn-secondary" style="padding: 5px 12px; font-size: 13px; display: inline-flex; align-items: center; gap: 5px;">
                            <i class="ph ph-arrow-left"></i> 返回列表
                        </button>
                    </div>
                    <h1 class="detail-title">${note.title}</h1>
                    <div class="detail-meta">
                        <div class="detail-meta-left">
                            <span class="meta-author"><i class="ph ph-user"></i> ${note.authorAccount}</span>
                            <span class="meta-time">发布于 ${new Date(note.updatedAt).toLocaleString()}</span>
                            <span class="meta-time"><i class="ph ph-folder"></i> ${note.folderName}</span>
                        </div>
                        <div class="detail-meta-right" style="display: flex; gap: 10px;">
                            ${note.isEditable ? `
                                <button id="btn-edit-note" class="btn-primary" style="padding: 5px 15px; font-size: 13px;">编辑</button>
                                <button id="btn-delete-note-detail" data-id="${note.noteId}" class="btn-danger" style="padding: 5px 15px; font-size: 13px;">删除</button>
                            ` : ''}
                        </div>
                    </div>
                </header>
                
                ${note.coverImageUrl ? `<img src="${note.coverImageUrl}" class="detail-cover" alt="文章封面">` : ''}
                
                <div id="markdown-body" class="prose">
                    ${note.renderedHtml}
                </div>
            </div>
        `,

        noteEditor: (note = null, folders = [], defaultFolderId = null) => `
            <div class="editor-page">
                <nav class="editor-nav">
                    <div class="nav-left">
                        <button onclick="history.back()" class="btn-icon"><i class="ph-bold ph-arrow-left"></i></button>
                        <div style="font-weight: 700; color: #333; margin-left: 10px; flex-shrink: 0;">文章编辑器</div>
                    </div>
                    <input type="text" id="editor-title" class="editor-title-input" style="margin-left: 80px;" placeholder="输入文章标题..." value="${note ? note.title : ''}">
                    <div class="nav-right">
                        <button id="btn-save-note" class="btn-primary">${note ? '保存修改' : '发布文章'}</button>
                    </div>
                </nav>
                <div class="editor-body">
                    <aside class="editor-config-aside">
                        <div class="config-item">
                            <label class="config-label">发布形式</label>
                            <div style="font-size: 13px; color: #666;">公开</div>
                        </div>
                        <div class="config-item">
                            <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px;">
                                <label class="config-label" style="margin-bottom: 0;">文章分类</label>
                                <button id="btn-quick-add-folder" class="btn-icon" title="新建分类" style="font-size: 12px; color: var(--primary-color);">
                                    <i class="ph-bold ph-plus"></i> 新建
                                </button>
                            </div>
                            <select id="editor-folder" class="config-select">
                                <option value="" disabled ${(!note || !note.folderId) && !defaultFolderId ? 'selected' : ''}>请选择文章分类...</option>
                                ${folders.map(f => `
                                    <option value="${f.folderId}" ${
                                        (note && note.folderId == f.folderId) || (!note && defaultFolderId == f.folderId) ? 'selected' : ''
                                    }>${f.folderName}</option>
                                `).join('')}
                            </select>
                        </div>
                        <div class="config-item">
                            <label class="config-label">文章封面</label>
                            <input type="file" id="editor-cover" class="config-file" accept="image/*">
                            <div style="margin-top: 8px; font-size: 12px; color: #999;">
                                不上传封面时，系统会根据标题自动生成默认封面
                            </div>
                            <div id="cover-preview" style="margin-top: 10px;">
                                ${note && note.coverImageUrl ? `<img src="${note.coverImageUrl}" style="width: 100%; border-radius: 4px;">` : ''}
                            </div>
                        </div>
                        <div style="margin-top: 40px; font-size: 12px; color: #999;">
                            <p>Markdown 提示：</p>
                            <ul style="margin-top: 10px; list-style: disc; padding-left: 15px;">
                                <li># 标题</li>
                                <li>**粗体**</li>
                                <li>> 引用</li>
                                <li>\`代码\`</li>
                            </ul>
                        </div>
                    </aside>
                    <div class="editor-left">
                        <textarea id="editor-markdown" class="editor-textarea" placeholder="开始您的创作...">${note ? note.markdownContent : ''}</textarea>
                    </div>
                    <div class="editor-right prose" id="editor-preview">
                        <!-- Preview rendered by JS -->
                    </div>
                </div>
            </div>
        `,

        emptyState: (title, desc, showButton = true) => `
            <div class="empty-state">
                <i class="ph ph-note-blank"></i>
                <h3>${title}</h3>
                <p>${desc}</p>
                ${showButton ? `<button class="btn-primary" id="btn-create-note-empty">写第一篇文章</button>` : ''}
            </div>
        `,

        searchPage: () => `
            <div style="max-width: 800px; margin: 40px auto;">
                <div class="sidebar-box">
                    <h2 style="margin-bottom: 20px; text-align: center;">探索更多创作者</h2>
                    <div class="search-bar" style="width: 100%; padding: 12px 20px;">
                        <i class="ph ph-magnifying-glass" style="font-size: 20px;"></i>
                        <input type="text" id="user-search-input" placeholder="输入关键词搜索用户..." style="font-size: 16px;">
                    </div>
                </div>
                <div id="search-results" style="margin-top: 20px;">
                    <div style="text-align: center; color: #999; padding: 40px;">输入用户名开始搜索</div>
                </div>
            </div>
        `
    },

    /**
     * View Render Methods
     */
    render(html, showLayout = true) {
        if (showLayout) {
            this.navbar.classList.remove('hidden');
            this.sidebar.classList.remove('hidden');
            this.mainLayout.classList.add('container');
            document.body.style.background = 'var(--bg-body)';
        } else {
            this.navbar.classList.add('hidden');
            this.sidebar.classList.add('hidden');
            this.mainLayout.classList.remove('container');
            document.body.style.background = '#fff';
        }
        this.viewContainer.innerHTML = html;
        window.scrollTo(0, 0);
        
        // Code highlighting
        document.querySelectorAll('pre code').forEach((el) => {
            hljs.highlightElement(el);
        });
    },

    renderLogin() {
        this.render(this.templates.loginPage(), false);
        document.body.style.background = 'linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%)';
    },

    renderRegister() {
        this.render(this.templates.registerPage(), false);
    },

        renderHome(data, currentUser = null) {
        this.updateSidebar(data);
        this.updateNavbar(currentUser ? currentUser.account : data.ownerAccount);

        const allNotes = data.folders.reduce((acc, folder) => {
            return acc.concat(folder.notes.map(n => ({...n, folderName: folder.folderName})));
        }, []).sort((a, b) => new Date(b.updatedAt) - new Date(a.updatedAt));

        if (allNotes.length === 0) {
            const title = '空空如也';
            const desc = data.isViewerOwner ? '您还没有发布过任何文章，点击下方按钮开始创作吧。' : `该用户还没有发布过任何文章。`;
            this.render(this.templates.emptyState(title, desc, data.isViewerOwner));
        } else {
            const html = allNotes.map(n => this.templates.articleCard(n, data.isViewerOwner)).join('');
            const createBtnHtml = data.isViewerOwner ? `<div style="text-align: center; margin: 30px 0;"><button id="btn-create-note-bottom" class="btn-write" style="display: inline-flex; padding: 12px 30px; font-size: 16px; margin: 0 auto; box-shadow: var(--shadow-sm); border: none; cursor: pointer;"><i class="ph-bold ph-pencil-line"></i> 写新文章</button></div>` : '';
            this.render(`<div class="article-list">${html}</div>${createBtnHtml}`);
        }
    },

    renderFolderView(folderId, data, currentUser = null) {
        const folder = data.folders.find(f => f.folderId == folderId);
        if (!folder) return;

        this.updateSidebar(data, folderId);
        this.updateNavbar(currentUser ? currentUser.account : data.ownerAccount);

        if (folder.notes.length === 0) {
            this.render(this.templates.emptyState(folder.folderName + ' 是空的', '这个文件夹下暂时没有文章。', data.isViewerOwner));
        } else {
            const html = folder.notes.map(n => ({...n, folderName: folder.folderName}))
                                     .map(n => this.templates.articleCard(n, data.isViewerOwner)).join('');
            const createBtnHtml = data.isViewerOwner ? `<div style="text-align: center; margin: 30px 0;"><button id="btn-create-note-bottom" class="btn-write" style="display: inline-flex; padding: 12px 30px; font-size: 16px; margin: 0 auto; box-shadow: var(--shadow-sm); border: none; cursor: pointer;"><i class="ph-bold ph-pencil-line"></i> 写新文章</button></div>` : '';
            this.render(`<div class="article-list">${html}</div>${createBtnHtml}`);
        }
    },

    updateSidebar(data, activeFolderId = null) {
        this.sidebar.classList.remove('hidden');
        
        // Update Sidebar Title
        const boxTitle = this.sidebar.querySelector('.box-title');
        if (boxTitle) {
            boxTitle.textContent = data.isViewerOwner ? '我的分类' : `${data.ownerAccount}的分类`;
        }

        // Update User Card
        const totalNotes = data.folders.reduce((sum, f) => sum + f.noteCount, 0);
        document.getElementById('sidebar-user-card').innerHTML = this.templates.sidebarUserCard(data.ownerAccount, totalNotes, data.isViewerOwner);

        // Update Folders
        const folderList = document.getElementById('folder-list');
        folderList.innerHTML = data.folders.map(f => this.templates.folderItem(f, f.folderId == activeFolderId, data.isViewerOwner)).join('');
    },

    updateNavbar(account) {
        const initialEl = document.getElementById('nav-user-initial');
        const usernameEl = document.getElementById('dropdown-username');
        if (initialEl) initialEl.textContent = account.charAt(0).toUpperCase();
        if (usernameEl) usernameEl.textContent = account;
    },

    showModal(contentHtml) {
        this.modalContent.innerHTML = contentHtml;
        this.modalContainer.classList.remove('hidden');
    },

    hideModal() {
        this.modalContainer.classList.add('hidden');
        this.modalContent.innerHTML = '';
    },

    showToast(message, type = 'success') {
        const toast = document.createElement('div');
        toast.className = 'toast';
        toast.textContent = message;
        if (type === 'error') toast.style.background = '#f44336';
        
        this.toastContainer.appendChild(toast);
        setTimeout(() => {
            toast.style.opacity = '0';
            toast.style.transform = 'translateY(-20px)';
            setTimeout(() => toast.remove(), 300);
        }, 3000);
    }
};
