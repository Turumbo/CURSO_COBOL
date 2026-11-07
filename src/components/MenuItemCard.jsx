import { formatMoney } from "../utils/format.js";

export default function MenuItemCard({ item, onAdd }) {
  return (
    <article className="menu-item-card">
      <div className="menu-item-copy">
        <span className="menu-item-category">{item.category}</span>
        <h3>{item.name}</h3>
        <p>{item.description}</p>
        <strong>{formatMoney(item.price)}</strong>
      </div>
      <div className="menu-item-art">
        <img src={item.imageUrl} alt="" loading="lazy" />
        <button onClick={onAdd} disabled={item.available === false} aria-label={item.available === false ? `${item.name} sem estoque` : `Adicionar ${item.name}`}>
          {item.available === false ? "Esgotado" : "＋"}
        </button>
      </div>
    </article>
  );
}
