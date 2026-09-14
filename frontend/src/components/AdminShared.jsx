export function AdminCard({ eyebrow, title, children }) {
  return (
    <section className="admin-card">
      <p className="eyebrow">{eyebrow}</p>
      <h2>{title}</h2>
      {children}
    </section>
  );
}

export function Field({ label, value, onChange, type = "text", required = false, disabled = false }) {
  return (
    <label className="admin-field">
      <span>{label}</span>
      <input
        className="admin-input"
        type={type}
        value={value ?? ""}
        onChange={(event) => onChange(event.target.value)}
        required={required}
        disabled={disabled}
      />
    </label>
  );
}

export function FormActions({ editing, busy, onCancel }) {
  return (
    <div className="form-actions">
      <button className="primary-button" disabled={busy}>
        {busy ? "Saving..." : editing ? "Update" : "Create"}
      </button>
      {editing && (
        <button className="secondary-button" type="button" onClick={onCancel}>
          Cancel
        </button>
      )}
    </div>
  );
}

export function AdminTable({ headers, children }) {
  return (
    <div className="admin-table-wrap">
      <table className="admin-table">
        <thead>
          <tr>{headers.map((header) => <th key={header}>{header}</th>)}</tr>
        </thead>
        <tbody>{children}</tbody>
      </table>
    </div>
  );
}

export function Modal({ title, children, onClose }) {
  return (
    <div className="modal-backdrop" role="presentation" onMouseDown={onClose}>
      <section className="modal-card" role="dialog" aria-modal="true" aria-label={title} onMouseDown={(event) => event.stopPropagation()}>
        <div className="modal-heading">
          <h3>{title}</h3>
          <button className="icon-button" type="button" onClick={onClose} aria-label="Close">
            ×
          </button>
        </div>
        {children}
      </section>
    </div>
  );
}
