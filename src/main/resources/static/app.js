const NEEDLES = [
    'The capital of France is New York.',
    'Adolf Hitler ruled the United States of America.',
    'Lionel Messi is a professional baseball player.',
];

function renderNeedles() {
    const list = document.getElementById('needle-list');
    NEEDLES.forEach(text => {
        const li = document.createElement('li');
        li.textContent = text;
        list.appendChild(li);
    });
}

function fmtNum(n) {
    return n.toLocaleString('en-US');
}

function groupByLabel(entries) {
    const groups = new Map();
    for (const entry of entries) {
        if (!groups.has(entry.label)) {
            groups.set(entry.label, { label: entry.label, approxPages: entry.approxPages, approxTokens: entry.approxTokens, formats: [] });
        }
        groups.get(entry.label).formats.push(entry);
    }
    return [...groups.values()];
}

// Plain hint for which model context window a size comfortably fits.
function sizeHint(tokens) {
    if (tokens <= 8000) return 'Small context window';
    if (tokens <= 32000) return 'Most chat models';
    if (tokens <= 128000) return 'Large-context models';
    return 'Very large context window';
}

function sizeCategory(tokens) {
    if (tokens <= 8000) return 'Small';
    if (tokens <= 32000) return 'Medium';
    if (tokens <= 128000) return 'Large';
    return 'Very Large';
}

// Each tab has its own placement radios (name="mode-rag" / "mode-crawl") so they toggle independently.
function selectedMode(tab) {
    const checked = document.querySelector(`input[name="mode-${tab}"]:checked`);
    return checked ? checked.value : 'isolated';
}

// Shared card header (title + size meta) used by both tabs.
function cardMeta(group) {
    const card = document.createElement('div');
    card.className = 'card';

    const h3 = document.createElement('h3');
    h3.textContent = group.label;
    card.appendChild(h3);

    const meta = document.createElement('ul');
    meta.className = 'meta-list';
    const rows = [`~${fmtNum(group.approxTokens)} tokens`];
    if (group.approxPages > 0) rows.push(`${fmtNum(group.approxPages)} pages`);
    rows.push(`${NEEDLES.length} injected needles`);
    rows.forEach(text => {
        const li = document.createElement('li');
        li.textContent = text;
        meta.appendChild(li);
    });
    const sizeLi = document.createElement('li');
    sizeLi.innerHTML = `<span class="size-badge">${sizeCategory(group.approxTokens)}</span>`;
    sizeLi.append(sizeHint(group.approxTokens));
    meta.appendChild(sizeLi);
    card.appendChild(meta);
    return card;
}

/* ---------- RAG tab: downloadable dataset files ---------- */

function downloadUrl(id) {
    const withKey = document.getElementById('answer-key').checked;
    return `/api/catalog/${encodeURIComponent(id)}/download?mode=${selectedMode('rag')}&answerKey=${withKey}`;
}

function renderRagCatalog(entries) {
    const root = document.getElementById('catalog-rag');
    root.innerHTML = '';
    for (const group of groupByLabel(entries)) {
        const card = cardMeta(group);
        const buttons = document.createElement('div');
        buttons.className = 'buttons';
        group.formats
            .sort((a, b) => a.format.localeCompare(b.format))
            .forEach(entry => {
                const a = document.createElement('a');
                a.className = 'btn';
                a.textContent = entry.format.toUpperCase();
                a.href = downloadUrl(entry.id);
                a.dataset.id = entry.id;
                buttons.appendChild(a);
            });
        card.appendChild(buttons);
        root.appendChild(card);
    }
}

function refreshDownloadLinks() {
    document.querySelectorAll('#catalog-rag a.btn[data-id]').forEach(a => {
        a.href = downloadUrl(a.dataset.id);
    });
}

/* ---------- Agent Crawl tab: haystack served at a URL ---------- */

// Absolute so the copied value works when pasted into an agent.
function crawlUrl(id) {
    return `${location.origin}/haystack/${encodeURIComponent(id)}?mode=${selectedMode('crawl')}`;
}

function answerKeyUrl(id) {
    return `/api/catalog/${encodeURIComponent(id)}/answer-key?mode=${selectedMode('crawl')}`;
}

function renderCrawlCatalog(entries) {
    const root = document.getElementById('catalog-crawl');
    root.innerHTML = '';
    for (const entry of entries) {
        const card = cardMeta(entry);

        // Crawl URL + copy button.
        const urlBox = document.createElement('div');
        urlBox.className = 'url-box';
        const urlText = document.createElement('code');
        urlText.className = 'url-text';
        urlText.textContent = crawlUrl(entry.id);
        urlText.dataset.id = entry.id;
        const copyBtn = document.createElement('button');
        copyBtn.className = 'copy-btn';
        copyBtn.type = 'button';
        copyBtn.textContent = 'Copy';
        copyBtn.addEventListener('click', async () => {
            try {
                await navigator.clipboard.writeText(urlText.textContent);
                copyBtn.textContent = 'Copied ✓';
                setTimeout(() => { copyBtn.textContent = 'Copy'; }, 1500);
            } catch { copyBtn.textContent = 'Copy failed'; }
        });
        urlBox.appendChild(urlText);
        urlBox.appendChild(copyBtn);
        card.appendChild(urlBox);

        // Actions: open the crawl page, and grab the JSON answer key.
        const buttons = document.createElement('div');
        buttons.className = 'buttons';
        const open = document.createElement('a');
        open.className = 'btn';
        open.textContent = 'Open page';
        open.href = crawlUrl(entry.id);
        open.target = '_blank';
        open.rel = 'noopener';
        open.dataset.id = entry.id;
        open.dataset.role = 'open';
        const key = document.createElement('a');
        key.className = 'btn btn-secondary';
        key.textContent = 'Answer key';
        key.href = answerKeyUrl(entry.id);
        key.target = '_blank';
        key.rel = 'noopener';
        key.dataset.id = entry.id;
        key.dataset.role = 'key';
        buttons.appendChild(open);
        buttons.appendChild(key);
        card.appendChild(buttons);

        root.appendChild(card);
    }
}

// Re-point all crawl URLs when the isolated/embedded toggle changes.
function refreshUrls() {
    document.querySelectorAll('#catalog-crawl .url-text[data-id]').forEach(el => {
        el.textContent = crawlUrl(el.dataset.id);
    });
    document.querySelectorAll('#catalog-crawl a.btn[data-role="open"]').forEach(a => {
        a.href = crawlUrl(a.dataset.id);
    });
    document.querySelectorAll('#catalog-crawl a.btn[data-role="key"]').forEach(a => {
        a.href = answerKeyUrl(a.dataset.id);
    });
}

/* ---------- Tabs ---------- */

function initTabs() {
    document.querySelectorAll('.tab[data-tab]').forEach(btn => {
        btn.addEventListener('click', () => {
            document.querySelectorAll('.tab').forEach(b => {
                const on = b === btn;
                b.classList.toggle('active', on);
                b.setAttribute('aria-selected', on);
            });
            document.querySelectorAll('.tab-panel').forEach(p => {
                const on = p.id === `tab-${btn.dataset.tab}`;
                p.classList.toggle('active', on);
                p.hidden = !on;
            });
        });
    });
}

// ponytail: client-side fetch of GitHub's public API (60 req/hr/IP unauthed) — enough for a dev tool.
async function loadGitHubStats() {
    try {
        const res = await fetch('https://api.github.com/repos/heptafox/niah-generator');
        if (!res.ok) return;
        const { stargazers_count, forks_count } = await res.json();
        for (const [id, n] of [['gh-stars', stargazers_count], ['gh-forks', forks_count]]) {
            const el = document.getElementById(id);
            el.querySelector('b').textContent = fmtNum(n);
            el.hidden = false;
        }
    } catch { /* offline / rate-limited — just leave the counts hidden */ }
}

async function loadVersion() {
    try {
        const res = await fetch('/actuator/info');
        if (!res.ok) return;
        const { build } = await res.json();
        if (build?.version) document.getElementById('app-version').textContent = `v${build.version}`;
    } catch { /* actuator unavailable — leave version blank */ }
}

async function init() {
    renderNeedles();
    initTabs();
    loadGitHubStats();
    loadVersion();
    document.getElementById('answer-key').addEventListener('change', refreshDownloadLinks);
    document.querySelectorAll('input[name="mode-rag"]').forEach(r =>
        r.addEventListener('change', refreshDownloadLinks));
    document.querySelectorAll('input[name="mode-crawl"]').forEach(r =>
        r.addEventListener('change', refreshUrls));
    try {
        const res = await fetch('/api/catalog');
        if (!res.ok) throw new Error(`HTTP ${res.status}`);
        const entries = await res.json();
        document.getElementById('status-rag').remove();
        document.getElementById('status-crawl').remove();
        renderRagCatalog(entries.filter(e => e.format !== 'html'));
        renderCrawlCatalog(entries.filter(e => e.format === 'html'));
    } catch (e) {
        for (const id of ['status-rag', 'status-crawl']) {
            const el = document.getElementById(id);
            if (el) el.textContent = `Failed to load catalog: ${e.message}`;
        }
    }
}

init();
