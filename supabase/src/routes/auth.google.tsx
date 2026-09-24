import { createFileRoute, useSearch } from "@tanstack/react-router";
import { useEffect, useState } from "react";
import { lovable } from "@/integrations/lovable/index";
import { supabase } from "@/integrations/supabase/client";

type Search = { next?: string };

export const Route = createFileRoute("/auth/google")({
  ssr: false,
  validateSearch: (s: Record<string, unknown>): Search => ({
    next: typeof s.next === "string" ? s.next : "/home.html",
  }),
  component: GoogleAuthPage,
});

function GoogleAuthPage() {
  const { next } = useSearch({ from: "/auth/google" });
  const [status, setStatus] = useState<"loading" | "error">("loading");
  const [message, setMessage] = useState("Connecting to Google…");

  useEffect(() => {
    let cancelled = false;
    (async () => {
      // Check if tokens came back in the URL hash (OAuth redirect)
      const hashParams = new URLSearchParams(window.location.hash.slice(1));
      const accessToken = hashParams.get("access_token");
      const refreshToken = hashParams.get("refresh_token");
      const expiresIn = hashParams.get("expires_in");

      // If tokens in URL, set the session and redirect with tokens in hash for static HTML pages
      if (accessToken && refreshToken) {
        await supabase.auth.setSession({
          access_token: accessToken,
          refresh_token: refreshToken,
        });
        
        // For static HTML pages (ending in .html), preserve tokens in URL hash
        const nextUrl = next || "/home.html";
        if (nextUrl.endsWith(".html")) {
          const tokenHash = `access_token=${encodeURIComponent(accessToken)}&refresh_token=${encodeURIComponent(refreshToken)}&expires_in=${encodeURIComponent(expiresIn || "3600")}`;
          window.location.replace(nextUrl + "#" + tokenHash);
          return;
        }
        
        // For React routes, just redirect
        window.location.replace(nextUrl);
        return;
      }

      // Check if already signed in
      const { data: existing } = await supabase.auth.getSession();
      if (existing.session) {
        const nextUrl = next || "/home.html";
        // For static HTML pages, we need to rebuild the hash from session
        if (nextUrl.endsWith(".html")) {
          const session = existing.session;
          const tokenHash = `access_token=${encodeURIComponent(session.access_token)}&refresh_token=${encodeURIComponent(session.refresh_token)}&expires_in=${encodeURIComponent(String(Math.floor((session.expires_at! - Date.now() / 1000))))}`;
          window.location.replace(nextUrl + "#" + tokenHash);
          return;
        }
        window.location.replace(nextUrl);
        return;
      }

      // Start OAuth flow
      const result = await lovable.auth.signInWithOAuth("google", {
        redirect_uri: window.location.origin + "/auth/google",
        extraParams: { prompt: "select_account" },
      });
      if (cancelled) return;
      if (result.error) {
        setStatus("error");
        setMessage(result.error.message || "Google sign-in failed.");
        return;
      }
      if (result.redirected) return; // browser is navigating away
      // Session is set — go back to the page that asked us to sign in.
      const nextUrl = next || "/home.html";
      if (nextUrl.endsWith(".html")) {
        const { data: sessionData } = await supabase.auth.getSession();
        if (sessionData.session) {
          const session = sessionData.session;
          const tokenHash = `access_token=${encodeURIComponent(session.access_token)}&refresh_token=${encodeURIComponent(session.refresh_token)}&expires_in=${encodeURIComponent(String(Math.floor((session.expires_at! - Date.now() / 1000))))}`;
          window.location.replace(nextUrl + "#" + tokenHash);
          return;
        }
      }
      window.location.replace(nextUrl);
    })();
    return () => {
      cancelled = true;
    };
  }, [next]);

  return (
    <div
      style={{
        minHeight: "100vh",
        display: "flex",
        alignItems: "center",
        justifyContent: "center",
        background:
          "radial-gradient(circle at 20% 20%, #152a5c, #070b1d 70%)",
        color: "#fff",
        fontFamily: "Arial, sans-serif",
        padding: "24px",
      }}
    >
      <div
        style={{
          background: "#fff",
          color: "#222",
          borderRadius: 16,
          padding: "32px 36px",
          maxWidth: 420,
          width: "100%",
          textAlign: "center",
          boxShadow: "0 30px 80px rgba(0,0,0,.5)",
        }}
      >
        <h2 style={{ margin: 0, color: "#0b1737", fontSize: 24 }}>
          {status === "loading" ? "Signing you in…" : "Sign-in failed"}
        </h2>
        <p style={{ marginTop: 12, color: "#555", fontSize: 14 }}>{message}</p>
        {status === "error" && (
          <a
            href={next || "/home.html"}
            style={{
              display: "inline-block",
              marginTop: 16,
              background: "linear-gradient(135deg,#c89f4a,#a07a2c)",
              color: "#fff",
              padding: "10px 22px",
              borderRadius: 10,
              fontWeight: 700,
              textDecoration: "none",
            }}
          >
            Back to site
          </a>
        )}
      </div>
    </div>
  );
}
