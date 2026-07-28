const trip = window.TRIP_DATA;
const stops = trip.stops;
const matrix = trip.routeMatrix;
const pairs = trip.pairRoutes;
const routeHighlights = trip.routeHighlights || [];
const routePresets = trip.routePresets || [];
const routePresetByKey = Object.fromEntries(routePresets.map(preset => [preset.key, preset]));
const legScenery = trip.legScenery || {};
const defaultPreset = routePresets.find(preset => preset.default) || routePresets[0] || null;
const includeElevation = Boolean(trip.includeElevation);
const ELEVATION_EXPAND_THRESHOLD_METERS = 2000;
const stopElevations = stops
  .map(stop => Number(stop.elevation))
  .filter(Number.isFinite);
const allStopsBelowElevationThreshold = stopElevations.length > 0
  && stopElevations.every(elevation => elevation < ELEVATION_EXPAND_THRESHOLD_METERS);
const fixedStart = Number.isInteger(trip.startIndex) ? trip.startIndex : null;
const fixedEnd = Number.isInteger(trip.endIndex) ? trip.endIndex : null;
const selected = new Set(
  defaultPreset
    ? defaultPreset.order
    : stops.map((stop, index) => stop.selected ? index : -1).filter(index => index >= 0)
);

const el = {
  title: document.querySelector('#tripTitle'),
  subtitle: document.querySelector('#tripSubtitle'),
  planner: document.querySelector('#planner'),
  plannerToggle: document.querySelector('#plannerToggle'),
  orderMode: document.querySelector('#orderMode'),
  plannerHint: document.querySelector('#plannerHint'),
  routeChoice: document.querySelector('#routeChoice'),
  routePreset: document.querySelector('#routePresetSelect'),
  routePresetInfo: document.querySelector('#routePresetInfo'),
  chips: document.querySelector('#chips'),
  recommended: document.querySelector('#recommendedBtn'),
  shortest: document.querySelector('#shortestBtn'),
  matrix: document.querySelector('#matrixBtn'),
  matrixDialog: document.querySelector('#matrixDialog'),
  matrixClose: document.querySelector('#matrixClose'),
  matrixTable: document.querySelector('#matrixTable'),
  highlights: document.querySelector('#highlightsBtn'),
  highlightsDialog: document.querySelector('#highlightsDialog'),
  highlightsClose: document.querySelector('#highlightsClose'),
  highlightsOverview: document.querySelector('#highlightsOverview'),
  highlightsList: document.querySelector('#highlightsList'),
  fit: document.querySelector('#fitBtn'),
  fullscreen: document.querySelector('#fullscreenBtn'),
  legs: document.querySelector('#legs'),
  total: document.querySelector('#total'),
  imageExport: document.querySelector('#imageExportBtn'),
  pdfExport: document.querySelector('#pdfExportBtn'),
  exportStatus: document.querySelector('#exportStatus'),
  detail: document.querySelector('#detail'),
  detailClose: document.querySelector('#detailClose'),
  detailDuration: document.querySelector('#detailDuration'),
  detailName: document.querySelector('#detailName'),
  detailDescription: document.querySelector('#detailDescription'),
  detailPrecautions: document.querySelector('#detailPrecautions'),
  detailStay: document.querySelector('#detailStay'),
  detailSources: document.querySelector('#detailSources'),
  profileWrap: document.querySelector('#profileWrap'),
  profileToggle: document.querySelector('#profileToggle'),
  profileInfo: document.querySelector('#profileInfo'),
  profileMeta: document.querySelector('#profileMeta'),
  profile: document.querySelector('#profile'),
  hoverTip: document.querySelector('#hoverTip'),
};

el.title.textContent = trip.title;
el.subtitle.textContent = trip.subtitle || '选择候选地点，使用预载车程矩阵自动生成路线';
if (fixedStart != null && fixedEnd != null) {
  el.plannerHint.textContent = `起点 ${stops[fixedStart].name}、终点 ${stops[fixedEnd].name} 固定；拖动中途景点调整顺序。`;
}
document.title = trip.title;

const map = L.map('map', { zoomControl: true, preferCanvas: true });
L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
  maxZoom: 18,
  attribution: '© OpenStreetMap',
}).addTo(map);

const routeLayer = L.layerGroup().addTo(map);
const markerLayer = L.layerGroup().addTo(map);
const markers = [];
let routeState = null;
let routeOrder = [];
let activePreset = defaultPreset?.key || null;
let manualOrder = defaultPreset ? [...defaultPreset.order] : null;
let manualOrderMode = defaultPreset ? 'preset' : 'manual';
let hoverMarker = null;
let currentDetailIndex = null;
let resizeTimer = null;
let profileCollapsed = includeElevation && allStopsBelowElevationThreshold;

function syncProfileVisibility() {
  if (!includeElevation) return;
  el.profileWrap.classList.toggle('profile-collapsed', profileCollapsed);
  el.profileToggle.textContent = profileCollapsed ? '展开' : '收起';
  el.profileToggle.setAttribute('aria-expanded', String(!profileCollapsed));
  if (profileCollapsed) {
    el.profileInfo.textContent = allStopsBelowElevationThreshold
      ? '海拔剖面 · 全部地点低于 2000m'
      : '海拔剖面';
    return;
  }
  requestAnimationFrame(() => {
    sizeCanvas();
    drawProfile();
    map.invalidateSize();
  });
}

function pairKey(a, b) {
  return `${Math.min(a, b)}-${Math.max(a, b)}`;
}

function pairData(a, b) {
  const source = pairs[pairKey(a, b)];
  const reversed = a > b;
  const coordinates = reversed
    ? [...source.coordinates].reverse()
    : source.coordinates;
  let elevationProfile = source.elevationProfile || [];
  if (reversed && elevationProfile.length) {
    const total = elevationProfile[elevationProfile.length - 1].distanceKm || 0;
    elevationProfile = [...elevationProfile].reverse().map(point => ({
      ...point,
      distanceKm: Math.max(0, total - point.distanceKm),
    }));
  }
  return { ...source, coordinates, elevationProfile };
}

function routeWeight(a, b) {
  return matrix.durationsSeconds[a][b]
    ?? matrix.distancesMeters[a][b]
    ?? Number.MAX_SAFE_INTEGER;
}

function routeCost(order) {
  return order.slice(1).reduce(
    (sum, current, index) => sum + routeWeight(order[index], current),
    0
  );
}

function optimizeSelection() {
  const all = [...selected].sort((a, b) => a - b);
  const hasFixedEndpoints = fixedStart != null && fixedEnd != null;
  const candidates = hasFixedEndpoints
    ? all.filter(index => index !== fixedStart && index !== fixedEnd)
    : all;
  if (!hasFixedEndpoints && candidates.length <= 2) return candidates;
  const order = hasFixedEndpoints ? [fixedStart] : [candidates.shift()];
  while (candidates.length) {
    const last = order[order.length - 1];
    let bestPosition = 0;
    let bestWeight = Number.MAX_SAFE_INTEGER;
    candidates.forEach((candidate, position) => {
      const weight = routeWeight(last, candidate);
      if (weight < bestWeight) {
        bestWeight = weight;
        bestPosition = position;
      }
    });
    order.push(candidates.splice(bestPosition, 1)[0]);
  }
  if (hasFixedEndpoints) order.push(fixedEnd);

  let improved = true;
  let best = order;
  while (improved) {
    improved = false;
    const before = routeCost(best);
    for (let from = 1; from < best.length - 1; from += 1) {
      const toLimit = hasFixedEndpoints ? best.length - 1 : best.length;
      for (let to = from + 1; to < toLimit; to += 1) {
        const candidate = [
          ...best.slice(0, from),
          ...best.slice(from, to + 1).reverse(),
          ...best.slice(to + 1),
        ];
        if (routeCost(candidate) + 1 < before) {
          best = candidate;
          improved = true;
          break;
        }
      }
      if (improved) break;
    }
  }
  return best;
}

function distanceWeight(a, b) {
  return Number(matrix.distancesMeters[a][b] ?? Number.MAX_SAFE_INTEGER);
}

function shortestDistanceOrder() {
  const all = [...selected].sort((a, b) => a - b);
  if (all.length <= 1) return all;

  const start = fixedStart != null && selected.has(fixedStart) ? fixedStart : null;
  const end = fixedEnd != null && selected.has(fixedEnd) ? fixedEnd : null;
  const middle = all.filter(index => index !== start && index !== end);
  if (!middle.length) return [start, end].filter(index => index != null);

  const count = middle.length;
  const stateCount = 1 << count;
  const costs = Array.from(
    { length: stateCount },
    () => new Float64Array(count).fill(Number.POSITIVE_INFINITY)
  );
  const parents = Array.from(
    { length: stateCount },
    () => new Int16Array(count).fill(-1)
  );

  middle.forEach((stopIndex, position) => {
    costs[1 << position][position] = start == null
      ? 0
      : distanceWeight(start, stopIndex);
  });

  for (let mask = 1; mask < stateCount; mask += 1) {
    for (let last = 0; last < count; last += 1) {
      if (!(mask & (1 << last))) continue;
      const currentCost = costs[mask][last];
      if (!Number.isFinite(currentCost)) continue;
      for (let next = 0; next < count; next += 1) {
        if (mask & (1 << next)) continue;
        const nextMask = mask | (1 << next);
        const nextCost = currentCost + distanceWeight(middle[last], middle[next]);
        if (nextCost < costs[nextMask][next]) {
          costs[nextMask][next] = nextCost;
          parents[nextMask][next] = last;
        }
      }
    }
  }

  const fullMask = stateCount - 1;
  let bestLast = -1;
  let bestCost = Number.POSITIVE_INFINITY;
  for (let last = 0; last < count; last += 1) {
    const endCost = end == null ? 0 : distanceWeight(middle[last], end);
    const totalCost = costs[fullMask][last] + endCost;
    if (totalCost < bestCost) {
      bestCost = totalCost;
      bestLast = last;
    }
  }
  if (bestLast < 0) return optimizeSelection();

  const sequence = [];
  let mask = fullMask;
  let cursor = bestLast;
  while (cursor >= 0) {
    sequence.push(middle[cursor]);
    const previous = parents[mask][cursor];
    mask ^= 1 << cursor;
    cursor = previous;
  }
  sequence.reverse();
  return [
    ...(start == null ? [] : [start]),
    ...sequence,
    ...(end == null ? [] : [end]),
  ];
}

function haversineKm(a, b) {
  const radians = value => value * Math.PI / 180;
  const lat1 = radians(a.lat);
  const lat2 = radians(b.lat);
  const dLat = lat2 - lat1;
  const dLon = radians(b.lon - a.lon);
  const value = Math.sin(dLat / 2) ** 2
    + Math.cos(lat1) * Math.cos(lat2) * Math.sin(dLon / 2) ** 2;
  return 6371 * 2 * Math.atan2(Math.sqrt(value), Math.sqrt(1 - value));
}

function buildRoute(order) {
  const legs = [];
  const coordinates = [];
  const profile = [];
  const stopDistances = new Map();
  let distanceMeters = 0;
  let durationSeconds = 0;
  let durationAvailable = true;

  if (order.length) stopDistances.set(order[0], 0);
  for (let index = 1; index < order.length; index += 1) {
    const from = order[index - 1];
    const to = order[index];
    const pair = pairData(from, to);
    const distance = Number(matrix.distancesMeters[from][to] ?? pair.distanceMeters ?? 0);
    const duration = matrix.durationsSeconds[from][to];
    const leg = { from, to, pair, distanceMeters: distance, durationSeconds: duration };
    legs.push(leg);

    const segmentCoordinates = coordinates.length
      ? pair.coordinates.slice(1)
      : pair.coordinates;
    coordinates.push(...segmentCoordinates);

    const baseDistanceKm = distanceMeters / 1000;
    const legProfile = pair.elevationProfile.length
      ? pair.elevationProfile
      : [
          { lat: stops[from].lat, lon: stops[from].lon, distanceKm: 0, elevation: stops[from].elevation },
          { lat: stops[to].lat, lon: stops[to].lon, distanceKm: distance / 1000, elevation: stops[to].elevation },
        ];
    legProfile.forEach((point, pointIndex) => {
      if (profile.length && pointIndex === 0) return;
      profile.push({ ...point, distanceKm: baseDistanceKm + point.distanceKm });
    });

    distanceMeters += distance;
    stopDistances.set(to, distanceMeters / 1000);
    if (duration == null) durationAvailable = false;
    else durationSeconds += Number(duration);
  }
  return {
    order,
    legs,
    coordinates,
    profile: profile.filter(point => point.elevation != null),
    stopDistances,
    distanceMeters,
    durationSeconds: durationAvailable ? durationSeconds : null,
  };
}

function colorForElevation(elevation) {
  if (elevation == null) return '#2b5fd9';
  if (elevation < 2500) return '#2f9e77';
  if (elevation < 3400) return '#9abb42';
  if (elevation < 4100) return '#e8b93c';
  if (elevation < 4500) return '#e07b39';
  return '#d1493f';
}

function formatDuration(seconds) {
  if (seconds == null) return '暂无车程';
  const minutes = Math.round(seconds / 60);
  const hours = Math.floor(minutes / 60);
  const rest = minutes % 60;
  return hours ? `${hours}小时${rest ? `${rest}分` : ''}` : `${rest}分钟`;
}

function formatDistance(meters) {
  return `${(Number(meters || 0) / 1000).toFixed(1)} km`;
}

function updatePresetInfo() {
  if (!routePresets.length) {
    el.routeChoice.hidden = true;
    return;
  }
  el.routeChoice.hidden = false;
  if (!activePreset || !routePresetByKey[activePreset]) {
    el.routePreset.value = 'custom';
    el.routePresetInfo.textContent = '已进入自定义模式；可点选地点或拖动中途景点。';
    return;
  }
  const preset = routePresetByKey[activePreset];
  el.routePreset.value = activePreset;
  el.routePresetInfo.innerHTML = `
    <span class="route-score">风景 ${preset.sceneryScore}</span> · ${preset.timing}<br>
    ${preset.summary}`;
}

function renderPresetOptions() {
  if (!routePresets.length) return;
  el.routePreset.innerHTML = routePresets
    .map(preset => `<option value="${preset.key}">${preset.name}</option>`)
    .join('');
  const custom = document.createElement('option');
  custom.value = 'custom';
  custom.textContent = '自定义途经点';
  el.routePreset.append(custom);
}

function applyRoutePreset(key) {
  const preset = routePresetByKey[key];
  if (!preset) {
    activePreset = null;
    manualOrderMode = 'manual';
    updatePresetInfo();
    return;
  }
  selected.clear();
  preset.order.forEach(index => selected.add(index));
  activePreset = key;
  manualOrder = [...preset.order];
  manualOrderMode = 'preset';
  render();
  fitCurrentRoute();
}

function sceneryForLeg(from, to) {
  return legScenery[pairKey(from, to)] || null;
}

function renderChips() {
  el.chips.innerHTML = '';
  const start = routeOrder[0];
  stops.forEach((stop, index) => {
    const isFixedStart = index === fixedStart;
    const isFixedEnd = index === fixedEnd;
    const button = document.createElement('button');
    button.type = 'button';
    button.className = `chip${selected.has(index) ? ' on' : ''}${start === index ? ' start' : ''}${isFixedEnd ? ' end' : ''}`;
    if (stop.featured) button.classList.add('featured');
    button.textContent = stop.name;
    button.disabled = isFixedStart || isFixedEnd;
    button.addEventListener('click', () => {
      if (isFixedStart || isFixedEnd) return;
      const current = [...routeOrder];
      if (selected.has(index)) {
        selected.delete(index);
        manualOrder = current.filter(stopIndex => stopIndex !== index);
      } else {
        selected.add(index);
        manualOrder = fixedEnd == null
          ? [...current, index]
          : [...current.slice(0, -1), index, fixedEnd];
      }
      activePreset = null;
      manualOrderMode = 'manual';
      render();
    });
    el.chips.append(button);
  });
}

function markerTooltip(index, position) {
  const stop = stops[index];
  const elevation = includeElevation && stop.elevation != null
    ? `<br>海拔 ${Math.round(stop.elevation)}m`
    : '';
  return `<b>${position ? `${position}. ` : ''}${stop.name}</b><br>建议游玩 ${stop.visitDuration}${elevation}<br><span>点击查看介绍</span>`;
}

function ensureMarkers() {
  if (!markers.length) {
    stops.forEach((stop, index) => {
      const marker = L.circleMarker([stop.lat, stop.lon], {
        radius: 6,
        weight: 2,
        color: '#1c2430',
        fillColor: '#ffd23f',
        fillOpacity: 1,
      }).addTo(markerLayer);
      marker.bindTooltip(`${stop.name}（点击查看攻略）`, {
        direction: 'top',
        className: 'stop-tooltip',
      });
      marker.on('click', () => openDetail(index));
      markers.push({ marker, number: null });
    });
  }
  markers.forEach(({ marker, number }, index) => {
    const position = routeOrder.indexOf(index);
    marker.setStyle({
      radius: selected.has(index) ? 8 : 5,
      color: selected.has(index) ? '#fff' : '#1c2430',
      fillColor: selected.has(index) ? '#2b5fd9' : '#ffd23f',
      opacity: selected.has(index) ? 0 : 1,
      fillOpacity: selected.has(index) ? 0 : 1,
    });
    marker.unbindTooltip().bindTooltip(markerTooltip(index, position + 1), {
      direction: 'top',
      className: 'stop-tooltip',
      offset: [0, -7],
    });
    if (number) markerLayer.removeLayer(number);
    const nextNumber = selected.has(index) ? L.marker([stops[index].lat, stops[index].lon], {
      interactive: true,
      icon: L.divIcon({
        className: 'numbered-stop',
        html: `<span>${position + 1}</span>`,
        iconSize: [24, 24],
        iconAnchor: [12, 12],
      }),
    }).addTo(markerLayer) : null;
    if (nextNumber) {
      nextNumber.bindTooltip(markerTooltip(index, position + 1), {
        direction: 'top',
        className: 'stop-tooltip',
        offset: [0, -12],
      });
      nextNumber.on('click', () => openDetail(index));
    }
    markers[index].number = nextNumber;
  });
}

function drawRoute(state) {
  routeLayer.clearLayers();
  state.legs.forEach(leg => {
    const profile = leg.pair.elevationProfile.filter(point => point.elevation != null);
    if (includeElevation && profile.length > 1) {
      for (let index = 1; index < profile.length; index += 1) {
        const previous = profile[index - 1];
        const current = profile[index];
        L.polyline(
          [[previous.lat, previous.lon], [current.lat, current.lon]],
          {
            color: colorForElevation((Number(previous.elevation) + Number(current.elevation)) / 2),
            weight: 5,
            opacity: 1,
            lineCap: 'round',
          }
        ).addTo(routeLayer);
      }
    } else {
      L.polyline(
        leg.pair.coordinates.map(([lon, lat]) => [lat, lon]),
        {
          color: leg.pair.status === 'osrm' ? '#2b5fd9' : '#8b96a3',
          weight: 5,
          opacity: .95,
          dashArray: leg.pair.status === 'osrm' ? null : '7 7',
        }
      ).addTo(routeLayer);
    }
  });
}

function renderLegs(state) {
  el.legs.innerHTML = '';
  if (!state.order.length) {
    el.legs.innerHTML = '<p class="hint">请从上方选择地点。</p>';
    el.total.textContent = '尚未生成路线';
    return;
  }
  state.order.forEach((stopIndex, position) => {
    const stop = stops[stopIndex];
    const isEndpoint = stopIndex === fixedStart || stopIndex === fixedEnd;
    const nextLeg = state.legs[position];
    const row = document.createElement('div');
    row.className = 'order-row';
    row.draggable = !isEndpoint;
    row.classList.toggle('fixed-endpoint', isEndpoint);
    row.dataset.stopIndex = String(stopIndex);
    const nextText = nextLeg
      ? `下一段 ≈${formatDuration(nextLeg.durationSeconds)} · ${formatDistance(nextLeg.distanceMeters)}`
      : '行程终点';
    const scenery = nextLeg ? sceneryForLeg(stopIndex, nextLeg.to) : null;
    row.innerHTML = `
      <span class="drag-handle${isEndpoint ? ' fixed' : ''}" aria-hidden="true">${stopIndex === fixedStart ? '起' : stopIndex === fixedEnd ? '终' : '⋮⋮'}</span>
      <span class="order-number">${position + 1}</span>
      <div class="order-copy">
        <div class="order-name">${stop.name} · 游玩 ${stop.visitDuration}</div>
        <div class="order-meta">${nextText} · 双击查看介绍</div>
        ${scenery ? `<div class="leg-scenery"><strong>下一段风景 ${scenery.sceneryScore}</strong> · ${scenery.description}<br><strong>路况：</strong>${scenery.roadAdvice}</div>` : ''}
      </div>`;
    const handle = row.querySelector('.drag-handle');
    let pointerTarget = null;
    handle.addEventListener('pointerdown', event => {
      if (isEndpoint) return;
      pointerTarget = stopIndex;
      row.classList.add('dragging');
      handle.setPointerCapture(event.pointerId);
      event.preventDefault();
    });
    handle.addEventListener('pointermove', event => {
      if (pointerTarget == null) return;
      document.querySelectorAll('.order-row.drag-over').forEach(item => item.classList.remove('drag-over'));
      const targetRow = document.elementFromPoint(event.clientX, event.clientY)?.closest('.order-row');
      if (targetRow && targetRow !== row) {
        targetRow.classList.add('drag-over');
        pointerTarget = Number(targetRow.dataset.stopIndex);
      }
    });
    handle.addEventListener('pointerup', event => {
      row.classList.remove('dragging');
      document.querySelectorAll('.order-row.drag-over').forEach(item => item.classList.remove('drag-over'));
      handle.releasePointerCapture(event.pointerId);
      const target = pointerTarget;
      pointerTarget = null;
      if (target != null && target !== stopIndex) reorderStop(stopIndex, target);
    });
    row.addEventListener('dragstart', event => {
      if (isEndpoint) {
        event.preventDefault();
        return;
      }
      row.classList.add('dragging');
      event.dataTransfer.effectAllowed = 'move';
      event.dataTransfer.setData('text/plain', String(stopIndex));
    });
    row.addEventListener('dragend', () => {
      row.classList.remove('dragging');
      document.querySelectorAll('.order-row.drag-over').forEach(item => item.classList.remove('drag-over'));
    });
    row.addEventListener('dragover', event => {
      event.preventDefault();
      row.classList.add('drag-over');
      event.dataTransfer.dropEffect = 'move';
    });
    row.addEventListener('dragleave', () => row.classList.remove('drag-over'));
    row.addEventListener('drop', event => {
      event.preventDefault();
      const source = Number(event.dataTransfer.getData('text/plain'));
      reorderStop(source, stopIndex);
    });
    row.addEventListener('dblclick', () => openDetail(stopIndex));
    el.legs.append(row);
  });
  const totalTime = state.durationSeconds == null
    ? '部分路段暂无车程'
    : `≈${formatDuration(state.durationSeconds)}`;
  el.total.textContent = `合计 ${totalTime} · ${formatDistance(state.distanceMeters)} · ${state.order.length} 个地点`;
  updatePresetInfo();
}

function reorderStop(source, target) {
  if (source === target) return;
  if (source === fixedStart || source === fixedEnd) return;
  const next = [...routeOrder];
  const sourcePosition = next.indexOf(source);
  const targetPosition = next.indexOf(target);
  if (sourcePosition < 0 || targetPosition < 0) return;
  next.splice(sourcePosition, 1);
  if (target === fixedStart) next.splice(1, 0, source);
  else if (target === fixedEnd) next.splice(next.length - 1, 0, source);
  else next.splice(next.indexOf(target), 0, source);
  manualOrder = next;
  activePreset = null;
  manualOrderMode = 'manual';
  render();
}

function fitCurrentRoute() {
  const desktop = window.innerWidth > 720 && !el.planner.classList.contains('collapsed');
  const options = desktop
    ? { paddingTopLeft: [334, 42], paddingBottomRight: [54, 42] }
    : { padding: [42, 42] };
  if (!routeState?.coordinates.length) {
    map.fitBounds(L.latLngBounds(stops.map(stop => [stop.lat, stop.lon])), options);
    return;
  }
  map.fitBounds(
    L.latLngBounds(routeState.coordinates.map(([lon, lat]) => [lat, lon])),
    options
  );
}

function openDetail(index) {
  currentDetailIndex = index;
  const stop = stops[index];
  el.detail.hidden = false;
  el.detailDuration.textContent = `建议游玩：${stop.visitDuration}`;
  el.detailName.textContent = stop.name;
  el.detailDescription.textContent = stop.description || stop.note;
  el.detailPrecautions.innerHTML = '';
  (stop.precautions || []).forEach(text => {
    const item = document.createElement('li');
    item.textContent = text;
    el.detailPrecautions.append(item);
  });
  el.detailStay.textContent = stop.stay ? `住宿：${stop.stay}` : '';
  el.detailSources.innerHTML = '';
  (stop.sources || []).forEach(source => {
    const link = document.createElement('a');
    link.href = source.url;
    link.target = '_blank';
    link.rel = 'noreferrer';
    link.textContent = `来源：${source.title}`;
    el.detailSources.append(link);
  });
}

function renderMatrix() {
  const head = stops.map(stop => `<th>${stop.name}</th>`).join('');
  const rows = stops.map((from, row) => {
    const cells = stops.map((to, column) => {
      if (row === column) return '<td>—</td>';
      return `<td>${formatDistance(matrix.distancesMeters[row][column])}<small>${formatDuration(matrix.durationsSeconds[row][column])}</small></td>`;
    }).join('');
    return `<tr><td>${from.name}</td>${cells}</tr>`;
  }).join('');
  el.matrixTable.innerHTML = `<table><thead><tr><th>地点</th>${head}</tr></thead><tbody>${rows}</tbody></table>`;
}

function renderHighlights() {
  if (!routeHighlights.length) {
    el.highlights.hidden = true;
    return;
  }
  el.highlightsOverview.textContent = trip.routeOverview
    || '只在正规服务区、停车区或明确开放的观景点停靠';
  el.highlightsList.innerHTML = '';
  const labels = {
    safe_stop: '正规停靠',
    drive_by: '沿途可见',
    optional_detour: '可选绕行',
  };
  routeHighlights.forEach(highlight => {
    const card = document.createElement('article');
    card.className = 'highlight-card';
    const type = document.createElement('span');
    type.className = `highlight-type ${highlight.type.replace('_', '-')}`;
    type.textContent = labels[highlight.type] || highlight.type;
    const road = document.createElement('div');
    road.className = 'highlight-road';
    road.textContent = highlight.road;
    const title = document.createElement('h3');
    title.textContent = highlight.name;
    const description = document.createElement('p');
    description.textContent = highlight.description;
    const advice = document.createElement('p');
    advice.className = 'highlight-advice';
    advice.textContent = `停靠建议：${highlight.stopAdvice}`;
    const sources = document.createElement('div');
    sources.className = 'highlight-sources';
    (highlight.sources || []).forEach(source => {
      const link = document.createElement('a');
      link.href = source.url;
      link.target = '_blank';
      link.rel = 'noreferrer';
      link.textContent = `来源：${source.title}`;
      sources.append(link);
    });
    card.append(type, road, title, description, advice, sources);
    el.highlightsList.append(card);
  });
}

function roundedRect(context, x, y, width, height, radius, fill, stroke) {
  context.beginPath();
  context.roundRect(x, y, width, height, radius);
  if (fill) {
    context.fillStyle = fill;
    context.fill();
  }
  if (stroke) {
    context.strokeStyle = stroke;
    context.lineWidth = 2;
    context.stroke();
  }
}

function canvasText(context, text, x, y, options = {}) {
  const {
    size = 28,
    weight = 400,
    color = '#1c2430',
    align = 'left',
    maxWidth,
  } = options;
  context.font = `${weight} ${size}px "PingFang SC", "Microsoft YaHei", sans-serif`;
  context.fillStyle = color;
  context.textAlign = align;
  context.textBaseline = 'alphabetic';
  context.fillText(String(text), x, y, maxWidth);
}

function drawExportRoute(context, state, x, y, width, height) {
  roundedRect(context, x, y, width, height, 24, '#f4f7f7', '#d8dee5');
  canvasText(context, '路线概览', x + 34, y + 50, { size: 25, weight: 650 });
  if (!state.coordinates.length) {
    canvasText(context, '至少选择两个地点后生成路线', x + width / 2, y + height / 2, {
      size: 23, color: '#68737f', align: 'center',
    });
    return;
  }
  const coordinates = state.coordinates;
  const lons = coordinates.map(point => point[0]);
  const lats = coordinates.map(point => point[1]);
  const minLon = Math.min(...lons);
  const maxLon = Math.max(...lons);
  const minLat = Math.min(...lats);
  const maxLat = Math.max(...lats);
  const padding = 72;
  const plotX = lon => x + padding + (lon - minLon) / Math.max(maxLon - minLon, .001) * (width - padding * 2);
  const plotY = lat => y + height - padding - (lat - minLat) / Math.max(maxLat - minLat, .001) * (height - padding * 2 - 22);

  context.beginPath();
  coordinates.forEach(([lon, lat], index) => {
    if (index) context.lineTo(plotX(lon), plotY(lat));
    else context.moveTo(plotX(lon), plotY(lat));
  });
  context.strokeStyle = '#2b5fd9';
  context.lineWidth = 10;
  context.lineJoin = 'round';
  context.lineCap = 'round';
  context.stroke();

  state.order.forEach((stopIndex, position) => {
    const stop = stops[stopIndex];
    const px = plotX(stop.lon);
    const py = plotY(stop.lat);
    context.beginPath();
    context.arc(px, py, 19, 0, Math.PI * 2);
    context.fillStyle = '#2b5fd9';
    context.fill();
    context.strokeStyle = '#fff';
    context.lineWidth = 5;
    context.stroke();
    canvasText(context, position + 1, px, py + 7, {
      size: 19, weight: 750, color: '#fff', align: 'center',
    });
  });
}

function drawExportProfile(context, state, x, y, width, height) {
  if (!includeElevation || state.profile.length < 2) return;
  roundedRect(context, x, y, width, height, 20, '#fff', '#d8dee5');
  canvasText(context, '全程海拔剖面', x + 28, y + 44, { size: 22, weight: 650 });
  const bounds = profileBounds(state.profile);
  const maxDistance = Math.max(state.profile[state.profile.length - 1].distanceKm, 1);
  const px = point => x + 30 + point.distanceKm / maxDistance * (width - 60);
  const py = point => y + 70 + (bounds.max - point.elevation) / (bounds.max - bounds.min) * (height - 100);
  context.beginPath();
  state.profile.forEach((point, index) => index ? context.lineTo(px(point), py(point)) : context.moveTo(px(point), py(point)));
  context.strokeStyle = '#2f9e77';
  context.lineWidth = 5;
  context.lineJoin = 'round';
  context.stroke();
  canvasText(context, `${bounds.min}m`, x + 28, y + height - 18, { size: 16, color: '#68737f' });
  canvasText(context, `${bounds.max}m`, x + 28, y + 78, { size: 16, color: '#68737f' });
}

async function buildExportCanvas() {
  await document.fonts?.ready;
  const width = 1600;
  const rowHeight = 142;
  const profileHeight = includeElevation ? 250 : 0;
  const height = 1040 + profileHeight + Math.max(routeOrder.length, 1) * rowHeight;
  const canvas = document.createElement('canvas');
  canvas.width = width;
  canvas.height = height;
  const context = canvas.getContext('2d');
  context.fillStyle = '#ffffff';
  context.fillRect(0, 0, width, height);

  canvasText(context, trip.title, 80, 100, { size: 52, weight: 700 });
  const exportRouteName = activePreset && routePresetByKey[activePreset]
    ? routePresetByKey[activePreset].name
    : routePresets.length ? '自定义路线' : '推荐路线';
  canvasText(context, routeHighlights.length
    ? `${exportRouteName} · 逐段风景 · 景点介绍 · 建议游玩时长`
    : `${exportRouteName} · 景点介绍 · 建议游玩时长`, 82, 150, { size: 23, color: '#68737f' });
  const totalTime = routeState.durationSeconds == null ? '部分路段暂无车程' : `约 ${formatDuration(routeState.durationSeconds)}`;
  roundedRect(context, 1080, 62, 440, 112, 20, '#f3f6fb', '#d8dee5');
  canvasText(context, `${routeOrder.length} 个地点`, 1110, 108, { size: 24, weight: 650, color: '#2b5fd9' });
  canvasText(context, `${formatDistance(routeState.distanceMeters)} · ${totalTime}`, 1110, 146, { size: 20, color: '#40566c' });

  drawExportRoute(context, routeState, 80, 210, 1440, 550);
  let cursorY = 800;
  if (includeElevation) {
    drawExportProfile(context, routeState, 80, cursorY, 1440, profileHeight - 20);
    cursorY += profileHeight;
  }
  canvasText(context, '每日行程', 80, cursorY + 56, { size: 32, weight: 700 });
  cursorY += 86;
  routeOrder.forEach((stopIndex, position) => {
    const stop = stops[stopIndex];
    const leg = routeState.legs[position];
    roundedRect(context, 80, cursorY, 1440, rowHeight - 18, 18, position % 2 ? '#fafbfc' : '#f4f7fb', '#e1e6eb');
    context.beginPath();
    context.arc(125, cursorY + 44, 24, 0, Math.PI * 2);
    context.fillStyle = '#2b5fd9';
    context.fill();
    canvasText(context, position + 1, 125, cursorY + 52, { size: 22, weight: 750, color: '#fff', align: 'center' });
    canvasText(context, `${stop.name}  ·  建议游玩 ${stop.visitDuration}`, 170, cursorY + 48, {
      size: 27, weight: 650, maxWidth: 780,
    });
    canvasText(context, stop.description || stop.note || '查看页面中的地点介绍与注意事项', 170, cursorY + 86, {
      size: 18, color: '#68737f', maxWidth: 900,
    });
    if (leg) {
      const scenery = sceneryForLeg(stopIndex, leg.to);
      if (scenery) canvasText(context, `下一段风景 ${scenery.sceneryScore} · ${scenery.description}`, 170, cursorY + 114, {
        size: 15, weight: 650, color: '#9a5c00', maxWidth: 920,
      });
    }
    canvasText(context, leg ? `下一段  ${formatDuration(leg.durationSeconds)} · ${formatDistance(leg.distanceMeters)}` : '行程终点',
      1480, cursorY + 51, { size: 20, weight: 650, color: '#2b5fd9', align: 'right', maxWidth: 330 });
    const caution = stop.precautions?.[0];
    if (caution) canvasText(context, `注意：${caution}`, 1480, cursorY + 87, {
      size: 17, color: '#8a5b20', align: 'right', maxWidth: 430,
    });
    cursorY += rowHeight;
  });
  canvasText(context, '路线、车程与海拔为规划参考；出发前请复核实时路况、天气和开放信息。', 80, height - 48, {
    size: 18, color: '#85909b',
  });
  return canvas;
}

function downloadBlob(blob, filename) {
  const link = document.createElement('a');
  link.href = URL.createObjectURL(blob);
  link.download = filename;
  document.body.append(link);
  link.click();
  link.remove();
  setTimeout(() => URL.revokeObjectURL(link.href), 1000);
}

function canvasBlob(canvas, type, quality) {
  return new Promise((resolve, reject) => canvas.toBlob(blob => {
    if (blob) resolve(blob);
    else reject(new Error('无法生成导出文件'));
  }, type, quality));
}

function concatBytes(chunks) {
  const size = chunks.reduce((sum, chunk) => sum + chunk.length, 0);
  const result = new Uint8Array(size);
  let offset = 0;
  chunks.forEach(chunk => {
    result.set(chunk, offset);
    offset += chunk.length;
  });
  return result;
}

async function canvasPdfBlob(canvas) {
  const jpegBlob = await canvasBlob(canvas, 'image/jpeg', .92);
  const jpeg = new Uint8Array(await jpegBlob.arrayBuffer());
  const encoder = new TextEncoder();
  const pageWidth = 595.28;
  const pageHeight = pageWidth * canvas.height / canvas.width;
  const objects = [
    encoder.encode('<< /Type /Catalog /Pages 2 0 R >>'),
    encoder.encode('<< /Type /Pages /Kids [3 0 R] /Count 1 >>'),
    encoder.encode(`<< /Type /Page /Parent 2 0 R /MediaBox [0 0 ${pageWidth.toFixed(2)} ${pageHeight.toFixed(2)}] /Resources << /XObject << /Im0 4 0 R >> >> /Contents 5 0 R >>`),
    concatBytes([
      encoder.encode(`<< /Type /XObject /Subtype /Image /Width ${canvas.width} /Height ${canvas.height} /ColorSpace /DeviceRGB /BitsPerComponent 8 /Filter /DCTDecode /Length ${jpeg.length} >>\nstream\n`),
      jpeg,
      encoder.encode('\nendstream'),
    ]),
  ];
  const content = encoder.encode(`q ${pageWidth.toFixed(2)} 0 0 ${pageHeight.toFixed(2)} 0 0 cm /Im0 Do Q`);
  objects.push(concatBytes([
    encoder.encode(`<< /Length ${content.length} >>\nstream\n`),
    content,
    encoder.encode('\nendstream'),
  ]));

  const chunks = [encoder.encode('%PDF-1.4\n%trip-map\n')];
  const offsets = [0];
  let byteOffset = chunks[0].length;
  objects.forEach((object, index) => {
    offsets.push(byteOffset);
    const chunk = concatBytes([
      encoder.encode(`${index + 1} 0 obj\n`),
      object,
      encoder.encode('\nendobj\n'),
    ]);
    chunks.push(chunk);
    byteOffset += chunk.length;
  });
  const xrefOffset = byteOffset;
  const xref = [
    `xref\n0 ${objects.length + 1}\n`,
    '0000000000 65535 f \n',
    ...offsets.slice(1).map(offset => `${String(offset).padStart(10, '0')} 00000 n \n`),
    `trailer\n<< /Size ${objects.length + 1} /Root 1 0 R >>\nstartxref\n${xrefOffset}\n%%EOF`,
  ].join('');
  chunks.push(encoder.encode(xref));
  return new Blob([concatBytes(chunks)], { type: 'application/pdf' });
}

async function exportItinerary(type) {
  const buttons = [el.imageExport, el.pdfExport];
  buttons.forEach(button => { button.disabled = true; });
  el.exportStatus.textContent = '正在生成行程文件…';
  try {
    const canvas = await buildExportCanvas();
    const safeTitle = trip.title.replace(/[\\/:*?"<>|]+/g, '-');
    const blob = type === 'pdf'
      ? await canvasPdfBlob(canvas)
      : await canvasBlob(canvas, 'image/png');
    const extension = type === 'pdf' ? 'pdf' : 'png';
    downloadBlob(blob, `${safeTitle}-行程.${extension}`);
    el.exportStatus.textContent = type === 'pdf' ? 'PDF 已导出' : '行程图片已导出';
    document.body.dataset.lastExport = type;
    document.body.dataset.lastExportBytes = String(blob.size);
  } catch (error) {
    console.error(error);
    el.exportStatus.textContent = `导出失败：${error.message}`;
  } finally {
    buttons.forEach(button => { button.disabled = false; });
  }
}

function addLegend() {
  if (!includeElevation) return;
  const legend = L.control({ position: 'topright' });
  legend.onAdd = () => {
    const container = L.DomUtil.create('div', 'legend');
    container.innerHTML = `
      <strong>海拔</strong>
      <div class="legend-scale">
        <div class="legend-bar"></div>
        <div class="legend-labels"><span>4800m</span><span>3300m</span><span>1800m</span></div>
      </div>`;
    return container;
  };
  legend.addTo(map);
}

function sizeCanvas() {
  if (!includeElevation || profileCollapsed) return;
  const rect = el.profile.getBoundingClientRect();
  const ratio = window.devicePixelRatio || 1;
  el.profile.width = Math.max(1, Math.round(rect.width * ratio));
  el.profile.height = Math.max(1, Math.round(rect.height * ratio));
  const context = el.profile.getContext('2d');
  context.setTransform(ratio, 0, 0, ratio, 0, 0);
}

function profileBounds(profile) {
  const elevations = profile.map(point => Number(point.elevation)).filter(Number.isFinite);
  if (!elevations.length) return { min: 0, max: 1 };
  const min = Math.floor((Math.min(...elevations) - 100) / 200) * 200;
  const max = Math.ceil((Math.max(...elevations) + 100) / 200) * 200;
  return { min, max: Math.max(max, min + 400) };
}

function drawProfile(highlightIndex = -1) {
  if (!includeElevation || profileCollapsed) return;
  const profile = routeState?.profile || [];
  const context = el.profile.getContext('2d');
  const width = el.profile.getBoundingClientRect().width;
  const height = el.profile.getBoundingClientRect().height;
  context.clearRect(0, 0, width, height);
  if (profile.length < 2) {
    el.profileInfo.textContent = '当前路线暂无海拔数据';
    el.profileMeta.textContent = trip.elevationSource || '';
    return;
  }
  const pad = { left: 45, right: 12, top: 9, bottom: 19 };
  const maxDistance = Math.max(profile[profile.length - 1].distanceKm, 1);
  const { min, max } = profileBounds(profile);
  const x = point => pad.left + point.distanceKm / maxDistance * (width - pad.left - pad.right);
  const y = point => pad.top + (max - point.elevation) / (max - min) * (height - pad.top - pad.bottom);

  context.font = '10px sans-serif';
  context.fillStyle = '#8b96a3';
  context.strokeStyle = '#e3e8ed';
  for (let line = 0; line < 4; line += 1) {
    const value = Math.round(min + (max - min) * line / 3);
    const lineY = pad.top + (height - pad.top - pad.bottom) * (1 - line / 3);
    context.beginPath();
    context.moveTo(pad.left, lineY);
    context.lineTo(width - pad.right, lineY);
    context.stroke();
    context.fillText(`${value}m`, 3, lineY + 3);
  }

  const gradient = context.createLinearGradient(0, pad.top, 0, height - pad.bottom);
  gradient.addColorStop(0, 'rgba(209,73,63,.36)');
  gradient.addColorStop(.52, 'rgba(232,185,60,.28)');
  gradient.addColorStop(1, 'rgba(47,158,119,.24)');
  context.beginPath();
  profile.forEach((point, index) => index ? context.lineTo(x(point), y(point)) : context.moveTo(x(point), y(point)));
  context.lineTo(x(profile[profile.length - 1]), height - pad.bottom);
  context.lineTo(x(profile[0]), height - pad.bottom);
  context.closePath();
  context.fillStyle = gradient;
  context.fill();

  const lineGradient = context.createLinearGradient(pad.left, 0, width - pad.right, 0);
  profile.forEach((point, index) => lineGradient.addColorStop(index / (profile.length - 1), colorForElevation(point.elevation)));
  context.beginPath();
  profile.forEach((point, index) => index ? context.lineTo(x(point), y(point)) : context.moveTo(x(point), y(point)));
  context.strokeStyle = lineGradient;
  context.lineWidth = 2.5;
  context.lineJoin = 'round';
  context.stroke();

  context.font = 'bold 10px sans-serif';
  routeOrder.forEach(index => {
    const distance = routeState.stopDistances.get(index);
    let point = profile[0];
    profile.forEach(candidate => {
      if (Math.abs(candidate.distanceKm - distance) < Math.abs(point.distanceKm - distance)) point = candidate;
    });
    context.beginPath();
    context.arc(x(point), y(point), 4, 0, Math.PI * 2);
    context.fillStyle = '#2b5fd9';
    context.fill();
    context.strokeStyle = '#fff';
    context.lineWidth = 1.5;
    context.stroke();
    context.fillStyle = '#244eb2';
    context.fillText(stops[index].name, Math.min(x(point) + 5, width - 58), Math.max(12, y(point) - 6));
  });

  if (highlightIndex >= 0 && profile[highlightIndex]) {
    const point = profile[highlightIndex];
    context.strokeStyle = '#1c2430';
    context.lineWidth = 1;
    context.beginPath();
    context.moveTo(x(point), pad.top);
    context.lineTo(x(point), height - pad.bottom);
    context.stroke();
    context.beginPath();
    context.arc(x(point), y(point), 5, 0, Math.PI * 2);
    context.fillStyle = colorForElevation(point.elevation);
    context.fill();
    context.strokeStyle = '#fff';
    context.lineWidth = 2;
    context.stroke();
  }

  const elevations = profile.map(point => Number(point.elevation));
  el.profileInfo.textContent = `海拔剖面 · ${formatDistance(routeState.distanceMeters)} · ${routeOrder.length} 个地点`;
  el.profileMeta.textContent = `最低 ${Math.round(Math.min(...elevations))}m · 最高 ${Math.round(Math.max(...elevations))}m · ${trip.elevationSource}`;
}

function showProfilePoint(index, clientX, clientY) {
  const point = routeState?.profile?.[index];
  if (!point) return;
  if (!hoverMarker) {
    hoverMarker = L.circleMarker([point.lat, point.lon], {
      radius: 7,
      color: '#1c2430',
      weight: 2,
      fillColor: colorForElevation(point.elevation),
      fillOpacity: 1,
    }).addTo(map);
  }
  hoverMarker.setLatLng([point.lat, point.lon]).setStyle({
    opacity: 1,
    fillOpacity: 1,
    fillColor: colorForElevation(point.elevation),
  });
  const mapPoint = map.latLngToContainerPoint([point.lat, point.lon]);
  const mapRect = document.querySelector('#map').getBoundingClientRect();
  el.hoverTip.style.display = 'block';
  el.hoverTip.style.left = `${clientX ?? mapRect.left + mapPoint.x}px`;
  el.hoverTip.style.top = `${clientY ?? mapRect.top + mapPoint.y}px`;
  el.hoverTip.innerHTML = `<b>${Math.round(point.elevation)} m</b><br>${point.distanceKm.toFixed(1)} km`;
  drawProfile(index);
}

function hideProfilePoint() {
  el.hoverTip.style.display = 'none';
  hoverMarker?.setStyle({ opacity: 0, fillOpacity: 0 });
  drawProfile(-1);
}

function nearestProfileIndex(latlng) {
  const profile = routeState?.profile || [];
  let best = -1;
  let distance = Infinity;
  profile.forEach((point, index) => {
    const next = haversineKm(
      { lat: latlng.lat, lon: latlng.lng },
      { lat: point.lat, lon: point.lon }
    );
    if (next < distance) {
      distance = next;
      best = index;
    }
  });
  return { best, distance };
}

function render() {
  const recommended = optimizeSelection();
  const validManual = manualOrder
    && manualOrder.length === selected.size
    && manualOrder.every(index => selected.has(index));
  routeOrder = validManual ? [...manualOrder] : recommended;
  if (!validManual) {
    manualOrder = null;
    manualOrderMode = 'manual';
    activePreset = null;
  }
  el.orderMode.textContent = manualOrder
    ? (manualOrderMode === 'shortest'
      ? '最短路径'
      : manualOrderMode === 'preset' && activePreset
        ? routePresetByKey[activePreset].name
        : '自定义顺序')
    : '推荐顺序';
  routeState = buildRoute(routeOrder);
  renderChips();
  ensureMarkers();
  drawRoute(routeState);
  renderLegs(routeState);
  drawProfile();
  if (currentDetailIndex != null && !el.detail.hidden) openDetail(currentDetailIndex);
}

el.plannerToggle.addEventListener('click', () => {
  el.planner.classList.toggle('collapsed');
  el.plannerToggle.textContent = el.planner.classList.contains('collapsed') ? '展开' : '收起';
  setTimeout(fitCurrentRoute, 120);
});
el.routePreset.addEventListener('change', event => {
  applyRoutePreset(event.target.value);
});
el.recommended.addEventListener('click', () => {
  manualOrder = null;
  manualOrderMode = 'manual';
  activePreset = null;
  render();
  fitCurrentRoute();
});
el.shortest.addEventListener('click', () => {
  manualOrder = shortestDistanceOrder();
  manualOrderMode = 'shortest';
  activePreset = null;
  render();
  fitCurrentRoute();
});
el.fit.addEventListener('click', fitCurrentRoute);
el.matrix.addEventListener('click', () => {
  el.matrixDialog.hidden = false;
});
el.matrixClose.addEventListener('click', () => {
  el.matrixDialog.hidden = true;
});
el.matrixDialog.addEventListener('click', event => {
  if (event.target === el.matrixDialog) el.matrixDialog.hidden = true;
});
el.highlights.addEventListener('click', () => {
  el.highlightsDialog.hidden = false;
});
el.highlightsClose.addEventListener('click', () => {
  el.highlightsDialog.hidden = true;
});
el.highlightsDialog.addEventListener('click', event => {
  if (event.target === el.highlightsDialog) el.highlightsDialog.hidden = true;
});
el.detailClose.addEventListener('click', () => {
  el.detail.hidden = true;
  currentDetailIndex = null;
});
el.fullscreen.addEventListener('click', async () => {
  if (document.fullscreenElement) await document.exitFullscreen();
  else await document.querySelector('#app').requestFullscreen();
  setTimeout(() => map.invalidateSize(), 120);
});
el.imageExport.addEventListener('click', () => exportItinerary('png'));
el.pdfExport.addEventListener('click', () => exportItinerary('pdf'));
el.profileToggle.addEventListener('click', () => {
  profileCollapsed = !profileCollapsed;
  syncProfileVisibility();
  setTimeout(() => map.invalidateSize(), 120);
});

el.profile.addEventListener('mousemove', event => {
  const profile = routeState?.profile || [];
  if (!profile.length) return;
  const rect = el.profile.getBoundingClientRect();
  const ratio = Math.min(1, Math.max(0, (event.clientX - rect.left - 45) / Math.max(1, rect.width - 57)));
  const distance = ratio * profile[profile.length - 1].distanceKm;
  let best = 0;
  profile.forEach((point, index) => {
    if (Math.abs(point.distanceKm - distance) < Math.abs(profile[best].distanceKm - distance)) best = index;
  });
  showProfilePoint(best, event.clientX, event.clientY);
});
el.profile.addEventListener('mouseleave', hideProfilePoint);
map.on('mousemove', event => {
  if (!includeElevation || !routeState?.profile.length) return;
  const { best, distance } = nearestProfileIndex(event.latlng);
  if (distance <= 18) showProfilePoint(best);
  else hideProfilePoint();
});
map.on('mouseout', hideProfilePoint);
window.addEventListener('resize', () => {
  clearTimeout(resizeTimer);
  resizeTimer = setTimeout(() => {
    sizeCanvas();
    drawProfile();
    map.invalidateSize();
  }, 80);
});

if (!includeElevation) {
  el.profileWrap.hidden = true;
  document.body.classList.add('no-elevation');
} else {
  syncProfileVisibility();
}
addLegend();
renderMatrix();
renderHighlights();
renderPresetOptions();
if (!profileCollapsed) sizeCanvas();
render();
fitCurrentRoute();
