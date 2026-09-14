import Availability from "./Availability";
const config = {
  key: "reservations",
  title: "Wedding reservations",
  role: "Front office staff",
  minor: "Hall search and date availability",
  member: 1,
  remaining:
    "Email booking confirmations, temporary date holds, and cancellation policy/refund automation.",
  customer: true,
  fields: [
    {
      name: "couple",
      label: "Couple names",
      type: "text",
      required: true,
    },
    {
      name: "email",
      label: "Contact email",
      type: "email",
      required: true,
    },
    {
      name: "hall",
      label: "Wedding hall",
      type: "select:Grand Ballroom,Garden Pavilion,Lotus Hall",
      required: true,
    },
    {
      name: "eventDate",
      label: "Wedding date",
      type: "date",
      required: true,
    },
    {
      name: "guests",
      label: "Guest count",
      type: "number",
      required: true,
    },
    {
      name: "packageName",
      label: "Selected package",
      type: "select:Rose,Peony,Orchid",
      required: true,
    },
    {
      name: "status",
      label: "Status",
      type: "select:PENDING,CONFIRMED,CANCELLED",
      required: true,
    },
    {
      name: "notes",
      label: "Special requests",
      type: "textarea",
      required: false,
    },
  ],
};

config.Before = Availability;
export default config;
