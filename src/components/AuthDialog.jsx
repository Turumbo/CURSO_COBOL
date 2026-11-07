import { useState } from "react";
import { api } from "../services/api.js";

export default function AuthDialog({
  open,
  onClose,
  onAuthenticated,
  onToast,
}) {
  const [mode, setMode] = useState("login");
  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [phone, setPhone] = useState("");
  const [password, setPassword] = useState("");
  const [busy, setBusy] = useState(false);

  if (!open) return null;

  async function submit(event) {
    event.preventDefault();
    setBusy(true);
    try {
      const session =
        mode === "login"
          ? await api.login({ email, password })
          : await api.register({ name, email, password, phone });
      localStorage.setItem("bora-session-token", session.token);
      localStorage.setItem("bora-user", JSON.stringify(session.user));
      onAuthenticated(session.user);
      onClose();
      onToast(`Boas-vindas, ${session.user.name}!`);
    } catch (error) {
      onToast(error.message);
    } finally {
      setBusy(false);
    }
  }

  return (
    <div
      className="overlay auth-overlay"
      onMouseDown={(event) => event.target === event.currentTarget && onClose()}
    >
      <section
        className="auth-dialog"
        role="dialog"
        aria-modal="true"
        aria-labelledby="auth-heading"
      >
        <button
          className="icon-button auth-close"
          onClick={onClose}
          aria-label="Fechar"
        >
          ×
        </button>
        <span className="brand-mark auth-brand">B</span>
        <span className="section-kicker">SUA PRÓXIMA REFEIÇÃO COMEÇA AQUI</span>
        <h2 id="auth-heading">
          {mode === "login"
            ? "Que bom ter você de volta."
            : "Crie sua conta Bora!"}
        </h2>
        <p>
          Entre para pedir, conversar com o restaurante e acompanhar sua
          entrega.
        </p>
        <form className="auth-form" onSubmit={submit}>
          {mode === "register" && (
            <label className="field-label">
              Seu nome
              <input
                autoComplete="name"
                value={name}
                onChange={(event) => setName(event.target.value)}
                minLength={2}
                maxLength={100}
                required
                placeholder="Como podemos chamar você?"
              />
            </label>
          )}
          {mode === "register" && (
            <label className="field-label">
              Celular (opcional)
              <input
                type="tel"
                autoComplete="tel"
                value={phone}
                onChange={(event) => setPhone(event.target.value)}
                maxLength={30}
                placeholder="(11) 99999-9999"
              />
            </label>
          )}
          <label className="field-label">
            E-mail
            <input
              type="email"
              autoComplete="email"
              value={email}
              onChange={(event) => setEmail(event.target.value)}
              maxLength={160}
              required
              placeholder="voce@email.com"
            />
          </label>
          <label className="field-label">
            Senha
            <input
              type="password"
              autoComplete={
                mode === "login" ? "current-password" : "new-password"
              }
              value={password}
              onChange={(event) => setPassword(event.target.value)}
              minLength={mode === "login" ? 1 : 8}
              maxLength={72}
              required
              placeholder={
                mode === "login" ? "Sua senha" : "No mínimo 8 caracteres"
              }
            />
          </label>
          <button className="primary-button auth-submit" disabled={busy}>
            {busy
              ? "Só um instante…"
              : mode === "login"
                ? "Entrar na minha conta"
                : "Criar conta"}
          </button>
        </form>
        <button
          className="auth-switch"
          onClick={() => setMode(mode === "login" ? "register" : "login")}
        >
          {mode === "login"
            ? "É sua primeira vez? Criar conta"
            : "Já tem conta? Entrar"}
        </button>
        <div className="demo-credentials">
          <strong>Ambiente de demonstração</strong>
          <span>Cliente: cliente@bora.demo · cliente123</span>
          <span>Restaurante: restaurante@bora.demo · restaurante123</span>
        </div>
      </section>
    </div>
  );
}
