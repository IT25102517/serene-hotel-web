const config = {
  key: "finance",
  title: "Finance & payments",
  role: "Finance & reservations officer",
  minor: "Printable invoices and digital receipts",
  member: 5,
  remaining:
    "Live payment gateway, verified webhook processing, refund approvals, and automatic reservation pricing.",
  customer: true,
  fields: [
    {
      name: "bookingReference",
      label: "Reservation reference",
      type: "text",
      required: true,
    },
    {
      name: "customerName",
      label: "Customer name",
      type: "text",
      required: true,
    },
    {
      name: "customerUsername",
      label: "Customer login",
      type: "select:customer,customer2",
      required: true,
    },
    {
      name: "total",
      label: "Invoice total (LKR)",
      type: "number",
      required: true,
    },
    {
      name: "dueDate",
      label: "Payment due date",
      type: "date",
      required: true,
    },
    {
      name: "notes",
      label: "Invoice notes",
      type: "textarea",
      required: false,
    },
  ],
};

export default config;
