import { useEffect, useState } from "react";
import { api } from "../services/api.js";
import { formatMoney, statusLabel } from "../utils/format.js";

export default function OrderHistory({ onSelect, onExplore }) {
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    let active = true;
    api.getMyOrders().then((result) => {
      if (active) {
        setOrders(result);
        setError("");
        setLoading(false);
      }
    }).catch((error) => {
      if (active) {
        setError(error.message);
        setLoading(false);
      }
    });
    return () => { active = false; };
  }, []);

  return (
    <main className="page-shell history-page">
      <div className="page-heading"><div><span className="section-kicker">SUA JORNADA COM A BORA!</span><h1>Meus pedidos</h1><p>Acompanhe os pedidos feitos neste navegador.</p></div><button className="secondary-button compact" onClick={onExplore}>Explorar restaurantes</button></div>
      {loading && <div className="state-card"><div className="loading-spinner" /><p>Carregando seu histórico…</p></div>}
      {!loading && !error && !orders.length && <div className="state-card"><strong>Seu próximo pedido começa aqui.</strong><p>Quando você fizer um pedido, poderá acompanhar as atualizações nesta página.</p><button className="primary-button compact-button" onClick={onExplore}>Descobrir restaurantes</button></div>}
      {!loading && error && <div className="state-card error-card"><strong>Não foi possível carregar o histórico.</strong><p>{error}</p><button className="secondary-button compact" onClick={() => window.location.reload()}>Tentar novamente</button></div>}
      {!loading && orders.length > 0 && <div className="history-list">{[...orders].reverse().map((order) => (
        <article className="history-card" key={order.id}>
          <div className="history-card-top"><div><span className="section-kicker">PEDIDO #{order.id}</span><h2>{order.restaurant?.name ?? "Restaurante"}</h2></div><span className={`status-pill status-${order.status?.toLowerCase()}`}>{statusLabel(order.status)}</span></div>
          <><div className="history-order-lines">{order.items.map((item) => <span key={item.id}>{item.quantity}× {item.name}{item.customizations?.length ? ` · ${item.customizations.join(", ")}` : ""}</span>)}</div><div className="history-card-bottom"><span>{new Date(order.createdAt).toLocaleString("pt-BR")}</span><strong>{formatMoney(order.total)}</strong><button className="text-button" onClick={() => onSelect(order.id)}>Ver acompanhamento →</button></div></>
        </article>
      ))}</div>}
    </main>
  );
}
