import React, { useEffect, useState } from "react";
import { api, money } from "../../api";
export default function Analytics() {
  const year = new Date().getFullYear();
  const [from, setFrom] = useState(`${year}-01-01`),
    [to, setTo] = useState(`${year + 1}-12-31`),
    [data, setData] = useState(null),
    [error, setError] = useState(""),
    [busy, setBusy] = useState(false);
  const load = async () => {
    setBusy(true);
    try {
      setData(await api(`/marketing/analytics?from=${from}&to=${to}`));
      setError("");
    } catch (e) {
      setError(e.message);
    } finally {
      setBusy(false);
    }
  };
  useEffect(() => {
    load();
  }, []);
  const chart = (title, rows) => (
    <article className="data-panel">
      <h3>{title}</h3>
      {rows.length ? (
        rows.map((r, i) => (
          <div className="chart-row" key={i}>
            <span>{r.label}</span>
            <div>
              <i
                style={{
                  width:
                    Math.max(
                      2,
                      (r.bookings / Math.max(...rows.map((x) => x.bookings))) *
                        100,
                    ) + "%",
                }}
              />
            </div>
            <strong>{r.bookings}</strong>
          </div>
        ))
      ) : (
        <p>No confirmed bookings in this period.</p>
      )}
    </article>
  );
  const seasons = {};
  data?.months.forEach((r) => {
    const key = `${r.year} Q${Math.ceil(r.month / 3)} · ${r.venue}`;
    seasons[key] = (seasons[key] || 0) + r.bookings;
  });
  return (
    <section>
      <div className="section-heading">
        <div>
          <p className="eyebrow">MARKETING INSIGHTS</p>
          <h1>What brings couples to Serene?</h1>
        </div>
      </div>
      <form
        className="filter-row"
        onSubmit={(e) => {
          e.preventDefault();
          load();
        }}
      >
        <label>
          Wedding dates from
          <input
            type="date"
            required
            value={from}
            onChange={(e) => setFrom(e.target.value)}
          />
        </label>
        <label>
          To
          <input
            type="date"
            required
            value={to}
            onChange={(e) => setTo(e.target.value)}
          />
        </label>
        <button className="primary" disabled={busy}>
          {busy ? "Loading…" : "Update insights"}
        </button>
      </form>
      {error && <p className="error">{error}</p>}
      {data && (
        <>
          <p className="analytics-note">{data.note}</p>
          <div className="analytics-grid">
            {chart("Most booked packages", data.packages)}
            {chart("Venue demand", data.venues)}
          </div>
          <div className="data-panel">
            <h3>Venue demand by calendar season</h3>
            <p>
              Q1 January–March · Q2 April–June · Q3 July–September · Q4
              October–December. Calendar quarters, not weather classifications.
            </p>
            {chart(
              "Bookings by season and venue",
              Object.entries(seasons).map(([label, bookings]) => ({
                label,
                bookings,
              })),
            )}
          </div>
          <div className="data-panel">
            <h3>Venue price and demand</h3>
            <div className="table-scroll">
              <table>
                <thead>
                  <tr>
                    <th>Venue</th>
                    <th>Confirmed bookings</th>
                    <th>Current venue price</th>
                    <th>Average guests</th>
                    <th>Quoted booking value</th>
                  </tr>
                </thead>
                <tbody>
                  {data.venues.map((r) => (
                    <tr key={r.label}>
                      <td>{r.label}</td>
                      <td>{r.bookings}</td>
                      <td>
                        {r.currentVenuePrice == null
                          ? "Quote only"
                          : money(r.currentVenuePrice)}
                      </td>
                      <td>{Number(r.averageGuests).toFixed(0)}</td>
                      <td>{money(r.quotedValue)}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
            <p>
              Catalogue price and booking count suggest patterns; they do not
              establish that changing a price caused demand.
            </p>
          </div>
          <div className="stats">
            {data.inquiries.map((r) => (
              <article key={r.label}>
                <small>{r.label} inquiries</small>
                <strong>{r.inquiries}</strong>
              </article>
            ))}
          </div>
        </>
      )}
    </section>
  );
}
