import React, { useState } from "react";
import DepartmentSummary from "../../shared/DepartmentSummary";
import { PlanningPanel, EventPlanSetup } from "../../shared/PlanningPanel";
import { Workbench } from "../../shared/Workbench";
const config = {
  key: "operations",
  member: 3,
  role: "Hotel operations manager / staff",
  title: "Hotel operations",
  fields: [
    {
      name: "bookingReference",
      label: "Wedding",
      type: "wedding",
      required: true,
    },
    { name: "task", label: "Task title", type: "text", required: true },
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
    { name: "dueDate", label: "Duty date", type: "date", required: true },
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
  description: "Coordinate staff duties, catering, preparation and equipment.",
  createLabel: "Assign task",
  customerTitle: "Guests & dining",
};
const guest = {
  key: "guest-requirements",
  title: "Guests & dining requirements",
  description:
    "Share your guest numbers, food preferences and accessibility needs.",
  createLabel: "Add guest requirements",
  columns: [
    "bookingReference",
    "guestCount",
    "menuPreference",
    "vegetarianMeals",
  ],
  fields: [
    {
      name: "bookingReference",
      label: "Wedding",
      type: "wedding",
      required: true,
    },
    {
      name: "guestCount",
      label: "Total guests",
      type: "number",
      integer: true,
      min: 1,
      required: true,
    },
    {
      name: "children",
      label: "Children",
      type: "number",
      integer: true,
      min: 0,
      required: true,
    },
    {
      name: "vegetarianMeals",
      label: "Vegetarian meals",
      type: "number",
      integer: true,
      min: 0,
      required: true,
    },
    {
      name: "menuPreference",
      label: "Menu preference",
      type: "text",
      required: true,
    },
    { name: "allergies", label: "Allergies & dietary needs", type: "textarea" },
    {
      name: "accessibility",
      label: "Accessibility requirements",
      type: "textarea",
    },
    { name: "notes", label: "Other guest requirements", type: "textarea" },
  ],
};
config.Page = function OperationsDashboard(props) {
  const [generation, setGeneration] = useState(0);
  return (
    <>
      <DepartmentSummary department="operations" />
      <Workbench {...props} config={config} />
      <Workbench {...props} config={guest} />
      <EventPlanSetup
        weddings={props.weddings}
        onReady={() => setGeneration(generation + 1)}
      />
      <PlanningPanel
        key={"c" + generation}
        resource="catering"
        title="Catering plans"
      />
      <PlanningPanel resource="duties" title="Staff duty assignments" />
    </>
  );
};
config.CustomerPage = (props) => <Workbench {...props} config={guest} />;
export default config;
