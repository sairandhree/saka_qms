import { useEffect, useState } from "react";
import { request } from "../api";
import { AdminCard, AdminTable, Field, Modal } from "../components/AdminShared";

const blankEmployee = {
  id: null, employeeId: "", employeeName: "", username: "", password: "",
  isAdmin: false, isDepartmentHead: false, departments: []
};

function toggleDepartment(employee, id) {
  const selected = employee.departments.some((department) => department.id === id);
  return {
    ...employee,
    departments: selected
      ? employee.departments.filter((department) => department.id !== id)
      : [...employee.departments, { id }]
  };
}

function DepartmentChecks({ employee, departments, onChange }) {
  return (
    <div className="department-options compact">
      {departments.map((department) => (
        <label className="check-field" key={department.id}>
          <input
            type="checkbox"
            checked={employee.departments.some((selected) => selected.id === department.id)}
            onChange={() => onChange(toggleDepartment(employee, department.id))}
          />
          {department.deptName}
        </label>
      ))}
    </div>
  );
}

export default function EmployeeAdmin({ onError }) {
  const [employees, setEmployees] = useState([]);
  const [departments, setDepartments] = useState([]);
  const [editingId, setEditingId] = useState(null);
  const [draft, setDraft] = useState(blankEmployee);
  const [createOpen, setCreateOpen] = useState(false);
  const [createDraft, setCreateDraft] = useState(blankEmployee);
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

  function startEdit(employee) {
    setEditingId(employee.id);
    setDraft({ ...employee, departments: employee.departments || [], password: "" });
  }

  async function saveEmployee(employee) {
    if (!employee.employeeName?.trim() || !employee.username?.trim()) {
      onError("Employee name and username are required.");
      return;
    }
    setBusy(true);
    onError("");
    try {
      await request(`/api/employees/${employee.id}`, {
        method: "PUT",
        body: JSON.stringify({
          employeeId: employee.employeeId ? Number(employee.employeeId) : null,
          employeeName: employee.employeeName,
          username: employee.username,
          password: employee.password || undefined,
          isAdmin: employee.isAdmin,
          isDepartmentHead: employee.isDepartmentHead,
          departments: employee.departments
        })
      });
      setEditingId(null);
      await load();
    } catch (error) {
      onError(error.message);
    } finally {
      setBusy(false);
    }
  }

  async function createEmployee(event) {
    event.preventDefault();
    setBusy(true);
    onError("");
    try {
      await request("/api/employees", {
        method: "POST",
        body: JSON.stringify({
          employeeId: createDraft.employeeId ? Number(createDraft.employeeId) : null,
          employeeName: createDraft.employeeName,
          username: createDraft.username,
          password: createDraft.password,
          isAdmin: createDraft.isAdmin,
          isDepartmentHead: createDraft.isDepartmentHead,
          departments: createDraft.departments
        })
      });
      setCreateOpen(false);
      setCreateDraft(blankEmployee);
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
      if (editingId === employee.id) setEditingId(null);
      await load();
    } catch (error) {
      onError(error.message);
    }
  }

  function renderEditor(employee, setEmployee, isCreate = false) {
    const disabledIdentity = !isCreate;
    return (
      <div className="inline-employee-editor">
        <input className="admin-input" type="number" value={employee.employeeId ?? ""} onChange={(event) => setEmployee({ ...employee, employeeId: event.target.value })} disabled={disabledIdentity} placeholder="Emp. ID" />
        <input className="admin-input" value={employee.employeeName ?? ""} onChange={(event) => setEmployee({ ...employee, employeeName: event.target.value })} placeholder="Employee name" required />
        <input className="admin-input" value={employee.username ?? ""} onChange={(event) => setEmployee({ ...employee, username: event.target.value })} disabled={disabledIdentity} placeholder="Username" required />
        <input className="admin-input" type="password" value={employee.password ?? ""} onChange={(event) => setEmployee({ ...employee, password: event.target.value })} placeholder={isCreate ? "Password" : "New password"} required={isCreate} />
        <label className="check-field"><input type="checkbox" checked={Boolean(employee.isAdmin)} onChange={(event) => setEmployee({ ...employee, isAdmin: event.target.checked })} /> Admin</label>
        <label className="check-field"><input type="checkbox" checked={Boolean(employee.isDepartmentHead)} onChange={(event) => setEmployee({ ...employee, isDepartmentHead: event.target.checked })} /> Dept. head</label>
        <DepartmentChecks employee={employee} departments={departments} onChange={setEmployee} />
      </div>
    );
  }

  return (
    <AdminCard title="Employees" eyebrow="USER ACCESS">
      <div className="admin-toolbar">
        <p className="muted">Edit employee details directly in the table.</p>
        <button className="primary-button" type="button" onClick={() => setCreateOpen(true)}>Add employee</button>
      </div>
      <AdminTable headers={["Employee", "Username", "Admin", "Dept. head", "Departments", "Actions"]}>
        {employees.map((employee) => {
          const editing = editingId === employee.id;
          const protectedEmployee = employee.id === 1 && employee.username?.toLowerCase() === "anand";
          return (
            <tr key={employee.id}>
              {editing ? (
                <td colSpan="5">{renderEditor(draft, setDraft)}</td>
              ) : (
                <>
                  <td>{employee.employeeName || "-"}</td>
                  <td>{employee.username}</td>
                  <td>{employee.isAdmin ? "Yes" : "No"}</td>
                  <td>{employee.isDepartmentHead ? "Yes" : "No"}</td>
                  <td>{employee.departments?.map((department) => department.deptName).join(", ") || "-"}</td>
                </>
              )}
              <td className="table-actions">
                {editing ? (
                  <>
                    <button className="icon-button save-icon" type="button" disabled={busy} onClick={() => saveEmployee(draft)} aria-label="Save employee" title="Save">
                      ✓
                    </button>
                    <button className="icon-button" type="button" onClick={() => setEditingId(null)} aria-label="Cancel editing" title="Cancel">
                      ×
                    </button>
                  </>
                ) : (
                  <button className="icon-button edit-icon" type="button" onClick={() => startEdit(employee)} aria-label={`Edit ${employee.username}`} title="Edit">
                    ✎
                  </button>
                )}
                <button className="icon-button delete-icon" type="button" onClick={() => remove(employee)} disabled={protectedEmployee} aria-label={`Delete ${employee.username}`} title={protectedEmployee ? "This employee cannot be deleted" : "Delete"}>🗑</button>
              </td>
            </tr>
          );
        })}
      </AdminTable>
      {createOpen && (
        <Modal title="Add employee" onClose={() => setCreateOpen(false)}>
          <form className="admin-form" onSubmit={createEmployee}>
            {renderEditor(createDraft, setCreateDraft, true)}
            <div className="form-actions">
              <button className="primary-button" disabled={busy}>{busy ? "Saving..." : "Save employee"}</button>
              <button className="secondary-button" type="button" onClick={() => setCreateOpen(false)}>Cancel</button>
            </div>
          </form>
        </Modal>
      )}
    </AdminCard>
  );
}
