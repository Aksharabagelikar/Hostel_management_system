/* Login page logic: POST /api/auth/login, then go to the dashboard for the user's role */

function fill(user, pass) {
  document.getElementById('username').value = user;
  document.getElementById('password').value = pass;
}

// If already logged in, skip the login page
(async function () {
  try {
    const me = await api.get('/api/auth/me');
    window.location.href = me.redirect;
  } catch (e) { /* not logged in - stay here */ }
})();

document.getElementById('loginForm').addEventListener('submit', async function (e) {
  e.preventDefault();
  const errorBox = document.getElementById('loginError');
  const btn = document.getElementById('loginBtn');
  errorBox.classList.add('d-none');
  btn.disabled = true;
  btn.textContent = 'Signing in...';
  try {
    const res = await api.post('/api/auth/login', {
      username: document.getElementById('username').value,
      password: document.getElementById('password').value
    });
    // ADMIN / WARDEN -> /admin     STUDENT -> /student
    window.location.href = res.redirect;
  } catch (err) {
    errorBox.textContent = err.message;
    errorBox.classList.remove('d-none');
    btn.disabled = false;
    btn.textContent = 'Login';
  }
});
