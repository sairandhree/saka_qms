import { useState } from "react";
import { tokenKey, userKey } from "../api";
import BrandLogo from "./BrandLogo";

export default function Login({ onLogin }) {
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
        <BrandLogo />
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
