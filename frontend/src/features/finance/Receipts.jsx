import React, { useState, useEffect } from "react";
import { api, download, money } from "../../api";
export default function Receipts({ invoiceId }) {
  const [rows, setRows] = useState([]),
    [error, setError] = useState("");
  useEffect(() => {
    api(`/finance/${invoiceId}/receipts`)
      .then(setRows)
      .catch((e) => setError(e.message));
  }, [invoiceId]);
  return (
    <section>
      <h3>Payment receipts</h3>
      {error && <p className="error">{error}</p>}
      {rows.length ? (
        rows.map((r) => (
          <p key={r.ReceiptID}>
            REC-{r.ReceiptID} · {money(r.Amount)}{" "}
            <button
              className="outline"
              onClick={() =>
                download(
                  `/finance/${invoiceId}/receipts/${r.ReceiptID}/pdf`,
                  `Serene-Receipt-${r.ReceiptID}.pdf`,
                ).catch((e) => setError(e.message))
              }
            >
              Download receipt
            </button>
          </p>
        ))
      ) : (
        <p>Receipts appear after a transfer is approved.</p>
      )}
    </section>
  );
}
