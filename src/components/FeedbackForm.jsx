import { useEffect, useState } from "react";
import { api } from "../services/api.js";

export default function FeedbackForm({ orderId, onToast }) {
  const [rating, setRating] = useState(0);
  const [comment, setComment] = useState("");
  const [existing, setExisting] = useState(null);
  const [busy, setBusy] = useState(false);

  useEffect(() => {
    let active = true;
    api.getReview(orderId).then((review) => {
      if (active) setExisting(review);
    }).catch((error) => onToast(`Não foi possível consultar a avaliação: ${error.message}`));
    return () => { active = false; };
  }, [orderId, onToast]);

  async function submit(event) {
    event.preventDefault();
    if (!rating) {
      onToast("Escolha uma nota de 1 a 5.");
      return;
    }
    setBusy(true);
    try {
      setExisting(await api.createReview({ orderId, rating, comment }));
      onToast("Obrigado! Sua avaliação foi enviada.");
    } catch (error) {
      onToast(error.message);
    } finally {
      setBusy(false);
    }
  }

  return (
    <section className="feedback-card">
      <span className="section-kicker">SUA OPINIÃO IMPORTA</span>
      <h2>{existing ? "Obrigado por avaliar!" : "Como foi seu pedido?"}</h2>
      {existing
        ? <div className="saved-review"><span>{"★".repeat(existing.rating)}{"☆".repeat(5 - existing.rating)}</span><p>{existing.comment || "Sem comentário."}</p></div>
        : <form onSubmit={submit}><div className="rating-picker" role="radiogroup" aria-label="Nota do pedido">{[1, 2, 3, 4, 5].map((value) => <button key={value} type="button" className={value <= rating ? "selected" : ""} onClick={() => setRating(value)} aria-label={`${value} estrelas`} aria-pressed={rating === value}>★</button>)}</div><textarea value={comment} onChange={(event) => setComment(event.target.value)} maxLength={1200} placeholder="Conte o que achou da comida e da entrega (opcional)" /><button className="primary-button feedback-submit" disabled={busy}>{busy ? "Enviando…" : "Enviar avaliação"}</button></form>}
      <p className="demo-disclaimer">Avaliação disponível apenas para pedidos concluídos e para a conta que fez o pedido.</p>
    </section>
  );
}
