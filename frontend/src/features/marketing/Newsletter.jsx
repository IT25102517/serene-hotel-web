import React, { useState } from "react";
import { api } from "../../api";
export default function Newsletter() {
  const [email, setEmail] = useState(""),
    [consent, setConsent] = useState(false),
    [message, setMessage] = useState(""),
    [busy, setBusy] = useState(false);
  return (
    <section className="newsletter-section">
      <p className="eyebrow">A LITTLE SOMETHING SPECIAL</p>
      <h2>
        Lovely things, <em>in your inbox.</em>
      </h2>
      <p>
        Join our wedding mailing list for inspiration and a personal offer code.
      </p>
      <form
        onSubmit={async (e) => {
          e.preventDefault();
          setBusy(true);
          setMessage("");
          try {
            const result = await api("/public/newsletter", {
              method: "POST",
              body: JSON.stringify({ email, consent }),
            });
            setMessage(
              `Thank you for joining us. Your offer code: ${result.coupon}`,
            );
          } catch (e) {
            setMessage(e.message);
          } finally {
            setBusy(false);
          }
        }}
      >
        <label>
          Email address
          <input
            type="email"
            required
            maxLength={160}
            value={email}
            onChange={(e) => setEmail(e.target.value)}
          />
        </label>
        <label className="consent">
          <input
            type="checkbox"
            required
            checked={consent}
            onChange={(e) => setConsent(e.target.checked)}
          />
          I agree to receive Serene wedding news and offers through MailerLite.
          I can unsubscribe at any time.
        </label>
        <button className="primary" disabled={busy}>
          {busy ? "Joining…" : "Join & receive my code"}
        </button>
        <p role="status">{message}</p>
      </form>
    </section>
  );
}
