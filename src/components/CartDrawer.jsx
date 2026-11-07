import { lazy, Suspense, useEffect, useState } from "react";
import { formatMoney } from "../utils/format.js";

const MapPicker = lazy(() => import("./MapPicker.jsx"));

export default function CartDrawer({
  open,
  restaurant,
  items,
  onChangeQuantity,
  onClose,
  onSubmit,
  busy,
  onToast,
  user,
  onRequireAuth,
  onValidateCoupon,
  initialCoupon,
}) {
  const [customerName, setCustomerName] = useState("");
  const [address, setAddress] = useState("");
  const [location, setLocation] = useState(null);
  const [paymentMethod, setPaymentMethod] = useState("PIX");
  const [coupon, setCoupon] = useState("");
  const [validatedCoupon, setValidatedCoupon] = useState(null);
  const [couponBusy, setCouponBusy] = useState(false);
  useEffect(() => {
    if (initialCoupon) setCoupon(initialCoupon);
  }, [initialCoupon]);
  useEffect(() => {
    if (open && user) {
      setCustomerName(user.name ?? "");
      setAddress(user.deliveryAddress ?? "");
    }
  }, [open, user]);

  if (!open) return null;

  const subtotal = items.reduce(
    (sum, item) => sum + item.price * item.quantity,
    0,
  );
  const discount = validatedCoupon?.valid
    ? Math.min(
        Number(validatedCoupon.maximumDiscount),
        subtotal * Number(validatedCoupon.percentOff),
      ) + Number(validatedCoupon.fixedDiscount)
    : 0;
  const total = Math.max(
    0,
    subtotal + (restaurant?.deliveryFee ?? 0) - discount,
  );
  const center = location ? [location.lat, location.lng] : [-23.5614, -46.656];

  function useCurrentLocation() {
    if (!navigator.geolocation) {
      onToast(
        "Seu navegador não oferece localização. Marque o ponto direto no mapa.",
      );
      return;
    }
    navigator.geolocation.getCurrentPosition(
      ({ coords }) =>
        setLocation({ lat: coords.latitude, lng: coords.longitude }),
      () =>
        onToast(
          "Não conseguimos acessar sua localização. Você pode marcar o mapa manualmente.",
        ),
      { enableHighAccuracy: true, timeout: 10000 },
    );
  }

  function submit(event) {
    event.preventDefault();
    if (!user) {
      onRequireAuth();
      return;
    }
    if (!location) {
      onToast("Marque o destino no mapa ou use sua localização.");
      return;
    }
    if (!address.trim()) {
      onToast("Informe o endereço e o complemento para entrega.");
      return;
    }
    onSubmit({
      customerName: customerName.trim(),
      deliveryAddress: address.trim(),
      latitude: location.lat,
      longitude: location.lng,
      paymentMethod,
      promoCode: discount ? validatedCoupon.code : "",
      items: items.map((item) => ({
        menuItemId: item.menuItemId ?? item.id,
        quantity: item.quantity,
        customizations: item.customizations ?? [],
      })),
    });
  }

  async function applyCoupon() {
    if (!coupon.trim()) {
      setValidatedCoupon(null);
      onToast("Digite um cupom para validar.");
      return;
    }
    setCouponBusy(true);
    try {
      const result = await onValidateCoupon(coupon);
      setValidatedCoupon(result);
      onToast(result.message);
    } catch (error) {
      setValidatedCoupon(null);
      onToast(error.message);
    } finally {
      setCouponBusy(false);
    }
  }

  return (
    <div
      className="overlay"
      onMouseDown={(event) => event.target === event.currentTarget && onClose()}
    >
      <aside
        className="drawer"
        role="dialog"
        aria-modal="true"
        aria-labelledby="cart-title"
      >
        <div className="drawer-heading">
          <div>
            <span className="section-kicker">QUASE LÁ</span>
            <h2 id="cart-title">Sua sacola</h2>
          </div>
          <button
            className="icon-button"
            onClick={onClose}
            aria-label="Fechar sacola"
          >
            ×
          </button>
        </div>
        <p className="drawer-restaurant">
          {restaurant?.name} <span>·</span> {items.length}{" "}
          {items.length === 1 ? "item" : "itens"}
        </p>
        <div className="cart-lines">
          {items.map((item) => (
            <div className="cart-line" key={item.id}>
              <div>
                <strong>{item.name}</strong>
                <span>
                  {item.customizations?.length
                    ? item.customizations.join(" · ")
                    : "Padrão"}{" "}
                  · {formatMoney(item.price)} cada
                </span>
              </div>
              <div className="stepper">
                <button
                  onClick={() => onChangeQuantity(item.id, -1)}
                  aria-label={`Remover ${item.name}`}
                >
                  −
                </button>
                <span>{item.quantity}</span>
                <button
                  onClick={() => onChangeQuantity(item.id, 1)}
                  aria-label={`Adicionar ${item.name}`}
                >
                  +
                </button>
              </div>
            </div>
          ))}
        </div>
        <form onSubmit={submit}>
          <section className="form-section">
            <div className="section-title-row">
              <h3>Entrega</h3>
              <button
                className="text-button"
                type="button"
                onClick={useCurrentLocation}
              >
                Usar meu GPS
              </button>
            </div>
            <label className="field-label">
              Seu nome
              <input
                value={customerName}
                onChange={(event) => setCustomerName(event.target.value)}
                placeholder="Como podemos chamar você?"
                required
              />
            </label>
            <label className="field-label">
              Endereço e complemento
              <input
                value={address}
                onChange={(event) => setAddress(event.target.value)}
                placeholder="Rua, número, apartamento…"
                required
              />
            </label>
            <Suspense
              fallback={
                <div className="map-loading" role="status">
                  Carregando mapa…
                </div>
              }
            >
              <MapPicker
                center={center}
                location={location}
                onSelect={setLocation}
                interactive
                height={220}
              />
            </Suspense>
            {location && (
              <p className="field-help">
                Destino marcado: {location.lat.toFixed(5)},{" "}
                {location.lng.toFixed(5)}.
              </p>
            )}
            <p className="field-help">
              Mapa © OpenStreetMap · A localização escolhida é usada apenas para
              este pedido.
            </p>
          </section>
          <section className="form-section">
            <h3>Pagamento</h3>
            <div className="payment-options">
              {[
                ["PIX", "◈", "Pix"],
                ["CREDIT_CARD", "▣", "Cartão (mock)"],
                ["CASH", "＄", "Dinheiro"],
                ["BB_PAY", "◉", "BB Pay (mock)"],
                ["NUPAY", "◉", "NuPay (mock)"],
                ["ALELO", "▤", "Alelo (mock)"],
              ].map(([value, icon, label]) => (
                <button
                  type="button"
                  key={value}
                  className={`payment-option ${paymentMethod === value ? "selected" : ""}`}
                  onClick={() => setPaymentMethod(value)}
                >
                  <span>{icon}</span>
                  {label}
                </button>
              ))}
            </div>
          </section>
          <section className="form-section">
            <h3>Tem um cupom?</h3>
            <div className="coupon-entry">
              <input
                value={coupon}
                onChange={(event) => {
                  setCoupon(event.target.value);
                  setValidatedCoupon(null);
                }}
                placeholder="Digite o código"
              />
              <button type="button" onClick={applyCoupon} disabled={couponBusy}>
                {couponBusy ? "…" : "Aplicar"}
              </button>
            </div>
          </section>
          <div className="order-summary">
            <div>
              <span>Subtotal</span>
              <strong>{formatMoney(subtotal)}</strong>
            </div>
            <div>
              <span>Entrega</span>
              <strong>{formatMoney(restaurant?.deliveryFee ?? 0)}</strong>
            </div>
            {discount > 0 && (
              <div className="discount-row">
                <span>Desconto</span>
                <strong>− {formatMoney(discount)}</strong>
              </div>
            )}
            <div className="summary-total">
              <span>Total</span>
              <strong>{formatMoney(total)}</strong>
            </div>
          </div>
          <button
            className="primary-button checkout-button"
            type="submit"
            disabled={busy || items.length === 0}
          >
            {busy
              ? "Confirmando pedido…"
              : `Fazer pedido · ${formatMoney(total)}`}
          </button>
          <p className="demo-disclaimer">
            {user
              ? `Conectado como ${user.email}. `
              : "Entre na sua conta para confirmar. "}
            Pagamentos demonstrativos: nenhuma cobrança, transferência Pix ou autorização financeira será feita.
          </p>
        </form>
      </aside>
    </div>
  );
}
