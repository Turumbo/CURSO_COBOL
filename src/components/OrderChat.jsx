import { useEffect, useRef, useState } from "react";
import { api } from "../services/api.js";

export default function OrderChat({ orderId, user, closed, onToast }) {
  const [messages, setMessages] = useState([]);
  const [text, setText] = useState("");
  const [loading, setLoading] = useState(true);
  const [sending, setSending] = useState(false);
  const [scrollRequest, setScrollRequest] = useState(0);
  const messagesContainerRef = useRef(null);
  const messagesRef = useRef(messages);
  const shouldScrollToBottomRef = useRef(false);
  messagesRef.current = messages;

  useEffect(() => {
    let active = true;
    async function refresh() {
      try {
        const result = await api.getChatMessages(orderId);
        if (active) {
          const currentMessages = messagesRef.current;
          const currentIds = new Set(currentMessages.map((message) => message.id));
          const hasNewMessages = result.some((message) => !currentIds.has(message.id));
          const hasChanges = result.length !== currentMessages.length
            || result.some((message, index) => {
              const current = currentMessages[index];
              return !current
                || message.id !== current.id
                || message.message !== current.message
                || message.sentAt !== current.sentAt
                || message.senderName !== current.senderName
                || message.senderRole !== current.senderRole
                || message.senderAccountId !== current.senderAccountId;
            });

          if (hasChanges) {
            if (hasNewMessages) {
              const container = messagesContainerRef.current;
              if (container) {
                const distanceFromBottom = container.scrollHeight - container.scrollTop - container.clientHeight;
                if (distanceFromBottom <= 80) shouldScrollToBottomRef.current = true;
              }
            }
            messagesRef.current = result;
            setMessages(result);
          }
        }
      } catch (error) {
        if (active) onToast(`Chat indisponível: ${error.message}`);
      } finally {
        if (active) setLoading(false);
      }
    }
    refresh();
    const timer = window.setInterval(refresh, 3000);
    return () => {
      active = false;
      window.clearInterval(timer);
    };
  }, [orderId, onToast]);

  useEffect(() => {
    if (!shouldScrollToBottomRef.current) return;
    shouldScrollToBottomRef.current = false;
    const container = messagesContainerRef.current;
    if (container) {
      container.scrollTo({ top: container.scrollHeight, behavior: "smooth" });
    }
  }, [messages, scrollRequest]);

  async function send(event) {
    event.preventDefault();
    if (!text.trim() || sending || closed) return;
    setSending(true);
    try {
      const message = await api.sendChatMessage(orderId, text);
      shouldScrollToBottomRef.current = true;
      setScrollRequest((current) => current + 1);
      setMessages((current) => (
        current.some((existing) => existing.id === message.id)
          ? current
          : [...current, message]
      ));
      setText("");
    } catch (error) {
      onToast(error.message);
    } finally {
      setSending(false);
    }
  }

  return (
    <section className="conversation-card">
      <div className="conversation-heading"><div><span className="section-kicker">CANAL DO PEDIDO</span><h2>Fale com a loja</h2></div><span className="conversation-online"><i /> Chat {closed ? "encerrado" : "ativo"}</span></div>
      <div className="chat-messages" ref={messagesContainerRef}>
        {loading && <span className="chat-note">Carregando conversa…</span>}
        {!loading && !messages.length && <span className="chat-note">Ainda não há mensagens. A loja está pronta para ajudar.</span>}
        {messages.map((message) => (
          <div className={`chat-row ${message.senderAccountId === user?.id ? "mine" : ""}`} key={message.id}>
            <div className="chat-bubble"><span>{message.senderName} · {message.senderRole === "CUSTOMER" ? "Você" : "Restaurante"}</span><p>{message.message}</p><small>{new Date(message.sentAt).toLocaleTimeString("pt-BR", { hour: "2-digit", minute: "2-digit" })}</small></div>
          </div>
        ))}
      </div>
      {closed
        ? <p className="chat-closed">A conversa deste pedido foi encerrada.</p>
        : <form className="chat-compose" onSubmit={send}><input value={text} onChange={(event) => setText(event.target.value)} maxLength={1000} placeholder="Escreva sua mensagem…" aria-label="Mensagem" /><button disabled={!text.trim() || sending}>{sending ? "…" : "Enviar"}</button></form>}
    </section>
  );
}
