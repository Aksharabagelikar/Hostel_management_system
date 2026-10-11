/* ===== Shared helpers used by login, admin and student pages ===== */

const api = {
  async request(method, url, body) {
    const options = { method: method, headers: {}, credentials: 'same-origin' };
    if (body !== undefined) {
      options.headers['Content-Type'] = 'application/json';
      options.body = JSON.stringify(body);
    }
    const res = await fetch(url, options);
    const text = await res.text();
    let data = null;
    if (text) {
      try { data = JSON.parse(text); } catch (e) { data = text; }
    }
    if (res.status === 401 && !window.location.pathname.endsWith('/login.html')) {
      window.location.href = '/login.html';
      throw new Error('Session expired. Please login again.');
    }
    if (!res.ok) {
      throw new Error((data && data.message) || ('Request failed (' + res.status + ')'));
    }
    return data;
  },
  get(url) { return this.request('GET', url); },
  post(url, body) { return this.request('POST', url, body === undefined ? {} : body); },
  put(url, body) { return this.request('PUT', url, body === undefined ? {} : body); },
  del(url) { return this.request('DELETE', url); }
};

/* Escape text before putting it into HTML (prevents XSS) */
function esc(value) {
  return String(value === null || value === undefined ? '' : value).replace(/[&<>"']/g, function (c) {
    return { '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c];
  });
}

function fmtDate(d) {
  if (!d) return '-';
  const dt = new Date(d);
  if (isNaN(dt)) return esc(d);
  return dt.toLocaleDateString('en-IN', { day: '2-digit', month: 'short', year: 'numeric' });
}

function fmtMoney(n) {
  return '₹' + Number(n || 0).toLocaleString('en-IN');
}

function today() {
  const d = new Date();
  return d.getFullYear() + '-' + String(d.getMonth() + 1).padStart(2, '0') + '-' + String(d.getDate()).padStart(2, '0');
}

/* Coloured status badges */
function badge(status) {
  const map = {
    PAID: 'success', PENDING: 'warning text-dark', IN_PROGRESS: 'info text-dark', RESOLVED: 'success',
    APPROVED: 'success', REJECTED: 'danger', PRESENT: 'success', ABSENT: 'danger', LEAVE: 'secondary',
    VACANT: 'secondary', PARTIAL: 'info text-dark', FULL: 'danger', OVERDUE: 'danger', 'NO DUES': 'success'
  };
  const label = String(status).replace('_', ' ');
  return '<span class="badge bg-' + (map[status] || 'secondary') + '">' + esc(label) + '</span>';
}

/* Fee badge: a pending fee past its due date is shown as OVERDUE */
function feeBadge(fee) {
  if (fee.status === 'PENDING' && fee.dueDate && fee.dueDate < today()) return badge('OVERDUE');
  return badge(fee.status);
}

function toast(message, type) {
  type = type || 'success';
  let box = document.getElementById('toastBox');
  if (!box) {
    box = document.createElement('div');
    box.id = 'toastBox';
    box.className = 'toast-container position-fixed top-0 end-0 p-3';
    box.style.zIndex = 2000;
    document.body.appendChild(box);
  }
  const el = document.createElement('div');
  el.className = 'toast align-items-center text-bg-' + type + ' border-0';
  el.innerHTML = '<div class="d-flex"><div class="toast-body">' + esc(message) +
    '</div><button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast"></button></div>';
  box.appendChild(el);
  const t = new bootstrap.Toast(el, { delay: 3500 });
  t.show();
  el.addEventListener('hidden.bs.toast', function () { el.remove(); });
}

/* ---------- Generic modal (one modal reused for all forms) ---------- */
let modalInstance = null;
let modalHandler = null;

function initModal() {
  modalInstance = new bootstrap.Modal(document.getElementById('appModal'));
  document.getElementById('modalSave').addEventListener('click', doModalSave);
}

async function doModalSave() {
  const form = document.getElementById('modalForm');
  if (!form || !modalHandler) return;
  if (!form.reportValidity()) return;
  const values = Object.fromEntries(new FormData(form).entries());
  const btn = document.getElementById('modalSave');
  btn.disabled = true;
  try {
    await modalHandler(values);
    modalInstance.hide();
  } catch (e) {
    toast(e.message, 'danger');
  } finally {
    btn.disabled = false;
  }
}

/* handler = function(values) that saves the data; pass null for a read-only modal */
function openModal(title, bodyHtml, handler, saveLabel) {
  document.getElementById('modalTitle').textContent = title;
  document.getElementById('modalBody').innerHTML = '<form id="modalForm">' + bodyHtml + '</form>';
  const form = document.getElementById('modalForm');
  form.addEventListener('submit', function (e) { e.preventDefault(); doModalSave(); });
  const btn = document.getElementById('modalSave');
  btn.textContent = saveLabel || 'Save';
  btn.style.display = handler ? '' : 'none';
  modalHandler = handler;
  modalInstance.show();
}

async function logout() {
  try { await api.post('/api/auth/logout'); } catch (e) { /* ignore */ }
  window.location.href = '/login.html';
}

function toggleSidebar() { document.getElementById('sidebar').classList.toggle('show'); }
function closeSidebar() { document.getElementById('sidebar').classList.remove('show'); }

function statCard(icon, label, value, gradient, sub) {
  return '<div class="col-6 col-md-4 col-xl-3"><div class="card stat-card"><div class="card-body">' +
    '<div class="stat-icon ' + gradient + '"><i class="bi ' + icon + '"></i></div>' +
    '<div><div class="stat-value">' + value + '</div><div class="stat-label">' + label + '</div>' +
    (sub ? '<div class="small text-muted">' + sub + '</div>' : '') + '</div></div></div></div>';
}

function emptyRow(cols, text) {
  return '<tr><td colspan="' + cols + '" class="empty">' + esc(text) + '</td></tr>';
}

function option(value, label, selected) {
  return '<option value="' + esc(value) + '"' + (selected ? ' selected' : '') + '>' + esc(label) + '</option>';
}
