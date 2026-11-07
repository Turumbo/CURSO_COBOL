import { useEffect, useMemo, useState } from "react";
import { api } from "../services/api.js";
import { statusLabel } from "../utils/format.js";
import MapPicker from "./MapPicker.jsx";
import OrderChat from "./OrderChat.jsx";
import FeedbackForm from "./FeedbackForm.jsx";

const STATUS_PROGRESS = {
  RECEIVED: 0,
  PREPARING: 0,
  READY_FOR_PICKUP: 0,
  ON_THE_WAY: 0.7,
  DELIVERED: 1,
  CANCELLED: 0,
};

export default function OrderTracking({ orderId, onBack, onToast }) {
  const [order, setOrder] = useState(null);
  const [payment, setPayment] = useState(null);
  const [fiscalDocument, setFiscalDocument] = useState(null);
  const [creatingFiscalDocument, setCreatingFiscalDocument] = useState(false);
  const [path, setPath] = useState([]);
  const [routeMessage, setRouteMessage] = useState("Calculando trajeto…");
  const [error, setError] = useState("");
  const deliveryElapsed =
    order?.status === "ON_THE_WAY"
      ? Math.max(0, Date.now() - new Date(order.statusUpdatedAt).getTime())
      : 0;
  const routeProgress =
    order?.status === "DELIVERED"
      ? 1
      : order?.status === "ON_THE_WAY"
        ? Math.min(1, deliveryElapsed / 180000)
        : 0;
  const progress =
    order?.status === "ON_THE_WAY"
      ? 0.62 + routeProgress * 0.35
      : (STATUS_PROGRESS[order?.status] ?? 0);

  useEffect(() => {
    let active = true;
    async function loadOrder() {
      try {
        const result = await api.getOrder(orderId);
        if (active) {
          setOrder(result);
          setError("");
        }
      } catch (loadError) {
        if (active) setError(loadError.message);
      }
    }
    loadOrder();
    const timer = window.setInterval(loadOrder, 5000);
    return () => {
      active = false;
      window.clearInterval(timer);
    };
  }, [orderId]);

  useEffect(() => {
    let active = true;
    api.getOrderPayment(orderId)
      .then((result) => { if (active) setPayment(result); })
      .catch((paymentError) => {
        if (active) onToast(`Transação de demonstração indisponível: ${paymentError.message}`);
      });
    return () => { active = false; };
  }, [orderId, onToast]);

  async function createFiscalDocument() {
    setCreatingFiscalDocument(true);
    try {
      setFiscalDocument(await api.createFiscalDocument(orderId));
    } catch (documentError) {
      onToast(`Não foi possível gerar o comprovante: ${documentError.message}`);
    } finally {
      setCreatingFiscalDocument(false);
    }
  }

  useEffect(() => {
    if (!order) return undefined;
    let active = true;
    const controller = new AbortController();
    const timeout = window.setTimeout(() => controller.abort(), 10000);
    const destination = [order.latitude, order.longitude];
    const origin = [order.restaurant.latitude, order.restaurant.longitude];
    const url = `https://router.project-osrm.org/route/v1/driving/${origin[1]},${origin[0]};${order.longitude},${order.latitude}?overview=full&geometries=geojson`;
    fetch(url, { signal: controller.signal })
      .then((response) => {
        if (!response.ok)
          throw new Error(`Rota indisponível (${response.status})`);
        return response.json();
      })
      .then((result) => {
        if (!result.routes?.length)
          throw new Error("Não encontramos um trajeto viário.");
        if (active) {
          setPath(
            result.routes[0].geometry.coordinates.map(([lng, lat]) => [
              lat,
              lng,
            ]),
          );
          setRouteMessage(
            `Rota de rua calculada · ${(result.routes[0].distance / 1000).toFixed(1)} km`,
          );
        }
      })
      .catch((routeError) => {
        if (active) {
          setPath([origin, destination]);
          setRouteMessage(
            "Rota direta aproximada; não foi possível calcular o trajeto viário.",
          );
          onToast(routeError.message);
        }
      })
      .finally(() => {
        window.clearTimeout(timeout);
      });
    return () => {
      active = false;
      window.clearTimeout(timeout);
      controller.abort();
    };
  }, [
    order?.id,
    order?.latitude,
    order?.longitude,
    order?.restaurant?.latitude,
    order?.restaurant?.longitude,
    onToast,
  ]);

  const eta = useMemo(() => {
    const remaining = Math.max(
      0,
      Math.ceil(
        (1 - routeProgress) * (order?.restaurant?.deliveryMinutes ?? 30),
      ),
    );
    if (order?.status === "DELIVERED") return "Pedido entregue";
    if (order?.status === "CANCELLED") return "Pedido cancelado";
    return remaining
      ? `Estimativa: ${remaining} min`
      : "Aguardando atualização";
  }, [order?.restaurant?.deliveryMinutes, order?.status, routeProgress]);

  if (error && !order) {
    return (
      <section className="state-card">
        <span className="section-kicker">ACOMPANHAMENTO</span>
        <h2>Não foi possível carregar seu pedido</h2>
        <p>{error}</p>
        <button className="secondary-button" onClick={onBack}>
          Voltar ao início
        </button>
      </section>
    );
  }
  if (!order)
    return (
      <section className="state-card">
        <div className="loading-spinner" />
        <p>Carregando seu pedido…</p>
      </section>
    );

  const isFinished = ["DELIVERED", "CANCELLED"].includes(order.status);
  return (
    <main className="page-shell tracking-page">
      <div className="page-heading">
        <div>
          <span className="section-kicker">PEDIDO #{order.id}</span>
          <h1>Acompanhe cada etapa</h1>
          <p>
            {order.restaurant.name} · Atualização automática a cada 5 segundos
          </p>
        </div>
        <button className="secondary-button compact" onClick={onBack}>
          Voltar ao início
        </button>
      </div>
      <div className="tracking-grid">
        <section className="tracking-map-card">
          {path.length ? (
            <MapPicker
              center={[order.restaurant.latitude, order.restaurant.longitude]}
              path={path}
              trackingStartedAt={new Date(order.statusUpdatedAt).getTime()}
              trackingActive={
                order.status === "ON_THE_WAY" || order.status === "DELIVERED"
              }
              height={470}
            />
          ) : (
            <div className="map-placeholder">
              <div className="loading-spinner" />
              <span>Carregando mapa e calculando rota…</span>
            </div>
          )}
          <div className="map-footer">
            <span>📍 Restaurante</span>
            <span>🛵 Entregador</span>
            <span>⌂ Destino</span>
            <small>{routeMessage} · posição do entregador demonstrativa</small>
          </div>
        </section>
        <aside className="tracking-card">
          <span className={`status-pill status-${order.status.toLowerCase()}`}>
            {statusLabel(order.status)}
          </span>
          <h2>
            {order.status === "DELIVERED"
              ? "Bom apetite!"
              : order.status === "CANCELLED"
                ? "Pedido cancelado"
                : "Estamos cuidando do seu pedido"}
          </h2>
          <p>
            Olá, {order.customerName}. Você pode acompanhar as atualizações por
            aqui.
          </p>
          <div className="journey">
            {[
              "RECEIVED",
              "PREPARING",
              "READY_FOR_PICKUP",
              "ON_THE_WAY",
              "DELIVERED",
            ].map((status, index) => {
              const statusIndex = [
                "RECEIVED",
                "PREPARING",
                "READY_FOR_PICKUP",
                "ON_THE_WAY",
                "DELIVERED",
              ].indexOf(order.status);
              return (
                <div
                  className={`journey-step ${statusIndex >= index ? "complete" : ""}`}
                  key={status}
                >
                  <span>{statusIndex > index ? "✓" : index + 1}</span>
                  <strong>{statusLabel(status)}</strong>
                </div>
              );
            })}
          </div>
          <div className="eta-panel">
            <span>⏱</span>
            <div>
              <strong>{eta}</strong>
              <small>Previsão baseada no restaurante</small>
            </div>
          </div>
          <div className="tracking-info">
            <span>Entrega para</span>
            <strong>{order.deliveryAddress}</strong>
          </div>
          <div className="tracking-info">
            <span>Pagamento</span>
            <strong>
              {{
                PIX: "Pix (simulado)",
                CREDIT_CARD: "Cartão (simulado)",
                CASH: "Dinheiro (simulado)",
                BB_PAY: "BB Pay (simulado)",
                NUPAY: "NuPay (simulado)",
                ALELO: "Alelo (simulado)",
              }[order.paymentMethod] ?? "Pagamento simulado"}
            </strong>
            {payment && <small>{payment.status} · referência {payment.providerReference}</small>}
          </div>
          <div className="tracking-total">
            <span>Total do pedido</span>
            <strong>
              {new Intl.NumberFormat("pt-BR", {
                style: "currency",
                currency: "BRL",
              }).format(order.total)}
            </strong>
          </div>
          <section className="demo-document-panel">
            <h3>Comprovante fiscal demonstrativo</h3>
            <p>Este comprovante é fictício e não tem validade fiscal.</p>
            {!fiscalDocument
              ? <button className="secondary-button compact" onClick={createFiscalDocument} disabled={creatingFiscalDocument}>{creatingFiscalDocument ? "Gerando…" : "Gerar comprovante de demonstração"}</button>
              : <div className="demo-document-result"><strong>{fiscalDocument.reference}</strong><span>Documento: {fiscalDocument.documentType}</span><span>{fiscalDocument.notice}</span></div>}
          </section>
          {isFinished && (
            <button className="primary-button" onClick={onBack}>
              Explorar restaurantes
            </button>
          )}
        </aside>
      </div>
      <div className="order-followup-grid">
        <OrderChat
          orderId={order.id}
          user={JSON.parse(localStorage.getItem("bora-user") ?? "null")}
          closed={isFinished}
          onToast={onToast}
        />
        {order.status === "DELIVERED" && (
          <FeedbackForm orderId={order.id} onToast={onToast} />
        )}
      </div>
    </main>
  );
}
