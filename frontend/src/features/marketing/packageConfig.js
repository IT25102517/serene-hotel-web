export default {
  key: "marketing",
  member: 4,
  role: "Marketing executive",
  title: "Packages & promotions",
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
    { name: "price", label: "Price (LKR)", type: "number", required: false },
    {
      name: "discountPercent",
      label: "Discount (%)",
      type: "number",
      required: true,
      integer: true,
    },
    { name: "expiresOn", label: "Valid until", type: "date", required: false },
    {
      name: "status",
      label: "Publication",
      type: "select:DRAFT,PUBLISHED,EXPIRED",
      required: true,
    },
    {
      name: "imageUrl",
      label: "Package photo",
      type: "image",
      required: true,
    },
  ],
  description: "Manage your public wedding packages, offers and stories.",
};
