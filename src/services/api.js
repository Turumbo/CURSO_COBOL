const API_BASE = import.meta.env.VITE_API_URL ?? "/api";

async function request(path, options = {}) {
  const token = localStorage.getItem("bora-session-token");
  const response = await fetch(`${API_BASE}${path}`, {
    headers: {
      "Content-Type": "application/json",
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...options.headers,
    },
    ...options,
  });

  if (!response.ok) {
    const body = await response.json().catch(() => null);
    const message =
      body?.message ?? body?.error ?? `Falha na API (${response.status})`;
    throw new Error(message);
  }

  if (response.status === 204) return null;
  return response.json();
}

export const api = {
  getRestaurants: ({
    page = 0,
    size = 18,
    category = "",
    q = "",
    sort = "recommended",
  } = {}) => {
    const params = new URLSearchParams({ page, size, category, q, sort });
    return request(`/restaurants?${params}`);
  },
  getMenu: (id, { page = 0, size = 25, category = "", q = "" } = {}) => {
    const params = new URLSearchParams({ page, size, category, q });
    return request(`/restaurants/${id}/menu?${params}`);
  },
  getCatalogSummary: () => request("/catalog/summary"),
  getBanners: () => request("/promotions/banners"),
  validateCoupon: (code) =>
    request(`/promotions/validate?${new URLSearchParams({ code })}`),
  register: (payload) =>
    request("/auth/register", {
      method: "POST",
      body: JSON.stringify(payload),
    }),
  login: (payload) =>
    request("/auth/login", { method: "POST", body: JSON.stringify(payload) }),
  logout: () => request("/auth/logout", { method: "POST" }),
  getMyAccount: () => request("/auth/me"),
  updateMyAccount: (payload) =>
    request("/auth/me", { method: "PUT", body: JSON.stringify(payload) }),
  getMyDemoCards: () => request("/auth/me/payment-methods"),
  addMyDemoCard: (payload) =>
    request("/auth/me/payment-methods", {
      method: "POST",
      body: JSON.stringify(payload),
    }),
  deleteMyDemoCard: (id) =>
    request(`/auth/me/payment-methods/${id}`, { method: "DELETE" }),
  getMyOrders: () => request("/orders"),
  getChatMessages: (orderId) => request(`/chat/orders/${orderId}/messages`),
  sendChatMessage: (orderId, message) =>
    request(`/chat/orders/${orderId}/messages`, {
      method: "POST",
      body: JSON.stringify({ message }),
    }),
  getReview: (orderId) => request(`/reviews/orders/${orderId}`),
  createReview: (payload) =>
    request("/reviews", { method: "POST", body: JSON.stringify(payload) }),
  createOrder: (payload) =>
    request("/orders", { method: "POST", body: JSON.stringify(payload) }),
  getOrder: (id) => request(`/orders/${id}`),
  getOrderPayment: (id) => request(`/orders/${id}/payment`),
  createFiscalDocument: (id) =>
    request(`/orders/${id}/fiscal-document`, { method: "POST" }),
  getAdminOrders: () => request("/admin/orders"),
  getInventory: ({ page = 0, size = 50 } = {}) =>
    request(`/admin/inventory?${new URLSearchParams({ page, size })}`),
  updateInventory: (menuItemId, quantity) =>
    request(`/admin/inventory/${menuItemId}`, {
      method: "PATCH",
      body: JSON.stringify({ quantity }),
    }),
  updateOrderStatus: (id, status) =>
    request(`/admin/orders/${id}/status`, {
      method: "PATCH",
      body: JSON.stringify({ status }),
    }),
};
