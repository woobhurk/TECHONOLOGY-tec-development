# Input schema

Use UTF-8 JSON.

```json
{
  "title": "梅里雪山经典景观线",
  "subtitle": "候选景点、推荐顺序与建议游玩时长",
  "include_elevation": true,
  "start": "飞来寺观景台",
  "end": "奔子栏",
  "travel_time_factor": 1.0,
  "region_hint": "云南省迪庆藏族自治州，中国",
  "route_overview": "G214 滇藏公路山地路线，沿途以金沙江峡谷和雪山远景为主。",
  "route_highlights": [
    {
      "name": "金沙江峡谷路段",
      "road": "G214",
      "type": "drive_by",
      "description": "行车途中可见峡谷与高山河谷地貌。",
      "stop_advice": "只在正规服务区或有明确停车标志的观景点停车。",
      "sources": [
        {
          "title": "云南省交通运输厅",
          "url": "https://jtyst.yn.gov.cn/"
        }
      ]
    }
  ],
  "route_presets": [
    {
      "key": "scenic",
      "name": "雪山峡谷风景线",
      "stops": ["飞来寺观景台", "金沙江大湾观景台", "奔子栏"],
      "scenery_score": "★★★★★",
      "timing": "约 2 小时，另加观景停留",
      "summary": "雪山与峡谷景观最完整，但山路多、天气依赖明显。",
      "default": true
    },
    {
      "key": "fastest",
      "name": "直接赶路线",
      "stops": ["飞来寺观景台", "奔子栏"],
      "scenery_score": "★★☆☆☆",
      "timing": "约 1.5 小时纯驾驶",
      "summary": "减少停靠，以尽快抵达为主。"
    }
  ],
  "leg_scenery": [
    {
      "from": "飞来寺观景台",
      "to": "金沙江大湾观景台",
      "scenery_score": "★★★★☆",
      "description": "由高原草甸逐渐下降到金沙江峡谷，可见针叶林和河谷村落。",
      "road_advice": "连续山弯，雨季注意落石，只在正规观景台停车。",
      "sources": [
        {
          "title": "云南省交通运输厅",
          "url": "https://jtyst.yn.gov.cn/"
        }
      ]
    },
    {
      "from": "金沙江大湾观景台",
      "to": "奔子栏",
      "scenery_score": "★★★☆☆",
      "description": "近距离沿金沙江河谷行驶，可见陡坡、河滩与村落。",
      "road_advice": "短途峡谷路，注意落石和临崖弯道。",
      "sources": [
        {
          "title": "云南省交通运输厅",
          "url": "https://jtyst.yn.gov.cn/"
        }
      ]
    },
    {
      "from": "飞来寺观景台",
      "to": "奔子栏",
      "scenery_score": "★★★★☆",
      "description": "由梅里雪山观景区下降至金沙江河谷，峡谷地貌变化明显。",
      "road_advice": "直接赶路线仍为山路，按实时导航和交通管制行驶。",
      "sources": [
        {
          "title": "云南省交通运输厅",
          "url": "https://jtyst.yn.gov.cn/"
        }
      ]
    }
  ],
  "stops": [
    {
      "name": "奔子栏",
      "lat": 28.243502,
      "lon": 99.3020134,
      "selected": true,
      "featured": false,
      "description": "金沙江河谷中的补给与住宿节点。",
      "visit_duration": "1–2小时",
      "stay": "奔子栏镇",
      "precautions": [
        "进镇后在正规停车区停靠。"
      ],
      "sources": [
        {
          "title": "德钦县人民政府",
          "url": "https://www.deqin.gov.cn/"
        }
      ]
    },
    {
      "name": "金沙江大湾观景台",
      "lat": 28.2588017,
      "lon": 99.2743433,
      "selected": true,
      "featured": true,
      "description": "可俯瞰金沙江大拐弯的正式观景点。",
      "visit_duration": "20–30分钟",
      "precautions": [
        "只在正式停车区域停靠。"
      ],
      "sources": [
        {
          "title": "德钦县人民政府",
          "url": "https://www.deqin.gov.cn/"
        }
      ]
    },
    {
      "name": "飞来寺观景台",
      "query": "飞来寺观景台 云南 德钦 中国",
      "description": "梅里雪山日照金山经典观景位置。",
      "visit_duration": "1–2小时",
      "precautions": [
        "早晚温差大，准备防风保暖层。",
        "出发前确认当日道路和观景台开放状态。"
      ],
      "sources": [
        {
          "title": "德钦县旅游资源",
          "url": "https://deqin.gov.cn/mldq/lyzj.html"
        }
      ]
    }
  ]
}
```

## Top-level fields

- `title` (required): visible trip title.
- `subtitle` (optional): one short line below the title.
- `include_elevation` (optional boolean, default `true`): elevation is collected by default without asking. Set `false` only when the user explicitly disables elevation; the generator then skips elevation collection and omits all elevation UI.
- `start`, `end` (optional pair): exact stop names for fixed-endpoint route planning. Supply both or neither. The generator forces both into the selected set, and the page keeps them at the beginning and end while optimizing or dragging intermediate stops.
- `travel_time_factor` (optional number, default `1.0`): multiplier applied to the embedded OSRM duration matrix. Use `1.0` for raw routing-engine time; use a reviewed planning buffer such as `1.2` only when the user requests it.
- `region_hint` (optional): appended to stop names during geocoding.
- `route_overview` (optional): concise overview of the practical road corridor and its landscape character.
- `route_highlights` (optional list): researched road numbers, formal viewpoints/service areas, roadside attractions, and drive-by scenery. Use `safe_stop` only for verified legal stopping places and `drive_by` when no stop should be suggested.
- `route_presets` (optional list): named one-click route alternatives. Each preset supplies an exact stop-name order, scenery score, timing, and tradeoff summary. The first preset becomes the default when none is explicitly marked.
- `leg_scenery` (required when route presets are used): one sourced description for every unordered consecutive stop pair used by any route preset.
- `stops` (required): 2–14 candidate stops in display order. The generator precomputes every unordered pair, so larger lists must be split into regions.

## Route highlight fields

- `name` (required): visible highlight title.
- `road` (required): road number or corridor name, such as `G5611 大丽高速`.
- `type` (required): `safe_stop`, `drive_by`, or `optional_detour`.
- `description` (required): what the traveler can see or experience.
- `stop_advice` (required): safe access, parking, and operational caveats.
- `sources` (required): direct source links supporting the road or viewpoint description.

## Route preset fields

- `key` (required): unique stable identifier used by the route selector. Do not use the reserved value `custom`.
- `name` (required): traveler-facing route name, such as `虎香公路风景线` or `高速省时线`.
- `stops` (required): exact stop names in travel order. Names must exist in `stops`, may not repeat, and must begin/end with top-level `start`/`end` when fixed endpoints are enabled.
- `scenery_score` (required): concise visual rating such as `★★★★★`.
- `timing` (required): practical time summary, clearly distinguishing pure driving from sightseeing when relevant.
- `summary` (required): short tradeoff description covering landscape character, road difficulty, and when to choose the preset.
- `default` (optional boolean): initial route. At most one preset may be `true`; otherwise the first preset is used.

Route presets select precomputed stop-to-stop geometry. If two alternatives must follow different roads, include verified corridor-specific waypoints in their `stops` sequences. Naming two presets differently while using the same stop sequence does not create different road geometry.

## Leg scenery fields

- `from`, `to` (required): two exact stop names. Direction is ignored for lookup, so describe scenery in a way that remains accurate in either direction unless every preset uses only one direction.
- `scenery_score` (required): leg-level scenery rating.
- `description` (required): what is actually visible along this road segment.
- `road_advice` (required): practical road condition, safe-stop, weather, and access advice.
- `sources` (required): direct source links supporting the road or landscape description.

The generator rejects a route preset when any consecutive stop pair lacks a `leg_scenery` entry.

## Stop fields

- `name` (required): visible label.
- `lat`, `lon` (recommended): WGS84 coordinates. Supply both or neither.
- `query` (optional): explicit Nominatim query when `name + region_hint` is insufficient.
- `selected` (optional boolean, default `true`): whether the candidate joins the initial route. Unselected candidates remain available as map markers and chooser chips.
- `featured` (optional boolean, default `false`): visually emphasize a must-visit or especially important waypoint in the chooser without making it immutable.
- `description` (required after research): concise scenic-spot or city introduction written for a traveler.
- `visit_duration` (required after research): practical suggested visit duration such as `30分钟`, `1–2小时`, or `半天`.
- `precautions` (required after research): practical cautions as a list of short strings.
- `sources` (required after research): a list of objects with `title` and direct `url`.
- `stay` (optional): lodging or overnight note.
- `source` (optional): coordinate provenance such as `用户提供`, `OSM Nominatim`, or `人工核对`.

## Geocoding rules

- Treat similarly named scenic spots as ambiguous until their returned province/county is correct.
- For campsites, bridges, and informal viewpoints, prefer coordinates copied from the user's navigation app.
- Never hide an unresolved stop. The generator exits with a readable error and asks for coordinates.
- The public Nominatim service is rate-limited; the generator waits between requests and writes a reusable `.geocode-cache.json` in the output.

## Pairwise routing rules

- The generator fetches and embeds a driving route, distance, and duration for every unordered pair of candidates.
- The browser builds a symmetric distance/time matrix from that payload and never calls OSRM.
- Without explicit endpoints, the earliest selected candidate in input order is the recommended-route start. The page uses the duration matrix to find a short route through the remaining selected candidates, then improves it with local 2-opt swaps.
- When `start` and `end` are supplied, both endpoints remain fixed. Recommended ordering and manual drag changes apply only to intermediate stops.
- Pair geometries are reused in either direction. Treat displayed time and distance as planning references and verify live navigation before departure.
- Route presets change only the selected stop set and exact order. Use verified intermediate waypoints to force materially different road corridors.
- `.route-pair-cache.json` in the output directory makes regeneration incremental. Keep it when only introductions, visit durations, cautions, or presentation change.
