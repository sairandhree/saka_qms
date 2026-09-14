import { useState } from "react";
import { tokenKey, userKey } from "./api";
import Login from "./components/Login";
import AdminWorkspace from "./components/AdminWorkspace";
import ChecklistWorkspace from "./components/ChecklistWorkspace";

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

  if (!user) return <Login onLogin={setUser} />;

  return user.isAdmin || user.isDepartmentHead
    ? <AdminWorkspace user={user} onLogout={logout} />
    : <ChecklistWorkspace user={user} onLogout={logout} />;
}

export default App;
