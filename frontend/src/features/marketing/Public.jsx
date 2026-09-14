import React, { useState, useEffect } from "react";
import { ArrowRight, Flower2 } from "lucide-react";
import { api } from "../../api";
import { Workbench } from "../../shared/Workbench";
const money = (n) =>
  new Intl.NumberFormat("en-LK", {
    style: "currency",
    currency: "LKR",
    maximumFractionDigits: 2,
  }).format(n || 0);
export function Newsletter() {
  const [email, setEmail] = useState(""),
    [consent, setConsent] = useState(false),
    [result, setResult] = useState(null),
    [error, setError] = useState(""),
    [busy, setBusy] = useState(false);
  return (
    <form
      className="newsletter-form"
      onSubmit={async (e) => {
        e.preventDefault();
        setBusy(true);
        setError("");
        try {
          setResult(
            await api("/public/newsletter", {
              method: "POST",
              body: JSON.stringify({ email, consent }),
            }),
          );
        } catch (e) {
          setError(e.message);
        } finally {
          setBusy(false);
        }
      }}
    >
      <label>
        Email address
        <div className="input-button">
          <input
            type="email"
            required
            placeholder="you@example.com"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
          />
          <button className="primary" disabled={busy}>
            {busy ? "Joining…" : "Get my code"}
            <ArrowRight size={16} />
          </button>
        </div>
      </label>
      <label className="checkbox">
        <input
          type="checkbox"
          required
          checked={consent}
          onChange={(e) => setConsent(e.target.checked)}
        />
        I agree to receive wedding offers via MailerLite. I can unsubscribe at
        any time.
      </label>
      <small>
        10% package discount, subject to front office approval. No automatic
        checkout redemption in this milestone.
      </small>
      {result && (
        <div className="success" role="status">
          {result.message}
          <strong className="coupon">{result.coupon}</strong>
        </div>
      )}
      {error && (
        <p className="error" role="alert">
          {error}
        </p>
      )}
    </form>
  );
}

export function Packages({ user, onLogin }) {
  const [rows, setRows] = useState([]),
    [error, setError] = useState("");
  useEffect(() => {
    api("/public/packages")
      .then(setRows)
      .catch((e) => setError(e.message));
  }, []);
  return (
    <section className="section">
      <p className="eyebrow">THOUGHTFULLY PUT TOGETHER</p>
      <h1>
        Room for every <em>little detail.</em>
      </h1>
      <p>Explore current wedding packages and offers published by our team.</p>
      {error && <p className="error">{error}</p>}
      <div className="venue-grid">
        {rows.map((p) => (
          <article className="package-card" key={p.id}>
            <Flower2 />
            <span className="badge">{p.discountPercent}% special offer</span>
            <h2>{p.title}</h2>
            <p>{p.description}</p>
            <strong>{money(p.price)}</strong>
            <small>Valid until {p.expiresOn}</small>
          </article>
        ))}
      </div>
      {!rows.length && !error && (
        <p className="empty">
          Our team is preparing new packages. Send an inquiry for a tailored
          quote.
        </p>
      )}
      <div className="split-section">
        <div>
          <h2>Let's make it yours.</h2>
          <p>Have a question? Send a wedding inquiry to our marketing team.</p>
          {user ? (
            <InquiryPanel user={user} />
          ) : (
            <button className="primary" onClick={onLogin}>
              Sign in to send an inquiry
            </button>
          )}
        </div>
        <div>
          <h2>A gift for your big day.</h2>
          <Newsletter />
        </div>
      </div>
    </section>
  );
}

const inquiryConfig = {
  key: "inquiries",
  title: "Customer inquiries",
  role: "Marketing executive",
  minor: "Search inquiries and track follow-up",
  fields: [
    { name: "name", label: "Name", type: "text", required: true },
    { name: "email", label: "Email", type: "email", required: true },
    {
      name: "message",
      label: "Your inquiry",
      type: "textarea",
      required: true,
    },
    {
      name: "status",
      label: "Status",
      type: "select:NEW,CONTACTED,CLOSED",
      required: true,
    },
  ],
};
export function InquiryPanel({ user }) {
  return <Workbench config={inquiryConfig} user={user} />;
}
export function SubscriberPanel() {
  const [rows, setRows] = useState([]),
    [error, setError] = useState("");
  useEffect(() => {
    api("/marketing/subscribers")
      .then(setRows)
      .catch((e) => setError(e.message));
  }, []);
  return (
    <section className="panel">
      <h3>Newsletter subscribers</h3>
      <p>
        Provider sync status and consent timestamps. Refresh the page after a
        new signup.
      </p>
      {error && <p className="error">{error}</p>}
      <div className="table-scroll">
        <table>
          <thead>
            <tr>
              <th>Email</th>
              <th>Sync status</th>
              <th>Consent recorded</th>
            </tr>
          </thead>
          <tbody>
            {rows.map((r) => (
              <tr key={r.email}>
                <td>{r.email}</td>
                <td>{r.syncStatus}</td>
                <td>{new Date(r.consentAt).toLocaleString()}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </section>
  );
}
