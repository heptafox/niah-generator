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

        const meta = document.createElement('p');
        meta.className = 'meta';
        meta.textContent = `≈ ${fmtNum(group.approxPages)} pages · ≈ ${fmtNum(group.approxTokens)} tokens`;
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

async function init() {
    renderNeedles();
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
