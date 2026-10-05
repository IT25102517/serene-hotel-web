import DepartmentSummary from "../../shared/DepartmentSummary";
import React, { useState, useEffect } from "react";
import { RefreshCw, Inbox, CalendarDays } from "lucide-react";
import { api } from "../../api";
import { Dialog, Fields } from "../../shared/Workbench";
const venueType = "select:Ivory Ballroom,Kandyan Pavilion,Serene Garden";
export default function Dashboard({ refreshWeddings }) {
  const [inquiries, setInquiries] = useState([]),
    [bookings, setBookings] = useState([]),
    [tab, setTab] = useState("inquiries"),
    [review, setReview] = useState(null),
    [edit, setEdit] = useState(null),
    [remove, setRemove] = useState(null),
    [error, setError] = useState(""),
    [busy, setBusy] = useState(false),
    [filter, setFilter] = useState("ALL");
  const load = async () => {
    try {
      const [i, b] = await Promise.all([
        api("/inquiries"),
        api("/reservations"),
      ]);
      setInquiries(i);
      setBookings(b);
      setError("");
      refreshWeddings?.();
    } catch (e) {
      setError(e.message);
    }
  };
  useEffect(() => {
    load();
  }, []);
  return (
    <section>
      <DepartmentSummary department="reservations" />
      <div className="section-heading">
        <div>
          <p className="eyebrow">RESERVATIONS TEAM</p>
          <h1>From first hello to “I do”.</h1>
          <p>
            Review inquiries, reserve a venue and bring the wedding team
            together.
          </p>
        </div>
        <button className="outline" onClick={load}>
          <RefreshCw size={15} />
          Refresh
        </button>
      </div>
      <div className="stats">
        <article>
          <small>New inquiries</small>
          <strong>{inquiries.filter((i) => i.status === "NEW").length}</strong>
        </article>
        <article>
          <small>Accepted weddings</small>
          <strong>
            {bookings.filter((w) => w.status === "CONFIRMED").length}
          </strong>
        </article>
        <article>
          <small>Awaiting follow-up</small>
          <strong>
            {inquiries.filter((i) => i.status === "CONTACTED").length}
          </strong>
        </article>
      </div>
      <div className="tabs">
        <button
          className={tab === "inquiries" ? "selected" : ""}
          onClick={() => setTab("inquiries")}
        >
          <Inbox size={16} />
          Inquiry inbox
        </button>
        <button
          className={tab === "bookings" ? "selected" : ""}
          onClick={() => setTab("bookings")}
        >
          <CalendarDays size={16} />
          Reservations
        </button>
      </div>
      {!review && !edit && !remove && error && <p className="error">{error}</p>}
      {tab === "inquiries" ? (
        <div className="data-panel">
          <div className="section-heading">
            <h3>Wedding inquiries</h3>
            <select
              aria-label="Inquiry status"
              value={filter}
              onChange={(e) => setFilter(e.target.value)}
            >
              {["ALL", "NEW", "CONTACTED", "ACCEPTED", "REJECTED"].map((v) => (
                <option key={v}>{v}</option>
              ))}
            </select>
          </div>
          <div className="table-scroll">
            <table>
              <thead>
                <tr>
                  <th>Customer</th>
                  <th>Wedding date</th>
                  <th>Package / guests</th>
                  <th>Status</th>
                  <th>Action</th>
                </tr>
              </thead>
              <tbody>
                {inquiries
                  .filter((i) => filter === "ALL" || i.status === filter)
                  .map((i) => (
                    <tr key={i.id}>
                      <td>
                        <b>{i.name}</b>
                        <small>{i.email}</small>
                      </td>
                      <td>{i.eventDate}</td>
                      <td>
                        {i.packageType}
                        <small>{i.guests} guests</small>
                      </td>
                      <td>
                        <span className="badge">{i.status}</span>
                      </td>
                      <td>
                        <button
                          className="text-button"
                          onClick={() => {
                            setError("");
                            setReview({
                              ...i,
                              decision: "CONTACTED",
                              venue: i.preferredVenue || "Ivory Ballroom",
                              note: i.staffNote || "",
                            });
                          }}
                        >
                          Review inquiry
                        </button>
                      </td>
                    </tr>
                  ))}
              </tbody>
            </table>
          </div>
          {!inquiries.length && (
            <p className="empty">New website inquiries will appear here.</p>
          )}
        </div>
      ) : (
        <div className="data-panel">
          <h3>Confirmed & cancelled reservations</h3>
          <div className="table-scroll">
            <table>
              <thead>
                <tr>
                  <th>Wedding</th>
                  <th>Venue</th>
                  <th>Date</th>
                  <th>Guests</th>
                  <th>Status</th>
                  <th>Action</th>
                </tr>
              </thead>
              <tbody>
                {bookings.map((w) => (
                  <tr key={w.id}>
                    <td>
                      {w.reference}
                      <small>{w.customerName}</small>
                    </td>
                    <td>{w.venue}</td>
                    <td>{w.eventDate}</td>
                    <td>{w.guests}</td>
                    <td>
                      <span className="badge">{w.status}</span>
                    </td>
                    <td>
                      <button
                        className="text-button"
                        onClick={() => {
                          setError("");
                          setEdit({ ...w });
                        }}
                      >
                        Manage
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}
      {review && (
        <Dialog
          title={"Inquiry INQ-" + review.id}
          onClose={() => setReview(null)}
        >
          <div className="inquiry-detail">
            <h3>{review.name}</h3>
            <p>
              {review.email} · {review.phone}
            </p>
            <p>
              {review.packageType} · {review.eventDate} · {review.guests} guests
            </p>
            <p>Preferred venue: {review.preferredVenue || "Help me choose"}</p>
            <p>{review.message}</p>
            <span className="badge">{review.status}</span>
          </div>
          {!review.weddingId ? (
            <form
              onSubmit={async (e) => {
                e.preventDefault();
                setBusy(true);
                try {
                  await api("/inquiries/" + review.id + "/decision", {
                    method: "PUT",
                    body: JSON.stringify({
                      status: review.decision,
                      venue: review.venue,
                      note: review.note,
                      version: review.version,
                    }),
                  });
                  setReview(null);
                  await load();
                } catch (e) {
                  setError(e.message);
                } finally {
                  setBusy(false);
                }
              }}
            >
              <label>
                Decision
                <select
                  value={review.decision}
                  onChange={(e) =>
                    setReview({ ...review, decision: e.target.value })
                  }
                >
                  <option value="CONTACTED">Contacted / follow up</option>
                  <option value="ACCEPTED">Accept and reserve venue</option>
                  <option value="REJECTED">Decline inquiry</option>
                </select>
              </label>
              {review.decision === "ACCEPTED" && (
                <>
                  <label>
                    Reserve venue
                    <select
                      value={review.venue}
                      onChange={(e) =>
                        setReview({ ...review, venue: e.target.value })
                      }
                    >
                      {venueType
                        .slice(7)
                        .split(",")
                        .map((v) => (
                          <option key={v}>{v}</option>
                        ))}
                    </select>
                  </label>
                  <p>
                    Acceptance reserves this venue for the date and shares the
                    wedding with all departments.
                  </p>
                </>
              )}
              <label>
                Response to customer
                <textarea
                  value={review.note}
                  onChange={(e) =>
                    setReview({ ...review, note: e.target.value })
                  }
                />
              </label>
              {error && <p className="error">{error}</p>}
              <div className="form-actions">
                <button
                  type="button"
                  className="outline danger-link"
                  onClick={() => {
                    setRemove(review);
                    setReview(null);
                  }}
                >
                  Remove duplicate inquiry
                </button>
                <button className="primary" disabled={busy}>
                  Save decision
                </button>
              </div>
            </form>
          ) : (
            <p>
              Accepted as SW-{review.weddingId}. Use Reservations to manage the
              booking.
            </p>
          )}
        </Dialog>
      )}
      {edit && (
        <Dialog
          title={"Manage " + edit.reference}
          onClose={() => setEdit(null)}
        >
          <form
            onSubmit={async (e) => {
              e.preventDefault();
              setBusy(true);
              try {
                await api("/reservations/" + edit.id, {
                  method: "PUT",
                  body: JSON.stringify({
                    venue: edit.venue,
                    eventDate: edit.eventDate,
                    guests: Number(edit.guests),
                    packageName: edit.packageName,
                    status: edit.status,
                    notes: edit.notes,
                    version: edit.version,
                  }),
                });
                setEdit(null);
                await load();
              } catch (e) {
                setError(e.message);
              } finally {
                setBusy(false);
              }
            }}
          >
            <Fields
              fields={[
                { name: "venue", label: "Venue", type: venueType },
                {
                  name: "eventDate",
                  label: "Wedding date",
                  type: "date",
                  required: true,
                },
                {
                  name: "guests",
                  label: "Guests",
                  type: "number",
                  integer: true,
                  required: true,
                  min: 1,
                },
                { name: "packageName", label: "Package", required: true },
                {
                  name: "status",
                  label: "Reservation status",
                  type: "select:CONFIRMED,CANCELLED",
                },
                { name: "notes", label: "Wedding notes", type: "textarea" },
              ]}
              data={edit}
              onChange={(n, v) => setEdit({ ...edit, [n]: v })}
            />
            <p>
              Cancelling releases the venue date and closes active planning
              access for this wedding.
            </p>
            {error && <p className="error">{error}</p>}
            <button className="primary" disabled={busy}>
              Save reservation
            </button>
          </form>
        </Dialog>
      )}
      {remove && (
        <Dialog title="Remove this inquiry?" onClose={() => setRemove(null)}>
          <p>
            This removes INQ-{remove.id}. Accepted reservations cannot be
            removed here.
          </p>
          {error && <p className="error">{error}</p>}
          <button
            className="primary"
            disabled={busy}
            onClick={async () => {
              setBusy(true);
              try {
                await api("/inquiries/" + remove.id, { method: "DELETE" });
                setRemove(null);
                load();
              } catch (e) {
                setError(e.message);
              } finally {
                setBusy(false);
              }
            }}
          >
            Confirm removal
          </button>
        </Dialog>
      )}
    </section>
  );
}
