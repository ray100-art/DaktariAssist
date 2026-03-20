// =============================================
// DaktariAssist — app.js
// All frontend logic: form, API calls, rendering
// =============================================


// Toggle a symptom or history chip on/off
function toggleChip(el) {
    el.classList.toggle('active');
}


// Get all selected chips from a container by ID
function getSelectedChips(containerId) {
    const container = document.getElementById(containerId);
    return [...container.querySelectorAll('.chip.active')]
        .map(c => c.textContent.trim());
}


// Show one state on the right panel, hide all others
function showState(state) {
    document.getElementById('emptyState').style.display   = 'none';
    document.getElementById('loadingState').style.display = 'none';
    document.getElementById('resultPanel').style.display  = 'none';
    document.getElementById('errorState').style.display   = 'none';

    const el = document.getElementById(state);
    if (state === 'loadingState' || state === 'errorState') {
        el.style.display = 'flex';
    } else {
        el.style.display = 'block';
    }
}


// =============================================
// ANALYSE — main function called by the button
// =============================================
async function analyse() {

    // Read form values
    const age               = document.getElementById('age').value;
    const sex               = document.getElementById('sex').value;
    const chiefComplaint    = document.getElementById('chiefComplaint').value.trim();
    const proposedDiagnosis = document.getElementById('proposedDiagnosis').value.trim();

    // Validate required fields
    if (!age || !sex || !chiefComplaint || !proposedDiagnosis) {
        alert('Please fill in Age, Sex, Chief Complaint, and Proposed Diagnosis.');
        return;
    }

    // Build the request object — matches DiagnosisRequest.java exactly
    const requestData = {
        age:               parseInt(age),
        sex:               sex,
        chiefComplaint:    chiefComplaint,
        duration:          document.getElementById('duration').value.trim(),
        symptoms:          getSelectedChips('symptomsContainer'),
        temperature:       document.getElementById('temperature').value.trim(),
        bloodPressure:     document.getElementById('bloodPressure').value.trim(),
        heartRate:         document.getElementById('heartRate').value.trim(),
        spo2:              document.getElementById('spo2').value.trim(),
        respiratoryRate:   document.getElementById('respiratoryRate').value.trim(),
        medicalHistory:    getSelectedChips('historyContainer'),
        proposedDiagnosis: proposedDiagnosis
    };

    // Show loading spinner
    showState('loadingState');
    document.getElementById('analyseBtn').disabled = true;

    try {
        // POST to Spring Boot backend
        const response = await fetch('/api/analyse', {
            method:  'POST',
            headers: { 'Content-Type': 'application/json' },
            body:    JSON.stringify(requestData)
        });

        const data = await response.json();

        if (data.error) {
            document.getElementById('errorMessage').textContent = data.error;
            showState('errorState');
        } else {
            renderResult(data);
            showState('resultPanel');
        }

    } catch (err) {
        document.getElementById('errorMessage').textContent =
            'Could not connect to the server. Make sure Spring Boot is running on port 8080.';
        showState('errorState');

    } finally {
        document.getElementById('analyseBtn').disabled = false;
    }
}


// =============================================
// RENDER RESULT — builds the right panel UI
// =============================================
function renderResult(data) {

    // 1. Consistency bar
    const cfg = {
        green: { icon: '✅', cls: 'cb-green' },
        amber: { icon: '⚠️', cls: 'cb-amber' },
        red:   { icon: '🚨', cls: 'cb-red'   }
    };
    const c = cfg[data.consistency] || cfg.amber;
    const bar = document.getElementById('consistencyBar');
    bar.className = 'consistency-bar ' + c.cls;
    bar.innerHTML = `
        <div class="cb-icon">${c.icon}</div>
        <div>
            <div class="cb-title">${data.consistencyTitle || ''}</div>
            <div class="cb-desc">${data.consistencyDescription || ''}</div>
        </div>`;

    // 2. Red flags
    const flagsEl = document.getElementById('redFlagsContent');
    if (data.redFlags && data.redFlags.length > 0) {
        flagsEl.innerHTML = data.redFlags.map(f =>
            `<div class="flag-item">
                <span class="flag-mark">!</span>
                <span>${f}</span>
             </div>`
        ).join('');
    } else {
        flagsEl.innerHTML = `
            <div class="flag-item">
                <span class="flag-mark" style="color:#22c55e">✓</span>
                <span>No immediate red flags identified.</span>
            </div>`;
    }

    // 3. Differentials
    const diffsEl = document.getElementById('differentialsContent');
    if (data.differentials && data.differentials.length > 0) {
        diffsEl.innerHTML = data.differentials.map(d =>
            `<div class="diff-card">
                <div class="diff-name">${d.name}</div>
                <div class="diff-reason">${d.reason}</div>
             </div>`
        ).join('');
    } else {
        diffsEl.innerHTML = '<p style="font-size:13px;color:#64748b">No additional differentials suggested.</p>';
    }

    // 4. Recommended tests
    const testsEl = document.getElementById('testsContent');
    if (data.recommendedTests && data.recommendedTests.length > 0) {
        testsEl.innerHTML = data.recommendedTests
            .map(t => `<span class="test-tag">${t}</span>`)
            .join('');
    } else {
        testsEl.innerHTML = '<p style="font-size:13px;color:#64748b">No specific tests recommended.</p>';
    }

    // 5. Follow-up question
    document.getElementById('followupContent').textContent =
        data.followupQuestion || 'No specific follow-up question suggested.';

    // Clear previous chat response
    const chatResp = document.getElementById('chatResponse');
    chatResp.style.display = 'none';
    chatResp.textContent   = '';
    document.getElementById('chatInput').value = '';
}


// =============================================
// ASK FOLLOW-UP — sends a follow-up question
// =============================================
async function askFollowup() {

    const question = document.getElementById('chatInput').value.trim();
    if (!question) return;

    const requestData = {
        age:               parseInt(document.getElementById('age').value) || 0,
        sex:               document.getElementById('sex').value,
        chiefComplaint:    document.getElementById('chiefComplaint').value.trim(),
        symptoms:          getSelectedChips('symptomsContainer'),
        proposedDiagnosis: document.getElementById('proposedDiagnosis').value.trim(),
        followupQuestion:  question
    };

    const chatResp = document.getElementById('chatResponse');
    chatResp.style.display = 'block';
    chatResp.textContent   = 'Thinking...';

    try {
        const response = await fetch('/api/analyse', {
            method:  'POST',
            headers: { 'Content-Type': 'application/json' },
            body:    JSON.stringify(requestData)
        });
        const data = await response.json();
        chatResp.textContent = data.followupQuestion || 'No response received.';
    } catch (err) {
        chatResp.textContent = 'Could not get a response. Please try again.';
    }
}