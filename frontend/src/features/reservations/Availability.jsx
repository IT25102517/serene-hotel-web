import React, { useState, useEffect } from "react";
import { api } from "../../api";
const today = () => new Date().toLocaleDateString("en-CA");
export default function Availability() {
  const [date, setDate] = useState(today()),
    [rows, setRows] = useState([]),
    [error, setError] = useState("");
  useEffect(() => {
    let active = true;
    const load = () =>
      api(`/public/availability?date=${date}`)
        .then((r) => {
          if (active) {
            setRows(r);
            setError("");
          }
        })
        .catch((e) => {
          if (active) setError(e.message);
        });
    load();
    const timer = setInterval(load, 15000);
    return () => {
      active = false;
      clearInterval(timer);
    };
  }, [date]);
  return (
    <section className="availability">
      <div>
        <h3>Find your date</h3>
        <small>
          Refreshed every 15 seconds. Your booking is checked again when saved.
        </small>
      </div>
      <label>
        Wedding date
        <input
          type="date"
          min={today()}
          required
          value={date}
          onChange={(e) => e.target.value && setDate(e.target.value)}
        />
      </label>
      <div className="hall-list">
        {rows.map((r) => (
          <div key={r.hall}>
            <strong>{r.hall}</strong>
            <small>{r.capacity} guests</small>
            <span className={`badge ${r.available ? "green" : "pink"}`}>
              {r.available ? "Available" : "Reserved"}
            </span>
          </div>
        ))}
      </div>
      {error && <p className="error">{error}</p>}
    </section>
  );
}
