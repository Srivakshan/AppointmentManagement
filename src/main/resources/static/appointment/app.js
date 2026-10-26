const API = "/api/appointments";

async function request(path = "", method = "GET", data) {
    const options = { method, headers: {} };
    if (data !== undefined) {
        options.headers["Content-Type"] = "application/json";
        options.body = JSON.stringify(data);
    }
    const response = await fetch(API + path, options);
    const text = await response.text();
    let value;
    try { value = JSON.parse(text); } catch { value = text; }
    if (!response.ok) {
        throw new Error(typeof value === "string" ? value : "Request failed (" + response.status + ")");
    }
    return value;
}

function message(text, error = false) {
    const el = document.getElementById("message");
    if (!el) return;
    el.textContent = text;
    el.className = "notice" + (error ? " error" : "");
    el.style.display = "block";
}

function cell(row, value) {
    const td = document.createElement("td");
    td.textContent = value == null ? "" : String(value);
    row.appendChild(td);
    return td;
}

function localToday() {
    const now = new Date();
    const year = now.getFullYear();
    const month = String(now.getMonth()+1).padStart(2,"0");
    const day = String(now.getDate()).padStart(2,"0");
    return `${year}-${month}-${day}`;
}
