import { useEffect, useState } from "react";
import { api } from "../services/api.js";

export default function ProfilePage({ user, onUpdated, onToast }) {
  const [form, setForm] = useState({
    name: user.name ?? "",
    email: user.email ?? "",
    phone: user.phone ?? "",
    deliveryAddress: user.deliveryAddress ?? "",
  });
  const [saving, setSaving] = useState(false);
  const [cards, setCards] = useState([]);
  const [cardForm, setCardForm] = useState({ brand: "VISA", lastFour: "" });
  const [cardBusy, setCardBusy] = useState(false);
  const isCustomer = user.role === "CUSTOMER";

  useEffect(() => {
    if (!isCustomer) return;
    api.getMyDemoCards()
      .then(setCards)
      .catch((error) => onToast(`Não foi possível carregar cartões demonstrativos: ${error.message}`));
  }, [isCustomer, onToast]);

  function updateField(event) {
    setForm((current) => ({ ...current, [event.target.name]: event.target.value }));
  }

  async function addCard(event) {
    event.preventDefault();
    setCardBusy(true);
    try {
      const card = await api.addMyDemoCard(cardForm);
      setCards((current) => [card, ...current]);
      setCardForm((current) => ({ ...current, lastFour: "" }));
      onToast("Cartão demonstrativo cadastrado. Ele não pode ser usado para cobrança.");
    } catch (error) {
      onToast(`Não foi possível cadastrar o cartão: ${error.message}`);
    } finally {
      setCardBusy(false);
    }
  }

  async function removeCard(id) {
    try {
      await api.deleteMyDemoCard(id);
      setCards((current) => current.filter((card) => card.id !== id));
      onToast("Cartão demonstrativo removido.");
    } catch (error) {
      onToast(`Não foi possível remover o cartão: ${error.message}`);
    }
  }

  async function save(event) {
    event.preventDefault();
    setSaving(true);
    try {
      const account = await api.updateMyAccount(form);
      onUpdated(account);
      onToast("Seu perfil foi atualizado.");
    } catch (error) {
      onToast(`Não foi possível salvar o perfil: ${error.message}`);
    } finally {
      setSaving(false);
    }
  }

  return (
    <main className="page-shell profile-page">
      <div className="page-heading">
        <div>
          <span className="section-kicker">SUA CONTA</span>
          <h1>Meu perfil</h1>
          <p>Revise seus dados de contato e o endereço usado como sugestão no checkout.</p>
        </div>
      </div>
      <div className="profile-layout">
        <form className="profile-card" onSubmit={save}>
          <h2>Dados pessoais</h2>
          <label className="field-label">Nome completo
            <input name="name" autoComplete="name" maxLength={100} value={form.name} onChange={updateField} required />
          </label>
          <label className="field-label">E-mail
            <input name="email" type="email" autoComplete="email" maxLength={160} value={form.email} onChange={updateField} required />
          </label>
          <label className="field-label">Celular
            <input name="phone" type="tel" autoComplete="tel" maxLength={30} value={form.phone} onChange={updateField} placeholder="(11) 99999-9999" />
          </label>
          <label className="field-label">Endereço sugerido para entrega
            <textarea name="deliveryAddress" autoComplete="street-address" maxLength={240} value={form.deliveryAddress} onChange={updateField} placeholder="Rua, número, complemento, bairro e cidade" />
          </label>
          <button className="primary-button profile-save" disabled={saving}>{saving ? "Salvando…" : "Salvar alterações"}</button>
        </form>
        <aside className="profile-card profile-security">
          <span className="profile-security-icon">⌑</span>
          <h2>Privacidade e pagamentos</h2>
          <p>Este ambiente aceita apenas cartões demonstrativos com bandeira e quatro últimos dígitos. Não informe número completo ou CVV: não há tokenização nem cobrança real.</p>
          <div className="profile-security-note">Perfil protegido por sessão autenticada. Não compartilhe seu acesso.</div>
        </aside>
      </div>
      {isCustomer && (
        <section className="profile-card profile-cards">
          <div>
            <h2>Cartões demonstrativos</h2>
            <p className="payment-demo-note">Apenas referência visual para testes; esses cartões não são salvos em um provedor e não podem pagar pedidos.</p>
          </div>
          <form className="demo-card-form" onSubmit={addCard}>
            <label className="field-label">Bandeira
              <select value={cardForm.brand} onChange={(event) => setCardForm((current) => ({ ...current, brand: event.target.value }))}>
                <option value="VISA">Visa</option>
                <option value="MASTERCARD">Mastercard</option>
                <option value="ELO">Elo</option>
                <option value="AMEX">American Express</option>
              </select>
            </label>
            <label className="field-label">Quatro últimos dígitos
              <input inputMode="numeric" autoComplete="off" maxLength={4} pattern="[0-9]{4}" value={cardForm.lastFour} onChange={(event) => setCardForm((current) => ({ ...current, lastFour: event.target.value.replace(/\D/g, "").slice(0, 4) }))} placeholder="0000" required />
            </label>
            <button className="primary-button profile-save" disabled={cardBusy || cardForm.lastFour.length !== 4}>{cardBusy ? "Salvando…" : "Adicionar cartão de demonstração"}</button>
          </form>
          {cards.length > 0 ? (
            <ul className="demo-card-list">
              {cards.map((card) => (
                <li key={card.id}>
                  <span><strong>{card.brand}</strong><span> •••• {card.lastFour}</span></span>
                  <button className="text-button" onClick={() => removeCard(card.id)}>Remover</button>
                </li>
              ))}
            </ul>
          ) : <p className="payment-demo-note">Nenhum cartão demonstrativo cadastrado.</p>}
        </section>
      )}
    </main>
  );
}
