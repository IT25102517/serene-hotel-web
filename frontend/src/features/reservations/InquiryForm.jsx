import React, { useState, useEffect } from "react";
import { ArrowUpRight, CheckCircle2 } from "lucide-react";
import { api, localDate } from "../../api";
export default function InquiryForm({ user, onClaim, navigate }) {
  const [packages, setPackages] = useState([]),
    [data, setData] = useState({
      name: user?.name || "",
      email: user?.email || "",
      phone: "",
      packageType: sessionStorage.getItem("serene-package") || "",
      eventDate: "",
      guests: "",
      preferredVenue: "",
      message: "",
    }),
    [busy, setBusy] = useState(false),
    [error, setError] = useState(""),
    [result, setResult] = useState(null);
  useEffect(() => {
    api("/public/packages")
      .then(setPackages)
      .catch(() => {});
    const chosen = (e) => setData((d) => ({ ...d, eventDate: e.detail }));
    window.addEventListener("serene-date", chosen);
    return () => window.removeEventListener("serene-date", chosen);
  }, []);
  const field = (name, value) => setData((d) => ({ ...d, [name]: value }));
  return (
    <div className="inquiry-form-wrap">
      {result ? (
        <div className="inquiry-success">
          <CheckCircle2 size={40} />
          <p className="eyebrow">A BEAUTIFUL FIRST STEP</p>
          <h2>Thank you, {data.name.split(" ")[0]}.</h2>
          <p>{result.message}</p>
          <strong>{result.reference}</strong>
          {!user && (
            <>
              <p>
                Keep your private code below. Create an account with{" "}
                <b>{data.email}</b> to follow your inquiry and, once accepted,
                plan with our team.
              </p>
              <code className="claim-code">{result.claimCode}</code>
              <button
                className="primary"
                onClick={() => {
                  onClaim(result.claimCode);
                  navigate("/signup");
                }}
              >
                Create your Serene account
              </button>
            </>
          )}
          {user && (
            <button className="primary" onClick={() => navigate("/account")}>
              View your inquiry
            </button>
          )}
        </div>
      ) : (
        <form
          onSubmit={async (e) => {
            e.preventDefault();
            setBusy(true);
            setError("");
            try {
              const r = await api("/public/inquiries", {
                method: "POST",
                body: JSON.stringify({ ...data, guests: Number(data.guests) }),
              });
              setResult(r);
              onClaim(r.claimCode);
            } catch (e) {
              setError(e.message);
            } finally {
              setBusy(false);
            }
          }}
        >
          <div className="form-grid">
            <label>
              Package type
              <select
                required
                value={data.packageType}
                onChange={(e) => field("packageType", e.target.value)}
              >
                <option value="">Choose a package</option>
                {packages.map((p) => (
                  <option key={p.id} value={p.title}>
                    {p.title}
                  </option>
                ))}
                <option>A tailored celebration</option>
              </select>
            </label>
            <label>
              Wedding date
              <input
                type="date"
                min={localDate()}
                required
                value={data.eventDate}
                onChange={(e) => field("eventDate", e.target.value)}
              />
            </label>
            <label>
              Your name
              <input
                required
                maxLength={120}
                autoComplete="name"
                value={data.name}
                onChange={(e) => field("name", e.target.value)}
              />
            </label>
            <label>
              Estimated guests
              <input
                type="number"
                min="1"
                max="2000"
                required
                value={data.guests}
                onChange={(e) => field("guests", e.target.value)}
              />
            </label>
            <label>
              Email address
              <input
                type="email"
                required
                maxLength={160}
                autoComplete="email"
                readOnly={!!user}
                value={data.email}
                onChange={(e) => field("email", e.target.value)}
              />
            </label>
            <label>
              Phone number
              <input
                type="tel"
                required
                maxLength={40}
                autoComplete="tel"
                minLength={7}
                placeholder="+94"
                value={data.phone}
                onChange={(e) => field("phone", e.target.value)}
              />
            </label>
            <label className="full">
              Preferred venue <span>(optional)</span>
              <select
                value={data.preferredVenue}
                onChange={(e) => field("preferredVenue", e.target.value)}
              >
                <option value="">Help me choose</option>
                {["Ivory Ballroom", "Kandyan Pavilion", "Serene Garden"].map(
                  (v) => (
                    <option key={v}>{v}</option>
                  ),
                )}
              </select>
            </label>
            <label className="full">
              Tell us about your plans
              <textarea
                required
                maxLength={1500}
                rows={4}
                placeholder="The little details, your ideas, and anything you’d like us to know…"
                value={data.message}
                onChange={(e) => field("message", e.target.value)}
              />
            </label>
          </div>
          <p className="form-note">
            Your inquiry goes directly to our reservations team. Sending an
            inquiry does not reserve a venue.
          </p>
          {error && (
            <p className="error" role="alert">
              {error}
            </p>
          )}
          <button className="primary full-width" disabled={busy}>
            {busy ? "Sending your inquiry…" : "Send wedding inquiry"}
            <ArrowUpRight size={17} />
          </button>
          <small>
            We use these details to respond to your wedding inquiry and
            coordinate your celebration.
          </small>
        </form>
      )}
    </div>
  );
}
