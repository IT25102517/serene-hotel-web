import OfferPreview from "./OfferPreview";
import React, { useEffect, useState } from "react";
import { Workbench } from "../../shared/Workbench";
import { api } from "../../api";
import Analytics from "./Analytics";
import { PlanningPanel } from "../../shared/PlanningPanel";
import config from "./packageConfig";
const content = {
  key: "content",
  title: "Announcement bar & stories",
  description:
    "Publish a message above the navigation or a feature story on the home page. The most recently created active announcement is displayed.",
  createLabel: "Create content",
  columns: ["kind", "title", "active"],
  fields: [
    {
      name: "kind",
      label: "Content placement",
      type: "select:ANNOUNCEMENT,STORY",
      required: true,
    },
    {
      name: "title",
      label: "Title / announcement text",
      required: true,
      maxLength: 160,
    },
    { name: "body", label: "Story text", type: "textarea", maxLength: 2000 },
    { name: "imageUrl", label: "Story photo", type: "image" },
    { name: "active", label: "Website visibility", type: "boolean" },
  ],
};
export default function Dashboard(props) {
  const [tab, setTab] = useState("insights");
  const [subscribers, setSubscribers] = useState([]),
    [error, setError] = useState("");
  const load = () =>
    api("/marketing/subscribers")
      .then(setSubscribers)
      .catch((e) => setError(e.message));
  useEffect(() => {
    load();
  }, []);
  return (
    <>
      <nav className="department-tabs">
        {["insights", "packages", "campaigns", "gallery", "subscribers"].map(
          (key) => (
            <button
              key={key}
              className={tab === key ? "primary" : "outline"}
              onClick={() => setTab(key)}
            >
              {key.charAt(0).toUpperCase() + key.slice(1)}
            </button>
          ),
        )}
      </nav>
      {tab === "insights" && <Analytics />}
      {tab === "campaigns" && (
        <>
          <OfferPreview />
          <PlanningPanel resource="campaigns" title="Promotional campaigns" />
          <PlanningPanel resource="offers" title="Campaign offers" />
          <PlanningPanel
            resource="offer-packages"
            title="Offer package eligibility"
          />
        </>
      )}
      {tab === "gallery" && (
        <Workbench
          {...props}
          config={{
            key: "gallery",
            title: "Venue gallery & slideshow",
            description: "Upload hotel photos and choose where they appear.",
            createLabel: "Add photo",
            columns: ["title", "placement", "active"],
            fields: [
              { name: "title", label: "Photo caption", required: true },
              {
                name: "imageUrl",
                label: "Photo",
                type: "image",
                required: true,
              },
              {
                name: "placement",
                label: "Location",
                type: "select:slideshow,venue1,venue2,venue3",
                required: true,
              },
              { name: "active", label: "Visibility", type: "boolean" },
            ],
          }}
        />
      )}
      {tab === "packages" && (
        <>
          <Workbench {...props} config={config} />
          <Workbench {...props} config={content} />
        </>
      )}
      {tab === "subscribers" && (
        <>
          <section className="data-panel">
            <div className="section-heading">
              <h3>Wedding newsletter subscribers</h3>
              <button className="outline" onClick={load}>
                Refresh
              </button>
            </div>
            <p>Subscriber consent and MailerLite connection status.</p>
            {error && <p className="error">{error}</p>}
            <div className="table-scroll">
              <table>
                <thead>
                  <tr>
                    <th>Email</th>
                    <th>Sync status</th>
                    <th>Consent date</th>
                  </tr>
                </thead>
                <tbody>
                  {subscribers.map((s) => (
                    <tr key={s.email}>
                      <td>{s.email}</td>
                      <td>{s.syncStatus}</td>
                      <td>{new Date(s.consentAt).toLocaleString()}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </section>
        </>
      )}
    </>
  );
}
