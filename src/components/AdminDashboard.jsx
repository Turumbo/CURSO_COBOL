import { useCallback, useEffect, useMemo, useState } from "react";
import { api } from "../services/api.js";
import { formatMoney, ORDER_STATUSES, statusLabel } from "../utils/format.js";

export default function AdminDashboard({ onToast }) {
  const [orders, setOrders] = useState([]);
  const [error, setError] = useState("");
  const [busyId, setBusyId] = useState(null);
  const [inventory, setInventory] = useState([]);
  const [inventoryPage, setInventoryPage] = useState(0);
  const [inventoryTotalPages, setInventoryTotalPages] = useState(0);
  const [inventoryTotalItems, setInventoryTotalItems] = useState(0);
  const [inventoryLoading, setInventoryLoading] = useState(false);
  const [inventoryError, setInventoryError] = useState("");
  const [stockDrafts, setStockDrafts] = useState({});
  const [savingStockId, setSavingStockId] = useState(null);
  const [refresh, setRefresh] = useState(0);
  const [section, setSection] = useState("overview");

  const loadOrders = useCallback(async () => {
    try {
      setOrders(await api.getAdminOrders());
      setError("");
    } catch (loadError) {
      setError(loadError.message);
    }
  }, []);

  useEffect(() => {
    loadOrders();
    const timer = window.setInterval(loadOrders, 8000);
    return () => window.clearInterval(timer);
  }, [loadOrders, refresh]);

  useEffect(() => {
    if (section !== "stock") return undefined;
    let active = true;
    setInventoryLoading(true);
    api.getInventory({ page: inventoryPage, size: 50 })
      .then((result) => {
        if (active) {
          setInventory(result.items);
          setInventoryTotalPages(result.totalPages);
          setInventoryTotalItems(result.totalItems);
          setInventoryError("");
        }
      })
      .catch((loadError) => { if (active) setInventoryError(loadError.message); })
      .finally(() => { if (active) setInventoryLoading(false); });
    return () => { active = false; };
  }, [section, inventoryPage, refresh]);

  async function changeStatus(orderId, status) {
    setBusyId(orderId);
    try {
      await api.updateOrderStatus(orderId, status);
      onToast(`Pedido #${orderId}: ${statusLabel(status).toLowerCase()}.`);
      setRefresh((value) => value + 1);
    } catch (updateError) {
      onToast(updateError.message);
    } finally {
      setBusyId(null);
    }
  }

  async function saveStock(item) {
    const quantity = Number(stockDrafts[item.menuItemId] ?? item.stockQuantity);
    if (!Number.isInteger(quantity) || quantity < 0 || quantity > 100_000) {
      onToast("Informe uma quantidade inteira entre 0 e 100.000.");
      return;
    }
    setSavingStockId(item.menuItemId);
    try {
      const updated = await api.updateInventory(item.menuItemId, quantity);
      setInventory((current) => current.map((entry) => entry.menuItemId === updated.menuItemId ? updated : entry));
      setStockDrafts((current) => {
        const next = { ...current };
        delete next[item.menuItemId];
        return next;
      });
      onToast(`Estoque de ${item.name} atualizado.`);
    } catch (updateError) {
      onToast(updateError.message);
    } finally {
      setSavingStockId(null);
    }
  }

  const activeCount = orders.filter((order) => !["DELIVERED", "CANCELLED"].includes(order.status)).length;
  const revenue = orders.filter((order) => order.status !== "CANCELLED").reduce((sum, order) => sum + order.total, 0);
  const deliveredCount = orders.filter((order) => order.status === "DELIVERED").length;
  const averageTicket = deliveredCount
    ? orders.filter((order) => order.status === "DELIVERED").reduce((sum, order) => sum + order.total, 0) / deliveredCount
    : 0;
  const dailySales = useMemo(() => {
    const days = Array.from({ length: 7 }, (_, index) => {
      const date = new Date();
      date.setHours(0, 0, 0, 0);
      date.setDate(date.getDate() - (6 - index));
      return { date, label: date.toLocaleDateString("pt-BR", { weekday: "short" }), total: 0, count: 0 };
    });
    for (const order of orders) {
      if (order.status === "CANCELLED") continue;
      const created = new Date(order.createdAt);
      const day = days.find((item) => item.date.toDateString() === created.toDateString());
      if (day) {
        day.total += order.total;
        day.count += 1;
      }
    }
    const maximum = Math.max(1, ...days.map((day) => day.total));
    return days.map((day) => ({ ...day, height: Math.max(day.total ? 8 : 3, (day.total / maximum) * 100) }));
  }, [orders]);
  const topProducts = useMemo(() => {
    const products = new Map();
    for (const order of orders) {
      if (order.status === "CANCELLED") continue;
      for (const item of order.items) {
        products.set(item.name, (products.get(item.name) ?? 0) + item.quantity);
      }
    }
    return [...products.entries()].sort((left, right) => right[1] - left[1]).slice(0, 5);
  }, [orders]);

  function exportSales() {
    const escapeCell = (value) => {
      const safe = String(value ?? "").replace(/^[=+\-@]/, "'$&");
      return `"${safe.replaceAll('"', '""')}"`;
    };
    const rows = [
      ["Pedido", "Data", "Restaurante", "Subtotal", "Entrega", "Desconto", "Total", "Status", "Pagamento"],
      ...orders.map((order) => [
        order.id,
        new Date(order.createdAt).toLocaleString("pt-BR"),
        order.restaurant.name,
        order.subtotal,
        order.deliveryFee,
        order.discount,
        order.total,
        statusLabel(order.status),
        order.paymentMethod,
      ]),
    ];
    const csv = `\uFEFF${rows.map((row) => row.map(escapeCell).join(";")).join("\r\n")}`;
    const url = URL.createObjectURL(new Blob([csv], { type: "text/csv;charset=utf-8" }));
    const link = document.createElement("a");
    link.href = url;
    link.download = `bora-relatorio-pedidos-${new Date().toISOString().slice(0, 10)}.csv`;
    link.click();
    URL.revokeObjectURL(url);
  }

  return (
    <main className="page-shell admin-page">
      <div className="page-heading"><div><span className="section-kicker">PAINEL DO RESTAURANTE</span><h1>Operação do dia</h1><p>Acompanhe pedidos e atualize cada etapa da entrega.</p></div><button className="secondary-button compact" onClick={() => setRefresh((value) => value + 1)}>↻ Atualizar</button></div>
      <nav className="dashboard-tabs" aria-label="Seções do painel">
        {[["overview", "Visão geral"], ["orders", "Pedidos"], ["stock", "Estoque"], ["finance", "Financeiro"], ["fiscal", "Fiscal"]].map(([value, label]) => (
          <button key={value} className={section === value ? "selected" : ""} onClick={() => setSection(value)}>{label}</button>
        ))}
      </nav>
      {error && <div className="inline-error">{error} — verifique se o backend Java está rodando.</div>}
      <div className="metric-grid">
        <article className="metric-card"><span>Pedidos no sistema</span><strong>{orders.length}</strong><small>Total desde a última inicialização</small></article>
        <article className="metric-card"><span>Em andamento</span><strong>{activeCount}</strong><small>Pedidos ainda não concluídos</small></article>
        <article className="metric-card"><span>Volume bruto de pedidos</span><strong>{formatMoney(revenue)}</strong><small>Não representa repasse ou saldo bancário</small></article>
        <article className="metric-card"><span>Ticket médio entregue</span><strong>{formatMoney(averageTicket)}</strong><small>{deliveredCount} pedidos entregues</small></article>
      </div>
      {section === "overview" && (
        <div className="dashboard-insights">
          <section className="insight-card">
            <div className="admin-table-heading"><div><h2>Movimento dos últimos 7 dias</h2><span>Pedidos não cancelados · valor bruto</span></div></div>
            <div className="sales-chart" role="img" aria-label="Gráfico de volume diário de pedidos nos últimos sete dias">
              {dailySales.map((day) => <div className="sales-chart-day" key={day.date.toISOString()} title={`${day.label}: ${formatMoney(day.total)} · ${day.count} pedidos`}>
                <strong>{day.total > 0 ? formatMoney(day.total) : "—"}</strong><div><i style={{ height: `${day.height}%` }} /></div><span>{day.label}</span>
              </div>)}
            </div>
          </section>
          <section className="insight-card">
            <div className="admin-table-heading"><div><h2>Mais pedidos</h2><span>Quantidade de unidades nos pedidos não cancelados</span></div></div>
            {topProducts.length
              ? <ol className="top-products">{topProducts.map(([name, count]) => <li key={name}><span>{name}</span><strong>{count} un.</strong></li>)}</ol>
              : <p className="empty-insight">Os itens mais pedidos aparecem aqui quando houver vendas.</p>}
          </section>
        </div>
      )}
      {section === "finance" && (
        <section className="admin-orders finance-panel">
          <div className="admin-table-heading"><div><h2>Relatório de pedidos</h2><span>Exportação informativa; não é extrato bancário nem conciliação.</span></div><button className="secondary-button compact" onClick={exportSales} disabled={!orders.length}>Baixar CSV</button></div>
          <div className="finance-notice"><strong>Financeiro demonstrativo</strong><p>O Bora ainda não recebe confirmações de pagamento nem dados de liquidação de bancos ou adquirentes. Os valores abaixo são calculados a partir dos pedidos registrados.</p></div>
          <div className="table-scroll"><table>
            <thead><tr><th>Data</th><th>Pedido</th><th>Restaurante</th><th>Subtotal</th><th>Taxa</th><th>Desconto</th><th>Total</th><th>Status</th></tr></thead>
            <tbody>{orders.slice(0, 100).map((order) => <tr key={order.id}>
              <td>{new Date(order.createdAt).toLocaleString("pt-BR")}</td><td>#{order.id}</td><td>{order.restaurant.name}</td>
              <td>{formatMoney(order.subtotal)}</td><td>{formatMoney(order.deliveryFee)}</td><td>{formatMoney(order.discount)}</td>
              <td>{formatMoney(order.total)}</td><td>{statusLabel(order.status)}</td>
            </tr>)}</tbody>
          </table></div>
        </section>
      )}
      {section === "stock" && (
        <section className="admin-orders inventory-panel">
          <div className="admin-table-heading"><div><h2>Estoque do catálogo</h2><span>Produtos de demonstração · {inventoryTotalItems.toLocaleString("pt-BR")} itens</span></div></div>
          <div className="finance-notice"><strong>Estoque demonstrativo</strong><p>As quantidades controlam a disponibilidade do catálogo e são reservadas ao confirmar um pedido. Cancelamentos elegíveis devolvem as unidades ao estoque.</p></div>
          {inventoryError && <div className="inline-error">{inventoryError}</div>}
          {inventoryLoading && !inventory.length && <div className="state-card"><div className="loading-spinner" /><p>Carregando estoque…</p></div>}
          {!inventoryLoading && !inventory.length && !inventoryError && <div className="state-card"><p>Nenhum produto encontrado.</p></div>}
          {inventory.length > 0 && <div className="table-scroll"><table>
            <thead><tr><th>Produto</th><th>Restaurante</th><th>Preço</th><th>Disponibilidade</th><th>Unidades</th><th></th></tr></thead>
            <tbody>{inventory.map((item) => <tr key={item.menuItemId}>
              <td>{item.name}<small>{item.category}</small></td>
              <td>{item.restaurantName}</td>
              <td>{formatMoney(item.price)}</td>
              <td><span className={`stock-pill ${item.stockQuantity === 0 ? "out" : item.lowStock ? "low" : "in"}`}>{item.stockQuantity === 0 ? "Esgotado" : item.lowStock ? "Estoque baixo" : "Disponível"}</span></td>
              <td><input className="stock-input" type="number" min="0" max="100000" value={stockDrafts[item.menuItemId] ?? item.stockQuantity} onChange={(event) => setStockDrafts((current) => ({ ...current, [item.menuItemId]: event.target.value }))} aria-label={`Quantidade em estoque de ${item.name}`} /></td>
              <td><button className="secondary-button compact" disabled={savingStockId === item.menuItemId} onClick={() => saveStock(item)}>{savingStockId === item.menuItemId ? "Salvando…" : "Salvar"}</button></td>
            </tr>)}</tbody>
          </table></div>}
          <div className="inventory-pagination"><button className="secondary-button compact" disabled={inventoryPage === 0} onClick={() => setInventoryPage((page) => page - 1)}>Anterior</button><span>Página {inventoryTotalPages ? inventoryPage + 1 : 0} de {inventoryTotalPages}</span><button className="secondary-button compact" disabled={inventoryPage + 1 >= inventoryTotalPages} onClick={() => setInventoryPage((page) => page + 1)}>Próxima</button></div>
        </section>
      )}
      {section === "fiscal" && (
        <section className="admin-orders fiscal-panel">
          <span className="section-kicker">DOCUMENTOS FISCAIS</span><h2>Emissão fiscal ainda não configurada</h2>
          <p>Esta tela não emite nota fiscal. Para emitir documentos válidos, é necessário definir o enquadramento fiscal, os dados do estabelecimento e integrar um provedor autorizado pelo município/SEFAZ, com certificado digital e credenciais protegidas.</p>
          <ul><li>NFS-e ou NF-e conforme a operação e orientação contábil.</li><li>CNPJ, inscrição estadual/municipal, regime tributário e série fiscal do estabelecimento.</li><li>Certificado digital e homologação com um provedor contratado.</li></ul>
          <div className="finance-notice"><strong>Não use os pedidos demonstrativos para fins contábeis.</strong><p>O projeto usa dados e pagamentos de demonstração; nenhum documento fiscal foi emitido.</p></div>
        </section>
      )}
      {section === "orders" && <section className="admin-orders">
        <div className="admin-table-heading"><h2>Pedidos recentes</h2><span>Atualiza automaticamente</span></div>
        {!orders.length && !error && <div className="state-card"><div className="loading-spinner" /><p>Carregando pedidos…</p></div>}
        {orders.length > 0 && (
          <div className="table-scroll"><table>
            <thead><tr><th>Pedido</th><th>Cliente</th><th>Restaurante</th><th>Total</th><th>Status</th><th>Atualizar status</th></tr></thead>
            <tbody>{orders.map((order) => (
              <tr key={order.id}>
                <td>#{order.id}<small>{new Date(order.createdAt).toLocaleString("pt-BR")}</small></td>
                <td>{order.customerName}<small>{order.deliveryAddress}</small></td>
                <td>{order.restaurant.name}</td>
                <td>{formatMoney(order.total)}</td>
                <td><span className={`status-pill status-${order.status.toLowerCase()}`}>{statusLabel(order.status)}</span></td>
                <td><select value={order.status} disabled={busyId === order.id} onChange={(event) => changeStatus(order.id, event.target.value)} aria-label={`Atualizar status do pedido ${order.id}`}>{ORDER_STATUSES.map((status) => <option value={status.value} key={status.value}>{status.label}</option>)}</select></td>
              </tr>
            ))}</tbody>
          </table></div>
        )}
      </section>}
    </main>
  );
}
