import React, { useState, useEffect } from "react";
import { createRoot } from "react-dom/client";
import {
  ArrowUpRight,
  ArrowRight,
  Heart,
  CalendarDays,
  Flower2,
  Menu,
  LogOut,
  Plus,
  Search,
  Check,
  LayoutDashboard,
  Users,
  ClipboardList,
  Megaphone,
  Wallet,
  MessageSquare,
  Printer,
  X,
  RefreshCw,
} from "lucide-react";
import { api, setCredentials, clearCredentials } from "./api";
import "./styles.css";
import { Workbench } from "./shared/Workbench";

const modules = Object.values(
  import.meta.glob("./features/*/index.jsx", { eager: true }),
)
  .map((m) => m.default)
  .sort((a, b) => a.member - b.member);
const MarketingPublic = modules.find((m) => m.key === "marketing")?.Public;
const MarketingNewsletter = modules.find(
  (m) => m.key === "marketing",
)?.Newsletter;
const icons = {
  reservations: CalendarDays,
  events: Flower2,
  operations: ClipboardList,
  marketing: Megaphone,
  finance: Wallet,
  feedback: MessageSquare,
};
const money = (n) =>
  new Intl.NumberFormat("en-LK", {
    style: "currency",
    currency: "LKR",
    maximumFractionDigits: 2,
  }).format(n || 0);
const today = () => new Date().toLocaleDateString("en-CA");
function App() {
  const [page, setPage] = useState("home"),
    [user, setUser] = useState(null),
    [login, setLogin] = useState(false),
    [profile, setProfile] = useState("CONNECTING"),
    [menu, setMenu] = useState(false);
  useEffect(() => {
    api("/health")
      .then((x) => setProfile(x.profile))
      .catch(() => setProfile("OFFLINE"));
  }, []);
  const navigate = (p) => {
    setPage(p);
    setMenu(false);
    window.scrollTo(0, 0);
  };
  const staff = user && user.role !== "CUSTOMER";
  return (
    <>
      <div className="environment">
        {profile === "demo"
          ? "EVALUATION DEMO · H2 local database"
          : profile === "sqlserver"
            ? "EVALUATION · SQL Server profile"
            : profile === "OFFLINE"
              ? "API OFFLINE · Start the Spring Boot backend"
              : profile}{" "}
        <span>Serene Hotel · Wedding services</span>
      </div>
      <header>
        <a
          className="logo"
          href="#"
          onClick={(e) => {
            e.preventDefault();
            navigate("home");
          }}
        >
          <img src="/logo.svg" alt="Serene Hotel" />
        </a>
        <button
          className="icon mobile"
          onClick={() => setMenu(!menu)}
          aria-label="Toggle menu"
        >
          <Menu />
        </button>
        <nav className={menu ? "expanded" : ""}>
          <button
            className={page === "home" ? "active" : ""}
            onClick={() => navigate("home")}
          >
            Discover
          </button>
          <button onClick={() => navigate("packages")}>Wedding packages</button>
          <button onClick={() => navigate("reservations")}>Our venues</button>
          {user && (
            <button
              onClick={() =>
                navigate(staff ? user.role.toLowerCase() : "events")
              }
            >
              {staff ? "My workspace" : "My wedding"}
            </button>
          )}
        </nav>
        <div className="header-actions">
          {user ? (
            <button
              className="text-button"
              onClick={() => {
                clearCredentials();
                setUser(null);
                navigate("home");
              }}
            >
              <LogOut size={16} /> Sign out
            </button>
          ) : (
            <button className="text-button" onClick={() => setLogin(true)}>
              Sign in
            </button>
          )}
          <button className="primary" onClick={() => navigate("reservations")}>
            Plan your wedding <ArrowUpRight size={16} />
          </button>
        </div>
      </header>
      {page === "home" ? (
        <Home navigate={navigate} />
      ) : page === "packages" ? (
        <>
          {MarketingPublic ? (
            <MarketingPublic user={user} onLogin={() => setLogin(true)} />
          ) : (
            <p className="empty">Marketing module awaiting merge.</p>
          )}
        </>
      ) : (
        <div className="app-shell">
          <aside>
            <p className="eyebrow">
              {staff ? "STAFF WORKSPACE" : "YOUR WEDDING JOURNEY"}
            </p>
            <h3>{staff ? user.username : "Beautifully organised."}</h3>
            {modules
              .filter((m) =>
                staff
                  ? m.key === user.role.toLowerCase()
                  : m.customer || m.key === "marketing",
              )
              .map((m) => {
                let Icon = icons[m.key];
                return (
                  <button
                    key={m.key}
                    className={page === m.key ? "selected" : ""}
                    onClick={() => navigate(m.key)}
                  >
                    <Icon size={18} />
                    {m.title}
                  </button>
                );
              })}
            <div className="aside-note">
              <Flower2 />
              <p>
                A little planning.
                <br />A lifetime of memories.
              </p>
              <small>Serene Hotel</small>
            </div>
          </aside>
          <main className="workspace">
            {modules.find((m) => m.key === page) ? (
              <Feature
                key={`${page}-${user?.username}`}
                config={modules.find((m) => m.key === page)}
                user={user}
                onLogin={() => setLogin(true)}
              />
            ) : (
              <p>This member module has not been merged yet.</p>
            )}
          </main>
        </div>
      )}
      <footer>
        <img src="/logo.svg" alt="Serene Hotel" />
        <span>For the moments that become forever.</span>
        <small>SE2030 · Group B6G2-09 · Evaluation build</small>
      </footer>
      {login && (
        <Login
          onClose={() => setLogin(false)}
          onSuccess={(u) => {
            setUser(u);
            setLogin(false);
            navigate(
              u.role === "CUSTOMER" ? "reservations" : u.role.toLowerCase(),
            );
          }}
        />
      )}
    </>
  );
}
function Home({ navigate }) {
  return (
    <main>
      <section className="hero">
        <div className="hero-copy">
          <div className="eyebrow">
            <span /> YOUR FOREVER STARTS HERE
          </div>
          <h1>
            A beautiful beginning.
            <br />
            <em>A day that's yours.</em>
          </h1>
          <p>
            Thoughtful details, graceful spaces, and the people you love. Let us
            bring your wedding story to life at Serene.
          </p>
          <div className="hero-buttons">
            <button
              className="primary"
              onClick={() => navigate("reservations")}
            >
              Find your perfect venue <ArrowUpRight size={18} />
            </button>
            <button
              className="text-button"
              onClick={() => navigate("packages")}
            >
              Explore packages <ArrowRight size={16} />
            </button>
          </div>
          <div className="hero-proof">
            <span className="mini-flower">✧</span>
            <div>
              <strong>Made for your kind of forever</strong>
              <small>Three distinctive venues. One unforgettable day.</small>
            </div>
          </div>
        </div>
        <div
          className="hero-art"
          role="img"
          aria-label="Illustration of a blush wedding arch surrounded by flowers"
        >
          <div className="art-tag">
            THE SERENE WEDDING COLLECTION <span>01 / 03</span>
          </div>
          <div className="arch arch-back" />
          <div className="arch arch-front">
            <div className="arch-inside" />
            <div className="aisle" />
            <div className="arch-caption">
              together
              <br />
              <em>is a beautiful place to be.</em>
            </div>
          </div>
          <div className="flower flower-one">✿</div>
          <div className="flower flower-two">✿</div>
          <div className="flower flower-three">✿</div>
          <div className="floating-card">
            <Heart size={20} />
            <div>
              Your day. Your story.<small>We take care of the details.</small>
            </div>
          </div>
          <span className="art-bottom">
            ELEGANT SPACES, ENDLESS POSSIBILITIES
          </span>
        </div>
      </section>
      <div className="benefit-strip">
        <span>
          <Flower2 /> Beautifully curated packages
        </span>
        <span>
          <CalendarDays /> Live hall availability
        </span>
        <span>
          <Users /> Dedicated wedding team
        </span>
        <span>
          <Heart /> Personal touches, always
        </span>
      </div>
      <section className="section">
        <div className="section-heading">
          <div>
            <p className="eyebrow">A SETTING FOR EVERY LOVE STORY</p>
            <h2>Find your somewhere special.</h2>
          </div>
          <button
            className="text-button"
            onClick={() => navigate("reservations")}
          >
            Explore our venues <ArrowUpRight size={18} />
          </button>
        </div>
        <div className="venue-grid">
          {[
            ["Grand Ballroom", "Timeless grandeur", "Up to 500 guests"],
            [
              "Garden Pavilion",
              "A little closer to nature",
              "Up to 250 guests",
            ],
            ["Lotus Hall", "Intimate and unforgettable", "Up to 120 guests"],
          ].map(([n, d, c], i) => (
            <article className="venue-card" key={n}>
              <div className={`venue-art venue-${i}`}>
                <div className="venue-door" />
                <span>0{i + 1}</span>
                <Flower2 size={60} />
              </div>
              <div>
                <small>{d}</small>
                <h3>{n}</h3>
                <p>
                  {c}
                  <button
                    aria-label={`Check ${n}`}
                    onClick={() => navigate("reservations")}
                  >
                    <ArrowUpRight size={20} />
                  </button>
                </p>
              </div>
            </article>
          ))}
        </div>
      </section>
      <section className="newsletter-section">
        <div>
          <p className="eyebrow">SOMETHING LOVELY, JUST FOR YOU</p>
          <h2>A little gift for your big day.</h2>
          <p>
            Join our wedding mailing list and receive a 10% package discount
            code.
          </p>
        </div>
        {MarketingNewsletter && <MarketingNewsletter />}
      </section>
    </main>
  );
}
function Login({ onClose, onSuccess }) {
  const [username, setUsername] = useState("customer"),
    [password, setPassword] = useState(""),
    [error, setError] = useState(""),
    [busy, setBusy] = useState(false);
  return (
    <div className="modal-backdrop">
      <section
        className="modal"
        role="dialog"
        aria-modal="true"
        aria-labelledby="login-title"
      >
        <button
          className="close icon"
          onClick={onClose}
          aria-label="Close sign in"
        >
          <X />
        </button>
        <p className="eyebrow">WELCOME TO SERENE</p>
        <h2 id="login-title">Your wedding, connected.</h2>
        <p>Sign in to your customer journey or dedicated staff workspace.</p>
        <form
          onSubmit={async (e) => {
            e.preventDefault();
            setBusy(true);
            setCredentials(username, password);
            try {
              onSuccess(await api("/me"));
            } catch (e) {
              clearCredentials();
              setError(e.message);
            } finally {
              setBusy(false);
            }
          }}
        >
          <label>
            Evaluation account
            <select
              value={username}
              onChange={(e) => setUsername(e.target.value)}
            >
              {["customer", "customer2", ...modules.map((m) => m.key)].map(
                (x) => (
                  <option key={x}>{x}</option>
                ),
              )}
            </select>
          </label>
          <label>
            Password
            <input
              type="password"
              autoComplete="current-password"
              required
              value={password}
              onChange={(e) => setPassword(e.target.value)}
            />
          </label>
          <p className="hint">
            Demo profile password: SereneDemo2026!
            <br />
            SQL Server profile uses your configured DEMO_PASSWORD.
          </p>
          {error && (
            <p role="alert" className="error">
              {error}
            </p>
          )}
          <button className="primary" disabled={busy}>
            {busy ? "Signing in…" : "Sign in"} <ArrowRight size={16} />
          </button>
        </form>
      </section>
    </div>
  );
}
function Feature({ config, user, onLogin }) {
  const staff = user?.role === config.key.toUpperCase(),
    Before = config.Before,
    After = config.After,
    Public = config.Public;
  return (
    <>
      {Before && <Before />}
      {Public && !staff ? (
        <Public user={user} onLogin={onLogin} />
      ) : user && (staff || (user.role === "CUSTOMER" && config.customer)) ? (
        <>
          <Workbench config={config} user={user} />
          {After && <After user={user} />}
        </>
      ) : (
        <section className="empty">
          <Flower2 size={40} />
          <h2>{config.title}</h2>
          <p>
            {user
              ? "This workspace is restricted to the responsible staff role."
              : "Sign in to view and manage your wedding records."}
          </p>
          {!user && (
            <button className="primary" onClick={onLogin}>
              Sign in to continue
            </button>
          )}
        </section>
      )}
    </>
  );
}
createRoot(document.getElementById("root")).render(<App />);
