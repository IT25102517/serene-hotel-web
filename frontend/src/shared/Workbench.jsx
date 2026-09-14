import React, { useState, useEffect } from "react";
import {
  Plus,
  Search,
  RefreshCw,
  Printer,
  Flower2,
  Check,
  X,
} from "lucide-react";
import { api } from "../api";
const money = (n) =>
  new Intl.NumberFormat("en-LK", {
    style: "currency",
    currency: "LKR",
    maximumFractionDigits: 2,
  }).format(n || 0);
const today = () => new Date().toLocaleDateString("en-CA");
export function Workbench({ config, user }) {
  const [rows, setRows] = useState([]),
    [query, setQuery] = useState(""),
    [filter, setFilter] = useState("ALL"),
    [editing, setEditing] = useState(null),
    [error, setError] = useState(""),
    [notice, setNotice] = useState(""),
    [loading, setLoading] = useState(true),
    [busy, setBusy] = useState(false),
    [logs, setLogs] = useState(null),
    [payment, setPayment] = useState(null),
    [remove, setRemove] = useState(null);
  const customer = user.role === "CUSTOMER",
    key = config.key;
  const load = async () => {
    setLoading(true);
    try {
      setRows(await api(`/${key}`));
      setError("");
    } catch (e) {
      setError(e.message);
    } finally {
      setLoading(false);
    }
  };
  useEffect(() => {
    load();
  }, [key]);
  const statuses = config.fields
    .find((f) => f.name === "status")
    ?.type.slice(7)
    .split(",") || ["UNPAID", "PARTIAL", "PAID"];
  const visible = rows
    .filter(
      (r) =>
        JSON.stringify(r).toLowerCase().includes(query.toLowerCase()) &&
        (filter === "ALL" || r.status === filter),
    )
    .sort((a, b) =>
      key === "events"
        ? `${a.eventDate} ${a.startTime}`.localeCompare(
            `${b.eventDate} ${b.startTime}`,
          )
        : b.id - a.id,
    );
  const create = () => {
    let data = {};
    config.fields.forEach((f) => {
      data[f.name] = f.type.startsWith("select:")
        ? f.type.slice(7).split(",")[0]
        : f.type === "number"
          ? ""
          : f.type === "date"
            ? today()
            : "";
    });
    setEditing(data);
    setError("");
  };
  const canEdit = !(customer && (key === "finance" || key === "reservations"));
  const metric =
    key === "finance"
      ? money(rows.reduce((sum, r) => sum + Number(r.balance), 0))
      : key === "feedback"
        ? rows.length
          ? (rows.reduce((sum, r) => sum + r.rating, 0) / rows.length).toFixed(
              1,
            )
          : "—"
        : rows.filter((r) =>
            [
              "CONFIRMED",
              "COMPLETED",
              "PUBLISHED",
              "RESOLVED",
              "CLOSED",
            ].includes(r.status),
          ).length;
  return (
    <section className="workbench">
      <div className="section-heading">
        <div>
          <p className="eyebrow">
            {customer ? "MY WEDDING" : config.role?.toUpperCase()}
          </p>
          <h1>{config.title}</h1>
          <p>{config.minor}</p>
        </div>
        {!(customer && key === "finance") && (
          <button className="primary" onClick={create}>
            <Plus size={18} />{" "}
            {key === "reservations"
              ? "New reservation"
              : key === "feedback"
                ? "Share feedback"
                : "Create record"}
          </button>
        )}
      </div>
      <div className="stats">
        <div>
          <small>Total records</small>
          <strong>{rows.length.toString().padStart(2, "0")}</strong>
          <span>In your workspace</span>
        </div>
        <div>
          <small>
            {key === "finance"
              ? "Outstanding balance"
              : key === "feedback"
                ? "Average guest rating"
                : "Completed / confirmed"}
          </small>
          <strong>{metric}</strong>
          <span>
            {key === "finance"
              ? "Verified offline payments only"
              : key === "feedback"
                ? "Out of 5 stars"
                : "Current records"}
          </span>
        </div>
        <div>
          <small>{customer ? "Your account" : "Assigned department"}</small>
          <strong className="small-stat">{user.username}</strong>
          <span>
            {customer
              ? "Only your records appear here"
              : "Role-protected access"}
          </span>
        </div>
      </div>
      <div className="panel">
        <div className="toolbar">
          <label className="search">
            <Search size={17} />
            <input
              placeholder="Search your records…"
              aria-label="Search records"
              value={query}
              onChange={(e) => setQuery(e.target.value)}
            />
          </label>
          <select
            aria-label="Filter by status"
            value={filter}
            onChange={(e) => setFilter(e.target.value)}
          >
            <option value="ALL">All statuses</option>
            {statuses.map((s) => (
              <option key={s}>{s}</option>
            ))}
          </select>
          <button className="secondary" onClick={load}>
            <RefreshCw size={15} /> Refresh
          </button>
          <button className="secondary" onClick={() => window.print()}>
            <Printer size={15} />{" "}
            {key === "events" ? "Print itinerary" : "Print"}
          </button>
          {!customer && (
            <button
              className="secondary"
              onClick={async () => {
                try {
                  setLogs(await api(`/${key}/activity`));
                } catch (e) {
                  setError(e.message);
                }
              }}
            >
              Activity
            </button>
          )}
        </div>
        {error && (
          <p className="error" role="alert">
            {error}
          </p>
        )}
        {notice && (
          <p className="success" role="status">
            {notice}
          </p>
        )}
        {loading ? (
          <p className="empty">Loading your records…</p>
        ) : !visible.length ? (
          <div className="empty">
            <Flower2 size={32} />
            <h3>{rows.length ? "No matching records" : "A fresh start"}</h3>
            <p>
              {rows.length
                ? "Try a different search or status."
                : "Create your first record to get started."}
            </p>
          </div>
        ) : (
          <div className="table-scroll">
            <table>
              <thead>
                <tr>
                  <th>Reference</th>
                  {config.fields.slice(0, 3).map((f) => (
                    <th key={f.name}>{f.label}</th>
                  ))}
                  <th>{key === "finance" ? "Balance" : "Status"}</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {visible.map((r) => (
                  <tr key={r.id}>
                    <td>
                      <strong>#{r.id.toString().padStart(4, "0")}</strong>
                      <small>
                        {new Date(r.createdAt).toLocaleDateString()}
                      </small>
                    </td>
                    {config.fields.slice(0, 3).map((f) => (
                      <td key={f.name}>{String(r[f.name] ?? "")}</td>
                    ))}
                    <td>
                      <span className="badge">
                        {key === "finance" ? money(r.balance) : r.status}
                      </span>
                    </td>
                    <td>
                      <div className="row-actions">
                        <button
                          onClick={() => {
                            setEditing({ ...r, _readOnly: !canEdit });
                            setError("");
                          }}
                        >
                          {canEdit ? "View / edit" : "Details"}
                        </button>
                        {!customer &&
                          key === "finance" &&
                          Number(r.balance) > 0 && (
                            <button
                              onClick={() =>
                                setPayment({
                                  id: r.id,
                                  amount: "",
                                  reference: "",
                                })
                              }
                            >
                              Add payment
                            </button>
                          )}
                        {!customer && (
                          <button
                            className="danger-link"
                            onClick={() => setRemove(r)}
                          >
                            Remove
                          </button>
                        )}
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
      {editing && (
        <div className="modal-backdrop">
          <section
            className="modal wide"
            role="dialog"
            aria-modal="true"
            aria-labelledby="record-title"
          >
            <button
              className="close icon"
              onClick={() => setEditing(null)}
              aria-label="Close record"
            >
              <X />
            </button>
            <p className="eyebrow">{config.title}</p>
            <h2 id="record-title">
              {editing.id
                ? `Record #${editing.id}`
                : "A new detail for your day"}
            </h2>
            <form
              onSubmit={async (e) => {
                e.preventDefault();
                setBusy(true);
                setError("");
                let payload = {};
                config.fields.forEach((f) => {
                  if (f.name in editing)
                    payload[f.name] =
                      f.type === "number"
                        ? editing[f.name] === ""
                          ? null
                          : Number(editing[f.name])
                        : editing[f.name];
                });
                if (editing.id) payload.version = editing.version;
                try {
                  await api(`/${key}${editing.id ? "/" + editing.id : ""}`, {
                    method: editing.id ? "PUT" : "POST",
                    body: JSON.stringify(payload),
                  });
                  setNotice(
                    editing.id
                      ? "Changes saved."
                      : "Record created successfully.",
                  );
                  setEditing(null);
                  await load();
                } catch (e) {
                  setError(e.message);
                } finally {
                  setBusy(false);
                }
              }}
            >
              <div className="form-grid">
                {config.fields
                  .filter(
                    (f) =>
                      !(
                        customer &&
                        key === "feedback" &&
                        ["status", "response"].includes(f.name)
                      ) &&
                      !(customer && key === "inquiries" && f.name === "status"),
                  )
                  .map((f) => (
                    <label
                      key={f.name}
                      className={f.type === "textarea" ? "full" : ""}
                    >
                      {f.label}
                      {f.required ? " *" : ""}
                      {f.type.startsWith("select:") ? (
                        <select
                          disabled={editing._readOnly}
                          value={editing[f.name] ?? ""}
                          onChange={(e) =>
                            setEditing({ ...editing, [f.name]: e.target.value })
                          }
                        >
                          {f.type
                            .slice(7)
                            .split(",")
                            .map((v) => (
                              <option key={v}>{v}</option>
                            ))}
                        </select>
                      ) : f.type === "textarea" ? (
                        <textarea
                          disabled={editing._readOnly}
                          required={f.required}
                          maxLength={1500}
                          value={editing[f.name] ?? ""}
                          onChange={(e) =>
                            setEditing({ ...editing, [f.name]: e.target.value })
                          }
                        />
                      ) : (
                        <input
                          disabled={editing._readOnly}
                          required={f.required}
                          type={f.type}
                          step={f.type === "number" ? "0.01" : undefined}
                          value={editing[f.name] ?? ""}
                          onChange={(e) =>
                            setEditing({ ...editing, [f.name]: e.target.value })
                          }
                        />
                      )}
                    </label>
                  ))}
              </div>
              {key === "finance" && editing.id && (
                <div className="receipt">
                  <h3>
                    Invoice #{editing.id} · {editing.status}
                  </h3>
                  <p>
                    Total {money(editing.total)} · Paid {money(editing.paid)} ·
                    Balance {money(editing.balance)}
                  </p>
                  <h4>Digital payment receipts</h4>
                  {editing.payments?.length ? (
                    editing.payments.map((p) => (
                      <p key={p.reference}>
                        {p.reference} · {money(p.amount)} ·{" "}
                        {new Date(p.paidAt).toLocaleString()}
                      </p>
                    ))
                  ) : (
                    <p>No payments recorded.</p>
                  )}
                  <button
                    type="button"
                    className="secondary"
                    onClick={() => window.print()}
                  >
                    <Printer size={16} /> Print invoice & receipts
                  </button>
                </div>
              )}
              {customer && key === "feedback" && editing.response && (
                <p className="success">Staff response: {editing.response}</p>
              )}
              {error && (
                <p className="error" role="alert">
                  {error}
                </p>
              )}
              <div className="form-actions">
                <button
                  type="button"
                  className="secondary"
                  onClick={() => setEditing(null)}
                >
                  Close
                </button>
                {!editing._readOnly && (
                  <button className="primary" disabled={busy}>
                    {busy ? "Saving…" : "Save record"}
                    <Check size={16} />
                  </button>
                )}
              </div>
            </form>
          </section>
        </div>
      )}
      {remove && (
        <div className="modal-backdrop">
          <section className="modal" role="dialog" aria-modal="true">
            <h2>Remove record #{remove.id}?</h2>
            <p>
              This removes the record. An audit entry will be retained. Active
              reservations and paid invoices are protected.
            </p>
            {error && <p className="error">{error}</p>}
            <div className="form-actions">
              <button className="secondary" onClick={() => setRemove(null)}>
                Keep record
              </button>
              <button
                className="primary"
                disabled={busy}
                onClick={async () => {
                  setBusy(true);
                  try {
                    await api(`/${key}/${remove.id}`, { method: "DELETE" });
                    setRemove(null);
                    setNotice("Record removed.");
                    await load();
                  } catch (e) {
                    setError(e.message);
                  } finally {
                    setBusy(false);
                  }
                }}
              >
                Remove
              </button>
            </div>
          </section>
        </div>
      )}
      {payment && (
        <div className="modal-backdrop">
          <section className="modal" role="dialog" aria-modal="true">
            <h2>Record verified payment</h2>
            <p>
              Invoice #{payment.id}. Enter an offline payment already received
              by the hotel. This does not charge a card.
            </p>
            <form
              onSubmit={async (e) => {
                e.preventDefault();
                setBusy(true);
                try {
                  await api(`/finance/${payment.id}/payments`, {
                    method: "POST",
                    body: JSON.stringify({
                      amount: Number(payment.amount),
                      reference: payment.reference,
                    }),
                  });
                  setPayment(null);
                  setNotice("Payment recorded and balance updated.");
                  await load();
                } catch (e) {
                  setError(e.message);
                } finally {
                  setBusy(false);
                }
              }}
            >
              <label>
                Amount (LKR)
                <input
                  type="number"
                  min="0.01"
                  step="0.01"
                  required
                  value={payment.amount}
                  onChange={(e) =>
                    setPayment({ ...payment, amount: e.target.value })
                  }
                />
              </label>
              <label>
                Unique bank / cash receipt reference
                <input
                  required
                  maxLength={100}
                  value={payment.reference}
                  onChange={(e) =>
                    setPayment({ ...payment, reference: e.target.value })
                  }
                />
              </label>
              {error && <p className="error">{error}</p>}
              <div className="form-actions">
                <button
                  type="button"
                  className="secondary"
                  onClick={() => setPayment(null)}
                >
                  Cancel
                </button>
                <button disabled={busy} className="primary">
                  Record payment
                </button>
              </div>
            </form>
          </section>
        </div>
      )}
      {logs && (
        <div className="modal-backdrop">
          <section className="modal" role="dialog" aria-modal="true">
            <button
              className="close icon"
              onClick={() => setLogs(null)}
              aria-label="Close activity"
            >
              <X />
            </button>
            <h2>Recent activity</h2>
            {logs.length ? (
              logs.map((l) => (
                <p key={l.id}>
                  <strong>
                    {l.action} #{l.recordId}
                  </strong>
                  <br />
                  {l.actor} · {new Date(l.occurredAt).toLocaleString()}
                </p>
              ))
            ) : (
              <p>No activity yet.</p>
            )}
          </section>
        </div>
      )}
    </section>
  );
}
