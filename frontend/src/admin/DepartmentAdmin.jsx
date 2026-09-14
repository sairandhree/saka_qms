import { useEffect, useState } from "react";
import { request } from "../api";
import { AdminCard, AdminTable, Field, Modal } from "../components/AdminShared";

const blankDepartment = { id: null, deptName: "" };

export default function DepartmentAdmin({ onError }) {
  const [departments, setDepartments] = useState([]);
  const [editingId, setEditingId] = useState(null);
  const [draft, setDraft] = useState(blankDepartment);
  const [createOpen, setCreateOpen] = useState(false);
  const [createDraft, setCreateDraft] = useState(blankDepartment);
  const [busy, setBusy] = useState(false);

  async function load() {
    try {
      setDepartments(await request("/api/departments"));
    } catch (error) {
      onError(error.message);
    }
  }

  useEffect(() => { load(); }, []);

  function startEdit(department) {
    setEditingId(department.id);
    setDraft({ ...department });
  }

  async function saveDepartment(department) {
    if (!department.deptName?.trim()) {
      onError("Department name is required.");
      return;
    }
    setBusy(true);
    onError("");
    try {
      await request(`/api/departments/${department.id}`, {
        method: "PUT",
        body: JSON.stringify({ deptName: department.deptName })
      });
      setEditingId(null);
      await load();
    } catch (error) {
      onError(error.message);
    } finally {
      setBusy(false);
    }
  }

  async function createDepartment(event) {
    event.preventDefault();
    setBusy(true);
    onError("");
    try {
      await request("/api/departments", {
        method: "POST",
        body: JSON.stringify({ deptName: createDraft.deptName })
      });
      setCreateOpen(false);
      setCreateDraft(blankDepartment);
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
      if (editingId === department.id) setEditingId(null);
      await load();
    } catch (error) {
      onError(error.message);
    }
  }

  return (
    <AdminCard title="Departments" eyebrow="MASTER DATA">
      <div className="admin-toolbar">
        <p className="muted">Edit department names directly in the table.</p>
        <button className="primary-button" type="button" onClick={() => setCreateOpen(true)}>Add department</button>
      </div>
      <AdminTable headers={["Department", "Actions"]}>
        {departments.map((department) => {
          const editing = editingId === department.id;
          return (
            <tr key={department.id}>
              <td>
                {editing ? (
                  <input className="admin-input inline-input" value={draft.deptName} onChange={(event) => setDraft({ ...draft, deptName: event.target.value })} autoFocus />
                ) : department.deptName}
              </td>
              <td className="table-actions">
                {editing ? (
                  <>
                    <button className="icon-button save-icon" type="button" disabled={busy} onClick={() => saveDepartment(draft)} aria-label="Save department" title="Save">
                      ✓
                    </button>
                    <button className="icon-button" type="button" onClick={() => setEditingId(null)} aria-label="Cancel editing" title="Cancel">
                      ×
                    </button>
                  </>
                ) : (
                  <button className="icon-button edit-icon" type="button" onClick={() => startEdit(department)} aria-label={`Edit ${department.deptName}`} title="Edit">
                    ✎
                  </button>
                )}
                <button className="icon-button delete-icon" type="button" onClick={() => remove(department)} aria-label={`Delete ${department.deptName}`} title="Delete">
                  🗑
                </button>
              </td>
            </tr>
          );
        })}
      </AdminTable>
      {createOpen && (
        <Modal title="Add department" onClose={() => setCreateOpen(false)}>
          <form className="admin-form" onSubmit={createDepartment}>
            <Field label="Department name" value={createDraft.deptName} onChange={(value) => setCreateDraft({ ...createDraft, deptName: value })} required />
            <div className="form-actions">
              <button className="primary-button" disabled={busy}>{busy ? "Saving..." : "Save department"}</button>
              <button className="secondary-button" type="button" onClick={() => setCreateOpen(false)}>Cancel</button>
            </div>
          </form>
        </Modal>
      )}
    </AdminCard>
  );
}
