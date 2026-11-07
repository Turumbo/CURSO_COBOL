import { useEffect, useMemo, useRef } from "react";
import {
  CircleMarker,
  MapContainer,
  Marker,
  Polyline,
  TileLayer,
  useMap,
  useMapEvents,
} from "react-leaflet";
import L from "leaflet";

const restaurantIcon = L.divIcon({
  className: "",
  html: '<span class="map-marker restaurant-marker"><span>🍽️</span></span>',
  iconSize: [36, 36],
  iconAnchor: [18, 30],
});

const riderIcon = L.divIcon({
  className: "",
  html: '<span class="map-marker rider-marker"><span>🛵</span></span>',
  iconSize: [42, 42],
  iconAnchor: [21, 21],
});

const destinationIcon = L.divIcon({
  className: "",
  html: '<span class="map-marker destination-marker"><span>⌂</span></span>',
  iconSize: [36, 36],
  iconAnchor: [18, 30],
});

function MapClickHandler({ enabled, onSelect }) {
  useMapEvents({
    click(event) {
      if (enabled) onSelect?.({ lat: event.latlng.lat, lng: event.latlng.lng });
    },
  });
  return null;
}

function FitRoute({ path }) {
  const map = useMap();
  useEffect(() => {
    if (path.length > 1) {
      map.fitBounds(path, { padding: [30, 30], maxZoom: 15 });
    }
  }, [map, path]);
  return null;
}

function RecenterMap({ location }) {
  const map = useMap();
  useEffect(() => {
    if (location) map.setView([location.lat, location.lng], Math.max(map.getZoom(), 14));
  }, [location, map]);
  return null;
}

function SmoothRider({ path, startedAt, durationMs, active }) {
  const map = useMap();
  const markerRef = useRef(null);
  const progressLineRef = useRef(null);
  const segmentLengths = useMemo(() => path.slice(1).map((point, index) =>
    L.latLng(path[index]).distanceTo(L.latLng(point))), [path]);

  useEffect(() => {
    if (path.length < 2) return undefined;
    const origin = path[0];
    markerRef.current = L.marker(origin, { icon: riderIcon, zIndexOffset: 1000 }).addTo(map);
    progressLineRef.current = L.polyline([origin], { color: "#2874e8", weight: 6 }).addTo(map);
    let animationFrame = 0;
    const totalLength = segmentLengths.reduce((sum, length) => sum + length, 0);

    function update() {
      const progress = !active ? 0 : Math.min(1, Math.max(0, (Date.now() - startedAt) / durationMs));
      let remaining = totalLength * progress;
      let position = path[0];
      let traveled = [path[0]];
      for (let index = 0; index < segmentLengths.length; index += 1) {
        const from = L.latLng(path[index]);
        const to = L.latLng(path[index + 1]);
        const length = segmentLengths[index];
        if (remaining >= length) {
          traveled.push(path[index + 1]);
          remaining -= length;
          position = path[index + 1];
          continue;
        }
        const ratio = length ? remaining / length : 0;
        position = [from.lat + (to.lat - from.lat) * ratio, from.lng + (to.lng - from.lng) * ratio];
        if (progress > 0) traveled.push(position);
        break;
      }
      markerRef.current?.setLatLng(position);
      progressLineRef.current?.setLatLngs(traveled);
      if (active && progress < 1) animationFrame = window.requestAnimationFrame(update);
    }

    animationFrame = window.requestAnimationFrame(update);
    return () => {
      window.cancelAnimationFrame(animationFrame);
      if (markerRef.current && map.hasLayer(markerRef.current)) map.removeLayer(markerRef.current);
      if (progressLineRef.current && map.hasLayer(progressLineRef.current)) map.removeLayer(progressLineRef.current);
    };
  }, [active, durationMs, map, path, segmentLengths, startedAt]);

  return null;
}

export default function MapPicker({
  center,
  location,
  onSelect,
  path = [],
  trackingStartedAt = 0,
  trackingActive = false,
  interactive = false,
  height = 300,
}) {
  return (
    <div className="map-frame" style={{ height }}>
      <MapContainer center={center} zoom={13} scrollWheelZoom className="leaflet-map">
        <RecenterMap location={interactive ? location : null} />
        <TileLayer
          attribution='&copy; <a href="https://www.openstreetmap.org/copyright" target="_blank" rel="noreferrer">OpenStreetMap</a>'
          url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
        />
        <MapClickHandler enabled={interactive} onSelect={onSelect} />
        {path.length > 1 && <FitRoute path={path} />}
        {path.length > 1 && (
          <>
            <Polyline positions={path} pathOptions={{ color: "#f04438", weight: 5, opacity: 0.7 }} />
            <Marker position={path[0]} icon={restaurantIcon} />
            <Marker position={path[path.length - 1]} icon={destinationIcon} />
            <SmoothRider
              path={path}
              startedAt={trackingStartedAt}
              durationMs={180000}
              active={trackingActive}
            />
          </>
        )}
        {location && (
          <CircleMarker
            center={[location.lat, location.lng]}
            radius={9}
            pathOptions={{ color: "#fff", weight: 3, fillColor: "#168a50", fillOpacity: 1 }}
          />
        )}
      </MapContainer>
      {interactive && (
        <span className="map-hint">
          {location ? "Destino marcado · toque para alterar" : "Toque no mapa para marcar o destino"}
        </span>
      )}
    </div>
  );
}
