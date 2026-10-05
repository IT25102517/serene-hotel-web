import Receipts from "./Receipts";
import DepartmentSummary from "../../shared/DepartmentSummary";
import React, { useEffect, useState } from "react";
import {
  Plus,
  Download,
  Send,
  Upload,
  Check,
  X,
  RefreshCw,
} from "lucide-react";
import { api, download, money, localDate } from "../../api";
import { Dialog, Fields } from "../../shared/Workbench";

const fields = [
  {
    name: "bookingReference",
    label: "Wedding",
    type: "wedding",
    required: true,
  },
  {
    name: "total",
    label: "Invoice total (LKR)",
    type: "number",
    min: 0.01,
    required: true,
  },
  { name: "dueDate", label: "Payment due date", type: "date", required: true },
  { name: "bankName", label: "Hotel bank name", required: true },
  { name: "accountHolder", label: "Account holder", required: true },
  { name: "accountNumber", label: "Account number", required: true },
  { name: "bankBranch", label: "Bank branch" },
  { name: "notes", label: "Invoice details & instructions", type: "textarea" },
];
export default function Dashboard({ user, weddings }) {
  const customer = user.role === "CUSTOMER";
  const [rows, setRows] = useState([]),
    [edit, setEdit] = useState(null),
    [detail, setDetail] = useState(null),
    [transfers, setTransfers] = useState([]),
    [upload, setUpload] = useState(null),
    [review, setReview] = useState(null),
    [error, setError] = useState(""),
    [notice, setNotice] = useState(""),
    [busy, setBusy] = useState(false);
  const load = async () => {
    try {
      setRows(await api("/finance"));
      setError("");
    } catch (e) {
      setError(e.message);
    }
  };
  useEffect(() => {
    load();
  }, []);
  const open = async (invoice) => {
    setError("");
    setDetail(invoice);
    try {
      setTransfers(await api(`/finance/${invoice.id}/transfers`));
    } catch (e) {
      setError(e.message);
    }
  };
  const freshDetail = async (id) => {
    const all = await api("/finance");
    setRows(all);
    setDetail(all.find((i) => i.id === id));
    setTransfers(await api(`/finance/${id}/transfers`));
  };
  const getPdf = async (i) => {
    try {
      await download(`/finance/${i.id}/pdf`, `Serene-Invoice-${i.id}.pdf`);
    } catch (e) {
      setError(e.message);
    }
  };
  return (
    <section>
      <div className="section-heading">
        <div>
          <p className="eyebrow">
            {customer ? "YOUR WEDDING PAYMENTS" : "FINANCE TEAM"}
          </p>
          <h1>Invoices & bank transfers</h1>
          <p>
            {customer
              ? "Download your invoice and send us your bank-transfer confirmation."
              : "Issue invoices, verify transfer evidence and keep wedding balances up to date."}
          </p>
        </div>
        {!customer && (
          <button
            className="primary"
            onClick={() => {
              setError("");
              setEdit({
                bookingReference:
                  weddings.find((w) => w.status === "CONFIRMED")?.reference ||
                  "",
                total: "",
                dueDate: localDate(),
                bankName: "",
                accountHolder: "",
                accountNumber: "",
                bankBranch: "",
                notes: "",
              });
            }}
          >
            <Plus size={16} />
            Create invoice
          </button>
        )}
      </div>
      <div className="stats">
        <article>
          <small>Invoices</small>
          <strong>{rows.length}</strong>
        </article>
        <article>
          <small>Payments approved</small>
          <strong className="money-stat">
            {money(rows.reduce((sum, i) => sum + Number(i.paid), 0))}
          </strong>
        </article>
        <article>
          <small>Outstanding</small>
          <strong className="money-stat">
            {money(rows.reduce((sum, i) => sum + Number(i.balance), 0))}
          </strong>
        </article>
      </div>
      <div className="data-panel">
        <button className="outline" onClick={load}>
          <RefreshCw size={14} />
          Refresh
        </button>
        {notice && (
          <p className="success" role="status">
            {notice}
          </p>
        )}
        {!edit && !detail && !upload && error && (
          <p className="error">{error}</p>
        )}
        <div className="table-scroll">
          <table>
            <thead>
              <tr>
                <th>Invoice</th>
                <th>Wedding / customer</th>
                <th>Total</th>
                <th>Balance</th>
                <th>Status</th>
                <th>Action</th>
              </tr>
            </thead>
            <tbody>
              {rows.map((i) => (
                <tr key={i.id}>
                  <td>
                    INV-{i.id}
                    <small>Due {i.dueDate}</small>
                  </td>
                  <td>
                    {i.bookingReference}
                    <small>{i.customerName}</small>
                  </td>
                  <td>{money(i.total)}</td>
                  <td>{money(i.balance)}</td>
                  <td>
                    <span className="badge">{i.sent ? i.status : "DRAFT"}</span>
                  </td>
                  <td>
                    <button className="text-button" onClick={() => open(i)}>
                      View invoice
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
        {!rows.length && (
          <p className="empty">
            {customer
              ? "Your finance team will publish your invoices here."
              : "Create an invoice for an accepted wedding to get started."}
          </p>
        )}
      </div>
      {edit && (
        <Dialog
          title={edit.id ? "Edit draft invoice" : "Create invoice"}
          onClose={() => setEdit(null)}
        >
          <form
            onSubmit={async (e) => {
              e.preventDefault();
              setBusy(true);
              setError("");
              try {
                const w = weddings.find(
                  (w) => w.reference === edit.bookingReference,
                );
                if (!w) throw new Error("Choose an accepted wedding.");
                const body = {
                  ...Object.fromEntries(
                    fields.map((f) => [f.name, edit[f.name]]),
                  ),
                  total: Number(edit.total),
                  customerName: w.customerName,
                  customerUsername: w.customerEmail,
                };
                if (edit.id) body.version = edit.version;
                await api("/finance" + (edit.id ? "/" + edit.id : ""), {
                  method: edit.id ? "PUT" : "POST",
                  body: JSON.stringify(body),
                });
                setEdit(null);
                setNotice(
                  "Invoice draft saved. Open it to send it to the customer.",
                );
                await load();
              } catch (e) {
                setError(e.message);
              } finally {
                setBusy(false);
              }
            }}
          >
            <Fields
              fields={fields}
              data={edit}
              weddings={weddings}
              onChange={(n, v) => setEdit({ ...edit, [n]: v })}
            />
            {error && <p className="error">{error}</p>}
            <button className="primary" disabled={busy}>
              Save invoice draft
            </button>
          </form>
        </Dialog>
      )}
      {detail && !upload && !review && (
        <Dialog
          title={"Invoice INV-" + detail.id}
          onClose={() => setDetail(null)}
        >
          <div className="invoice-overview">
            <div>
              <h3>{detail.customerName}</h3>
              <p>
                {detail.bookingReference} · Due {detail.dueDate}
              </p>
              <span className="badge">
                {detail.sent ? detail.status : "DRAFT"}
              </span>
            </div>
            <strong>
              {money(detail.balance)}
              <small>Outstanding balance</small>
            </strong>
          </div>
          <p className="preserve-lines">{detail.notes}</p>
          <div className="bank-details">
            <strong>Bank transfer details</strong>
            <p>
              {detail.bankName} · {detail.bankBranch}
              <br />
              {detail.accountHolder}
              <br />
              Account: {detail.accountNumber}
            </p>
            <p>Payment description: INV-{detail.id}</p>
          </div>
          <div className="toolbar">
            <button className="outline" onClick={() => getPdf(detail)}>
              <Download size={15} />
              Download invoice PDF
            </button>
            {!customer && !detail.sent && (
              <>
                {!customer && <DepartmentSummary department="finance" />}
                <button
                  className="outline"
                  onClick={() => {
                    setEdit({ ...detail });
                    setDetail(null);
                  }}
                >
                  Edit draft
                </button>
                <button
                  className="primary"
                  disabled={busy}
                  onClick={async () => {
                    setBusy(true);
                    try {
                      await api(`/finance/${detail.id}/send`, {
                        method: "POST",
                      });
                      await freshDetail(detail.id);
                      setNotice("Invoice sent to the customer dashboard.");
                    } catch (e) {
                      setError(e.message);
                    } finally {
                      setBusy(false);
                    }
                  }}
                >
                  <Send size={15} />
                  Send to customer
                </button>
                <button
                  className="outline danger-link"
                  onClick={async () => {
                    if (!window.confirm("Remove this unsent invoice draft?"))
                      return;
                    setBusy(true);
                    try {
                      await api("/finance/" + detail.id, { method: "DELETE" });
                      setDetail(null);
                      load();
                    } catch (e) {
                      setError(e.message);
                    } finally {
                      setBusy(false);
                    }
                  }}
                >
                  Remove draft
                </button>
              </>
            )}
            {customer && Number(detail.balance) > 0 && (
              <button
                className="primary"
                onClick={() => {
                  setError("");
                  setUpload({
                    amount: "",
                    reference: "",
                    transferDate: localDate(),
                    proof: null,
                  });
                }}
              >
                <Upload size={15} />
                Submit bank transfer
              </button>
            )}
          </div>
          <h3 className="subheading">Bank-transfer submissions</h3>
          {transfers.length ? (
            transfers.map((t) => (
              <article className="transfer-card" key={t.id}>
                <div>
                  <strong>
                    {money(t.amount)} · {t.reference}
                  </strong>
                  <span className="badge">{t.status}</span>
                </div>
                <small>Transfer date: {t.transferDate}</small>
                {t.reviewNote && <p>{t.reviewNote}</p>}
                <div className="row-actions">
                  <button
                    onClick={async () => {
                      try {
                        await download(
                          `/finance/transfers/${t.id}/proof`,
                          t.filename,
                        );
                      } catch (e) {
                        setError(e.message);
                      }
                    }}
                  >
                    Download proof
                  </button>
                  {!customer && t.status === "PENDING" && (
                    <button
                      onClick={() => {
                        setError("");
                        setReview({ ...t, decision: "APPROVED", note: "" });
                      }}
                    >
                      Review transfer
                    </button>
                  )}
                </div>
              </article>
            ))
          ) : (
            <p>No bank-transfer evidence has been submitted yet.</p>
          )}
          <h3 className="subheading">Approved payments</h3>
          <Receipts
            key={detail.id + "-" + detail.payments.length}
            invoiceId={detail.id}
          />
          {detail.payments.length ? (
            detail.payments.map((p) => (
              <p key={p.reference}>
                {p.reference} · {money(p.amount)} ·{" "}
                {new Date(p.paidAt).toLocaleDateString()}
              </p>
            ))
          ) : (
            <p>No approved payments yet.</p>
          )}
          {error && <p className="error">{error}</p>}
        </Dialog>
      )}
      {upload && (
        <Dialog
          title="Submit bank-transfer proof"
          onClose={() => setUpload(null)}
        >
          <p>
            Invoice INV-{detail.id} · Balance {money(detail.balance)}. Your
            payment will appear as paid after our finance team verifies it.
          </p>
          <form
            onSubmit={async (e) => {
              e.preventDefault();
              setBusy(true);
              setError("");
              try {
                if (!upload.proof)
                  throw new Error("Choose your transfer confirmation file.");
                const form = new FormData();
                form.append("amount", upload.amount);
                form.append("reference", upload.reference);
                form.append("transferDate", upload.transferDate);
                form.append("proof", upload.proof);
                await api(`/finance/${detail.id}/transfers`, {
                  method: "POST",
                  body: form,
                });
                setUpload(null);
                await freshDetail(detail.id);
                setNotice("Transfer proof submitted for finance approval.");
              } catch (e) {
                setError(e.message);
              } finally {
                setBusy(false);
              }
            }}
          >
            <Fields
              fields={[
                {
                  name: "amount",
                  label: "Transferred amount (LKR)",
                  type: "number",
                  min: 0.01,
                  max: detail.balance,
                  required: true,
                },
                {
                  name: "reference",
                  label: "Bank transaction reference",
                  required: true,
                  maxLength: 100,
                },
                {
                  name: "transferDate",
                  label: "Transfer date",
                  type: "date",
                  required: true,
                },
              ]}
              data={upload}
              onChange={(n, v) => setUpload({ ...upload, [n]: v })}
            />
            <label>
              Bank-transfer receipt
              <input
                type="file"
                accept="application/pdf,image/png,image/jpeg"
                required
                onChange={(e) =>
                  setUpload({ ...upload, proof: e.target.files[0] })
                }
              />
            </label>
            <small>
              PDF, PNG or JPEG · Up to 5 MB. If a submission was rejected, use
              the same transaction reference to replace its evidence.
            </small>
            {error && (
              <p className="error" role="alert">
                {error}
              </p>
            )}
            <button className="primary" disabled={busy}>
              {busy ? "Uploading…" : "Submit for verification"}
            </button>
          </form>
        </Dialog>
      )}
      {review && (
        <Dialog title="Verify bank transfer" onClose={() => setReview(null)}>
          <p>
            INV-{detail.id} · {money(review.amount)} · {review.reference}
          </p>
          <p>
            Check the bank transaction and supporting document before approving.
          </p>
          <button
            className="outline"
            onClick={async () => {
              try {
                await download(
                  `/finance/transfers/${review.id}/proof`,
                  review.filename,
                );
              } catch (e) {
                setError(e.message);
              }
            }}
          >
            <Download size={15} />
            Download transfer proof
          </button>
          <form
            onSubmit={async (e) => {
              e.preventDefault();
              setBusy(true);
              try {
                await api(`/finance/transfers/${review.id}/review`, {
                  method: "POST",
                  body: JSON.stringify({
                    decision: review.decision,
                    note: review.note,
                    version: review.version,
                  }),
                });
                setReview(null);
                await freshDetail(detail.id);
                setNotice("Transfer review saved.");
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
                <option value="APPROVED">Approve payment</option>
                <option value="REJECTED">Reject submission</option>
              </select>
            </label>
            <label>
              Review note
              <textarea
                required={review.decision === "REJECTED"}
                maxLength={1000}
                value={review.note}
                onChange={(e) => setReview({ ...review, note: e.target.value })}
              />
            </label>
            {error && <p className="error">{error}</p>}
            <button className="primary" disabled={busy}>
              <Check size={15} />
              Save review
            </button>
          </form>
        </Dialog>
      )}
    </section>
  );
}
