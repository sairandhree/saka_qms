import { useEffect, useMemo, useState } from "react";

const tokenKey = "saka_qms_token";
const userKey = "saka_qms_user";

async function request(path, options = {}) {
  const token = localStorage.getItem(tokenKey);
  const response = await fetch(path, {
    ...options,
    headers: {
      "Content-Type": "application/json",
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...(options.headers || {})
    }
  });

  if (response.status === 401) {
    localStorage.removeItem(tokenKey);
    localStorage.removeItem(userKey);
    throw new Error("Your session has expired. Please sign in again.");
  }

  if (!response.ok) {
    let message = "The request could not be completed.";
    try {
      const body = await response.json();
      message = body.message || body.error || message;
    } catch {
      // Keep the user-facing fallback when the server has no JSON response.
    }
    throw new Error(message);
  }

  return response.status === 204 ? null : response.json();
}

function Login({ onLogin }) {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [busy, setBusy] = useState(false);

  async function submit(event) {
    event.preventDefault();
    setBusy(true);
    setError("");
    try {
      const response = await fetch("/api/auth/login", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ username, password })
      });
      const body = await response.json();
      if (!response.ok) {
        throw new Error(body.message || "Invalid username or password.");
      }
      localStorage.setItem(tokenKey, body.token);
      localStorage.setItem(userKey, JSON.stringify(body));
      onLogin(body);
    } catch (submitError) {
      setError(submitError.message);
    } finally {
      setBusy(false);
    }
  }

  return (
    <main className="login-shell">
      <section className="login-card">
        <div className="brand-mark">QMS</div>
        <p className="eyebrow">SAKA QUALITY MANAGEMENT</p>
        <h1>Checklist workspace</h1>
        <p className="muted">Sign in to access your assigned checklist items.</p>
        <form onSubmit={submit} className="login-form">
          <label>
            Username
            <input value={username} onChange={(event) => setUsername(event.target.value)} required autoFocus />
          </label>
          <label>
            Password
            <input type="password" value={password} onChange={(event) => setPassword(event.target.value)} required />
          </label>
          {error && <p className="error-message">{error}</p>}
          <button className="primary-button" disabled={busy}>
            {busy ? "Signing in..." : "Sign in"}
          </button>
        </form>
      </section>
    </main>
  );
}

function App() {
  const [user, setUser] = useState(() => {
    const stored = localStorage.getItem(userKey);
    return stored ? JSON.parse(stored) : null;
  });

  function logout() {
    localStorage.removeItem(tokenKey);
    localStorage.removeItem(userKey);
    setUser(null);
  }

  if (!user) {
    return <Login onLogin={setUser} />;
  }

  return <ChecklistWorkspace user={user} onLogout={logout} />;
}

function ChecklistWorkspace({ user, onLogout }) {
  const [query, setQuery] = useState("");
  const [matches, setMatches] = useState([]);
  const [selectedItem, setSelectedItem] = useState(null);
  const [details, setDetails] = useState([]);
  const [comments, setComments] = useState({});
  const [jobInfo, setJobInfo] = useState({
    projectNumber: "",
    invoiceNumber: "",
    itemNumber: "",
    drawingNumber: "",
    quantity: ""
  });
  const [loading, setLoading] = useState(false);
  const [loadingDetails, setLoadingDetails] = useState(false);
  const [error, setError] = useState("");
  const [now, setNow] = useState(new Date());

  useEffect(() => {
    const timer = setInterval(() => setNow(new Date()), 1000);
    return () => clearInterval(timer);
  }, []);

  useEffect(() => {
    const trimmedQuery = query.trim();
    if (trimmedQuery.length < 3) {
      setMatches([]);
      return undefined;
    }

    const timer = setTimeout(async () => {
      setLoading(true);
      setError("");
      try {
        setMatches(await request(`/api/items/search?name=${encodeURIComponent(trimmedQuery)}`));
      } catch (searchError) {
        setError(searchError.message);
      } finally {
        setLoading(false);
      }
    }, 300);

    return () => clearTimeout(timer);
  }, [query]);

  async function selectItem(item) {
    setSelectedItem(item);
    setQuery(item.nameOfItem || "");
    setMatches([]);
    setLoadingDetails(true);
    setError("");
    try {
      const allDetails = await request("/api/details");
      setDetails(allDetails.filter((detail) => detail.relatedItemId === item.id));
      setComments({});
    } catch (detailsError) {
      setError(detailsError.message);
    } finally {
      setLoadingDetails(false);
    }
  }

  const displayName = useMemo(
    () => user.username || "Signed-in user",
    [user.username]
  );

  return (
    <div className="app-shell">
      <header className="topbar no-print">
        <div>
          <p className="eyebrow">SAKA QMS</p>
          <h1>Checklist workspace</h1>
        </div>
        <div className="topbar-actions">
          <span className="role-pill">{user.isAdmin ? "Administrator" : "Employee"}</span>
          <button className="text-button" onClick={onLogout}>Sign out</button>
        </div>
      </header>

      <main className="workspace">
        <section className="search-panel no-print">
          <label htmlFor="item-search">Search checklist items</label>
          <div className="search-row">
            <input
              id="item-search"
              className="search-input"
              value={query}
              onChange={(event) => setQuery(event.target.value)}
              placeholder="Type at least 3 characters..."
              maxLength={150}
            />
            <button className="print-button" onClick={() => window.print()} disabled={!selectedItem}>
              Print A4
            </button>
          </div>
          <p className="search-hint">Search starts after three characters and updates as you type.</p>
          {loading && <p className="muted">Searching...</p>}
          {!loading && query.trim().length > 0 && query.trim().length < 3 && (
            <p className="muted">Enter at least 3 characters.</p>
          )}
          {matches.length > 0 && (
            <div className="suggestions">
              {matches.map((item) => (
                <button key={item.id} className="suggestion" onClick={() => selectItem(item)}>
                  <strong>{item.nameOfItem}</strong>
                  {item.department?.deptName && <span>{item.department.deptName}</span>}
                </button>
              ))}
            </div>
          )}
        </section>

        {error && <div className="error-banner">{error}</div>}

        {selectedItem ? (
          <article className="report-sheet">
            <section className="report-header">
              <div>
                <p className="eyebrow">CHECKLIST ITEM</p>
                <h2>{selectedItem.nameOfItem}</h2>
              </div>
              {selectedItem.department?.deptName && (
                <div className="department-label">
                  <span>Department:</span> {selectedItem.department.deptName}
                </div>
              )}
              {selectedItem.note && <p className="item-note">{selectedItem.note}</p>}
              <div className="item-codes">
                {Array.from({ length: 5 }, (_, index) => {
                  const codeNumber = index + 1;
                  const code = selectedItem[`code${codeNumber}`];
                  const codeInfo = selectedItem[`cdinf${codeNumber}`];
                  if (!code && !codeInfo) return null;
                  return (
                    <div className="code-pair" key={codeNumber}>
                      <span className="code-label">Code {codeNumber}</span>
                      <strong>{code || "-"}</strong>
                      <span className="code-info">{codeInfo || "-"}</span>
                    </div>
                  );
                })}
              </div>
            </section>

            <section className="job-info-section">
              {[
                ["projectNumber", "Project number"],
                ["invoiceNumber", "Invoice number"],
                ["itemNumber", "Item number"],
                ["drawingNumber", "Drawing number"],
                ["quantity", "Quantity"]
              ].map(([field, label]) => (
                <label className="job-info-field" key={field}>
                  <span>{label}</span>
                  <input
                    value={jobInfo[field]}
                    onChange={(event) =>
                      setJobInfo((current) => ({ ...current, [field]: event.target.value }))
                    }
                    placeholder="Enter"
                  />
                </label>
              ))}
            </section>

            <section className="detail-section">
              <div className="section-heading">
                <div>
                  <p className="eyebrow">INSPECTION DETAILS</p>
                  <h3>Checklist response</h3>
                </div>
                <span className="detail-count">{details.length} detail{details.length === 1 ? "" : "s"}</span>
              </div>
              {loadingDetails ? (
                <p className="muted">Loading details...</p>
              ) : details.length === 0 ? (
                <p className="empty-state">No details are configured for this item.</p>
              ) : (
                <div className="detail-grid">
                  <div className="grid-heading">Detail</div>
                  <div className="grid-heading">Note</div>
                  <div className="grid-heading">Parameters</div>
                  <div className="grid-heading">Comments</div>
                  {details.map((detail) => (
                    <div className="detail-row" key={detail.id}>
                      <div className="detail-cell">{detail.details}</div>
                      <div className="detail-cell">{detail.note}</div>
                      <div className="detail-cell">{detail.parameters}</div>
                      <textarea
                        className="comment-cell no-print"
                        value={comments[detail.id] || ""}
                        onChange={(event) =>
                          setComments((current) => ({ ...current, [detail.id]: event.target.value }))
                        }
                        placeholder="Enter comments..."
                      />
                      <div className="print-comment print-only">{comments[detail.id] || ""}</div>
                    </div>
                  ))}
                </div>
              )}
            </section>

            <footer className="report-footer">
              <span>Prepared by <strong>{displayName}</strong></span>
              <span>{now.toLocaleString()}</span>
            </footer>
          </article>
        ) : (
          <section className="welcome-card">
            <div className="welcome-icon">⌕</div>
            <h2>Find a checklist item</h2>
            <p>Search by item name, then select a result to view its details and record comments.</p>
          </section>
        )}
      </main>
    </div>
  );
}

export default App;
