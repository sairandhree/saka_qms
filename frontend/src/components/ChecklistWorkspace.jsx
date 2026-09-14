import { useEffect, useMemo, useRef, useState } from "react";
import { request } from "../api";
import BrandLogo from "./BrandLogo";

function sortDetailsBySequence(details) {
  return [...details].sort((first, second) => {
    const firstSequence = Number(first.sequence);
    const secondSequence = Number(second.sequence);
    const firstValue = Number.isFinite(firstSequence) ? firstSequence : Number.POSITIVE_INFINITY;
    const secondValue = Number.isFinite(secondSequence) ? secondSequence : Number.POSITIVE_INFINITY;
    return firstValue - secondValue || (first.id ?? 0) - (second.id ?? 0);
  });
}

export default function ChecklistWorkspace({ user, onLogout, embedded = false }) {
  const [query, setQuery] = useState("");
  const [matches, setMatches] = useState([]);
  const [selectedItem, setSelectedItem] = useState(null);
  const [details, setDetails] = useState([]);
  const [comments, setComments] = useState({});
  const [jobInfo, setJobInfo] = useState({ projectNumber: "", invoiceNumber: "", itemNumber: "", drawingNumber: "", quantity: "" });
  const [loading, setLoading] = useState(false);
  const [loadingDetails, setLoadingDetails] = useState(false);
  const [error, setError] = useState("");
  const [now, setNow] = useState(new Date());
  const selectionRef = useRef(0);

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
    const selection = ++selectionRef.current;
    setSelectedItem(item);
    setQuery(item.nameOfItem || "");
    setMatches([]);
    setDetails([]);
    setComments({});
    setLoadingDetails(true);
    setError("");
    try {
      const itemDetails = await request(`/api/details/item/${item.id}`);
      if (selection === selectionRef.current) {
        setDetails(sortDetailsBySequence(itemDetails));
      }
    } catch (detailsError) {
      if (selection === selectionRef.current) setError(detailsError.message);
    } finally {
      if (selection === selectionRef.current) setLoadingDetails(false);
    }
  }

  const displayName = useMemo(() => user.employeeName || user.username || "Signed-in user", [user.employeeName, user.username]);

  return (
    <div className="app-shell">
      <header className={`topbar no-print ${embedded ? "embedded-hidden" : ""}`}>
        <div className="topbar-brand"><BrandLogo /><h1>Checklist workspace</h1></div>
        <div className="topbar-actions">
          <span className="role-pill">{user.employeeName || user.username}</span>
          <button className="text-button" onClick={onLogout}>Sign out</button>
        </div>
      </header>
      <main className="workspace">
        <section className="search-panel no-print">
          <label htmlFor="item-search">Search checklist items</label>
          <div className="search-row">
            <input id="item-search" className="search-input" value={query} onChange={(event) => setQuery(event.target.value)} placeholder="Type at least 3 characters..." maxLength={150} />
            <button className="print-button" onClick={() => window.print()} disabled={!selectedItem}>Print</button>
          </div>
          <p className="search-hint">Search starts after three characters and updates as you type.</p>
          {loading && <p className="muted">Searching...</p>}
          {!loading && query.trim().length > 0 && query.trim().length < 3 && <p className="muted">Enter at least 3 characters.</p>}
          {matches.length > 0 && <div className="suggestions">{matches.map((item) => (
            <button key={item.id} className="suggestion" onClick={() => selectItem(item)}>
              <strong>{item.nameOfItem}</strong>
              {item.department?.deptName && <span>{item.department.deptName}</span>}
            </button>
          ))}</div>}
        </section>
        {error && <div className="error-banner">{error}</div>}
        {selectedItem ? (
          <article className="report-sheet">
            <section className="report-header">
              <div><p className="eyebrow">CHECKLIST ITEM</p><h2>{selectedItem.nameOfItem}</h2></div>
              {selectedItem.department?.deptName && <div className="department-label"><span>Department:</span> {selectedItem.department.deptName}</div>}
              {selectedItem.note && <p className="item-note">{selectedItem.note}</p>}
              <div className="item-codes">{Array.from({ length: 5 }, (_, index) => {
                const number = index + 1;
                const code = selectedItem[`code${number}`];
                const codeInfo = selectedItem[`cdinf${number}`];
                if (!code && !codeInfo) return null;
                return <div className="code-pair" key={number}><span className="code-label">Code {number}</span><strong>{code || "-"}</strong><span className="code-info">{codeInfo || "-"}</span></div>;
              })}</div>
            </section>
            <section className="job-info-section">{[
              ["projectNumber", "Project number"], ["invoiceNumber", "Invoice number"], ["itemNumber", "Item number"], ["drawingNumber", "Drawing number"], ["quantity", "Quantity"]
            ].map(([field, label]) => <label className="job-info-field" key={field}><span>{label}</span><input value={jobInfo[field]} onChange={(event) => setJobInfo((current) => ({ ...current, [field]: event.target.value }))} placeholder="Enter" /></label>)}</section>
            <section className="detail-section">
              <div className="section-heading"><div><p className="eyebrow">INSPECTION DETAILS</p><h3>Checklist response</h3></div><span className="detail-count">{details.length} detail{details.length === 1 ? "" : "s"}</span></div>
              {loadingDetails ? <p className="muted">Loading details...</p> : details.length === 0 ? <p className="empty-state">No details are configured for this item.</p> : (
                <div className="detail-grid">
                  <div className="grid-heading">Detail</div><div className="grid-heading note-column">Note</div><div className="grid-heading">Parameters</div><div className="grid-heading">Comments</div>
                  {details.map((detail) => <div className="detail-row" key={detail.id}>
                    <div className="detail-cell">{detail.details}</div><div className="detail-cell note-column">{detail.note}</div><div className="detail-cell">{detail.parameters}</div>
                    <textarea className="comment-cell no-print" value={comments[detail.id] || ""} onChange={(event) => setComments((current) => ({ ...current, [detail.id]: event.target.value }))} placeholder="Enter comments..." />
                    <div className="print-comment print-only">{comments[detail.id] || ""}</div>
                  </div>)}
                </div>
              )}
            </section>
            <footer className="report-footer"><span>Prepared by <strong>{displayName}</strong></span><span>{now.toLocaleString()}</span></footer>
          </article>
        ) : <section className="welcome-card"><div className="welcome-icon">⌕</div><h2>Find a checklist item</h2><p>Search by item name, then select a result to view its details and record comments.</p></section>}
      </main>
    </div>
  );
}
