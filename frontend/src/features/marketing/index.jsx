import React from "react";
import { Packages, Newsletter, InquiryPanel, SubscriberPanel } from "./Public";
const config = {
  key: "marketing",
  title: "Packages & promotions",
  role: "Marketing executive",
  minor: "MailerLite email signup and discount code",
  member: 4,
  remaining:
    "Campaign analytics, automatic newsletter journeys in MailerLite, and coupon redemption at checkout.",
  customer: false,
  fields: [
    {
      name: "title",
      label: "Package / promotion title",
      type: "text",
      required: true,
    },
    {
      name: "description",
      label: "What is included",
      type: "textarea",
      required: true,
    },
    {
      name: "price",
      label: "Price (LKR)",
      type: "number",
      required: true,
    },
    {
      name: "discountPercent",
      label: "Discount (%)",
      type: "number",
      required: true,
    },
    {
      name: "expiresOn",
      label: "Valid until",
      type: "date",
      required: true,
    },
    {
      name: "status",
      label: "Publication",
      type: "select:DRAFT,PUBLISHED,EXPIRED",
      required: true,
    },
  ],
};

config.Public = Packages;
config.Newsletter = Newsletter;
config.After = ({ user }) => (
  <>
    <InquiryPanel user={user} />
    <SubscriberPanel />
  </>
);
export default config;
