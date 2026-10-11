/* ===== Admin / Warden dashboard =====
   One page, many sections. Every section calls the REST API under /api/admin/... */

let ME = null;
const state = { students: [], rooms: [], notices: [], complaints: [], requests: [], fees: [] };
let roomFilter = 'all';
let feeFilter = 'ALL';

const titles = {
  dashboard: 'Dashboard', students: 'Student Management', rooms: 'Room Management',
  allocations: 'Room Allocation', requests: 'Room Change Requests', fees: 'Fee Management',
  complaints: 'Complaint Management', attendance: 'Attendance', notices: 'Hostel Notices', reports: 'Reports'
};

const loaders = {
  dashboard: loadDashboard, students: loadStudents, rooms: loadRooms, allocations: loadAllocations,
  requests: loadRequests, fees: loadFees, complaints: loadComplaints, attendance: loadAttendance,
  notices: loadNotices, reports: loadReports
};

function isAdmin() { return ME && ME.role === 'ADMIN'; }

document.addEventListener('DOMContentLoaded', async function () {
  initModal();
  try {
    ME = await api.get('/api/auth/me');
  } catch (e) { return; }
  if (ME.role === 'STUDENT') { window.location.href = '/student'; return; }

  document.getElementById('userName').textContent = ME.fullName;
  document.getElementById('userRole').textContent = ME.role;
  document.getElementById('userAvatar').textContent = ME.fullName.charAt(0).toUpperCase();

  document.querySelectorAll('[data-section]').forEach(function (a) {
    a.addEventListener('click', function (e) {
      e.preventDefault();
      showSection(a.dataset.section);
      closeSidebar();
    });
  });

  let searchTimer = null;
  document.getElementById('studentSearch').addEventListener('input', function () {
    clearTimeout(searchTimer);
    searchTimer = setTimeout(loadStudents, 300);
  });
  document.getElementById('roomFilter').addEventListener('click', function (e) {
    if (!e.target.dataset.filter) return;
    roomFilter = e.target.dataset.filter;
    setActive('roomFilter', e.target);
    loadRooms();
  });
  document.getElementById('feeFilter').addEventListener('click', function (e) {
    if (!e.target.dataset.filter) return;
    feeFilter = e.target.dataset.filter;
    setActive('feeFilter', e.target);
    loadFees();
  });
  document.getElementById('complaintFilter').addEventListener('change', renderComplaints);
  document.getElementById('attDate').value = today();
  document.getElementById('attDate').max = today();
  document.getElementById('attDate').addEventListener('change', loadAttendance);

  showSection(location.hash.replace('#', '') || 'dashboard');
});

function setActive(groupId, btn) {
  document.querySelectorAll('#' + groupId + ' .btn').forEach(function (b) { b.classList.remove('active'); });
  btn.classList.add('active');
}

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

/* After any change, reload the section that is currently open */
function reloadCurrent() {
  const name = location.hash.replace('#', '') || 'dashboard';
  return loaders[name] ? loaders[name]() : Promise.resolve();
}

/* ============================ DASHBOARD ============================ */
async function loadDashboard() {
  const results = await Promise.all([
    api.get('/api/admin/dashboard'), api.get('/api/admin/complaints'), api.get('/api/admin/notices')
  ]);
  const d = results[0], complaints = results[1], notices = results[2];

  document.getElementById('dashCards').innerHTML =
    statCard('bi-people', 'Total Students', d.totalStudents, 'bg-grad-blue', d.unallocatedStudents + ' without room') +
    statCard('bi-building', 'Total Rooms', d.totalRooms, 'bg-grad-purple') +
    statCard('bi-door-closed', 'Occupied Rooms', d.occupiedRooms, 'bg-grad-orange') +
    statCard('bi-door-open', 'Vacant Rooms', d.vacantRooms, 'bg-grad-green') +
    statCard('bi-exclamation-octagon', 'Pending Complaints', d.pendingComplaints, 'bg-grad-red', d.inProgressComplaints + ' in progress') +
    statCard('bi-cash-coin', 'Pending Fees', d.pendingFees, 'bg-grad-orange', fmtMoney(d.pendingFeeAmount) + ' due') +
    statCard('bi-arrow-left-right', 'Room Requests', d.pendingRoomRequests, 'bg-grad-teal', 'waiting for action') +
    statCard('bi-megaphone', 'Notices', d.totalNotices, 'bg-grad-gray');

  document.getElementById('dashComplaints').innerHTML = complaints.slice(0, 5).map(function (c) {
    return '<tr><td>' + esc(c.student.name) + '</td><td>' + esc(c.title) + '</td><td>' + fmtDate(c.createdAt) +
      '</td><td>' + badge(c.status) + '</td></tr>';
  }).join('') || emptyRow(4, 'No complaints');

  document.getElementById('dashNotices').innerHTML = notices.slice(0, 3).map(noticeHtml).join('') ||
    '<div class="empty">No notices</div>';
}

function noticeHtml(n, withActions) {
  let actions = '';
  if (withActions) {
    actions = '<div class="mt-2">' +
      '<button class="btn btn-sm btn-outline-primary me-1" onclick="showNoticeForm(' + n.id + ')"><i class="bi bi-pencil"></i> Edit</button>' +
      (isAdmin() ? '<button class="btn btn-sm btn-outline-danger" onclick="deleteNotice(' + n.id + ')"><i class="bi bi-trash"></i> Delete</button>' : '') +
      '</div>';
  }
  return '<div class="notice-card' + (n.important ? ' important' : '') + '">' +
    '<div class="d-flex justify-content-between"><strong>' + (n.important ? '<i class="bi bi-pin-angle-fill text-danger"></i> ' : '') + esc(n.title) + '</strong>' +
    '<small class="text-muted">' + fmtDate(n.postedDate) + '</small></div>' +
    '<div>' + esc(n.content) + '</div><small class="text-muted">Posted by ' + esc(n.postedBy) + '</small>' + actions + '</div>';
}

/* ============================ STUDENTS ============================ */
async function loadStudents() {
  const q = document.getElementById('studentSearch').value.trim();
  const results = await Promise.all([
    api.get('/api/admin/students?q=' + encodeURIComponent(q)), api.get('/api/admin/allocations')
  ]);
  state.students = results[0];
  const roomOf = {};
  results[1].forEach(function (a) { roomOf[a.student.id] = a.room.roomNumber; });

  document.getElementById('studentsBody').innerHTML = state.students.map(function (s) {
    return '<tr><td>' + esc(s.rollNo) + '</td><td class="fw-semibold">' + esc(s.name) + '</td><td>' + esc(s.course) +
      '</td><td>' + esc(s.yearOfStudy) + '</td><td>' + esc(s.phone) + '</td><td>' +
      (roomOf[s.id] ? '<span class="badge bg-primary">' + esc(roomOf[s.id]) + '</span>' : '<span class="text-muted">Not allotted</span>') +
      '</td><td class="text-end text-nowrap">' +
      '<button class="btn btn-sm btn-outline-secondary me-1" title="View" onclick="viewStudent(' + s.id + ')"><i class="bi bi-eye"></i></button>' +
      '<button class="btn btn-sm btn-outline-primary me-1" title="Edit" onclick="showStudentForm(' + s.id + ')"><i class="bi bi-pencil"></i></button>' +
      (isAdmin() ? '<button class="btn btn-sm btn-outline-danger" title="Delete" onclick="deleteStudent(' + s.id + ')"><i class="bi bi-trash"></i></button>' : '') +
      '</td></tr>';
  }).join('') || emptyRow(7, 'No students found');
}

function showStudentForm(id) {
  const s = id ? state.students.find(function (x) { return x.id === id; }) : null;
  const v = function (f) { return s ? esc(s[f]) : ''; };
  let years = '';
  for (let y = 1; y <= 5; y++) years += option(y, 'Year ' + y, s && s.yearOfStudy === y);

  const html =
    '<div class="row g-3">' +
    '<div class="col-md-6"><label class="form-label">Full Name *</label><input class="form-control" name="name" required value="' + v('name') + '"></div>' +
    '<div class="col-md-6"><label class="form-label">Roll Number *</label><input class="form-control" name="rollNo" required value="' + v('rollNo') + '"></div>' +
    '<div class="col-md-6"><label class="form-label">Email</label><input type="email" class="form-control" name="email" value="' + v('email') + '"></div>' +
    '<div class="col-md-6"><label class="form-label">Phone</label><input class="form-control" name="phone" value="' + v('phone') + '"></div>' +
    '<div class="col-md-6"><label class="form-label">Course</label><input class="form-control" name="course" value="' + v('course') + '"></div>' +
    '<div class="col-md-6"><label class="form-label">Year</label><select class="form-select" name="yearOfStudy">' + years + '</select></div>' +
    '<div class="col-md-6"><label class="form-label">Guardian Name</label><input class="form-control" name="guardianName" value="' + v('guardianName') + '"></div>' +
    '<div class="col-md-6"><label class="form-label">Guardian Phone</label><input class="form-control" name="guardianPhone" value="' + v('guardianPhone') + '"></div>' +
    '<div class="col-12"><label class="form-label">Address</label><input class="form-control" name="address" value="' + v('address') + '"></div>' +
    (s ? '' : '<div class="col-md-6"><label class="form-label">Login Username *</label><input class="form-control" name="username" required></div>') +
    '<div class="col-md-6"><label class="form-label">' + (s ? 'New Password (leave blank to keep)' : 'Login Password * (min 6 chars)') + '</label>' +
    '<input type="password" class="form-control" name="password" minlength="6" ' + (s ? '' : 'required') + '></div>' +
    '</div>';

  openModal(s ? 'Edit Student' : 'Add Student', html, async function (values) {
    values.yearOfStudy = Number(values.yearOfStudy);
    if (s) {
      await api.put('/api/admin/students/' + id, values);
    } else {
      await api.post('/api/admin/students', values);
    }
    toast(s ? 'Student updated' : 'Student added');
    await reloadCurrent();
  }, s ? 'Update' : 'Save');
}

async function viewStudent(id) {
  const d = await api.get('/api/admin/students/' + id);
  const s = d.student;
  const row = function (k, val) { return '<div><div class="k">' + k + '</div><div class="v">' + (val || '-') + '</div></div>'; };
  const html =
    '<div class="detail-grid mb-3">' +
    row('Name', esc(s.name)) + row('Roll No', esc(s.rollNo)) + row('Email', esc(s.email)) + row('Phone', esc(s.phone)) +
    row('Course', esc(s.course)) + row('Year', esc(s.yearOfStudy)) + row('Guardian', esc(s.guardianName)) +
    row('Guardian Phone', esc(s.guardianPhone)) + row('Address', esc(s.address)) + row('Joined', fmtDate(s.joinDate)) +
    row('Room', d.allocation ? esc(d.allocation.room.roomNumber) + ' (' + esc(d.allocation.room.blockName) + ')' : 'Not allotted') +
    row('Attendance', d.attendance.percent + '% (' + d.attendance.present + '/' + d.attendance.total + ' days)') +
    '</div>' +
    '<h6>Fees</h6><div class="table-wrap mb-3"><table class="table table-sm"><thead><tr><th>Description</th><th>Amount</th><th>Due</th><th>Status</th></tr></thead><tbody>' +
    (d.fees.map(function (f) {
      return '<tr><td>' + esc(f.description) + '</td><td>' + fmtMoney(f.amount) + '</td><td>' + fmtDate(f.dueDate) + '</td><td>' + feeBadge(f) + '</td></tr>';
    }).join('') || emptyRow(4, 'No fee records')) + '</tbody></table></div>' +
    '<h6>Complaints</h6><div class="table-wrap"><table class="table table-sm"><thead><tr><th>Title</th><th>Date</th><th>Status</th></tr></thead><tbody>' +
    (d.complaints.map(function (c) {
      return '<tr><td>' + esc(c.title) + '</td><td>' + fmtDate(c.createdAt) + '</td><td>' + badge(c.status) + '</td></tr>';
    }).join('') || emptyRow(3, 'No complaints')) + '</tbody></table></div>';
  openModal('Student Details - ' + s.name, html, null);
}

async function deleteStudent(id) {
  if (!confirm('Delete this student and all their fees, complaints and attendance? This cannot be undone.')) return;
  try {
    await api.del('/api/admin/students/' + id);
    toast('Student deleted');
    await reloadCurrent();
  } catch (e) { toast(e.message, 'danger'); }
}

/* ============================ ROOMS ============================ */
async function loadRooms() {
  state.rooms = await api.get('/api/admin/rooms?filter=' + roomFilter);
  document.getElementById('roomsBody').innerHTML = state.rooms.map(function (r) {
    return '<tr><td class="fw-semibold">' + esc(r.roomNumber) + '</td><td>' + esc(r.blockName) + '</td><td>' + esc(r.floorNo) +
      '</td><td>' + esc(r.roomType) + '</td><td>' + r.capacity + '</td><td>' + r.occupied + ' / ' + r.capacity +
      '</td><td>' + fmtMoney(r.monthlyRent) + '</td><td>' + badge(r.status) + '</td><td class="text-end text-nowrap">' +
      '<button class="btn btn-sm btn-outline-primary me-1" onclick="showRoomForm(' + r.id + ')"><i class="bi bi-pencil"></i></button>' +
      (isAdmin() ? '<button class="btn btn-sm btn-outline-danger" onclick="deleteRoom(' + r.id + ')"><i class="bi bi-trash"></i></button>' : '') +
      '</td></tr>';
  }).join('') || emptyRow(9, 'No rooms found');
}

async function showRoomForm(id) {
  let r = null;
  if (id) {
    const all = await api.get('/api/admin/rooms?filter=all');
    r = all.find(function (x) { return x.id === id; });
  }
  const v = function (f) { return r ? esc(r[f]) : ''; };
  const types = ['', 'Single', 'Double', 'Triple', 'Dormitory'].map(function (t) {
    return option(t, t === '' ? 'Auto (from capacity)' : t, r && r.roomType === t);
  }).join('');
  const html =
    '<div class="row g-3">' +
    '<div class="col-md-6"><label class="form-label">Room Number *</label><input class="form-control" name="roomNumber" required value="' + v('roomNumber') + '"></div>' +
    '<div class="col-md-6"><label class="form-label">Block / Hostel *</label><input class="form-control" name="blockName" required list="blockList" value="' + v('blockName') + '">' +
    '<datalist id="blockList"><option value="Block A (Boys)"><option value="Block B (Girls)"></datalist></div>' +
    '<div class="col-md-3"><label class="form-label">Floor</label><input type="number" min="0" class="form-control" name="floorNo" value="' + (r ? r.floorNo : 1) + '"></div>' +
    '<div class="col-md-3"><label class="form-label">Capacity *</label><input type="number" min="1" max="12" class="form-control" name="capacity" required value="' + (r ? r.capacity : 2) + '"></div>' +
    '<div class="col-md-3"><label class="form-label">Type</label><select class="form-select" name="roomType">' + types + '</select></div>' +
    '<div class="col-md-3"><label class="form-label">Rent / month (₹)</label><input type="number" min="0" class="form-control" name="monthlyRent" value="' + (r ? r.monthlyRent : 4500) + '"></div>' +
    '</div>';
  openModal(r ? 'Edit Room' : 'Add Room', html, async function (values) {
    values.floorNo = Number(values.floorNo);
    values.capacity = Number(values.capacity);
    values.monthlyRent = Number(values.monthlyRent);
    if (r) { await api.put('/api/admin/rooms/' + id, values); } else { await api.post('/api/admin/rooms', values); }
    toast(r ? 'Room updated' : 'Room added');
    await reloadCurrent();
  }, r ? 'Update' : 'Save');
}

async function deleteRoom(id) {
  if (!confirm('Delete this room?')) return;
  try {
    await api.del('/api/admin/rooms/' + id);
    toast('Room deleted');
    await reloadCurrent();
  } catch (e) { toast(e.message, 'danger'); }
}

/* ============================ ALLOCATIONS ============================ */
async function loadAllocations() {
  const list = await api.get('/api/admin/allocations');
  document.getElementById('allocBody').innerHTML = list.map(function (a) {
    return '<tr><td class="fw-semibold">' + esc(a.student.name) + '</td><td>' + esc(a.student.rollNo) + '</td><td><span class="badge bg-primary">' +
      esc(a.room.roomNumber) + '</span></td><td>' + esc(a.room.blockName) + '</td><td>' + fmtDate(a.allocatedDate) +
      '</td><td class="text-end text-nowrap">' +
      '<button class="btn btn-sm btn-outline-primary me-1" onclick="showChangeRoom(' + a.student.id + ')"><i class="bi bi-arrow-left-right"></i> Change Room</button>' +
      '<button class="btn btn-sm btn-outline-danger" onclick="vacateRoom(' + a.id + ')"><i class="bi bi-box-arrow-right"></i> Vacate</button></td></tr>';
  }).join('') || emptyRow(6, 'No active allocations');
}

function roomOptions(rooms) {
  return rooms.map(function (r) {
    return option(r.id, r.roomNumber + ' - ' + r.blockName + ' (' + r.occupied + '/' + r.capacity + ' occupied)');
  }).join('');
}

async function showAllocateForm() {
  const results = await Promise.all([
    api.get('/api/admin/students'), api.get('/api/admin/allocations'), api.get('/api/admin/rooms?filter=available')
  ]);
  const allocated = {};
  results[1].forEach(function (a) { allocated[a.student.id] = true; });
  const free = results[0].filter(function (s) { return !allocated[s.id]; });
  if (free.length === 0) { toast('All students already have rooms', 'warning'); return; }
  if (results[2].length === 0) { toast('No rooms with free beds. Add a room first.', 'warning'); return; }

  const html =
    '<div class="mb-3"><label class="form-label">Student (without a room) *</label><select class="form-select" name="studentId" required>' +
    free.map(function (s) { return option(s.id, s.name + ' (' + s.rollNo + ')'); }).join('') + '</select></div>' +
    '<div class="mb-3"><label class="form-label">Room (with free beds) *</label><select class="form-select" name="roomId" required>' +
    roomOptions(results[2]) + '</select></div>';
  openModal('Allocate Room', html, async function (values) {
    await api.post('/api/admin/allocations', { studentId: Number(values.studentId), roomId: Number(values.roomId) });
    toast('Room allocated');
    await reloadCurrent();
  }, 'Allocate');
}

async function showChangeRoom(studentId) {
  const rooms = await api.get('/api/admin/rooms?filter=available');
  if (rooms.length === 0) { toast('No rooms with free beds available', 'warning'); return; }
  const html = '<div class="mb-3"><label class="form-label">Move student to *</label><select class="form-select" name="roomId" required>' +
    roomOptions(rooms) + '</select></div>';
  openModal('Change Room', html, async function (values) {
    await api.post('/api/admin/allocations/change', { studentId: studentId, roomId: Number(values.roomId) });
    toast('Room changed');
    await reloadCurrent();
  }, 'Move');
}

async function vacateRoom(allocationId) {
  if (!confirm('Vacate this room for the student?')) return;
  try {
    await api.put('/api/admin/allocations/' + allocationId + '/vacate');
    toast('Room vacated');
    await reloadCurrent();
  } catch (e) { toast(e.message, 'danger'); }
}

/* ============================ ROOM CHANGE REQUESTS ============================ */
async function loadRequests() {
  state.requests = await api.get('/api/admin/room-requests');
  document.getElementById('requestsBody').innerHTML = state.requests.map(function (r) {
    const pending = r.status === 'PENDING';
    return '<tr><td class="fw-semibold">' + esc(r.student.name) + '</td><td>' + esc(r.currentRoom ? r.currentRoom.roomNumber : '-') +
      '</td><td>' + esc(r.requestedRoom ? r.requestedRoom.roomNumber : 'Any available') + '</td><td>' + esc(r.reason) +
      (r.adminRemarks ? '<div class="small text-muted">Remarks: ' + esc(r.adminRemarks) + '</div>' : '') +
      '</td><td>' + fmtDate(r.createdAt) + '</td><td>' + badge(r.status) + '</td><td class="text-end text-nowrap">' +
      (pending ? '<button class="btn btn-sm btn-success me-1" onclick="processRequest(' + r.id + ', true)">Approve</button>' +
        '<button class="btn btn-sm btn-outline-danger" onclick="processRequest(' + r.id + ', false)">Reject</button>' : '-') +
      '</td></tr>';
  }).join('') || emptyRow(7, 'No room change requests');
}

async function processRequest(id, approve) {
  const req = state.requests.find(function (x) { return x.id === id; });
  let roomField = '';
  if (approve) {
    const rooms = await api.get('/api/admin/rooms?filter=available');
    const currentId = req.currentRoom ? req.currentRoom.id : null;
    const choices = rooms.filter(function (r) { return r.id !== currentId; });
    if (choices.length === 0) { toast('No free rooms available to move into', 'warning'); return; }
    roomField = '<div class="mb-3"><label class="form-label">Move student to room *</label><select class="form-select" name="roomId" required>' +
      choices.map(function (r) {
        return option(r.id, r.roomNumber + ' - ' + r.blockName + ' (' + r.occupied + '/' + r.capacity + ')', req.requestedRoom && req.requestedRoom.id === r.id);
      }).join('') + '</select></div>';
  }
  const html = '<p><strong>' + esc(req.student.name) + '</strong>: ' + esc(req.reason) + '</p>' + roomField +
    '<div class="mb-3"><label class="form-label">Remarks (optional)</label><textarea class="form-control" name="remarks" rows="2"></textarea></div>';
  openModal(approve ? 'Approve Room Change' : 'Reject Request', html, async function (values) {
    const body = { roomId: values.roomId ? Number(values.roomId) : null, remarks: values.remarks };
    await api.put('/api/admin/room-requests/' + id + (approve ? '/approve' : '/reject'), body);
    toast(approve ? 'Request approved and student moved' : 'Request rejected');
    await reloadCurrent();
  }, approve ? 'Approve' : 'Reject');
}

/* ============================ FEES ============================ */
async function loadFees() {
  const results = await Promise.all([
    api.get('/api/admin/fees?status=' + feeFilter), api.get('/api/admin/reports')
  ]);
  state.fees = results[0];
  const rep = results[1];
  document.getElementById('feeCards').innerHTML =
    statCard('bi-check-circle', 'Fees Collected', fmtMoney(rep.feeCollected), 'bg-grad-green', rep.feesPaidCount + ' payments') +
    statCard('bi-hourglass-split', 'Fees Pending', fmtMoney(rep.feePending), 'bg-grad-orange', rep.feesPendingCount + ' records');

  document.getElementById('feesBody').innerHTML = state.fees.map(function (f) {
    return '<tr><td class="fw-semibold">' + esc(f.student.name) + '</td><td>' + esc(f.description) + '</td><td>' + fmtMoney(f.amount) +
      '</td><td>' + fmtDate(f.dueDate) + '</td><td>' + feeBadge(f) + '</td><td>' +
      (f.paidDate ? fmtDate(f.paidDate) + '<div class="small text-muted">' + esc(f.paymentMode) + '</div>' : '-') +
      '</td><td class="text-end text-nowrap">' +
      (f.status === 'PENDING' ? '<button class="btn btn-sm btn-success me-1" onclick="showPayForm(' + f.id + ')">Mark Paid</button>' : '') +
      (isAdmin() ? '<button class="btn btn-sm btn-outline-danger" onclick="deleteFee(' + f.id + ')"><i class="bi bi-trash"></i></button>' : '') +
      '</td></tr>';
  }).join('') || emptyRow(7, 'No fee records');
}

async function showFeeForm() {
  const students = await api.get('/api/admin/students');
  if (students.length === 0) { toast('Add a student first', 'warning'); return; }
  const d = new Date();
  d.setDate(d.getDate() + 10);
  const due = d.getFullYear() + '-' + String(d.getMonth() + 1).padStart(2, '0') + '-' + String(d.getDate()).padStart(2, '0');
  const html =
    '<div class="row g-3">' +
    '<div class="col-12"><label class="form-label">Student *</label><select class="form-select" name="studentId" required>' +
    students.map(function (s) { return option(s.id, s.name + ' (' + s.rollNo + ')'); }).join('') + '</select></div>' +
    '<div class="col-12"><label class="form-label">Description *</label><input class="form-control" name="description" required value="Hostel Fee"></div>' +
    '<div class="col-md-6"><label class="form-label">Amount (₹) *</label><input type="number" min="1" class="form-control" name="amount" required value="4500"></div>' +
    '<div class="col-md-6"><label class="form-label">Due Date *</label><input type="date" class="form-control" name="dueDate" required value="' + due + '"></div>' +
    '</div>';
  openModal('Add Fee', html, async function (values) {
    await api.post('/api/admin/fees', {
      studentId: Number(values.studentId), description: values.description,
      amount: Number(values.amount), dueDate: values.dueDate
    });
    toast('Fee added');
    await reloadCurrent();
  }, 'Save');
}

function showPayForm(id) {
  const html = '<div class="mb-3"><label class="form-label">Payment Mode</label><select class="form-select" name="paymentMode">' +
    ['Cash', 'UPI', 'Net Banking', 'Card', 'Cheque'].map(function (m) { return option(m, m); }).join('') + '</select></div>';
  openModal('Record Payment', html, async function (values) {
    await api.put('/api/admin/fees/' + id + '/pay', { paymentMode: values.paymentMode });
    toast('Fee marked as paid');
    await reloadCurrent();
  }, 'Mark Paid');
}

async function deleteFee(id) {
  if (!confirm('Delete this fee record?')) return;
  try {
    await api.del('/api/admin/fees/' + id);
    toast('Fee record deleted');
    await reloadCurrent();
  } catch (e) { toast(e.message, 'danger'); }
}

/* ============================ COMPLAINTS ============================ */
async function loadComplaints() {
  state.complaints = await api.get('/api/admin/complaints');
  renderComplaints();
}

function renderComplaints() {
  const f = document.getElementById('complaintFilter').value;
  const list = state.complaints.filter(function (c) { return f === 'ALL' || c.status === f; });
  document.getElementById('complaintsBody').innerHTML = list.map(function (c) {
    return '<tr><td class="fw-semibold">' + esc(c.student.name) + '</td><td>' + esc(c.title) +
      '<div class="small text-muted">' + esc(c.description) + '</div>' +
      (c.adminRemarks ? '<div class="small text-primary">Remarks: ' + esc(c.adminRemarks) + '</div>' : '') + '</td><td>' + esc(c.category) +
      '</td><td>' + fmtDate(c.createdAt) + '</td><td>' + badge(c.status) + '</td><td class="text-end">' +
      '<button class="btn btn-sm btn-outline-primary" onclick="showComplaintForm(' + c.id + ')">Update</button></td></tr>';
  }).join('') || emptyRow(6, 'No complaints found');
}

function showComplaintForm(id) {
  const c = state.complaints.find(function (x) { return x.id === id; });
  const html =
    '<p><strong>' + esc(c.title) + '</strong><br><span class="text-muted">' + esc(c.description) + '</span></p>' +
    '<div class="mb-3"><label class="form-label">Status</label><select class="form-select" name="status">' +
    option('PENDING', 'Pending', c.status === 'PENDING') + option('IN_PROGRESS', 'In progress', c.status === 'IN_PROGRESS') +
    option('RESOLVED', 'Resolved', c.status === 'RESOLVED') + '</select></div>' +
    '<div class="mb-3"><label class="form-label">Remarks for the student</label><textarea class="form-control" name="remarks" rows="3">' + esc(c.adminRemarks) + '</textarea></div>';
  openModal('Update Complaint', html, async function (values) {
    await api.put('/api/admin/complaints/' + id, { status: values.status, remarks: values.remarks });
    toast('Complaint updated');
    await reloadCurrent();
  }, 'Update');
}

/* ============================ ATTENDANCE ============================ */
async function loadAttendance() {
  const date = document.getElementById('attDate').value || today();
  const rows = await api.get('/api/admin/attendance?date=' + date);
  document.getElementById('attBody').innerHTML = rows.map(function (r) {
    return '<tr><td>' + esc(r.rollNo) + '</td><td class="fw-semibold">' + esc(r.name) + '</td><td>' +
      '<select class="form-select form-select-sm att-select" data-student="' + r.studentId + '">' +
      option('', '-- not marked --', !r.status) + option('PRESENT', 'Present', r.status === 'PRESENT') +
      option('ABSENT', 'Absent', r.status === 'ABSENT') + option('LEAVE', 'On leave', r.status === 'LEAVE') +
      '</select></td></tr>';
  }).join('') || emptyRow(3, 'No students');
}

function markAllPresent() {
  document.querySelectorAll('.att-select').forEach(function (s) { s.value = 'PRESENT'; });
}

async function saveAttendance() {
  const date = document.getElementById('attDate').value;
  const records = [];
  document.querySelectorAll('.att-select').forEach(function (s) {
    if (s.value) records.push({ studentId: Number(s.dataset.student), status: s.value });
  });
  if (records.length === 0) { toast('Select attendance for at least one student', 'warning'); return; }
  try {
    const res = await api.post('/api/admin/attendance', { date: date, records: records });
    toast(res.message);
  } catch (e) { toast(e.message, 'danger'); }
}

/* ============================ NOTICES ============================ */
async function loadNotices() {
  state.notices = await api.get('/api/admin/notices');
  document.getElementById('noticesList').innerHTML = state.notices.map(function (n) {
    return noticeHtml(n, true);
  }).join('') || '<div class="empty">No notices yet</div>';
}

function showNoticeForm(id) {
  const n = id ? state.notices.find(function (x) { return x.id === id; }) : null;
  const html =
    '<div class="mb-3"><label class="form-label">Title *</label><input class="form-control" name="title" required value="' + (n ? esc(n.title) : '') + '"></div>' +
    '<div class="mb-3"><label class="form-label">Content *</label><textarea class="form-control" name="content" rows="5" required>' + (n ? esc(n.content) : '') + '</textarea></div>' +
    '<div class="form-check"><input class="form-check-input" type="checkbox" name="important" id="impChk" value="yes"' + (n && n.important ? ' checked' : '') + '>' +
    '<label class="form-check-label" for="impChk">Mark as important</label></div>';
  openModal(n ? 'Edit Notice' : 'Add Notice', html, async function (values) {
    const body = { title: values.title, content: values.content, important: values.important === 'yes' };
    if (n) { await api.put('/api/admin/notices/' + id, body); } else { await api.post('/api/admin/notices', body); }
    toast(n ? 'Notice updated' : 'Notice published');
    await reloadCurrent();
  }, n ? 'Update' : 'Publish');
}

async function deleteNotice(id) {
  if (!confirm('Delete this notice?')) return;
  try {
    await api.del('/api/admin/notices/' + id);
    toast('Notice deleted');
    await reloadCurrent();
  } catch (e) { toast(e.message, 'danger'); }
}

/* ============================ REPORTS ============================ */
async function loadReports() {
  const r = await api.get('/api/admin/reports');
  document.getElementById('reportCards').innerHTML =
    statCard('bi-cash-stack', 'Fees Collected', fmtMoney(r.feeCollected), 'bg-grad-green') +
    statCard('bi-hourglass-split', 'Fees Pending', fmtMoney(r.feePending), 'bg-grad-orange') +
    statCard('bi-calendar-check', 'Overall Attendance', r.attendancePercent + '%', 'bg-grad-blue') +
    statCard('bi-exclamation-octagon', 'Open Complaints', (r.complaintsByStatus.PENDING + r.complaintsByStatus.IN_PROGRESS), 'bg-grad-red');

  const occupancy = r.occupancyByBlock.map(function (b) {
    const pct = b.capacity ? Math.round(b.occupied * 100 / b.capacity) : 0;
    return '<div class="mb-3"><div class="d-flex justify-content-between"><span>' + esc(b.block) + ' <small class="text-muted">(' + b.rooms + ' rooms)</small></span><span>' +
      b.occupied + ' / ' + b.capacity + ' beds (' + pct + '%)</span></div><div class="progress"><div class="progress-bar" style="width:' + pct + '%"></div></div></div>';
  }).join('') || '<div class="empty">No rooms</div>';

  const complaints = Object.keys(r.complaintsByStatus).map(function (k) {
    return '<tr><td>' + badge(k) + '</td><td class="text-end">' + r.complaintsByStatus[k] + '</td></tr>';
  }).join('');
  const courses = Object.keys(r.studentsByCourse).map(function (k) {
    return '<tr><td>' + esc(k) + '</td><td class="text-end">' + r.studentsByCourse[k] + '</td></tr>';
  }).join('') || emptyRow(2, 'No students');

  document.getElementById('reportPanels').innerHTML =
    '<div class="col-lg-6"><div class="panel"><div class="panel-title">Room Occupancy by Block</div>' + occupancy + '</div></div>' +
    '<div class="col-lg-3 col-md-6"><div class="panel"><div class="panel-title">Complaints by Status</div><table class="table table-sm mb-0"><tbody>' + complaints + '</tbody></table></div></div>' +
    '<div class="col-lg-3 col-md-6"><div class="panel"><div class="panel-title">Students by Course</div><table class="table table-sm mb-0"><tbody>' + courses + '</tbody></table></div></div>';
}
