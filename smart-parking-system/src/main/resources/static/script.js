document.addEventListener('DOMContentLoaded', () => {
    // Elements
    const slotsContainer = document.getElementById('slotsContainer');
    const parkBtn = document.getElementById('parkBtn');
    const bookBtn = document.getElementById('bookBtn');
    const removeBtn = document.getElementById('removeBtn');
    const vehicleInput = document.getElementById('vehicleInput');
    const vehicleTypeSelect = document.getElementById('vehicleType');
    const slotInput = document.getElementById('slotInput');
    const toastContainer = document.getElementById('toastContainer');

    // Stats elements
    const totalSlotsEl = document.getElementById('totalSlots');
    const occupiedSlotsEl = document.getElementById('occupiedSlots');
    const availableSlotsEl = document.getElementById('availableSlots');
    const occupancyGauge = document.getElementById('occupancyGauge');
    const progressValue = document.querySelector('.progress-value');

    const BASE_URL = '/parking';

    // Toast Notification System
    const showToast = (message, type = 'info') => {
        const toast = document.createElement('div');
        toast.className = `toast ${type}`;

        let icon = 'info';
        let color = '#3b82f6';
        if (type === 'success') { icon = 'check-circle'; color = '#10b981'; }
        if (type === 'error') { icon = 'alert-circle'; color = '#f43f5e'; }

        toast.innerHTML = `
            <i data-lucide="${icon}" color="${color}"></i>
            <span>${message}</span>
        `;

        toastContainer.appendChild(toast);
        lucide.createIcons();

        setTimeout(() => {
            toast.style.opacity = '0';
            toast.style.transform = 'translateX(100%)';
            setTimeout(() => toast.remove(), 400);
        }, 4000);
    };

    // Live Clock
    const updateTime = () => {
        const now = new Date();
        document.getElementById('liveTime').textContent = now.toLocaleTimeString();
        document.getElementById('liveDate').textContent = now.toLocaleDateString('en-US', {
            weekday: 'long', year: 'numeric', month: 'short', day: 'numeric'
        });
    };
    setInterval(updateTime, 1000);
    updateTime();

    // Fetch and render slots
    const refreshDashboard = async () => {
        try {
            const response = await fetch(`${BASE_URL}/slots`);
            const slots = await response.json();
            renderSlots(slots);
            updateStats();
        } catch (error) {
            console.error('Error fetching slots:', error);
        }
    };

    const renderSlots = (slots) => {
        slotsContainer.innerHTML = '';
        slots.forEach(slot => {
            const slotEl = document.createElement('div');
            slotEl.className = `slot-box ${slot.occupied ? 'occupied' : 'available'}`;

            const isBooked = slot.vehicleNumber && slot.vehicleNumber.includes('(Booked)');
            const vehicleId = isBooked ? slot.vehicleNumber.replace(' (Booked)', '') : slot.vehicleNumber;

            if (slot.occupied) {
                slotEl.innerHTML = `
                    <span class="slot-id">F${slot.floorNumber} #${slot.slotNumber}</span>
                    <div class="car-visualization">
                        <i data-lucide="${isBooked ? 'calendar-days' : 'car'}" size="48" color="var(--accent)"></i>
                    </div>
                    <span class="status-label" style="color: ${isBooked ? 'var(--warning)' : 'var(--danger)'}">${isBooked ? 'RESERVED' : 'BUSY'}</span>
                    <span style="font-size: 0.7rem; color: var(--text-muted); font-weight: 600;">${vehicleId}</span>
                    <span style="font-size: 0.6rem; color: var(--accent); opacity: 0.6; font-weight: 800;">${slot.slotType}</span>
                `;
                slotEl.style.cursor = 'pointer';
                if (!isBooked) {
                    slotEl.onclick = () => removeVehicle(vehicleId);
                }
            } else {
                slotEl.innerHTML = `
                    <span class="slot-id">F${slot.floorNumber} #${slot.slotNumber}</span>
                    <div class="car-visualization" style="opacity: 0.1">
                        <i data-lucide="car-front" size="48"></i>
                    </div>
                    <span class="status-label" style="color: var(--success)">VACANT</span>
                    <span style="font-size: 0.6rem; color: var(--text-muted); font-weight: 800;">${slot.slotType}</span>
                `;
            }

            slotsContainer.appendChild(slotEl);
        });
        lucide.createIcons();
    };

    const updateStats = async () => {
        try {
            const response = await fetch(`${BASE_URL}/stats`);
            const stats = await response.json();

            totalSlotsEl.textContent = stats.totalSlots;
            occupiedSlotsEl.textContent = stats.occupiedSlots;
            availableSlotsEl.textContent = stats.availableSlots;

            // Animate Gauge
            const percent = Math.round(stats.occupancyPercentage);
            progressValue.textContent = `${percent}%`;
            occupancyGauge.style.background = `conic-gradient(
                var(--accent) ${percent * 3.6}deg,
                rgba(255, 255, 255, 0.05) 0deg
            )`;
        } catch (e) { console.error("Stats fail", e); }
    };

    // Park Vehicle
    parkBtn.onclick = async () => {
        const vehicleNumber = vehicleInput.value.trim();
        const vehicleType = vehicleTypeSelect.value;
        if (!vehicleNumber) {
            showToast('Please enter a vehicle plate number', 'error');
            return;
        }

        try {
            const response = await fetch(`${BASE_URL}/park?vehicleNumber=${encodeURIComponent(vehicleNumber)}&vehicleType=${vehicleType}`, {
                method: 'POST'
            });
            const result = await response.text();

            if (result.includes('parked')) {
                showToast(result, 'success');
                vehicleInput.value = '';
            } else {
                showToast(result, 'error');
            }
            refreshDashboard();
        } catch (error) {
            showToast('Server connection failed', 'error');
        }
    };

    // Book Slot
    bookBtn.onclick = async () => {
        const vehicleNumber = vehicleInput.value.trim();
        const vehicleType = vehicleTypeSelect.value;
        if (!vehicleNumber) {
            showToast('Please enter a vehicle number to book', 'error');
            return;
        }

        try {
            const response = await fetch(`${BASE_URL}/book?vehicleNumber=${encodeURIComponent(vehicleNumber)}&vehicleType=${vehicleType}`, {
                method: 'POST'
            });
            const result = await response.text();

            if (result.includes('confirmed')) {
                showToast(result, 'success');
                vehicleInput.value = '';
            } else {
                showToast(result, 'error');
            }
            refreshDashboard();
        } catch (error) {
            showToast('Booking failed', 'error');
        }
    };

    // Remove Vehicle by Slot Number
    const removeBySlot = async (slotNumber) => {
        try {
            const response = await fetch(`${BASE_URL}/removeBySlot?slotNumber=${slotNumber}`, {
                method: 'POST'
            });
            const result = await response.text();

            if (result.includes('removed') || result.includes('cancelled')) {
                showToast(result, 'success');
                slotInput.value = '';
            } else {
                showToast(result, 'error');
            }
            refreshDashboard();
        } catch (error) {
            showToast('Execution failed', 'error');
        }
    };

    removeBtn.onclick = () => {
        const slotNumber = slotInput.value.trim();
        if (!slotNumber) {
            showToast('Enter a slot number to remove', 'error');
            return;
        }
        removeBySlot(slotNumber);
    };

    // Initial load and periodic refresh
    refreshDashboard();
    setInterval(refreshDashboard, 5000);
});
