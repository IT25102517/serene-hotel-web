import React, { useEffect, useState } from "react";
import { api, money } from "../../api";
export default function AvailableOffers({ packageId }) {
  const [rows, setRows] = useState([]);
  useEffect(() => {
    api("/public/offers")
      .then(setRows)
      .catch(() => {});
  }, []);
  return rows
    .filter((r) => r.PackageID === packageId)
    .map((r) => (
      <p key={r.OfferID} className="badge">
        {r.OfferName}:{" "}
        {r.DiscountType === "PERCENT"
          ? r.DiscountValue + "%"
          : money(r.DiscountValue)}{" "}
        off · until {r.EndDate}
      </p>
    ));
}
