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

  return user.isAdmin
    ? <AdminWorkspace user={user} onLogout={logout} />
    : <ChecklistWorkspace user={user} onLogout={logout} />;
}

function AdminWorkspace({ user, onLogout }) {
  const [screen, setScreen] = useState("departments");
  const [error, setError] = useState("");

  function openScreen(nextScreen) {
    setError("");
    setScreen(nextScreen);
  }

  return (
    <div className="app-shell">
      <header className="topbar no-print">
        <div>
          <p className="eyebrow">SAKA QMS</p>
          <h1>Administration</h1>
        </div>
        <div className="topbar-actions">
          <span className="role-pill">Administrator</span>
          <button className="text-button" onClick={onLogout}>Sign out</button>
        </div>
      </header>
      <main className="workspace">
        <nav className="admin-tabs no-print">
          <button className={screen === "departments" ? "active" : ""} onClick={() => openScreen("departments")}>
            Departments
          </button>
          <button className={screen === "employees" ? "active" : ""} onClick={() => openScreen("employees")}>
            Employees
          </button>
          <button className={screen === "items" ? "active" : ""} onClick={() => openScreen("items")}>
            Checklist items
          </button>
          <button className={screen === "specification" ? "active" : ""} onClick={() => openScreen("specification")}>
            Specification print
          </button>
        </nav>
        {error && <div className="error-banner">{error}</div>}
        {screen === "departments" && <DepartmentAdmin onError={setError} />}
        {screen === "employees" && <EmployeeAdmin onError={setError} />}
        {screen === "items" && <ItemAdmin onError={setError} />}
        {screen === "specification" && <ChecklistWorkspace user={user} onLogout={onLogout} embedded />}
        <p className="admin-footer no-print">Signed in as {user.username}</p>
      </main>
    </div>
  );
}

function DepartmentAdmin({ onError }) {
  const blank = { id: null, deptName: "", manager: "" };
  const [departments, setDepartments] = useState([]);
  const [form, setForm] = useState(blank);
  const [busy, setBusy] = useState(false);

  async function load() {
    try {
      setDepartments(await request("/api/departments"));
    } catch (error) {
      onError(error.message);
    }
  }

  useEffect(() => { load(); }, []);

  async function save(event) {
    event.preventDefault();
    setBusy(true);
    onError("");
    try {
      await request(form.id ? `/api/departments/${form.id}` : "/api/departments", {
        method: form.id ? "PUT" : "POST",
        body: JSON.stringify({ deptName: form.deptName, manager: form.manager })
      });
      setForm(blank);
      await load();
    } catch (error) {
      onError(error.message);
    } finally {
      setBusy(false);
    }
  }

  async function remove(department) {
    if (!window.confirm(`Delete department "${department.deptName}"?`)) return;
    try {
      await request(`/api/departments/${department.id}`, { method: "DELETE" });
      if (form.id === department.id) setForm(blank);
      await load();
    } catch (error) {
      onError(error.message);
    }
  }

  return (
    <AdminCard title="Departments" eyebrow="MASTER DATA">
      <form className="admin-form" onSubmit={save}>
        <Field label="Department name" value={form.deptName} onChange={(value) => setForm({ ...form, deptName: value })} required />
        <Field label="Manager" value={form.manager} onChange={(value) => setForm({ ...form, manager: value })} />
        <FormActions editing={Boolean(form.id)} busy={busy} onCancel={() => setForm(blank)} />
      </form>
      <AdminTable headers={["Department", "Manager", ""]}>
        {departments.map((department) => (
          <tr key={department.id}>
            <td>{department.deptName}</td>
            <td>{department.manager || "-"}</td>
            <td className="table-actions">
              <button className="table-action" onClick={() => setForm(department)}>Edit</button>
              <button className="remove-button" onClick={() => remove(department)}>Delete</button>
            </td>
          </tr>
        ))}
      </AdminTable>
    </AdminCard>
  );
}

function EmployeeAdmin({ onError }) {
  const blank = {
    id: null, employeeId: "", employeeName: "", username: "", password: "",
    isAdmin: false, departments: []
  };
  const [employees, setEmployees] = useState([]);
  const [departments, setDepartments] = useState([]);
  const [form, setForm] = useState(blank);
  const [busy, setBusy] = useState(false);

  async function load() {
    try {
      const [employeeData, departmentData] = await Promise.all([
        request("/api/employees"),
        request("/api/departments")
      ]);
      setEmployees(employeeData);
      setDepartments(departmentData);
    } catch (error) {
      onError(error.message);
    }
  }

  useEffect(() => { load(); }, []);

  function toggleDepartment(id) {
    const selected = form.departments.some((department) => department.id === id);
    setForm({
      ...form,
      departments: selected
        ? form.departments.filter((department) => department.id !== id)
        : [...form.departments, { id }]
    });
  }

  async function save(event) {
    event.preventDefault();
    setBusy(true);
    onError("");
    try {
      await request(form.id ? `/api/employees/${form.id}` : "/api/employees", {
        method: form.id ? "PUT" : "POST",
        body: JSON.stringify({
          employeeId: form.employeeId ? Number(form.employeeId) : null,
          employeeName: form.employeeName,
          username: form.username,
          password: form.password || undefined,
          isAdmin: form.isAdmin,
          departments: form.departments
        })
      });
      setForm(blank);
      await load();
    } catch (error) {
      onError(error.message);
    } finally {
      setBusy(false);
    }
  }

  async function remove(employee) {
    if (!window.confirm(`Delete employee "${employee.username}"?`)) return;
    try {
      await request(`/api/employees/${employee.id}`, { method: "DELETE" });
      if (form.id === employee.id) setForm(blank);
      await load();
    } catch (error) {
      onError(error.message);
    }
  }

  return (
    <AdminCard title="Employees" eyebrow="USER ACCESS">
      <form className="admin-form" onSubmit={save}>
        <div className="form-grid two">
          <Field label="Employee number" type="number" value={form.employeeId} onChange={(value) => setForm({ ...form, employeeId: value })} />
          <Field label="Employee name" value={form.employeeName} onChange={(value) => setForm({ ...form, employeeName: value })} required />
          <Field label="Username" value={form.username} onChange={(value) => setForm({ ...form, username: value })} required />
          <Field label={form.id ? "New password (optional)" : "Password"} type="password" value={form.password} onChange={(value) => setForm({ ...form, password: value })} required={!form.id} />
        </div>
        <label className="check-field">
          <input type="checkbox" checked={form.isAdmin} onChange={(event) => setForm({ ...form, isAdmin: event.target.checked })} />
          Administrator
        </label>
        <div>
          <p className="field-label">Departments</p>
          <div className="department-options">
            {departments.map((department) => (
              <label className="check-field" key={department.id}>
                <input
                  type="checkbox"
                  checked={form.departments.some((selected) => selected.id === department.id)}
                  onChange={() => toggleDepartment(department.id)}
                />
                {department.deptName}
              </label>
            ))}
          </div>
        </div>
        <FormActions editing={Boolean(form.id)} busy={busy} onCancel={() => setForm(blank)} />
      </form>
      <AdminTable headers={["Name", "Username", "Admin", "Departments", ""]}>
        {employees.map((employee) => (
          <tr key={employee.id}>
            <td>{employee.employeeName || "-"}</td>
            <td>{employee.username}</td>
            <td>{employee.isAdmin ? "Yes" : "No"}</td>
            <td>{employee.departments?.map((department) => department.deptName).join(", ") || "-"}</td>
            <td className="table-actions">
              <button className="table-action" onClick={() => setForm({ ...employee, password: "" })}>Edit</button>
              <button className="remove-button" onClick={() => remove(employee)}>Delete</button>
            </td>
          </tr>
        ))}
      </AdminTable>
    </AdminCard>
  );
}

function ItemAdmin({ onError }) {
  const blankItem = {
    id: null, nameOfItem: "", note: "", department: null,
    code1: "", code2: "", code3: "", code4: "", code5: "",
    cdinf1: "", cdinf2: "", cdinf3: "", cdinf4: "", cdinf5: ""
  };
  const blankDetail = { id: null, details: "", note: "", parameters: "", sequence: 1 };
  const [items, setItems] = useState([]);
  const [departments, setDepartments] = useState([]);
  const [form, setForm] = useState(blankItem);
  const [details, setDetails] = useState([]);
  const [busy, setBusy] = useState(false);

  async function load() {
    try {
      const [itemData, departmentData] = await Promise.all([
        request("/api/items"),
        request("/api/departments")
      ]);
      setItems(itemData);
      setDepartments(departmentData);
    } catch (error) {
      onError(error.message);
    }
  }

  useEffect(() => { load(); }, []);

  async function editItem(item) {
    setForm({ ...blankItem, ...item });
    try {
      const allDetails = await request("/api/details");
      setDetails(allDetails.filter((detail) => detail.relatedItemId === item.id));
    } catch (error) {
      onError(error.message);
    }
  }

  function updateDetail(index, field, value) {
    setDetails((current) => current.map((detail, detailIndex) =>
      detailIndex === index ? { ...detail, [field]: value } : detail
    ));
  }

  async function save() {
    setBusy(true);
    onError("");
    try {
      const item = await request(form.id ? `/api/items/${form.id}` : "/api/items", {
        method: form.id ? "PUT" : "POST",
        body: JSON.stringify({
          ...form,
          id: undefined,
          department: form.department ? { id: form.department.id } : null
        })
      });
      const itemId = item.id;
      for (const detail of details) {
        await request(detail.id ? `/api/details/${detail.id}` : "/api/details", {
          method: detail.id ? "PUT" : "POST",
          body: JSON.stringify({
            ...detail,
            id: undefined,
            relatedItemId: itemId
          })
        });
      }
      setForm({ ...blankItem, id: itemId });
      await load();
    } catch (error) {
      onError(error.message);
    } finally {
      setBusy(false);
    }
  }

  async function removeItem() {
    if (!form.id || !window.confirm(`Delete item "${form.nameOfItem}"?`)) return;
    try {
      await request(`/api/items/${form.id}`, { method: "DELETE" });
      setForm(blankItem);
      setDetails([]);
      await load();
    } catch (error) {
      onError(error.message);
    }
  }

  async function removeDetail(index, detail) {
    try {
      if (detail.id) {
        await request(`/api/details/${detail.id}`, { method: "DELETE" });
      }
      setDetails(details.filter((_, detailIndex) => detailIndex !== index));
    } catch (error) {
      onError(error.message);
    }
  }

  return (
    <AdminCard title="Checklist item and details" eyebrow="CHECKLIST CONTENT">
      <div className="admin-form">
        <label className="field-label">Edit existing item</label>
        <div className="editor-select-row">
          <select
            className="admin-input"
            value={form.id || ""}
            onChange={(event) => {
              const item = items.find((candidate) => candidate.id === Number(event.target.value));
              if (item) editItem(item);
              else { setForm(blankItem); setDetails([]); }
            }}
          >
            <option value="">Create a new item</option>
            {items.map((item) => <option value={item.id} key={item.id}>{item.nameOfItem}</option>)}
          </select>
          {form.id && <button className="remove-button" type="button" onClick={removeItem}>Delete item</button>}
        </div>

        <div className="form-grid two">
          <Field label="Item name" value={form.nameOfItem} onChange={(value) => setForm({ ...form, nameOfItem: value })} required />
          <label className="admin-field">
            <span>Department</span>
            <select
              className="admin-input"
              value={form.department?.id || ""}
              onChange={(event) => setForm({
                ...form,
                department: event.target.value ? { id: Number(event.target.value) } : null
              })}
              required
            >
              <option value="">Select department</option>
              {departments.map((department) => <option value={department.id} key={department.id}>{department.deptName}</option>)}
            </select>
          </label>
        </div>
        <Field label="Note" value={form.note} onChange={(value) => setForm({ ...form, note: value })} />
        <div className="code-editor-grid">
          {Array.from({ length: 5 }, (_, index) => {
            const number = index + 1;
            return (
              <div className="code-editor-pair" key={number}>
                <Field label={`Code ${number}`} value={form[`code${number}`]} onChange={(value) => setForm({ ...form, [`code${number}`]: value })} />
                <Field label={`Code info ${number}`} value={form[`cdinf${number}`]} onChange={(value) => setForm({ ...form, [`cdinf${number}`]: value })} />
              </div>
            );
          })}
        </div>

        <div className="detail-editor">
          <div className="editor-heading">
            <div><p className="eyebrow">INSPECTION DETAILS</p><h3>Details</h3></div>
            <button className="secondary-button" type="button" onClick={() => setDetails([...details, { ...blankDetail, sequence: details.length + 1 }])}>Add detail</button>
          </div>
          {details.map((detail, index) => (
            <div className="detail-editor-row" key={detail.id || `new-${index}`}>
              <Field label="Detail" value={detail.details} onChange={(value) => updateDetail(index, "details", value)} />
              <Field label="Note" value={detail.note} onChange={(value) => updateDetail(index, "note", value)} />
              <Field label="Parameters" value={detail.parameters} onChange={(value) => updateDetail(index, "parameters", value)} />
              <button className="remove-button" type="button" onClick={() => removeDetail(index, detail)}>Delete</button>
            </div>
          ))}
        </div>
        <button className="primary-button admin-save" type="button" onClick={save} disabled={busy}>
          {busy ? "Saving..." : form.id ? "Update item and details" : "Create item and details"}
        </button>
      </div>
    </AdminCard>
  );
}

function AdminCard({ eyebrow, title, children }) {
  return <section className="admin-card"><p className="eyebrow">{eyebrow}</p><h2>{title}</h2>{children}</section>;
}

function Field({ label, value, onChange, type = "text", required = false }) {
  return (
    <label className="admin-field">
      <span>{label}</span>
      <input className="admin-input" type={type} value={value ?? ""} onChange={(event) => onChange(event.target.value)} required={required} />
    </label>
  );
}

function FormActions({ editing, busy, onCancel }) {
  return (
    <div className="form-actions">
      <button className="primary-button" disabled={busy}>{busy ? "Saving..." : editing ? "Update" : "Create"}</button>
      {editing && <button className="secondary-button" type="button" onClick={onCancel}>Cancel</button>}
    </div>
  );
}

function AdminTable({ headers, children }) {
  return (
    <div className="admin-table-wrap">
      <table className="admin-table">
        <thead><tr>{headers.map((header) => <th key={header}>{header}</th>)}</tr></thead>
        <tbody>{children}</tbody>
      </table>
    </div>
  );
}

function ChecklistWorkspace({ user, onLogout, onBack, embedded = false }) {
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
      <header className={`topbar no-print ${embedded ? "embedded-hidden" : ""}`}>
        <div>
          <p className="eyebrow">SAKA QMS</p>
          <h1>Checklist workspace</h1>
        </div>
        <div className="topbar-actions">
          <span className="role-pill">{user.isAdmin ? "Administrator" : "Employee"}</span>
          {onBack && <button className="text-button" onClick={onBack}>Admin home</button>}
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
              Print
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
