const config = {
  key: "operations",
  title: "Hotel operations",
  role: "Hotel operations manager / staff",
  minor: "Task filtering and readiness dashboard",
  member: 3,
  remaining:
    "Staff account directory, resource stock collision checks, and automatic event-change notifications.",
  customer: false,
  fields: [
    {
      name: "bookingReference",
      label: "Reservation reference",
      type: "text",
      required: true,
    },
    {
      name: "task",
      label: "Task title",
      type: "text",
      required: true,
    },
    {
      name: "department",
      label: "Department",
      type: "select:Catering,Venue,Equipment,Service",
      required: true,
    },
    {
      name: "assignee",
      label: "Assigned staff member",
      type: "text",
      required: true,
    },
    {
      name: "dueDate",
      label: "Duty date",
      type: "date",
      required: true,
    },
    {
      name: "shift",
      label: "Shift",
      type: "select:Morning,Afternoon,Evening",
      required: true,
    },
    {
      name: "resources",
      label: "Resources / catering / equipment",
      type: "textarea",
      required: true,
    },
    {
      name: "status",
      label: "Status",
      type: "select:TODO,IN_PROGRESS,COMPLETED,CANCELLED",
      required: true,
    },
  ],
};

export default config;
