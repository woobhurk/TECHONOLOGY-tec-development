---
name: generate-trip-map
description: Generate a researched interactive travel-planning map from candidate cities, scenic spots, campsites, coordinates, or a supplied start and end point. Use when Codex needs to research attractions and road-corridor highlights, compare named route alternatives, enforce must-visit waypoints, plan a fixed-endpoint path, or turn pasted places into a portable static website where the user switches route presets, selects destinations, restores a recommended order or drag-reorders intermediate stops, reads stop and per-leg scenery/road descriptions, and exports PNG or PDF using a fully pre-collected driving matrix, default synchronized elevation, cautions, sources, and no runtime data collection.
---

# Generate Trip Map

Create a self-contained, single-file static trip-map website from candidate stops. Complete all research, pairwise routing, and optional elevation collection before emitting the page.

## Workflow

1. Extract candidate stop names from the user's text or image. When the user supplies only a start and end point, research a compact set of worthwhile cities and attractions along the practical driving corridor, including reasonable short detours, and record the exact endpoints.
2. Confirm likely OCR errors by inspecting the source image. Keep uncertain names visible in the input rather than silently replacing them.
3. Enable elevation by default without asking. Set `include_elevation: true` when the field is omitted. Honor an explicit user request to disable elevation with `include_elevation: false`.
4. Research every stop or city before generation:
   - search the exact place and its local administrative area;
   - prefer government, scenic-area, transport, meteorological, and other primary sources;
   - write a concise traveler-facing introduction, a practical suggested visit duration, plus cautions about altitude, weather, access, road closures, opening status, cultural rules, and ambiguous/private locations when relevant;
   - record source titles and direct URLs;
   - label unresolved or approximate locations explicitly and require navigation-app verification.
5. Research the driving corridor itself, not only the selected destinations:
   - identify verified road numbers, formal service areas, signed observation decks, roadside cultural sites, and scenery visible while driving;
   - separate `safe_stop` highlights with legitimate parking from `drive_by` scenery that must only be viewed from the vehicle;
   - never recommend stopping on a shoulder, bridge, tunnel entrance, bend, or unsigned informal pull-off;
   - describe the visible landscape, the safest way to experience it, operational uncertainty, and direct sources;
   - add worthwhile formal roadside attractions as optional stops when their coordinates and access can be verified.
   - when multiple practical alternatives exist, research named route presets such as scenic, balanced, and fastest; give each a traveler-facing scenery score, timing summary, and tradeoff description;
   - research every consecutive leg used by a route preset, including visible scenery, road conditions, safe-stop advice, and direct sources;
   - to force a preset onto a specific road corridor, include one or more verified intermediate waypoints on that road. Different preset names with the same stop sequence reuse the same precomputed pair geometry and therefore do not create distinct road choices.
6. Create an input JSON file using [references/input-schema.md](references/input-schema.md). Include `precautions` and `sources` for every stop plus `route_overview` and researched `route_highlights`. Use `selected: false` for optional candidates that should appear in the chooser without joining the default route.
   - For fixed-endpoint planning, set top-level `start` and `end` to exact stop names.
   - Keep endpoints selected and treat them as immutable first/last stops; optimize and drag only intermediate stops.
   - When route alternatives matter, add `route_presets` with stop names in exact travel order and one `default: true`; mark must-visit or visually important stops with `featured: true`.
   - Add one sourced `leg_scenery` entry for every consecutive stop pair used by every route preset. The generator rejects incomplete presets.
7. Prefer user-supplied coordinates. For missing coordinates, let the generator query Nominatim. Review every geocoded result; public geocoding can confuse similarly named places.
8. Collect all route data during generation:
   - an OSRM route, distance, and duration for every unordered pair of candidate stops, embedded as `pairRoutes` plus a symmetric `routeMatrix`;
   - pair-route and stop elevations only when `include_elevation` is `true`;
   - researched introductions, visit durations, cautions, and sources.
   Keep candidate lists at 14 stops or fewer. Split larger trips into regions instead of emitting a quadratic route payload.
9. Run:

```bash
python3 scripts/generate_trip_map.py \
  --input /absolute/path/trip.json \
  --output /absolute/path/output-directory
```

10. Open the generated `index.html` directly with a browser for the primary QA path. It must work from `file://` without localhost or deployment. A temporary HTTP server may be used only when the browser test surface blocks local-file navigation:

```bash
python3 -m http.server 8765 --directory /absolute/path/output-directory
```

11. Verify desktop and mobile layouts, every route-preset switch, preset distance/time changes, per-leg scenery and road descriptions, featured stops, route fitting, full-screen mode, stop selection, fixed start/end behavior when enabled, intermediate-stop drag ordering, recommended-order restoration, exact shortest-distance ordering, the distance/time matrix, the road-highlight dialog, introductions, visit durations, one-click PNG/PDF export, cautions/sources, and console errors. Confirm exports show the active preset name and per-leg scenery. Open the exported PDF, render it to PNG, and inspect the result before handoff.
12. When elevation is enabled, verify the selected-route elevation chart, min/max elevation, per-stop elevation, and synchronized hover in both directions:
   - default the elevation section to collapsed only when every candidate stop elevation is below 2000 meters;
   - default it to expanded when any candidate stop elevation is 2000 meters or higher;
   - always let the reader manually expand or collapse it;
   - hovering near the route moves the chart guide line and elevation dot;
   - hovering the elevation profile moves the corresponding dot and tooltip on the map.
   When elevation is disabled, verify that no elevation request is made and that the page contains no elevation profile or legend. Also verify that the browser makes no route/elevation/research API requests.
13. Report ambiguous geocodes, route/elevation fallbacks, road-highlight access uncertainty, and the generated site path.

## Output contract

The generated site must:

- show all candidate stops on one Leaflet map with a compact floating selector modeled on the proven map-first interface;
- embed every candidate-to-candidate distance, duration, and route geometry before page creation;
- let the reader select any subset, automatically generate a recommended order from the embedded duration matrix, restore it with one control, and freely reorder all selected stops by drag-and-drop without network requests;
- when `route_presets` are provided, expose a one-click route selector that changes the selected stop set and exact stop order, shows each preset's scenery score, timing, and tradeoff summary, and falls back to a visible custom mode after manual edits;
- visually distinguish stops marked `featured: true`, while keeping fixed endpoints immutable;
- show a researched scenery score, visible landscape description, and road/safety advice beneath every route leg covered by `leg_scenery`;
- provide a separate one-click shortest-path control that finds the exact minimum total driving distance across all currently selected stops from the embedded distance matrix, while preserving fixed endpoints when present;
- provide a control labeled `显示完整路线` that recenters and zooms the map to include the complete current route and all selected stops; do not use the ambiguous label `适配路线`, and do not imply that this control changes route order;
- when start and end are supplied, keep them fixed as the first and last stops and allow only intermediate stops to be selected, optimized, or drag-reordered;
- expose the full pairwise distance/time matrix in the page;
- expose a compact `沿途看点` dialog with the researched road overview, road numbers, formal service-area/viewpoint stops, drive-by scenery, safe-stop guidance, and source links;
- keep road highlights out of permanent map labels so route geometry stays readable;
- draw the selected OSRM driving legs when available and clearly style straight-line fallbacks;
- keep the route readable by using compact numbered map markers and hover/click details instead of permanent stop-name and leg-time labels over the route;
- collect and show route and per-stop elevation by default unless the user explicitly disables it;
- make the elevation section manually collapsible, default it to collapsed only when every candidate stop is below 2000 meters, and default it to expanded when any candidate stop is 2000 meters or higher;
- when elevation is enabled, rebuild the profile from the selected pair routes and synchronize route/profile hover in both directions;
- when elevation is disabled, skip elevation collection and omit all elevation UI;
- show a researched introduction, suggested visit duration, practical cautions, and source links for every stop;
- export a clean itinerary poster directly as PNG and a real downloadable PDF without requiring a print dialog or runtime API; when presets and leg scenery exist, exports must include the active preset name and the per-leg scenery summary;
- preserve source attribution and forecast limitations;
- work as static files with no build step;
- make `index.html` a standalone file with the local CSS, JavaScript, and trip payload inlined, so it can be opened directly without localhost or deployment;
- embed the trip payload directly in `index.html` so a stale or missing sidecar data file cannot leave a silent blank shell;
- never fetch elevation, routing, geocoding, matrix, or research data in the browser; only map tiles and the Leaflet library may load at view time;
- remain usable on phone-sized screens.

## Bundled resources

- `scripts/generate_trip_map.py`: validates input, resolves coordinates, fetches route and optional elevation data, and emits the website.
- `assets/trip-map-template/`: stable HTML, CSS, and JavaScript UI template.
- `references/input-schema.md`: input fields, examples, and coordinate guidance.
