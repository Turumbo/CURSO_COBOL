import { lazy, Suspense, useCallback, useEffect, useMemo, useRef, useState } from "react";
import AdminDashboard from "./components/AdminDashboard.jsx";
import AuthDialog from "./components/AuthDialog.jsx";
import CartDrawer from "./components/CartDrawer.jsx";
import ItemCustomizationDialog from "./components/ItemCustomizationDialog.jsx";
import MenuItemCard from "./components/MenuItemCard.jsx";
import OrderHistory from "./components/OrderHistory.jsx";
const OrderTracking = lazy(() => import("./components/OrderTracking.jsx"));
import ProfilePage from "./components/ProfilePage.jsx";
import PromotionCarousel from "./components/PromotionCarousel.jsx";
import RestaurantCard from "./components/RestaurantCard.jsx";
import { api } from "./services/api.js";
import { formatMoney } from "./utils/format.js";

const RESTAURANT_CATEGORIES = ["Todos", "Brasileira", "Hambúrguer", "Pizza", "Japonesa", "Cafeteria", "Saudável", "Árabe", "Mexicana"];
const RESTAURANT_PAGE_SIZE = 18;

function readStorage(key, fallback) {
  try {
    const value = localStorage.getItem(key);
    return value ? JSON.parse(value) : fallback;
  } catch (error) {
    console.warn(`Não foi possível ler ${key} do armazenamento local.`, error);
    return fallback;
  }
}

export default function App() {
  const [restaurants, setRestaurants] = useState([]);
  const [selectedRestaurant, setSelectedRestaurant] = useState(null);
  const [loading, setLoading] = useState(true);
  const [loadError, setLoadError] = useState("");
  const [hasMoreRestaurants, setHasMoreRestaurants] = useState(false);
  const [loadingMore, setLoadingMore] = useState(false);
  const [catalogSummary, setCatalogSummary] = useState(null);
  const [banners, setBanners] = useState([]);
  const [menuItems, setMenuItems] = useState([]);
  const [menuHasMore, setMenuHasMore] = useState(false);
  const [menuLoading, setMenuLoading] = useState(false);
  const [menuPage, setMenuPage] = useState(0);
  const [menuTotal, setMenuTotal] = useState(0);
  const [menuCategory, setMenuCategory] = useState("Todos");
  const [view, setView] = useState("discover");
  const [search, setSearch] = useState("");
  const [category, setCategory] = useState("Todos");
  const [sort, setSort] = useState("recommended");
  const [favorites, setFavorites] = useState(() => readStorage("bora-favorites", []));
  const [cart, setCart] = useState(() => readStorage("bora-cart", { restaurantId: null, items: [] }));
  const [cartOpen, setCartOpen] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [activeOrderId, setActiveOrderId] = useState(() => readStorage("bora-active-order", null));
  const [orderHistory, setOrderHistory] = useState(() => readStorage("bora-order-history", []));
  const [toastMessage, setToastMessage] = useState("");
  const [authOpen, setAuthOpen] = useState(false);
  const [user, setUser] = useState(() => localStorage.getItem("bora-session-token")
    ? readStorage("bora-user", null) : null);
  const [customizingItem, setCustomizingItem] = useState(null);
  const [pendingCoupon, setPendingCoupon] = useState("");
  const toastTimeout = useRef(null);

  const showToast = useCallback((message) => {
    setToastMessage(message);
    window.clearTimeout(toastTimeout.current);
    toastTimeout.current = window.setTimeout(() => setToastMessage(""), 3200);
  }, []);

  useEffect(() => {
    let active = true;
    setLoading(true);
    setLoadError("");
    const query = { page: 0, size: RESTAURANT_PAGE_SIZE, category, q: search, sort };
    const load = view === "favorites"
      ? Promise.all(Array.from({ length: 7 }, (_, page) =>
        api.getRestaurants({ ...query, page, size: 60 })))
        .then((pages) => ({
          items: pages.flatMap((page) => page.items).filter((restaurant) => favorites.includes(restaurant.id)),
          hasNext: false,
        }))
      : api.getRestaurants(query);
    load.then((result) => {
      if (active) {
        setRestaurants(result.items);
        setHasMoreRestaurants(result.hasNext);
      }
    })
      .catch((error) => { if (active) setLoadError(error.message); })
      .finally(() => { if (active) setLoading(false); });
    return () => { active = false; };
  }, [category, search, sort, view, favorites]);

  useEffect(() => {
    let active = true;
    Promise.all([api.getCatalogSummary(), api.getBanners()])
      .then(([summary, promotionBanners]) => {
        if (active) {
          setCatalogSummary(summary);
          setBanners(promotionBanners);
        }
      })
      .catch((error) => console.error("Não foi possível carregar promoções e dados do catálogo.", error));
    return () => { active = false; };
  }, []);

  useEffect(() => {
    const token = localStorage.getItem("bora-session-token");
    if (!token) return undefined;
    let active = true;
    api.getMyAccount().then((account) => {
      if (active) {
        setUser(account);
        localStorage.setItem("bora-user", JSON.stringify(account));
      }
    }).catch(() => {
      localStorage.removeItem("bora-session-token");
      localStorage.removeItem("bora-user");
      if (active) setUser(null);
    });
    return () => { active = false; };
  }, []);

  useEffect(() => {
    localStorage.setItem("bora-user", JSON.stringify(user));
  }, [user]);

  useEffect(() => {
    if (!selectedRestaurant) {
      setMenuItems([]);
      return undefined;
    }
    let active = true;
    setMenuLoading(true);
    api.getMenu(selectedRestaurant.id, {
      page: 0,
      size: 25,
      category: menuCategory,
      q: search,
    }).then((result) => {
      if (active) {
        setMenuItems(result.items);
        setMenuHasMore(result.hasNext);
        setMenuTotal(result.totalItems);
        setMenuPage(0);
      }
    }).catch((error) => showToast(`Não foi possível carregar o cardápio: ${error.message}`))
      .finally(() => { if (active) setMenuLoading(false); });
    return () => { active = false; };
  }, [selectedRestaurant?.id, menuCategory, search, showToast]);

  useEffect(() => { localStorage.setItem("bora-favorites", JSON.stringify(favorites)); }, [favorites]);
  useEffect(() => { localStorage.setItem("bora-cart", JSON.stringify(cart)); }, [cart]);
  useEffect(() => { localStorage.setItem("bora-active-order", JSON.stringify(activeOrderId)); }, [activeOrderId]);
  useEffect(() => { localStorage.setItem("bora-order-history", JSON.stringify(orderHistory)); }, [orderHistory]);

  const cartRestaurant = restaurants.find((item) => item.id === cart.restaurantId)
    ?? (selectedRestaurant?.id === cart.restaurantId ? selectedRestaurant : cart.restaurant ?? null);
  const cartCount = cart.items.reduce((sum, item) => sum + item.quantity, 0);
  const menuCategories = ["Lanches", "Pratos", "Pizzas", "Combinados", "Cafés", "Bowls", "Esfihas", "Tacos"];

  function toggleFavorite(restaurantId) {
    setFavorites((items) => {
      const next = items.includes(restaurantId) ? items.filter((id) => id !== restaurantId) : [...items, restaurantId];
      return next;
    });
  }

  function addToCart(menuItem, customizations = [], unitPrice = menuItem.price) {
    const restaurantId = selectedRestaurant.id;
    if (cart.restaurantId && cart.restaurantId !== restaurantId) {
      showToast("Sua sacola tem itens de outro restaurante. Finalize ou esvazie antes de trocar.");
      setCartOpen(true);
      return;
    }
    setCart((current) => {
      const cartId = `${menuItem.id}-${customizations.slice().sort().join("|")}`;
      const existing = current.items.find((item) => item.id === cartId);
      const items = existing
        ? current.items.map((item) => item.id === cartId ? { ...item, quantity: item.quantity + 1 } : item)
        : [...current.items, { ...menuItem, id: cartId, menuItemId: menuItem.id, price: unitPrice, customizations, quantity: 1 }];
      return { restaurantId, restaurant: selectedRestaurant, items };
    });
    showToast(`${menuItem.name} adicionado à sacola.`);
    setCustomizingItem(null);
  }

  function changeQuantity(itemId, amount) {
    setCart((current) => {
      const items = current.items
        .map((item) => item.id === itemId ? { ...item, quantity: item.quantity + amount } : item)
        .filter((item) => item.quantity > 0);
      return { restaurantId: items.length ? current.restaurantId : null, items };
    });
  }

  async function placeOrder(payload) {
    setSubmitting(true);
    try {
      const order = await api.createOrder({
        ...payload,
        restaurantId: cart.restaurantId,
      });
      setActiveOrderId(order.id);
      setOrderHistory((current) => [...current.filter((id) => id !== order.id), order.id]);
      setCart({ restaurantId: null, items: [] });
      setCartOpen(false);
      setSelectedRestaurant(null);
      setView("tracking");
      showToast(`Pedido #${order.id} confirmado. Acompanhe o status por aqui.`);
    } catch (error) {
      showToast(`Não foi possível confirmar: ${error.message}`);
    } finally {
      setSubmitting(false);
    }
  }

  function openRestaurant(restaurant) {
    setSelectedRestaurant(restaurant);
    setMenuCategory("Todos");
    setSearch("");
    window.scrollTo({ top: 0, behavior: "smooth" });
  }

  function navigate(nextView) {
    setSelectedRestaurant(null);
    setView(nextView);
    setCategory("Todos");
    setSearch("");
    window.scrollTo({ top: 0, behavior: "smooth" });
  }

  async function loadMoreRestaurants() {
    if (loadingMore || !hasMoreRestaurants) return;
    setLoadingMore(true);
    try {
      const result = await api.getRestaurants({
        page: Math.ceil(restaurants.length / RESTAURANT_PAGE_SIZE),
        size: RESTAURANT_PAGE_SIZE,
        category,
        q: search,
        sort,
      });
      setRestaurants((current) => [...current, ...result.items]);
      setHasMoreRestaurants(result.hasNext);
    } catch (error) {
      showToast(`Não foi possível carregar mais restaurantes: ${error.message}`);
    } finally {
      setLoadingMore(false);
    }
  }

  async function loadMoreMenu() {
    if (!selectedRestaurant || menuLoading || !menuHasMore) return;
    setMenuLoading(true);
    try {
      const result = await api.getMenu(selectedRestaurant.id, {
        page: menuPage + 1,
        size: 25,
        category: menuCategory,
        q: search,
      });
      setMenuItems((current) => [...current, ...result.items]);
      setMenuHasMore(result.hasNext);
      setMenuPage((current) => current + 1);
    } catch (error) {
      showToast(`Não foi possível carregar mais itens: ${error.message}`);
    } finally {
      setMenuLoading(false);
    }
  }

  async function signOut() {
    try {
      await api.logout();
    } catch (error) {
      console.warn("A sessão local será encerrada; o servidor não confirmou a saída.", error);
    }
    localStorage.removeItem("bora-session-token");
    localStorage.removeItem("bora-user");
    setUser(null);
    showToast("Você saiu da sua conta.");
  }

  async function validateCoupon(code) {
    return api.validateCoupon(code);
  }

  function usePromotion(code) {
    setPendingCoupon(code);
    if (!cartCount) showToast(`Cupom ${code} guardado. Escolha um prato para usar na sacola.`);
    setCartOpen(true);
  }

  function startCheckout() {
    if (!user) setAuthOpen(true);
  }

  return (
    <div className="app">
      <header className="site-header">
        <div className="header-inner">
          <button className="brand" onClick={() => navigate("discover")}><span className="brand-mark">B</span><span>Bora<span className="brand-dot">!</span><small>DELIVERY</small></span></button>
          <label className="search-box"><span>⌕</span><input value={search} onChange={(event) => setSearch(event.target.value)} placeholder={selectedRestaurant ? "Buscar neste cardápio" : "Prato, cozinha ou restaurante"} aria-label="Buscar" />{search && <button onClick={() => setSearch("")} aria-label="Limpar busca">×</button>}</label>
          <nav className="main-nav" aria-label="Navegação principal">
            <button className={view === "discover" ? "active" : ""} onClick={() => navigate("discover")}>Descobrir</button>
            <button className={view === "favorites" ? "active" : ""} onClick={() => navigate("favorites")}>♡ Favoritos</button>
            <button className={view === "orders" ? "active" : ""} onClick={() => navigate("orders")}>Pedidos</button>
            {user && <button className={view === "profile" ? "active" : ""} onClick={() => navigate("profile")}>Meu perfil</button>}
            {user && user.role !== "CUSTOMER" && <button className={view === "admin" ? "active" : ""} onClick={() => navigate("admin")}>Operação</button>}
          </nav>
          {user
            ? <button className="account-button" onClick={() => navigate("profile")} aria-label="Abrir meu perfil"><span className="account-avatar">{user.name?.slice(0, 1).toUpperCase()}</span><span>{user.name}<small>Meu perfil</small></span></button>
            : <button className="login-button" onClick={() => setAuthOpen(true)}>Entrar</button>}
          {user && <button className="logout-button" onClick={signOut}>Sair</button>}
          <button className="header-cart" onClick={() => setCartOpen(true)}><span>♧</span><span>Sacola</span><b>{cartCount}</b></button>
        </div>
      </header>

      {view === "admin" ? user && user.role !== "CUSTOMER" ? <AdminDashboard onToast={showToast} /> : <main className="page-shell"><div className="state-card"><strong>Acesso de restaurante</strong><p>Entre com a conta de operação de demonstração para abrir o painel.</p><button className="primary-button compact-button" onClick={() => setAuthOpen(true)}>Entrar como restaurante</button></div></main> :
        view === "profile" ? user ? <ProfilePage user={user} onUpdated={setUser} onToast={showToast} /> : <main className="page-shell"><div className="state-card"><strong>Entre para gerenciar seu perfil.</strong><button className="primary-button compact-button" onClick={() => setAuthOpen(true)}>Entrar na minha conta</button></div></main> :
        view === "orders" ? user ? <OrderHistory onSelect={(id) => { setActiveOrderId(id); setView("tracking"); }} onExplore={() => navigate("discover")} /> : <main className="page-shell"><div className="state-card"><strong>Entre para acompanhar seus pedidos.</strong><p>Seu histórico fica associado à sua conta Bora.</p><button className="primary-button compact-button" onClick={() => setAuthOpen(true)}>Entrar na minha conta</button></div></main> :
          view === "tracking" && activeOrderId ? <Suspense fallback={<main className="page-shell state-card"><div className="loading-spinner" /><p>Carregando acompanhamento…</p></main>}><OrderTracking key={activeOrderId} orderId={activeOrderId} onBack={() => navigate("orders")} onToast={showToast} /></Suspense> :
          <main className="page-shell">
            {!selectedRestaurant ? (
              <>
                <section className="welcome-hero">
                  <div className="hero-text"><span className="hero-eyebrow"><span className="hero-dot" /> SABOR QUE CHEGA ATÉ VOCÊ</span><h1>Hoje merece<br /><em>algo especial.</em></h1><p>Descubra lugares incríveis e receba seus favoritos sem sair de casa.</p><button className="hero-button" onClick={() => document.getElementById("restaurant-list")?.scrollIntoView({ behavior: "smooth" })}>Explorar restaurantes <span>↘</span></button></div>
                  <div className="hero-orbit orbit-one" /><div className="hero-orbit orbit-two" /><div className="hero-food">🥘<span>✦</span></div>
                  <div className="hero-note"><strong>Feito na hora</strong><span>com carinho de verdade</span></div>
                </section>
                <section className="perks-row"><div><span className="perk-icon">◷</span><span><strong>Do pedido à porta</strong><small>Acompanhe cada etapa</small></span></div><div><span className="perk-icon">✳</span><span><strong>Seu jeito de pedir</strong><small>{catalogSummary?.menuItems.toLocaleString("pt-BR") ?? "10.000"} itens em catálogo demonstrativo</small></span></div><div><span className="perk-icon">⌖</span><span><strong>Dados de exemplo</strong><small>Estabelecimentos fictícios, não afiliados</small></span></div></section>
                <PromotionCarousel banners={banners} onUse={usePromotion} />
                <section className="browse-section" id="restaurant-list">
                  <div className="section-header"><div><span className="section-kicker">{view === "favorites" ? "GUARDADOS COM CARINHO" : "SELEÇÃO DE DEMONSTRAÇÃO"}</span><h2>{view === "favorites" ? "Seus lugares favoritos" : "Explore o catálogo de exemplo"}</h2><p>{view === "favorites" ? "Seus restaurantes preferidos, sempre à mão." : `${catalogSummary?.restaurants.toLocaleString("pt-BR") ?? "400"} estabelecimentos fictícios para testar a experiência.`}</p></div><label className="sort-control"><span>Ordenar:</span><select value={sort} onChange={(event) => setSort(event.target.value)}><option value="recommended">Recomendados</option><option value="rating">Melhor avaliados</option><option value="delivery">Entrega mais rápida</option></select></label></div>
                  <div className="category-list">{RESTAURANT_CATEGORIES.map((item) => <button key={item} className={`category-chip ${category === item ? "selected" : ""}`} onClick={() => setCategory(item)}>{item === "Todos" ? "✦" : item === "Hambúrguer" ? "🍔" : item === "Pizza" ? "🍕" : item === "Japonesa" ? "🍣" : item === "Cafeteria" ? "☕" : "🍲"} <span>{item}</span></button>)}</div>
                  {loading ? <div className="state-card"><div className="loading-spinner" /><p>Encontrando bons lugares para você…</p></div> :
                    loadError ? <div className="state-card error-card"><strong>Não conseguimos carregar os restaurantes.</strong><p>{loadError}. Verifique se o backend Java está iniciado.</p><button className="secondary-button compact" onClick={() => window.location.reload()}>Tentar novamente</button></div> :
                      <div className="restaurant-grid">{restaurants.map((restaurant) => <RestaurantCard key={restaurant.id} restaurant={restaurant} favorite={favorites.includes(restaurant.id)} onFavorite={() => toggleFavorite(restaurant.id)} onSelect={() => openRestaurant(restaurant)} />)}
                        {!restaurants.length && <div className="state-card"><strong>{view === "favorites" ? "Você ainda não salvou nenhum lugar." : "Nenhum restaurante encontrado."}</strong><p>{view === "favorites" ? "Toque no coração de um restaurante para adicioná-lo aos favoritos." : "Mude os filtros ou tente outra busca."}</p></div>}</div>}
                  {!loading && hasMoreRestaurants && <button className="load-more-button" onClick={loadMoreRestaurants} disabled={loadingMore}>{loadingMore ? "Carregando…" : `Ver mais restaurantes${catalogSummary ? ` · ${Math.max(0, catalogSummary.restaurants - restaurants.length)} restantes` : ""}`}</button>}
                </section>
              </>
            ) : (
              <section className="restaurant-detail">
                <button className="back-link" onClick={() => setSelectedRestaurant(null)}>← Voltar aos restaurantes</button>
                <div className="detail-cover"><img src={selectedRestaurant.imageUrl} alt="" /><div className="detail-cover-shade" /><div className="detail-cover-content"><span className="hero-eyebrow">UMA ESCOLHA DELICIOSA</span><h1>{selectedRestaurant.name}</h1><p>{selectedRestaurant.description}</p></div><button className={`detail-favorite ${favorites.includes(selectedRestaurant.id) ? "is-favorite" : ""}`} onClick={() => toggleFavorite(selectedRestaurant.id)}>{favorites.includes(selectedRestaurant.id) ? "♥ Salvo" : "♡ Favoritar"}</button></div>
                <div className="restaurant-facts"><span><strong>★ {Number(selectedRestaurant.rating).toFixed(1)}</strong> avaliação</span><span>{selectedRestaurant.cuisine}</span><span>◷ {selectedRestaurant.deliveryMinutes}–{selectedRestaurant.deliveryMinutes + 10} min</span><span>Entrega {formatMoney(selectedRestaurant.deliveryFee)}</span></div>
                <div className="detail-menu-header"><div><span className="section-kicker">CARDÁPIO</span><h2>O que vai ser hoje?</h2></div><span>{menuTotal.toLocaleString("pt-BR")} opções</span></div>
                <div className="category-list menu-categories">{["Todos", ...menuCategories].map((item) => <button key={item} className={`category-chip ${menuCategory === item ? "selected" : ""}`} onClick={() => setMenuCategory(item)}>{item}</button>)}</div>
                {menuLoading && !menuItems.length ? <div className="state-card"><div className="loading-spinner" /><p>Preparando o cardápio…</p></div> : <div className="menu-items">{menuItems.map((item) => <MenuItemCard key={item.id} item={item} onAdd={() => setCustomizingItem(item)} />)}</div>}
                {!menuLoading && menuHasMore && <button className="load-more-button" onClick={loadMoreMenu}>Ver mais itens do cardápio</button>}
                {cartCount > 0 && <button className="floating-cart" onClick={() => setCartOpen(true)}><span>Ver sacola · {cartCount} {cartCount === 1 ? "item" : "itens"}</span><strong>{formatMoney(cart.items.reduce((sum, item) => sum + item.price * item.quantity, 0))}</strong></button>}
              </section>
            )}
          </main>}

      <footer className="site-footer"><div className="footer-inner"><div className="footer-brand"><span className="brand-mark">B</span><span>Bora! <small>Uma experiência de delivery demonstrativa.</small></span></div><div><strong>Projeto de estudo</strong><span>React · Java · Spring Boot · H2</span></div><span className="footer-note">Catálogo e pagamentos demonstrativos · Mapa © OpenStreetMap</span></div></footer>
      <CartDrawer open={cartOpen} restaurant={cartRestaurant} items={cart.items} onChangeQuantity={changeQuantity} onClose={() => setCartOpen(false)} onSubmit={placeOrder} busy={submitting} onToast={showToast} user={user} onRequireAuth={startCheckout} onValidateCoupon={validateCoupon} initialCoupon={pendingCoupon} />
      <AuthDialog open={authOpen} onClose={() => setAuthOpen(false)} onAuthenticated={setUser} onToast={showToast} />
      <ItemCustomizationDialog item={customizingItem} onClose={() => setCustomizingItem(null)} onAdd={(options, price) => addToCart(customizingItem, options, price)} />
      {toastMessage && <div className="toast-message" role="status">{toastMessage}</div>}
    </div>
  );
}
