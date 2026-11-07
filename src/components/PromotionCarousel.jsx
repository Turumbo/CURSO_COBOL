import { useEffect, useState } from "react";

export default function PromotionCarousel({ banners, onUse }) {
  const [index, setIndex] = useState(0);
  useEffect(() => {
    if (banners.length < 2) return undefined;
    const timer = window.setInterval(() => setIndex((current) => (current + 1) % banners.length), 7000);
    return () => window.clearInterval(timer);
  }, [banners.length]);
  if (!banners.length) return null;
  const promotion = banners[index % banners.length];
  return (
    <section className={`promotion-card promotion-${promotion.theme}`} aria-label="Ofertas em destaque">
      <div className="promotion-decoration">✦</div>
      <div className="promotion-copy">
        <span className="promotion-badge">{promotion.badge}</span>
        <h2>{promotion.title}</h2>
        <p>{promotion.subtitle}</p>
        <button onClick={() => onUse(promotion.code)}>Usar código <strong>{promotion.code}</strong> <span>→</span></button>
      </div>
      <div className="promotion-controls">
        <span>{String(index + 1).padStart(2, "0")} / {String(banners.length).padStart(2, "0")}</span>
        <button aria-label="Próxima promoção" onClick={() => setIndex((current) => (current + 1) % banners.length)}>→</button>
      </div>
    </section>
  );
}
