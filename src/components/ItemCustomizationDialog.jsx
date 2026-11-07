import { useState } from "react";
import { formatMoney } from "../utils/format.js";

export default function ItemCustomizationDialog({ item, onClose, onAdd }) {
  const [selected, setSelected] = useState([]);
  if (!item) return null;

  const extra = (item.customizationOptions ?? [])
    .filter((option) => selected.includes(option.name))
    .reduce((sum, option) => sum + option.price, 0);

  function toggle(option) {
    setSelected((current) => current.includes(option.name)
      ? current.filter((name) => name !== option.name)
      : current.length >= 5 ? current : [...current, option.name]);
  }

  return (
    <div className="overlay auth-overlay" onMouseDown={(event) => event.target === event.currentTarget && onClose()}>
      <section className="customize-dialog" role="dialog" aria-modal="true" aria-labelledby="customize-title">
        <button className="icon-button auth-close" onClick={onClose} aria-label="Fechar">×</button>
        <span className="section-kicker">FEITO DO SEU JEITO</span>
        <h2 id="customize-title">{item.name}</h2>
        <p>{item.description}</p>
        <span className="customize-base-price">A partir de {formatMoney(item.price)}</span>
        <h3>Deixe ainda mais gostoso <small>Escolha até 5 adicionais</small></h3>
        <div className="customize-options">
          {(item.customizationOptions ?? []).map((option) => (
            <label className="customize-option" key={option.name}>
              <span><strong>{option.name}</strong><small>{option.price === 0 ? "Sem custo extra" : `+ ${formatMoney(option.price)}`}</small></span>
              <input type="checkbox" checked={selected.includes(option.name)} onChange={() => toggle(option)} disabled={!selected.includes(option.name) && selected.length >= 5} />
            </label>
          ))}
          {!item.customizationOptions?.length && <p>Este item não tem adicionais disponíveis no momento.</p>}
        </div>
        <button className="primary-button checkout-button" onClick={() => onAdd(selected, item.price + extra)}>
          Adicionar à sacola · {formatMoney(item.price + extra)}
        </button>
      </section>
    </div>
  );
}
