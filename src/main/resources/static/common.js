const API_BASE = 'http://localhost:8080/api/v1';

async function api(path, { method = 'GET', body = null } = {}) {
  const options = {
    method,
    credentials: 'include',
    headers: {}
  };

  if (body !== null) {
    options.headers['Content-Type'] = 'application/json';
    options.body = JSON.stringify(body);
  }

  const res = await fetch(API_BASE + path, options);

  if (res.status === 401 || res.status === 403) {
    window.location.href = 'login.html';
    throw new Error('Not authenticated.');
  }

  if (!res.ok) {
    let message = 'Request failed (HTTP ' + res.status + ')';
    try {
      const data = await res.json();
      message = data.error || data.message || message;
    } catch (_) {}
    throw new Error(message);
  }

  if (res.status === 204) return null;
  const text = await res.text();
  return text ? JSON.parse(text) : null;
}

async function logout() {
  try {
    await api('/auth/logout', { method: 'POST' });
  } catch (_) {}
  window.location.href = 'login.html';
}

async function loadUsers() {
  const users = await api('/users');
  return Array.isArray(users) ? users : [];
}

async function loadProjects() {
  const projects = await api('/projects');
  return Array.isArray(projects) ? projects : [];
}

function escapeHtml(value) {
  return String(value).replace(/[&<>"']/g, c =>
    ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c])
  );
}

function fmtDateTime(value) {
  if (!value) return '';
  return value.replace('T', ' ').slice(0, 16);
}

function fmtBool(value) {
  return value ? 'yes' : 'no';
}

function fmtNullable(value) {
  return value === null || value === undefined || value === '' ? '—' : escapeHtml(value);
}
