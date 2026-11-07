import { formatMoney } from "../utils/format.js";

export default function RestaurantCard({ restaurant, favorite, onFavorite, onSelect }) {
  return (
    <article className="restaurant-card">
      <button className="restaurant-cover" onClick={onSelect} aria-label={`Ver ${restaurant.name}`}>
        <img src={restaurant.imageUrl} alt="" loading="lazy" />
        {restaurant.featured && <span className="cover-tag">⭐ Destaque</span>}
        <span className="cover-time">{restaurant.deliveryMinutes}–{restaurant.deliveryMinutes + 10} min</span>
      </button>
      <button
        className={`favorite-button ${favorite ? "is-favorite" : ""}`}
        aria-label={favorite ? "Remover dos favoritos" : "Adicionar aos favoritos"}
        onClick={onFavorite}
      >
        {favorite ? "♥" : "♡"}
      </button>
      <button className="restaurant-details" onClick={onSelect}>
        <span className="restaurant-title">{restaurant.name}</span>
        <span className="restaurant-cuisine">{restaurant.cuisine} · {restaurant.category}</span>
        <span className="restaurant-meta">
          <strong>★ {Number(restaurant.rating).toFixed(1)}</strong>
          <span>·</span>
          <span>Entrega {formatMoney(restaurant.deliveryFee)}</span>
          {restaurant.minimumOrder > 0 && <><span>·</span><span>Mín. {formatMoney(restaurant.minimumOrder)}</span></>}
        </span>
      </button>
    </article>
  );
}
