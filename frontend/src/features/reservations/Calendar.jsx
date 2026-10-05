import React, { useEffect, useState } from "react";
import { ChevronLeft, ChevronRight, Check, CalendarDays } from "lucide-react";
import { api, localDate } from "../../api";
export default function Calendar({ onChoose }) {
  const [month, setMonth] = useState(localDate().slice(0, 7)),
    [days, setDays] = useState([]),
    [selected, setSelected] = useState(localDate()),
    [error, setError] = useState("");
  useEffect(() => {
    let alive = true;
    const load = () =>
      api("/public/availability?month=" + month)
        .then((r) => {
          if (alive) {
            setDays(r.days);
            setError("");
          }
        })
        .catch((e) => {
          if (alive) setError(e.message);
        });
    load();
    const t = setInterval(load, 15000);
    return () => {
      alive = false;
      clearInterval(t);
    };
  }, [month]);
  const move = (n) => {
    const [y, m] = month.split("-").map(Number);
    const d = new Date(y, m - 1 + n, 1);
    setMonth(`${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, "0")}`);
    setSelected("");
  };
  const detail = days.find((d) => d.date === selected),
    start = new Date(month + "-01T12:00:00").getDay();
  return (
    <div className="live-calendar">
      <div className="calendar-head">
        <button
          className="icon"
          aria-label="Previous month"
          onClick={() => move(-1)}
        >
          <ChevronLeft size={20} />
        </button>
        <h3>
          {new Date(month + "-01T12:00:00").toLocaleDateString("en-GB", {
            month: "long",
            year: "numeric",
          })}
        </h3>
        <button
          className="icon"
          aria-label="Next month"
          onClick={() => move(1)}
        >
          <ChevronRight size={20} />
        </button>
      </div>
      <div className="calendar-grid weekdays">
        {["S", "M", "T", "W", "T", "F", "S"].map((d, i) => (
          <span key={i}>{d}</span>
        ))}
      </div>
      <div className="calendar-grid">
        {Array.from({ length: start }, (_, i) => (
          <span key={"blank" + i} />
        ))}
        {days.map((d) => (
          <button
            key={d.date}
            disabled={d.date < localDate()}
            aria-label={`${d.date}, ${d.fullyBooked ? "fully booked" : d.availableVenues.length + " venues available"}`}
            aria-pressed={selected === d.date}
            className={`${selected === d.date ? "selected " : ""}${d.fullyBooked ? "full" : "available"}`}
            onClick={() => setSelected(d.date)}
          >
            <span>{Number(d.date.slice(8))}</span>
            <i />
          </button>
        ))}
      </div>
      <div className="calendar-legend">
        <span>
          <i /> Venues available
        </span>
        <span>
          <i className="full" /> Fully booked
        </span>
      </div>
      {error && <p className="error">{error}</p>}
      {detail ? (
        <div className="date-detail" aria-live="polite">
          <strong>
            <CalendarDays size={16} />
            {new Date(selected + "T12:00:00").toLocaleDateString("en-GB", {
              day: "numeric",
              month: "long",
              year: "numeric",
            })}
          </strong>
          {detail.fullyBooked ? (
            <p>All three venues are booked. Please explore another date.</p>
          ) : (
            <>
              <p>
                {detail.availableVenues.length}{" "}
                {detail.availableVenues.length === 1
                  ? "venue is"
                  : "venues are"}{" "}
                available for your celebration.
              </p>
              <div>
                {detail.availableVenues.map((v) => (
                  <span key={v}>
                    <Check size={13} />
                    {v}
                  </span>
                ))}
              </div>
              {onChoose && (
                <button
                  className="text-button"
                  onClick={() => onChoose(selected)}
                >
                  Inquire for this date →
                </button>
              )}
            </>
          )}
        </div>
      ) : (
        <p className="calendar-prompt">
          Select a day to discover available venues.
        </p>
      )}
    </div>
  );
}
