import { useState } from "react";
import DepartmentAdmin from "../admin/DepartmentAdmin";
import EmployeeAdmin from "../admin/EmployeeAdmin";
import ItemAdmin from "../admin/ItemAdmin";
import ChecklistWorkspace from "./ChecklistWorkspace";
import BrandLogo from "./BrandLogo";

export default function AdminWorkspace({ user, onLogout }) {
  const [screen, setScreen] = useState(user.isAdmin ? "departments" : "items");
  const [error, setError] = useState("");

  function openScreen(nextScreen) {
    setError("");
    setScreen(nextScreen);
  }

  return (
    <div className="app-shell">
      <header className="topbar no-print">
        <div className="topbar-brand"><BrandLogo /><h1>Administration</h1></div>
        <div className="topbar-actions">
          <span className="role-pill">{user.employeeName || user.username}</span>
          <button className="text-button" onClick={onLogout}>Sign out</button>
        </div>
      </header>
      <main className="workspace">
        <nav className="admin-tabs no-print">
          {user.isAdmin && <>
            <button className={screen === "departments" ? "active" : ""} onClick={() => openScreen("departments")}>Departments</button>
            <button className={screen === "employees" ? "active" : ""} onClick={() => openScreen("employees")}>Employees</button>
          </>}
          <button className={screen === "items" ? "active" : ""} onClick={() => openScreen("items")}>Checklist items</button>
          <button className={screen === "specification" ? "active" : ""} onClick={() => openScreen("specification")}>Specification print</button>
        </nav>
        {error && <div className="error-banner">{error}</div>}
        {screen === "departments" && user.isAdmin && <DepartmentAdmin onError={setError} />}
        {screen === "employees" && user.isAdmin && <EmployeeAdmin onError={setError} />}
        {screen === "items" && <ItemAdmin onError={setError} />}
        {screen === "specification" && <ChecklistWorkspace user={user} onLogout={onLogout} embedded />}
        <p className="admin-footer no-print">Signed in as {user.username}</p>
      </main>
    </div>
  );
}
