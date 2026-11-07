export const formatMoney = (value) =>
  new Intl.NumberFormat("pt-BR", {
    style: "currency",
    currency: "BRL",
  }).format(value ?? 0);

export const ORDER_STATUSES = [
  { value: "RECEIVED", label: "Pedido recebido" },
  { value: "PREPARING", label: "Em preparo" },
  { value: "READY_FOR_PICKUP", label: "Pronto para retirada" },
  { value: "ON_THE_WAY", label: "Saiu para entrega" },
  { value: "DELIVERED", label: "Entregue" },
  { value: "CANCELLED", label: "Cancelado" },
];

export const statusLabel = (status) =>
  ORDER_STATUSES.find((item) => item.value === status)?.label ?? status;
