import { useEffect, useState } from "react";
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
  const [form, setForm] = useState(blankItem);
  const [details, setDetails] = useState([]);
  const [busy, setBusy] = useState(false);

  async function load() {
    try {
      const [itemData, departmentData] = await Promise.all([request("/api/items"), request("/api/departments")]);
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
      for (const detail of details) {
        await request(detail.id ? `/api/details/${detail.id}` : "/api/details", {
          method: detail.id ? "PUT" : "POST",
          body: JSON.stringify({ ...detail, id: undefined, relatedItemId: item.id })
        });
      }
      setForm({ ...blankItem, id: item.id });
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
      if (detail.id) await request(`/api/details/${detail.id}`, { method: "DELETE" });
      setDetails((current) => current.filter((_, detailIndex) => detailIndex !== index));
    } catch (error) {
      onError(error.message);
    }
  }

  return (
    <AdminCard title="Checklist item and details" eyebrow="CHECKLIST CONTENT">
      <div className="admin-form">
        <label className="field-label">Edit existing item</label>
        <div className="editor-select-row">
          <select className="admin-input" value={form.id || ""} onChange={(event) => {
            const item = items.find((candidate) => candidate.id === Number(event.target.value));
            if (item) editItem(item);
            else { setForm(blankItem); setDetails([]); }
          }}>
            <option value="">Create a new item</option>
            {items.map((item) => <option value={item.id} key={item.id}>{item.nameOfItem}</option>)}
          </select>
          {form.id && <button className="remove-button" type="button" onClick={removeItem}>Delete item</button>}
        </div>
        <div className="form-grid two">
          <Field label="Item name" value={form.nameOfItem} onChange={(value) => setForm({ ...form, nameOfItem: value })} required />
          <label className="admin-field">
            <span>Department</span>
            <select className="admin-input" value={form.department?.id || ""} onChange={(event) => setForm({ ...form, department: event.target.value ? { id: Number(event.target.value) } : null })} required>
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
