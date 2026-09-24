(function () {
  const CSS = `
  .lm-overlay{position:fixed;inset:0;background:rgba(8,12,28,.75);backdrop-filter:blur(8px);-webkit-backdrop-filter:blur(8px);display:none;align-items:center;justify-content:center;z-index:9999;padding:20px;font-family:'Inter',system-ui,sans-serif}
  .lm-overlay.open{display:flex;animation:lmFadeIn .3s cubic-bezier(0.16,1,0.3,1)}
  .lm-box{background:#fff;width:100%;max-width:420px;border-radius:20px;box-shadow:0 25px 50px -12px rgba(0,0,0,.25),0 0 0 1px rgba(0,0,0,.05);overflow:hidden;position:relative;animation:lmSlideUp .4s cubic-bezier(0.16,1,0.3,1);transform-origin:center}
  @keyframes lmFadeIn{from{opacity:0}}@keyframes lmSlideUp{from{opacity:0;transform:translateY(24px) scale(.97)}}
  .lm-head{background:linear-gradient(135deg,#0b1737 0%,#1a2d52 50%,#0b1737 100%);color:#fff;padding:32px 28px 28px;text-align:center;position:relative;overflow:hidden}
  .lm-head::before{content:"";position:absolute;top:-50%;left:-50%;width:200%;height:200%;background:radial-gradient(circle,rgba(200,159,74,.08) 0%,transparent 60%);animation:lmShine 8s ease-in-out infinite}
  @keyframes lmShine{0%,100%{transform:translate(-10%,-10%)}50%{transform:translate(10%,10%)}}
  .lm-head>*{position:relative;z-index:1}
  .lm-close{position:absolute;top:14px;right:14px;background:rgba(255,255,255,.08);border:0;color:rgba(255,255,255,.7);font-size:20px;cursor:pointer;width:32px;height:32px;border-radius:50%;display:flex;align-items:center;justify-content:center;transition:all .2s;z-index:2}
  .lm-close:hover{background:rgba(255,255,255,.15);color:#fff;transform:rotate(90deg)}
  .lm-head h2{margin:0;font-size:26px;font-weight:600;letter-spacing:.3px;color:#f0c674;font-family:'Cormorant Garamond',Georgia,serif}
  .lm-head p{margin:8px 0 0;font-size:14px;color:rgba(255,255,255,.65);font-weight:400}
  .lm-body{padding:24px 28px 28px}
  .lm-social{display:flex;flex-direction:column;gap:12px;margin-bottom:20px}
  .lm-sbtn{display:flex;align-items:center;justify-content:center;gap:12px;padding:14px 18px;border:1.5px solid #e5e7eb;border-radius:12px;background:#fff;cursor:pointer;font-size:14.5px;font-weight:500;color:#374151;transition:all .2s;position:relative;overflow:hidden}
  .lm-sbtn::before{content:"";position:absolute;inset:0;background:linear-gradient(135deg,rgba(0,0,0,.02) 0%,transparent 100%);opacity:0;transition:opacity .2s}
  .lm-sbtn:hover{border-color:#0b1737;box-shadow:0 4px 12px rgba(0,0,0,.08);transform:translateY(-1px)}
  .lm-sbtn:hover::before{opacity:1}
  .lm-sbtn:active{transform:translateY(0)}
  .lm-sbtn.google{background:linear-gradient(135deg,#fff 0%,#f8f9fa 100%)}
  .lm-sbtn.google i{font-size:18px;background:conic-gradient(from 180deg,#ea4335,#ffbb00,#34a853,#4285f4,#ea4335);-webkit-background-clip:text;-webkit-text-fill-color:transparent;background-clip:text}
  .lm-sbtn .lm-spinner{width:18px;height:18px;border:2px solid #e5e7eb;border-top-color:#0b1737;border-radius:50%;animation:lmSpin .8s linear infinite;display:none}
  .lm-sbtn.loading .lm-spinner{display:block}
  .lm-sbtn.loading span,.lm-sbtn.loading i{display:none}
  @keyframes lmSpin{to{transform:rotate(360deg)}}
  .lm-divider{display:flex;align-items:center;gap:14px;color:#9ca3af;font-size:12.5px;font-weight:500;margin:18px 0 16px}
  .lm-divider::before,.lm-divider::after{content:"";flex:1;height:1px;background:linear-gradient(90deg,transparent,#e5e7eb,#transparent)}
  .lm-field{margin-bottom:16px;position:relative}
  .lm-field label{display:block;font-size:13px;color:#374151;margin-bottom:7px;font-weight:500}
  .lm-field input{width:100%;padding:13px 15px;border:1.5px solid #e5e7eb;border-radius:10px;font-size:14.5px;outline:none;transition:all .2s;box-sizing:border-box;background:#fafbfc;color:#1f2937}
  .lm-field input::placeholder{color:#9ca3af}
  .lm-field input:focus{border-color:#c89f4a;box-shadow:0 0 0 3px rgba(200,159,74,.12),0 2px 8px rgba(0,0,0,.04);background:#fff;transform:translateY(-1px)}
  .lm-field input:hover:not(:focus){border-color:#d1d5db}
  .lm-field .lm-input-icon{position:absolute;right:14px;top:38px;color:#9ca3af;font-size:16px;pointer-events:none;transition:color .2s}
  .lm-field input:focus+.lm-input-icon{color:#c89f4a}
  .lm-field-error{font-size:12px;color:#dc2626;margin-top:6px;display:none}
  .lm-field input:invalid:not(:placeholder-shown):not(:focus){border-color:#dc2626}
  .lm-row{display:flex;justify-content:space-between;align-items:center;font-size:13.5px;margin:6px 0 20px;flex-wrap:wrap;gap:8px}
  .lm-row label{display:flex;align-items:center;gap:8px;color:#4b5563;cursor:pointer;user-select:none}
  .lm-row input[type="checkbox"]{width:16px;height:16px;accent-color:#c89f4a;cursor:pointer}
  .lm-row a{color:#0b1737;text-decoration:none;font-weight:600;cursor:pointer;position:relative}
  .lm-row a::after{content:"";position:absolute;bottom:-2px;left:0;width:0;height:1.5px;background:#c89f4a;transition:width .2s}
  .lm-row a:hover::after{width:100%}
  .lm-submit{width:100%;padding:15px;background:linear-gradient(135deg,#c89f4a,#b8913f);color:#fff;border:0;border-radius:12px;font-size:15px;font-weight:600;letter-spacing:.3px;cursor:pointer;transition:all .25s;position:relative;overflow:hidden;box-shadow:0 4px 14px rgba(200,159,74,.35)}
  .lm-submit::before{content:"";position:absolute;inset:0;background:linear-gradient(135deg,rgba(255,255,255,.15) 0%,transparent 50%);opacity:0;transition:opacity .2s}
  .lm-submit:hover{filter:brightness(1.08);transform:translateY(-2px);box-shadow:0 6px 20px rgba(200,159,74,.4)}
  .lm-submit:hover::before{opacity:1}
  .lm-submit:active{transform:translateY(0)}
  .lm-submit:disabled{opacity:.65;cursor:wait;transform:none;box-shadow:none}
  .lm-submit .lm-btn-spinner{width:20px;height:20px;border:2px solid rgba(255,255,255,.3);border-top-color:#fff;border-radius:50%;animation:lmSpin .8s linear infinite;display:none;margin:0 auto}
  .lm-submit:disabled .lm-btn-spinner{display:block}
  .lm-submit:disabled .lm-btn-text{opacity:0}
  .lm-foot{text-align:center;font-size:14px;color:#6b7280;margin-top:20px}
  .lm-foot a{color:#0b1737;font-weight:600;text-decoration:none;cursor:pointer;position:relative}
  .lm-foot a::after{content:"";position:absolute;bottom:-1px;left:0;width:0;height:1.5px;background:#c89f4a;transition:width .2s}
  .lm-foot a:hover::after{width:100%}
  .lm-admin{display:block;text-align:center;margin-top:16px;font-size:12.5px;color:#9ca3af;text-decoration:none;transition:color .2s}
  .lm-admin:hover{color:#0b1737}
  .lm-admin i{margin-right:4px}
  .lm-view{display:none;animation:lmViewFade .3s ease}
  .lm-view.active{display:block}
  @keyframes lmViewFade{from{opacity:0;transform:translateX(12px)}to{opacity:1;transform:translateX(0)}}
  .lm-msg{margin:0 0 16px;padding:13px 15px;border-radius:10px;font-size:13.5px;display:none;align-items:flex-start;gap:10px;animation:lmShake .4s ease}
  @keyframes lmShake{0%,100%{transform:translateX(0)}20%,60%{transform:translateX(-4px)}40%,80%{transform:translateX(4px)}}
  .lm-msg.show{display:flex}
  .lm-msg.err{background:linear-gradient(135deg,#fef2f2,#fee2e2);color:#991b1b;border:1px solid rgba(220,38,38,.15)}
  .lm-msg.ok{background:linear-gradient(135deg,#ecfdf5,#d1fae5);color:#065f46;border:1px solid rgba(5,150,105,.15)}
  .lm-msg i{font-size:16px;margin-top:1px}
  .lm-userpill{position:fixed;top:16px;right:16px;z-index:9998;background:linear-gradient(135deg,#0b1737,#1a2d52);color:#fff;padding:10px 16px;border-radius:50px;font-family:'Inter',system-ui,sans-serif;font-size:13px;display:none;align-items:center;gap:10px;box-shadow:0 8px 32px rgba(11,23,55,.35),0 0 0 1px rgba(255,255,255,.08);max-width:calc(100% - 32px);backdrop-filter:blur(8px);-webkit-backdrop-filter:blur(8px);animation:lmPillPop .3s cubic-bezier(0.34,1.56,.64,1)}
  @keyframes lmPillPop{from{opacity:0;transform:scale(.9) translateY(-8px)}}
  .lm-userpill.show{display:inline-flex}
  .lm-userpill i{color:#c89f4a;font-size:15px}
  .lm-userpill b{color:#f0c674;font-weight:600;max-width:160px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap}
  .lm-userpill button{background:rgba(255,255,255,.1);border:1px solid rgba(255,255,255,.2);color:#fff;border-radius:20px;padding:6px 14px;font-size:12px;cursor:pointer;transition:all .2s}
  .lm-userpill button:hover{background:rgba(255,255,255,.2);border-color:rgba(255,255,255,.35)}
  @media (max-width: 640px){
    .lm-overlay{padding:12px;align-items:flex-start;padding-top:20px}
    .lm-box{max-width:100%;border-radius:16px;margin-top:8px}
    .lm-head{padding:26px 20px 24px}
    .lm-head h2{font-size:22px}
    .lm-head p{font-size:13px}
    .lm-body{padding:20px 20px 24px}
    .lm-sbtn{padding:13px 16px;font-size:14px}
    .lm-submit{padding:14px;font-size:14.5px}
    .lm-userpill{top:10px;right:10px;font-size:12px;padding:8px 12px}
    .lm-userpill b{max-width:110px}
  }
  @media (max-width: 380px){
    .lm-head h2{font-size:20px}
    .lm-row,.lm-foot{font-size:12.5px}
    .lm-field{margin-bottom:14px}
  }
  `;

  const HTML = `
  <div class="lm-overlay" id="lmOverlay" role="dialog" aria-modal="true" aria-labelledby="lmTitle">
    <div class="lm-box">
      <div class="lm-head">
        <button class="lm-close" id="lmClose" aria-label="Close"><i class="fa-solid fa-xmark"></i></button>
        <h2 id="lmTitle">Welcome Back</h2>
        <p>Sign in to manage your stay at Royal Pearl</p>
      </div>
      <div class="lm-body">
        <div id="lmMsg" class="lm-msg"></div>
        <!-- LOGIN VIEW -->
        <div class="lm-view active" data-view="login">
          <div class="lm-social">
            <button type="button" class="lm-sbtn google" data-social="google">
              <i class="fa-brands fa-google"></i>
              <span>Continue with Google</span>
              <div class="lm-spinner"></div>
            </button>
          </div>
          <div class="lm-divider">or sign in with email</div>
          <form id="lmLoginForm" novalidate>
            <div class="lm-field">
              <label>Email Address</label>
              <input type="email" name="email" placeholder="you@example.com" required autocomplete="email">
            </div>
            <div class="lm-field">
              <label>Password</label>
              <input type="password" name="password" placeholder="Enter your password" required minlength="6" autocomplete="current-password">
            </div>
            <div class="lm-row">
              <label><input type="checkbox" name="remember"> Remember Me</label>
              <a data-view-switch="forgot">Forgot Password?</a>
            </div>
            <button type="submit" class="lm-submit">
              <span class="lm-btn-text">Sign In</span>
              <div class="lm-btn-spinner"></div>
            </button>
          </form>
          <div class="lm-foot">Don't have an account? <a data-view-switch="register">Register Here</a></div>
          <a class="lm-admin" href="/admin-login"><i class="fa-solid fa-shield-halved"></i> Admin Login</a>
        </div>
        <!-- REGISTER VIEW -->
        <div class="lm-view" data-view="register">
          <div class="lm-social">
            <button type="button" class="lm-sbtn google" data-social="google">
              <i class="fa-brands fa-google"></i>
              <span>Continue with Google</span>
              <div class="lm-spinner"></div>
            </button>
          </div>
          <div class="lm-divider">or create with email</div>
          <form id="lmRegisterForm" novalidate>
            <div class="lm-field">
              <label>Full Name</label>
              <input type="text" name="name" placeholder="John Doe" required autocomplete="name">
            </div>
            <div class="lm-field">
              <label>Email Address</label>
              <input type="email" name="email" placeholder="you@example.com" required autocomplete="email">
            </div>
            <div class="lm-field">
              <label>Password</label>
              <input type="password" name="password" placeholder="At least 6 characters" required minlength="6" autocomplete="new-password">
            </div>
            <button type="submit" class="lm-submit">
              <span class="lm-btn-text">Create Account</span>
              <div class="lm-btn-spinner"></div>
            </button>
          </form>
          <div class="lm-foot">Already have an account? <a data-view-switch="login">Login Here</a></div>
        </div>
        <!-- FORGOT VIEW -->
        <div class="lm-view" data-view="forgot">
          <form id="lmForgotForm" novalidate>
            <p style="font-size:14px;color:#6b7280;margin:0 0 18px;line-height:1.5">Enter your registered email and we'll send you a password reset link.</p>
            <div class="lm-field">
              <label>Email Address</label>
              <input type="email" name="email" placeholder="you@example.com" required>
            </div>
            <button type="submit" class="lm-submit">
              <span class="lm-btn-text">Send Reset Link</span>
              <div class="lm-btn-spinner"></div>
            </button>
          </form>
          <div class="lm-foot"><a data-view-switch="login"><i class="fa-solid fa-arrow-left"></i> Back to Login</a></div>
        </div>
      </div>
    </div>
  </div>
  <div class="lm-userpill" id="lmUserPill">
    <i class="fa-solid fa-circle-user"></i>
    <span>Signed in as <b id="lmUserEmail"></b></span>
    <button type="button" id="lmSignOut">Sign out</button>
  </div>`;

  const SUPA_URL = "https://blrkxhxtkqcogxispuxu.supabase.co";

  function init() {
    if (document.getElementById('lmOverlay')) return;
    const style = document.createElement('style');
    style.textContent = CSS;
    document.head.appendChild(style);
    // Inject site-wide responsive helpers
    if (!document.getElementById('rp-responsive-css')) {
      const link = document.createElement('link');
      link.id = 'rp-responsive-css';
      link.rel = 'stylesheet';
      link.href = 'assets/responsive.css';
      document.head.appendChild(link);
    }
    const wrap = document.createElement('div');
    wrap.innerHTML = HTML;
    while (wrap.firstElementChild) document.body.appendChild(wrap.firstElementChild);

    const overlay = document.getElementById('lmOverlay');
    const msgEl = document.getElementById('lmMsg');
    const pill = document.getElementById('lmUserPill');
    const pillEmail = document.getElementById('lmUserEmail');

    function showMsg(text, kind) {
      msgEl.className = 'lm-msg ' + (kind || 'err');
      msgEl.textContent = text;
    }
    function clearMsg() { msgEl.className = 'lm-msg'; msgEl.textContent = ''; }

    const close = () => overlay.classList.remove('open');
    const open = () => { clearMsg(); overlay.classList.add('open'); switchView('login'); };
    document.getElementById('lmClose').addEventListener('click', close);
    overlay.addEventListener('click', (e) => { if (e.target === overlay) close(); });
    document.addEventListener('keydown', (e) => { if (e.key === 'Escape') close(); });

    function switchView(name) {
      clearMsg();
      overlay.querySelectorAll('.lm-view').forEach(v => v.classList.toggle('active', v.dataset.view === name));
      const titles = { login: 'Welcome Back', register: 'Create Account', forgot: 'Reset Password' };
      document.getElementById('lmTitle').textContent = titles[name] || 'Welcome';
    }

    function refreshPill() {
      const s = window.RP_API && window.RP_API.session();
      if (s && s.user && s.user.email) {
        pillEmail.textContent = s.user.email;
        pill.classList.add('show');
      } else {
        pill.classList.remove('show');
      }
    }

    function greet(email) {
      try { alert("✅ Welcome to Royal Pearl, " + email + "!"); } catch (_) {}
    }

    overlay.addEventListener('click', (e) => {
      const sw = e.target.closest('[data-view-switch]');
      if (sw) { e.preventDefault(); switchView(sw.dataset.viewSwitch); return; }
      const sc = e.target.closest('[data-social]');
      if (sc && sc.dataset.social === 'google') {
        e.preventDefault();
        // Use Supabase's direct OAuth URL instead of custom /auth/google route
        const supabaseUrl = "https://blrkxhxtkqcogxispuxu.supabase.co";
        const redirectTo = encodeURIComponent(location.origin + location.pathname);
        const googleAuthUrl = `${supabaseUrl}/auth/v1/authorize?provider=google&redirect_to=${redirectTo}`;
        window.location.href = googleAuthUrl;
      }
    });

    // Login form
    document.getElementById('lmLoginForm').addEventListener('submit', async (e) => {
      e.preventDefault();
      const f = e.target;
      if (!f.checkValidity()) { f.reportValidity(); return; }
      const btn = f.querySelector('.lm-submit');
      btn.disabled = true; btn.textContent = 'Signing in…';
      clearMsg();
      const email = f.email.value.trim();
      const password = f.password.value;
      try {
        await window.RP_API.signIn(email, password);
        showMsg('Logged in successfully.', 'ok');
        refreshPill();
        setTimeout(() => { close(); greet(email); }, 300);
      } catch (err) {
        showMsg(err.message || 'Login failed.');
      } finally {
        btn.disabled = false; btn.textContent = 'Login';
      }
    });

    // Register form — auto-confirm is enabled, so signUp logs them in.
    document.getElementById('lmRegisterForm').addEventListener('submit', async (e) => {
      e.preventDefault();
      const f = e.target;
      if (!f.checkValidity()) { f.reportValidity(); return; }
      const btn = f.querySelector('.lm-submit');
      btn.disabled = true; btn.textContent = 'Creating account…';
      clearMsg();
      const email = f.email.value.trim();
      const password = f.password.value;
      try {
        await window.RP_API.signUp(email, password);
        // If session wasn't returned (rare), sign in immediately.
        if (!window.RP_API.session()) {
          await window.RP_API.signIn(email, password);
        }
        showMsg('Account created. Welcome!', 'ok');
        refreshPill();
        setTimeout(() => { close(); greet(email); }, 300);
      } catch (err) {
        // If they already exist, try logging in.
        if (/registered|exists/i.test(err.message || '')) {
          try {
            await window.RP_API.signIn(email, password);
            refreshPill();
            close(); greet(email);
            return;
          } catch (e2) {
            showMsg(e2.message || 'Could not sign in.');
          }
        } else {
          showMsg(err.message || 'Signup failed.');
        }
      } finally {
        btn.disabled = false; btn.textContent = 'Create Account';
      }
    });

    // Forgot password
    document.getElementById('lmForgotForm').addEventListener('submit', async (e) => {
      e.preventDefault();
      const f = e.target;
      if (!f.checkValidity()) { f.reportValidity(); return; }
      const btn = f.querySelector('.lm-submit');
      btn.disabled = true; btn.textContent = 'Sending…';
      clearMsg();
      try {
        await window.RP_API.sendPasswordReset(f.email.value.trim(), location.origin + '/home.html');
        showMsg('Reset link sent. Check your inbox.', 'ok');
      } catch (err) {
        showMsg(err.message || 'Could not send reset link.');
      } finally {
        btn.disabled = false; btn.textContent = 'Send Reset Link';
      }
    });

    // Sign out
    document.getElementById('lmSignOut').addEventListener('click', async () => {
      try { await window.RP_API.signOut(); } catch (_) {}
      refreshPill();
      alert('Signed out.');
    });

    // Open via [data-login]
    document.addEventListener('click', (e) => {
      const t = e.target.closest('[data-login]');
      if (t) { e.preventDefault(); open(); }
    });
    window.openLoginModal = open;

    // Handle Google OAuth redirect (tokens come back in URL query params or hash)
    function handleOAuthCallback() {
      const hashParams = location.hash && location.hash.includes('access_token=') 
        ? new URLSearchParams(location.hash.slice(1)) 
        : null;
      const queryParams = location.search && location.search.includes('access_token=')
        ? new URLSearchParams(location.search.slice(1))
        : null;
      
      const params = hashParams || queryParams;
      if (params) {
        const at = params.get('access_token');
        const rt = params.get('refresh_token');
        const ex = params.get('expires_in');
        if (at && rt) {
          window.RP_API.setSessionFromTokens(at, rt, ex);
          // Fetch user info to populate the pill
          fetch(SUPA_URL + '/auth/v1/user', {
            headers: { 'apikey': 'sb_publishable_GyjxSxtypOVlh8dYZ0RpEQ_ZJeRIzfU', 'Authorization': 'Bearer ' + at }
          }).then(r => r.json()).then(u => {
            const raw = localStorage.getItem('sb-blrkxhxtkqcogxispuxu-auth-token');
            if (raw) {
              const s = JSON.parse(raw); s.user = u;
              localStorage.setItem('sb-blrkxhxtkqcogxispuxu-auth-token', JSON.stringify(s));
            }
            refreshPill();
            if (u && u.email) greet(u.email);
          }).catch(() => refreshPill());
          history.replaceState(null, '', location.pathname);
        }
      }
    }
    handleOAuthCallback();

    refreshPill();
  }

  if (document.readyState === 'loading') document.addEventListener('DOMContentLoaded', init);
  else init();
})();
