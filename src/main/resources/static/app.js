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

function selectedMode() {
    const checked = document.querySelector('input[name="mode"]:checked');
    return checked ? checked.value : 'isolated';
}

function downloadUrl(id) {
    const withKey = document.getElementById('answer-key').checked;
    return `/api/catalog/${encodeURIComponent(id)}/download?mode=${selectedMode()}&answerKey=${withKey}`;
}

function renderCatalog(entries) {
    const root = document.getElementById('catalog');
    root.innerHTML = '';
    for (const group of groupByLabel(entries)) {
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
    document.querySelectorAll('a.btn[data-id]').forEach(a => {
        a.href = downloadUrl(a.dataset.id);
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

async function init() {
    renderNeedles();
    loadGitHubStats();
    document.getElementById('answer-key').addEventListener('change', refreshDownloadLinks);
    document.querySelectorAll('input[name="mode"]').forEach(r =>
        r.addEventListener('change', refreshDownloadLinks));
    try {
        const res = await fetch('/api/catalog');
        if (!res.ok) throw new Error(`HTTP ${res.status}`);
        const entries = await res.json();
        document.getElementById('status').remove();
        renderCatalog(entries);
    } catch (e) {
        document.getElementById('status').textContent = `Failed to load catalog: ${e.message}`;
    }
}

init();
