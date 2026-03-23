const API_BASE = "http://localhost:8080/api";

document.addEventListener("DOMContentLoaded", () => {
    updateClock();
    setInterval(updateClock, 1000);
    fetchStatus();
});

function updateClock() {
    const now = new Date();

    // Formatting date: "Saturday, Feb 28, 2026"
    const dateOptions = { weekday: 'long', month: 'short', day: 'numeric', year: 'numeric' };
    document.getElementById('current-date').textContent = now.toLocaleDateString('en-US', dateOptions);

    // Formatting time: "10:11:50 PM"
    document.getElementById('current-time').textContent = now.toLocaleTimeString('en-US');
}

function showMessage(msg, isError = false) {
    const box = document.getElementById("message-container");
    box.textContent = msg;
    box.className = "message-box " + (isError ? "error" : "success");
    setTimeout(() => {
        box.classList.add("hidden");
    }, 5000);
}

async function parkCar() {
    const plate = document.getElementById("park-plate").value.trim();
    const type = document.getElementById("vehicle-type").value;
    if (!plate) {
        showMessage("Please enter a valid Registration Number", true);
        return;
    }

    try {
        const response = await fetch(`${API_BASE}/park?plate=${encodeURIComponent(plate)}&type=${encodeURIComponent(type)}`, { method: "POST" });
        const text = await response.text();
        showMessage(text, text.toLowerCase().includes("full") || text.toLowerCase().includes("already"));
        document.getElementById("park-plate").value = "";
        fetchStatus();
    } catch (err) {
        showMessage("Make sure the Java backend is running!", true);
    }
}

async function bookCar() {
    const plate = document.getElementById("park-plate").value.trim();
    const type = document.getElementById("vehicle-type").value;
    if (!plate) {
        showMessage("Please enter a valid Registration Number to book", true);
        return;
    }

    try {
        const response = await fetch(`${API_BASE}/book?plate=${encodeURIComponent(plate)}&type=${encodeURIComponent(type)}`, { method: "POST" });
        const text = await response.text();
        showMessage(text, text.toLowerCase().includes("full") || text.toLowerCase().includes("already"));
        document.getElementById("park-plate").value = "";
        fetchStatus();
    } catch (err) {
        showMessage("Make sure the Java backend is running!", true);
    }
}

async function unbookSlot() {
    const slotStr = document.getElementById("unbook-slot").value.trim();
    if (!slotStr || isNaN(slotStr)) {
        showMessage("Please enter a valid slot number", true);
        return;
    }

    try {
        const response = await fetch(`${API_BASE}/unbook?slot=${encodeURIComponent(slotStr)}`, { method: "POST" });
        const text = await response.text();
        showMessage(text, text.toLowerCase().includes("not found") || text.toLowerCase().includes("invalid"));
        document.getElementById("unbook-slot").value = "";
        fetchStatus();
    } catch (err) {
        showMessage("Make sure the Java backend is running!", true);
    }
}

async function removeCar() {
    const plate = document.getElementById("remove-plate").value.trim();
    if (!plate) {
        showMessage("Please enter a valid Registration Number", true);
        return;
    }

    try {
        const response = await fetch(`${API_BASE}/remove?plate=${encodeURIComponent(plate)}`, { method: "POST" });
        const text = await response.text();
        showMessage(text, text.toLowerCase().includes("not found"));
        document.getElementById("remove-plate").value = "";
        fetchStatus();
    } catch (err) {
        showMessage("Make sure the Java backend is running!", true);
    }
}

async function fetchStatus() {
    try {
        const response = await fetch(`${API_BASE}/status`);
        const data = await response.json();

        const container = document.getElementById("slots-container");
        container.innerHTML = "";

        let capacity = 0;
        let parked = 0;

        for (const [slot, value] of Object.entries(data)) {
            capacity++;
            const isVacant = value === 'Empty';
            if (!isVacant) parked++;

            let plateText = isVacant ? 'CAR' : value.toUpperCase();
            let iconClass = "fa-car-side";

            if (!isVacant) {
                const parts = value.split('-');
                if (parts.length > 1) {
                    plateText = parts[0].toUpperCase();
                    const vType = parts[1].toLowerCase();
                    if (vType === 'bike') iconClass = "fa-motorcycle";
                    else if (vType === 'truck') iconClass = "fa-truck";
                }
            }

            const slotNum = parseInt(slot);
            const floor = slotNum <= 5 ? 'F1' : 'F2';
            const displayNumber = slotNum <= 5 ? (100 + slotNum) : (200 + slotNum - 5);

            const div = document.createElement("div");
            div.className = `slot ${isVacant ? 'vacant' : 'busy'}`;

            div.innerHTML = `
                <div class="slot-id">${floor} #${displayNumber}</div>
                <div class="slot-icon"><i class="fa-solid ${iconClass}"></i></div>
                <div class="slot-status">
                    <div class="slot-state">${isVacant ? 'VACANT' : 'BUSY'}</div>
                    <div class="slot-plate">${plateText}</div>
                </div>
            `;
            container.appendChild(div);
        }

        document.getElementById("stat-capacity").textContent = capacity;
        document.getElementById("stat-parked").textContent = parked;
        document.getElementById("stat-available").textContent = (capacity - parked);

    } catch (err) {
        console.error("Backend not running or CORS issue", err);
    }
}
