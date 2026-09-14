const config = {
  key: "feedback",
  title: "Guest feedback",
  role: "Customer relations officer",
  minor: "Rating summary and complaint status filtering",
  member: 6,
  remaining:
    "Verified attendance checks, public review moderation, and automated complaint escalation.",
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
      label: "Your name",
      type: "text",
      required: true,
    },
    {
      name: "kind",
      label: "Feedback type",
      type: "select:REVIEW,COMPLAINT",
      required: true,
    },
    {
      name: "rating",
      label: "Rating (1-5)",
      type: "number",
      required: true,
    },
    {
      name: "message",
      label: "Review or complaint",
      type: "textarea",
      required: true,
    },
    {
      name: "status",
      label: "Resolution status",
      type: "select:OPEN,IN_PROGRESS,RESOLVED",
      required: true,
    },
    {
      name: "response",
      label: "Staff response / resolution",
      type: "textarea",
      required: false,
    },
  ],
};

export default config;
