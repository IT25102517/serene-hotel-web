import React, { useState } from "react";
import { Workbench } from "../../shared/Workbench";
import DepartmentSummary from "../../shared/DepartmentSummary";
import { PlanningPanel, EventPlanSetup } from "../../shared/PlanningPanel";
const config = {
  key: "events",
  member: 2,
  role: "Wedding event manager",
  title: "Wedding schedule",
  fields: [
    {
      name: "bookingReference",
      label: "Wedding",
      type: "wedding",
      required: true,
    },
    { name: "activity", label: "Activity", type: "text", required: true },
    { name: "eventDate", label: "Event date", type: "date", required: true },
    { name: "startTime", label: "Start time", type: "time", required: true },
    { name: "endTime", label: "End time", type: "time", required: true },
    {
      name: "guests",
      label: "Guests",
      type: "number",
      required: true,
      integer: true,
    },
    {
      name: "requirements",
      label: "Guest needs and special requests",
      type: "textarea",
      required: false,
    },
    {
      name: "status",
      label: "Status",
      type: "select:PLANNED,CONFIRMED,CANCELLED",
      required: true,
    },
  ],
  customerTitle: "Schedule & special requests",
  description:
    "Plan the moments, timings and special touches for your wedding.",
  createLabel: "Add activity",
  columns: ["activity", "eventDate", "startTime", "endTime", "status"],
};

config.Page = function EventDashboard(props) {
  const [generation, setGeneration] = useState(0);
  return (
    <>
      <DepartmentSummary department="events" />
      <Workbench {...props} config={config} />
      <EventPlanSetup
        weddings={props.weddings}
        onReady={() => setGeneration(generation + 1)}
      />
      <PlanningPanel
        key={"r" + generation}
        resource="requirements"
        title="Event requirements"
      />
      <PlanningPanel
        key={"p" + generation}
        resource="event-packages"
        title="Selected event packages"
      />
    </>
  );
};
config.CustomerPage = (props) => <Workbench {...props} config={config} />;
export default config;
