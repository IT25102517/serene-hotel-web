import React, { useState } from "react";
import { Workbench } from "../../shared/Workbench";
import DepartmentSummary from "../../shared/DepartmentSummary";
const config = {
  key: "feedback",
  member: 6,
  role: "Customer relations officer",
  title: "Guest feedback",
  fields: [
    {
      name: "bookingReference",
      label: "Wedding",
      type: "wedding",
      required: true,
    },
    { name: "customerName", label: "Your name", type: "text", required: true },
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
      integer: true,
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
      staffOnly: true,
    },
    {
      name: "response",
      label: "Staff response / resolution",
      type: "textarea",
      required: false,
      staffOnly: true,
    },
  ],
  description: "Reviews, concerns and thoughtful responses from our team.",
  customerTitle: "Feedback & support",
  createLabel: "Share feedback",
};

config.Page = (props) => (
  <>
    <DepartmentSummary department="feedback" />
    <Workbench {...props} config={config} />
  </>
);
config.CustomerPage = (props) => <Workbench {...props} config={config} />;
export default config;
