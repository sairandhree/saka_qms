import { useEffect, useMemo, useRef, useState } from "react";
import { request } from "../api";
import { AdminCard, Field } from "../components/AdminShared";

const blankItem = {
  id: null, nameOfItem: "", note: "", department: null,
  code1: "", code2: "", code3: "", code4: "", code5: "",
  cdinf1: "", cdinf2: "", cdinf3: "", cdinf4: "", cdinf5: ""
};
const blankDetail = { id: null, details: "", note: "", parameters: "", sequence: 1 };

export default function ItemAdmin({ onError }) {
  const [items, setItems] = useState([]);
  const [departments, setDepartments] = useState([]);
  const [filterText, setFilterText] = useState("");
  const [form, setForm] = useState(blankItem);
  const [details, setDetails] = useState([]);
  const [busy, setBusy] = useState(false);
  const selectionRef = useRef(0);

  const filteredItems = useMemo(() => {
    const filter = filterText.trim().toLowerCase();
    if (!filter) return items;
    return items.filter((item) => item.nameOfItem?.toLowerCase().includes(filter));
  }, [filterText, items]);

  const selectableDepartments = useMemo(() => departments, [departments]);

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

  async function selectItem(item) {
    const selection = ++selectionRef.current;
    setForm({ ...blankItem, ...item });
    setDetails([]);
    try {
      const itemDetails = await request(`/api/details/item/${item.id}`);
      if (selection === selectionRef.current) setDetails(itemDetails);
    } catch (error) {
      if (selection === selectionRef.current) onError(error.message);
    }
  }

  function startNewItem() {
    selectionRef.current += 1;
    setForm(blankItem);
    setDetails([]);
  }

  function updateDetail(index, field, value) {
    setDetails((current) => current.map((detail, detailIndex) =>
      detailIndex === index ? { ...detail, [field]: value } : detail
    ));
  }

  async function save() {
    if (!form.nameOfItem.trim() || !form.department?.id) {
      onError("Item name and department are required.");
      return;
    }
    setBusy(true);
    onError("");
    try {
      const response = await request(form.id ? `/api/items/${form.id}/with-details` : "/api/items/with-details", {
        method: form.id ? "PUT" : "POST",
        body: JSON.stringify({
          item: {
            nameOfItem: form.nameOfItem,
            note: form.note,
            sequence: form.sequence,
            code1: form.code1,
            code2: form.code2,
            code3: form.code3,
            code4: form.code4,
            code5: form.code5,
            cdinf1: form.cdinf1,
            cdinf2: form.cdinf2,
            cdinf3: form.cdinf3,
            cdinf4: form.cdinf4,
            cdinf5: form.cdinf5,
            departmentId: form.department.id
          },
          details
        })
      });
      setForm({ ...blankItem, ...response.item });
      setDetails(response.details || []);
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
      startNewItem();
      await load();
    } catch (error) {
      onError(error.message);
    }
  }

  function removeDetail(index) {
    setDetails((current) => current.filter((_, detailIndex) => detailIndex !== index));
  }

  return (
    <AdminCard title="Checklist item and details" eyebrow="CHECKLIST CONTENT">
      <div className="item-admin-toolbar">
        <label className="admin-field item-filter-field">
          <span>Filter items</span>
          <input
            className="admin-input"
            value={filterText}
            onChange={(event) => setFilterText(event.target.value)}
            placeholder="Type to filter the item list..."
          />
        </label>
        <button className="icon-button primary-icon" type="button" onClick={startNewItem} aria-label="Create new item" title="Create new item">
          ＋
        </button>
      </div>

      <div className="item-admin-layout">
        <aside className="item-list-panel">
          <div className="item-list-heading">
            <span>Items</span>
            <span>{filteredItems.length}</span>
          </div>
          <div className="item-list">
            {filteredItems.map((item) => (
              <button
                className={`item-list-entry ${form.id === item.id ? "selected" : ""}`}
                type="button"
                key={item.id}
                onClick={() => selectItem(item)}
              >
                <strong>{item.nameOfItem || "Unnamed item"}</strong>
                <span>{item.department?.deptName || "No department"}</span>
              </button>
            ))}
            {filteredItems.length === 0 && <p className="empty-state">No matching items.</p>}
          </div>
        </aside>

        <section className="item-editor-panel">
          <div className="editor-heading">
            <div>
              <p className="eyebrow">{form.id ? "EDIT ITEM" : "NEW ITEM"}</p>
              <h3>{form.nameOfItem || "Create a checklist item"}</h3>
            </div>
            <div className="editor-icon-actions">
              {form.id && (
                <button className="icon-button delete-icon" type="button" onClick={removeItem} aria-label="Delete item" title="Delete item">
                  🗑
                </button>
              )}
              <button className="icon-button save-icon" type="button" onClick={save} disabled={busy} aria-label="Save item and details" title="Save item and details">
                ✓
              </button>
            </div>
          </div>

          <div className="form-grid two">
            <Field label="Item name" value={form.nameOfItem} onChange={(value) => setForm({ ...form, nameOfItem: value })} required />
            <label className="admin-field">
              <span>Department</span>
              <select className="admin-input" value={form.department?.id || ""} onChange={(event) => setForm({ ...form, department: event.target.value ? { id: Number(event.target.value) } : null })} required>
                <option value="">Select department</option>
                {selectableDepartments.map((department) => <option value={department.id} key={department.id}>{department.deptName}</option>)}
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
              <button className="icon-button primary-icon" type="button" onClick={() => setDetails([...details, { ...blankDetail, sequence: details.length + 1 }])} aria-label="Add detail" title="Add detail">
                ＋
              </button>
            </div>
            {details.map((detail, index) => (
              <div className="detail-editor-row" key={detail.id || `new-${index}`}>
                <Field label="Detail" value={detail.details} onChange={(value) => updateDetail(index, "details", value)} />
                <Field label="Note" value={detail.note} onChange={(value) => updateDetail(index, "note", value)} />
                <Field label="Parameters" value={detail.parameters} onChange={(value) => updateDetail(index, "parameters", value)} />
                <button className="icon-button delete-icon" type="button" onClick={() => removeDetail(index, detail)} aria-label="Delete detail" title="Delete detail">
                  🗑
                </button>
              </div>
            ))}
          </div>
        </section>
      </div>
    </AdminCard>
  );
}
