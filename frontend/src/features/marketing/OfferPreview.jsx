import React, { useState } from "react";
import { api, money } from "../../api";
export default function OfferPreview() {
  const [type, setType] = useState("PERCENT"),
    [price, setPrice] = useState(100000),
    [value, setValue] = useState(10),
    [result, setResult] = useState(null),
    [error, setError] = useState("");
  return (
    <section className="data-panel">
      <h2>Offer price preview</h2>
      <p>Compare percentage and fixed-amount offers before publishing.</p>
      <form
        className="filter-row"
        onSubmit={async (e) => {
          e.preventDefault();
          try {
            setResult(
              await api(
                `/marketing/offer-preview?type=${type}&price=${price}&value=${value}`,
              ),
            );
            setError("");
          } catch (ex) {
            setError(ex.message);
          }
        }}
      >
        <label>
          Original price
          <input
            required
            min="0"
            step="0.01"
            type="number"
            value={price}
            onChange={(e) => setPrice(e.target.value)}
          />
        </label>
        <label>
          Offer type
          <select value={type} onChange={(e) => setType(e.target.value)}>
            <option value="PERCENT">Percentage</option>
            <option value="AMOUNT">Fixed amount</option>
          </select>
        </label>
        <label>
          Discount value
          <input
            required
            min="0.01"
            step="0.01"
            type="number"
            value={value}
            onChange={(e) => setValue(e.target.value)}
          />
        </label>
        <button className="primary">Calculate</button>
      </form>
      {error && <p className="error">{error}</p>}
      {result && (
        <p>
          Discount: <strong>{money(result.discount)}</strong> · Offer price:{" "}
          <strong>{money(result.finalPrice)}</strong>
        </p>
      )}
    </section>
  );
}
