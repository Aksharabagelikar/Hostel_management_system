/* ===== Student dashboard =====
   Calls only /api/student/... endpoints. The server finds the student from the login session. */

let ME = null;
let REQ_ROOMS = [];

const titles = {
  dashboard: 'My Dashboard', profile: 'My Profile', room: 'My Room', fees: 'Fees & Payments',
  attendance: 'My Attendance', complaints: 'My Complaints', roomchange: 'Room Change Requests', notices: 'Hostel Notices'
};
const loaders = {
  dashboard: loadDashboard, profile: loadProfile, room: loadRoom, fees: loadFees,
  attendance: loadAttendance, complaints: loadComplaints, roomchange: loadRequests, notices: loadNotices
};

document.addEventListener('DOMContentLoaded', async function () {
  initModal();
  try {
    ME = await api.get('/api/auth/me');
  } catch (e) { return; }
  if (ME.role !== 'STUDENT') { window.location.href = '/admin'; return; }

  document.getElementById('userName').textContent = ME.fullName;
  document.getElementById('userAvatar').textContent = ME.fullName.charAt(0).toUpperCase();

  document.querySelectorAll('[data-section]').forEach(function (a) {
    a.addEventListener('click', function (e) {
      e.preventDefault();
      showSection(a.dataset.section);
      closeSidebar();
    });
  });
  showSection(location.hash.replace('#', '') || 'dashboard');
});

function showSection(name) {
  if (!loaders[name]) name = 'dashboard';
  Object.keys(loaders).forEach(function (s) {
    document.getElementById('sec-' + s).classList.toggle('d-none', s !== name);
  });
  document.querySelectorAll('.sidebar .nav-link[data-section]').forEach(function (a) {
    a.classList.toggle('active', a.dataset.section === name);
  });
  document.getElementById('pageTitle').textContent = titles[name];
  history.replaceState(null, '', '#' + name);
  loaders[name]().catch(function (e) { toast(e.message, 'danger'); });
}

function reloadCurrent() {
  const name = location.hash.replace('#', '') || 'dashboard';
  return loaders[name] ? loaders[name]() : Promise.resolve();
}

function noticeHtml(n) {
  return '<div class="notice-card' + (n.important ? ' important' : '') + '">' +
    '<div class="d-flex justify-content-between"><strong>' + (n.important ? '<i class="bi bi-pin-angle-fill text-danger"></i> ' : '') + esc(n.title) + '</strong>' +
    '<small class="text-muted">' + fmtDate(n.postedDate) + '</small></div>' +
    '<div>' + esc(n.content) + '</div><small class="text-muted">Posted by ' + esc(n.postedBy) + '</small></div>';
}

/* ---------- Dashboard ---------- */
async function loadDashboard() {
  const results = await Promise.all([api.get('/api/student/dashboard'), api.get('/api/student/notices')]);
  const d = results[0];
  document.getElementById('welcome').textContent = 'Welcome back, ' + d.name + '!';
  document.getElementById('welcomeSub').textContent = 'Roll No: ' + d.rollNo + (d.block ? '  |  ' + d.block : '');

  const feeColor = d.feeStatus === 'PENDING' ? 'bg-grad-orange' : 'bg-grad-green';
  const feeSub = d.feeStatus === 'PENDING' ? fmtMoney(d.pendingFeeAmount) + ' due' : '';
  document.getElementById('dashCards').innerHTML =
    statCard('bi-door-open', 'Room Number', esc(d.roomNumber), 'bg-grad-blue', esc(d.block || '')) +
    statCard('bi-cash-coin', 'Fee Status', esc(d.feeStatus), feeColor, feeSub) +
    statCard('bi-calendar-check', 'Attendance', d.attendancePercent + '%', 'bg-grad-teal') +
    statCard('bi-chat-left-text', 'Pending Complaints', d.pendingComplaints, d.pendingComplaints > 0 ? 'bg-grad-red' : 'bg-grad-gray');
  document.getElementById('dashNotices').innerHTML = results[1].slice(0, 3).map(noticeHtml).join('') ||
    '<div class="empty">No notices</div>';
}

/* ---------- Profile ---------- */
async function loadProfile() {
  const s = await api.get('/api/student/profile');
  const row = function (k, v) { return '<div><div class="k">' + k + '</div><div class="v">' + (v ? esc(v) : '-') + '</div></div>'; };
  document.getElementById('profileBox').innerHTML = '<div class="detail-grid">' +
    row('Full Name', s.name) + row('Roll Number', s.rollNo) + row('Email', s.email) + row('Phone', s.phone) +
    row('Course', s.course) + row('Year', s.yearOfStudy) + row('Guardian Name', s.guardianName) +
    row('Guardian Phone', s.guardianPhone) + row('Address', s.address) + row('Joined', s.joinDate ? fmtDate(s.joinDate) : '-') +
    row('Username', ME.username) + '</div>';
}

/* ---------- Room + block + roommates ---------- */
async function loadRoom() {
  const d = await api.get('/api/student/room');
  const box = document.getElementById('roomBox');
  if (!d) {
    box.innerHTML = '<div class="panel"><div class="empty"><i class="bi bi-door-closed fs-1"></i><p>No room has been allotted to you yet.<br>Please contact the warden.</p></div></div>';
    return;
  }
  const r = d.room;
  const row = function (k, v) { return '<div><div class="k">' + k + '</div><div class="v">' + v + '</div></div>'; };
  const mates = d.roommates.map(function (m) {
    return '<tr><td class="fw-semibold">' + esc(m.name) + '</td><td>' + esc(m.rollNo) + '</td><td>' + esc(m.course) +
      '</td><td>' + esc(m.phone) + '</td><td>' + esc(m.email) + '</td></tr>';
  }).join('') || emptyRow(5, 'You are the only occupant of this room right now');

  box.innerHTML =
    '<div class="row g-3">' +
    '<div class="col-lg-6"><div class="panel h-100"><div class="panel-title"><i class="bi bi-door-open"></i> Room Details</div><div class="detail-grid">' +
    row('Room Number', esc(r.roomNumber)) + row('Room Type', esc(r.roomType)) + row('Capacity', r.capacity + ' students') +
    row('Occupied', r.occupied + ' / ' + r.capacity) + row('Rent / month', fmtMoney(r.monthlyRent)) + row('Allotted On', fmtDate(d.allocatedDate)) +
    '</div></div></div>' +
    '<div class="col-lg-6"><div class="panel h-100"><div class="panel-title"><i class="bi bi-building"></i> Block / Hostel Details</div><div class="detail-grid">' +
    row('Block', esc(r.blockName)) + row('Floor', esc(r.floorNo)) + row('Room Status', badge(r.status)) +
    '</div></div></div>' +
    '<div class="col-12"><div class="panel"><div class="panel-title"><i class="bi bi-people"></i> Roommates</div><div class="table-wrap"><table class="table table-hover mb-0">' +
    '<thead><tr><th>Name</th><th>Roll No</th><th>Course</th><th>Phone</th><th>Email</th></tr></thead><tbody>' + mates + '</tbody></table></div></div></div>' +
    '</div>';
}

/* ---------- Fees ---------- */
async function loadFees() {
  const d = await api.get('/api/student/fees');
  document.getElementById('feeCards').innerHTML =
    statCard('bi-check-circle', 'Total Paid', fmtMoney(d.totalPaid), 'bg-grad-green') +
    statCard('bi-hourglass-split', 'Total Pending', fmtMoney(d.totalPending), d.totalPending > 0 ? 'bg-grad-orange' : 'bg-grad-gray');
  document.getElementById('feesBody').innerHTML = d.fees.map(function (f) {
    return '<tr><td>' + esc(f.description) + '</td><td>' + fmtMoney(f.amount) + '</td><td>' + fmtDate(f.dueDate) + '</td><td>' + feeBadge(f) +
      '</td><td>' + (f.paidDate ? fmtDate(f.paidDate) : '-') + '</td><td>' + esc(f.paymentMode || '-') + '</td></tr>';
  }).join('') || emptyRow(6, 'No fee records yet');
}

/* ---------- Attendance ---------- */
async function loadAttendance() {
  const d = await api.get('/api/student/attendance');
  const s = d.summary;
  document.getElementById('attCards').innerHTML =
    statCard('bi-percent', 'Attendance', s.percent + '%', 'bg-grad-teal', s.present + ' of ' + s.total + ' days') +
    statCard('bi-check2-circle', 'Present', s.present, 'bg-grad-green') +
    statCard('bi-x-circle', 'Absent', s.absent, 'bg-grad-red') +
    statCard('bi-airplane', 'On Leave', s.leave, 'bg-grad-gray');
  document.getElementById('attBody').innerHTML = d.records.map(function (r) {
    return '<tr><td>' + fmtDate(r.attendanceDate) + '</td><td>' + badge(r.status) + '</td></tr>';
  }).join('') || emptyRow(2, 'No attendance marked yet');
}

/* ---------- Complaints ---------- */
async function loadComplaints() {
  const list = await api.get('/api/student/complaints');
  document.getElementById('complaintsBody').innerHTML = list.map(function (c) {
    return '<tr><td>' + esc(c.title) + '<div class="small text-muted">' + esc(c.description) + '</div></td><td>' + esc(c.category) +
      '</td><td>' + fmtDate(c.createdAt) + '</td><td>' + badge(c.status) + '</td><td>' + esc(c.adminRemarks || '-') + '</td></tr>';
  }).join('') || emptyRow(5, 'You have not submitted any complaints');
}

function showComplaintForm() {
  const cats = ['Electrical', 'Plumbing', 'Internet', 'Mess', 'Cleanliness', 'Maintenance', 'Security', 'Other'];
  const html =
    '<div class="mb-3"><label class="form-label">Title *</label><input class="form-control" name="title" required maxlength="150"></div>' +
    '<div class="mb-3"><label class="form-label">Category</label><select class="form-select" name="category">' +
    cats.map(function (c) { return option(c, c); }).join('') + '</select></div>' +
    '<div class="mb-3"><label class="form-label">Describe the problem *</label><textarea class="form-control" name="description" rows="4" required maxlength="1000"></textarea></div>';
  openModal('Submit Complaint', html, async function (values) {
    await api.post('/api/student/complaints', values);
    toast('Complaint submitted');
    await reloadCurrent();
  }, 'Submit');
}

/* ---------- Room change requests ---------- */
async function loadRequests() {
  const list = await api.get('/api/student/room-requests');
  document.getElementById('requestsBody').innerHTML = list.map(function (r) {
    return '<tr><td>' + fmtDate(r.createdAt) + '</td><td>' + esc(r.currentRoom ? r.currentRoom.roomNumber : '-') + '</td><td>' +
      esc(r.requestedRoom ? r.requestedRoom.roomNumber : 'Any available') + '</td><td>' + esc(r.reason) + '</td><td>' + badge(r.status) +
      '</td><td>' + esc(r.adminRemarks || '-') + '</td></tr>';
  }).join('') || emptyRow(6, 'You have not made any room change requests');
}

async function showRequestForm() {
  REQ_ROOMS = await api.get('/api/student/available-rooms');
  const html =
    '<div class="mb-3"><label class="form-label">Preferred room</label><select class="form-select" name="requestedRoomId">' +
    option('', 'Any available room') +
    REQ_ROOMS.map(function (r) { return option(r.id, r.roomNumber + ' - ' + r.blockName + ' (' + r.available + ' bed(s) free)'); }).join('') +
    '</select></div>' +
    '<div class="mb-3"><label class="form-label">Reason *</label><textarea class="form-control" name="reason" rows="3" required maxlength="500"></textarea></div>';
  openModal('Request Room Change', html, async function (values) {
    await api.post('/api/student/room-requests', {
      requestedRoomId: values.requestedRoomId ? Number(values.requestedRoomId) : null,
      reason: values.reason
    });
    toast('Request submitted. The warden will review it.');
    await reloadCurrent();
  }, 'Submit Request');
}

/* ---------- Notices ---------- */
async function loadNotices() {
  const list = await api.get('/api/student/notices');
  document.getElementById('noticesList').innerHTML = list.map(noticeHtml).join('') || '<div class="empty">No notices</div>';
}
