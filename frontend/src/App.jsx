import { useEffect, useState } from "react";
import { setUnauthorizedHandler, tokenKey, userKey } from "./api";
import Login from "./components/Login";
import AdminWorkspace from "./components/AdminWorkspace";
import ChecklistWorkspace from "./components/ChecklistWorkspace";

function App() {
  const [user, setUser] = useState(() => {
    const stored = localStorage.getItem(userKey);
    if (!stored) return null;
    try {
      return JSON.parse(stored);
    } catch {
      localStorage.removeItem(userKey);
      return null;
    }
  });

  useEffect(() => {
    setUnauthorizedHandler(() => setUser(null));
    return () => setUnauthorizedHandler(null);
  }, []);

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
