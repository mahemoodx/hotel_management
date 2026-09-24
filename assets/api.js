// Shared Supabase REST helper for static HTML pages on Royal Pearl.
// Uses the publishable key — safe to ship to the browser. RLS enforces access.
window.RP_API = (function () {
  const URL = "https://blrkxhxtkqcogxispuxu.supabase.co";
  const KEY = "sb_publishable_GyjxSxtypOVlh8dYZ0RpEQ_ZJeRIzfU";
  const SESSION_KEY = "sb-blrkxhxtkqcogxispuxu-auth-token";

  function session() {
    try {
      const raw = localStorage.getItem(SESSION_KEY);
      if (!raw) return null;
      const s = JSON.parse(raw);
      if (s.expires_at && Date.now() > s.expires_at * 1000) return null;
      return s;
    } catch (_) { return null; }
  }
  function authHeader() {
    const s = session();
    return s && s.access_token ? "Bearer " + s.access_token : "Bearer " + KEY;
  }
  function headers(extra) {
    return Object.assign({
      "apikey": KEY,
      "Authorization": authHeader(),
      "Content-Type": "application/json",
    }, extra || {});
  }

  async function insert(table, row) {
    const r = await fetch(URL + "/rest/v1/" + table, {
      method: "POST",
      headers: headers({ "Prefer": "return=representation" }),
      body: JSON.stringify(row),
    });
    if (!r.ok) throw new Error((await r.text()) || ("Insert failed " + r.status));
    return r.json();
  }
  async function select(table, query) {
    const q = query ? ("?" + query) : "";
    const r = await fetch(URL + "/rest/v1/" + table + q, { headers: headers() });
    if (!r.ok) throw new Error((await r.text()) || ("Select failed " + r.status));
    return r.json();
  }
  async function update(table, id, patch) {
    const r = await fetch(URL + "/rest/v1/" + table + "?id=eq." + id, {
      method: "PATCH",
      headers: headers({ "Prefer": "return=representation" }),
      body: JSON.stringify(patch),
    });
    if (!r.ok) throw new Error((await r.text()) || ("Update failed " + r.status));
    return r.json();
  }
  async function remove(table, id) {
    const r = await fetch(URL + "/rest/v1/" + table + "?id=eq." + id, {
      method: "DELETE", headers: headers(),
    });
    if (!r.ok) throw new Error((await r.text()) || ("Delete failed " + r.status));
    return true;
  }
  async function signIn(email, password) {
    const r = await fetch(URL + "/auth/v1/token?grant_type=password", {
      method: "POST",
      headers: { "apikey": KEY, "Content-Type": "application/json" },
      body: JSON.stringify({ email, password }),
    });
    const data = await r.json();
    if (!r.ok) throw new Error(data.error_description || data.msg || "Login failed");
    // Store in the same shape supabase-js uses.
    localStorage.setItem(SESSION_KEY, JSON.stringify({
      access_token: data.access_token,
      refresh_token: data.refresh_token,
      expires_at: data.expires_at,
      token_type: data.token_type,
      user: data.user,
    }));
    return data;
  }
  async function signUp(email, password) {
    const r = await fetch(URL + "/auth/v1/signup", {
      method: "POST",
      headers: { "apikey": KEY, "Content-Type": "application/json" },
      body: JSON.stringify({ email, password }),
    });
    const data = await r.json();
    if (!r.ok) throw new Error(data.error_description || data.msg || data.error || "Signup failed");
    if (data.access_token) {
      localStorage.setItem(SESSION_KEY, JSON.stringify({
        access_token: data.access_token,
        refresh_token: data.refresh_token,
        expires_at: data.expires_at,
        token_type: data.token_type,
        user: data.user,
      }));
    }
    return data;
  }
  async function claimAdmin() {
    const s = session();
    if (!s || !s.access_token) throw new Error("Not signed in");
    const r = await fetch(URL + "/functions/v1/claim-admin", {
      method: "POST",
      headers: { "Authorization": "Bearer " + s.access_token, "Content-Type": "application/json" },
    });
    const data = await r.json();
    if (!r.ok) throw new Error(data.error || "Could not grant admin");
    return data;
  }
  async function signOut() {
    const s = session();
    if (s) {
      await fetch(URL + "/auth/v1/logout", {
        method: "POST",
        headers: { "apikey": KEY, "Authorization": "Bearer " + s.access_token },
      }).catch(() => {});
    }
    localStorage.removeItem(SESSION_KEY);
  }
  async function isAdmin() {
    const s = session();
    if (!s || !s.user) return false;
    const r = await fetch(URL + "/rest/v1/rpc/has_role", {
      method: "POST",
      headers: headers(),
      body: JSON.stringify({ _user_id: s.user.id, _role: "admin" }),
    });
    if (!r.ok) return false;
    return (await r.json()) === true;
  }

  async function sendPasswordReset(email, redirectTo) {
    const r = await fetch(URL + "/auth/v1/recover", {
      method: "POST",
      headers: { "apikey": KEY, "Content-Type": "application/json" },
      body: JSON.stringify({ email, redirect_to: redirectTo || (location.origin + "/admin.html") }),
    });
    if (!r.ok) {
      const data = await r.json().catch(() => ({}));
      throw new Error(data.error_description || data.msg || "Could not send reset email");
    }
    return true;
  }
  async function updatePassword(newPassword, accessToken) {
    const r = await fetch(URL + "/auth/v1/user", {
      method: "PUT",
      headers: { "apikey": KEY, "Authorization": "Bearer " + accessToken, "Content-Type": "application/json" },
      body: JSON.stringify({ password: newPassword }),
    });
    const data = await r.json();
    if (!r.ok) throw new Error(data.error_description || data.msg || "Could not update password");
    return data;
  }
  function setSessionFromTokens(access_token, refresh_token, expires_in) {
    const expires_at = Math.floor(Date.now() / 1000) + (Number(expires_in) || 3600);
    localStorage.setItem(SESSION_KEY, JSON.stringify({
      access_token, refresh_token, expires_at, token_type: "bearer", user: null,
    }));
  }

  return { session, insert, select, update, remove, signIn, signUp, signOut, isAdmin, claimAdmin, sendPasswordReset, updatePassword, setSessionFromTokens };
})();
