#!/usr/bin/env python3
"""Generate an interactive dated trip map as static files."""

from __future__ import annotations

import argparse
import json
import math
import os
import shutil
import sys
import time
import urllib.error
import urllib.parse
import urllib.request
from datetime import date
from pathlib import Path
from typing import Any


SKILL_DIR = Path(__file__).resolve().parent.parent
TEMPLATE_DIR = SKILL_DIR / "assets" / "trip-map-template"
USER_AGENT = "Codex-generate-trip-map/1.0"
ELEVATION_SOURCE = "Open-Meteo Elevation API · Copernicus DEM GLO-90"


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--input", required=True, type=Path, help="Trip JSON input")
    parser.add_argument("--output", required=True, type=Path, help="Output directory")
    parser.add_argument(
        "--no-network",
        action="store_true",
        help="Skip geocoding, OSRM, and elevation requests",
    )
    parser.add_argument(
        "--pair-delay",
        type=float,
        default=0.08,
        help="Delay between uncached OSRM pair requests (seconds)",
    )
    return parser.parse_args()


def read_json(path: Path) -> dict[str, Any]:
    try:
        return json.loads(path.read_text(encoding="utf-8"))
    except (OSError, json.JSONDecodeError) as exc:
        raise SystemExit(f"无法读取输入 JSON：{exc}") from exc


def http_json(url: str, timeout: int = 45) -> Any:
    delays = (0, 4, 12, 28)
    for attempt, delay in enumerate(delays):
        if delay:
            time.sleep(delay)
        request = urllib.request.Request(
            url,
            headers={"User-Agent": USER_AGENT, "Accept": "application/json"},
        )
        try:
            with urllib.request.urlopen(request, timeout=timeout) as response:
                return json.load(response)
        except urllib.error.HTTPError as exc:
            retryable = exc.code == 429 or 500 <= exc.code < 600
            if not retryable or attempt == len(delays) - 1:
                raise
            print(
                f"远端服务返回 HTTP {exc.code}，{delays[attempt + 1]} 秒后重试",
                file=sys.stderr,
            )
    raise RuntimeError("远端服务重试耗尽")


def validate_trip(raw: dict[str, Any]) -> dict[str, Any]:
    title = str(raw.get("title", "")).strip()
    if not title:
        raise SystemExit("输入缺少 title")
    stops = raw.get("stops")
    if not isinstance(stops, list) or len(stops) < 2:
        raise SystemExit("stops 至少需要两个地点")
    include_elevation = raw.get("include_elevation", True)
    if not isinstance(include_elevation, bool):
        raise SystemExit("include_elevation 必须为布尔值；省略时默认启用海拔")

    route_highlights: list[dict[str, Any]] = []
    raw_highlights = raw.get("route_highlights", [])
    if not isinstance(raw_highlights, list):
        raise SystemExit("route_highlights 必须为数组")
    for index, highlight in enumerate(raw_highlights, 1):
        if not isinstance(highlight, dict):
            raise SystemExit(f"第 {index} 个 route_highlight 必须为对象")
        item = {
            "name": str(highlight.get("name", "")).strip(),
            "road": str(highlight.get("road", "")).strip(),
            "type": str(highlight.get("type", "")).strip(),
            "description": str(highlight.get("description", "")).strip(),
            "stopAdvice": str(highlight.get("stop_advice", "")).strip(),
            "sources": [
                {
                    "title": str(source.get("title", "")).strip(),
                    "url": str(source.get("url", "")).strip(),
                }
                for source in highlight.get("sources", [])
                if isinstance(source, dict)
                and str(source.get("title", "")).strip()
                and str(source.get("url", "")).strip()
            ],
        }
        missing = [
            field
            for field in ("name", "road", "type", "description", "stopAdvice")
            if not item[field]
        ]
        if missing or not item["sources"]:
            raise SystemExit(
                f"第 {index} 个 route_highlight 缺少字段或 sources："
                f"{', '.join(missing) if missing else 'sources'}"
            )
        if item["type"] not in {"safe_stop", "drive_by", "optional_detour"}:
            raise SystemExit(
                f"第 {index} 个 route_highlight type 必须为 "
                "safe_stop、drive_by 或 optional_detour"
            )
        route_highlights.append(item)

    cleaned: list[dict[str, Any]] = []
    for index, stop in enumerate(stops, 1):
        if not isinstance(stop, dict) or not str(stop.get("name", "")).strip():
            raise SystemExit(f"第 {index} 个 stop 缺少 name")
        item = {
            "name": str(stop["name"]).strip(),
            "description": str(stop.get("description") or stop.get("note", "")).strip(),
            "visitDuration": str(stop.get("visit_duration") or stop.get("visitDuration", "")).strip(),
            "stay": str(stop.get("stay", "")).strip(),
            "source": str(stop.get("source", "")).strip(),
            "query": str(stop.get("query", "")).strip(),
            "selected": bool(stop.get("selected", True)),
            "featured": bool(stop.get("featured", False)),
            "precautions": [
                str(value).strip()
                for value in stop.get("precautions", [])
                if str(value).strip()
            ],
            "sources": [
                {
                    "title": str(source.get("title", "")).strip(),
                    "url": str(source.get("url", "")).strip(),
                }
                for source in stop.get("sources", [])
                if isinstance(source, dict)
                and str(source.get("title", "")).strip()
                and str(source.get("url", "")).strip()
            ],
        }
        if not item["description"]:
            raise SystemExit(f"{item['name']} 缺少调研后的 description")
        if not item["visitDuration"]:
            raise SystemExit(f"{item['name']} 缺少 visit_duration，例如 1小时")
        if not item["precautions"]:
            raise SystemExit(f"{item['name']} 缺少 precautions")
        if not item["sources"]:
            raise SystemExit(f"{item['name']} 缺少 sources")
        has_lat = stop.get("lat") is not None
        has_lon = stop.get("lon") is not None
        if has_lat != has_lon:
            raise SystemExit(f"{item['name']} 必须同时提供 lat 和 lon")
        if has_lat:
            item["lat"] = float(stop["lat"])
            item["lon"] = float(stop["lon"])
        if stop.get("elevation") is not None:
            item["elevation"] = float(stop["elevation"])
        cleaned.append(item)

    start_name = str(raw.get("start", "")).strip()
    end_name = str(raw.get("end", "")).strip()
    if bool(start_name) != bool(end_name):
        raise SystemExit("固定路线模式必须同时提供 start 和 end")
    start_index = end_index = None
    if start_name and end_name:
        names = [stop["name"] for stop in cleaned]
        if start_name not in names:
            raise SystemExit(f"start 未匹配任何地点：{start_name}")
        if end_name not in names:
            raise SystemExit(f"end 未匹配任何地点：{end_name}")
        start_index = names.index(start_name)
        end_index = names.index(end_name)
        if start_index == end_index:
            raise SystemExit("start 和 end 不能是同一个地点")
        cleaned[start_index]["selected"] = True
        cleaned[end_index]["selected"] = True

    names = [stop["name"] for stop in cleaned]
    route_presets: list[dict[str, Any]] = []
    raw_presets = raw.get("route_presets", [])
    if not isinstance(raw_presets, list):
        raise SystemExit("route_presets 必须为数组")
    seen_preset_keys: set[str] = set()
    for index, preset in enumerate(raw_presets, 1):
        if not isinstance(preset, dict):
            raise SystemExit(f"第 {index} 个 route_preset 必须为对象")
        key = str(preset.get("key", "")).strip()
        preset_name = str(preset.get("name", "")).strip()
        preset_stops = preset.get("stops", [])
        if not key or not preset_name:
            raise SystemExit(f"第 {index} 个 route_preset 缺少 key 或 name")
        if key == "custom":
            raise SystemExit("route_preset key 不能使用保留值 custom")
        if key in seen_preset_keys:
            raise SystemExit(f"route_preset key 重复：{key}")
        seen_preset_keys.add(key)
        if not isinstance(preset_stops, list) or len(preset_stops) < 2:
            raise SystemExit(f"route_preset {key} 的 stops 至少需要两个地点")
        preset_stop_names = [str(value).strip() for value in preset_stops]
        if len(set(preset_stop_names)) != len(preset_stop_names):
            raise SystemExit(f"route_preset {key} 的 stops 不得重复")
        unknown = [name for name in preset_stop_names if name not in names]
        if unknown:
            raise SystemExit(
                f"route_preset {key} 包含未知地点：{', '.join(unknown)}"
            )
        order = [names.index(name) for name in preset_stop_names]
        if start_index is not None and (
            order[0] != start_index or order[-1] != end_index
        ):
            raise SystemExit(
                f"route_preset {key} 必须以 start 开头、end 结尾"
            )
        item = {
            "key": key,
            "name": preset_name,
            "order": order,
            "sceneryScore": str(preset.get("scenery_score", "")).strip(),
            "timing": str(preset.get("timing", "")).strip(),
            "summary": str(preset.get("summary", "")).strip(),
            "default": bool(preset.get("default", False)),
        }
        missing = [
            field
            for field in ("sceneryScore", "timing", "summary")
            if not item[field]
        ]
        if missing:
            raise SystemExit(
                f"route_preset {key} 缺少字段：{', '.join(missing)}"
            )
        route_presets.append(item)
    defaults = [preset for preset in route_presets if preset["default"]]
    if len(defaults) > 1:
        raise SystemExit("route_presets 最多只能有一个 default: true")
    if route_presets and not defaults:
        route_presets[0]["default"] = True
        defaults = [route_presets[0]]
    if defaults:
        default_order = set(defaults[0]["order"])
        for index, stop in enumerate(cleaned):
            stop["selected"] = index in default_order

    leg_scenery: dict[str, dict[str, Any]] = {}
    raw_leg_scenery = raw.get("leg_scenery", [])
    if not isinstance(raw_leg_scenery, list):
        raise SystemExit("leg_scenery 必须为数组")
    for index, leg in enumerate(raw_leg_scenery, 1):
        if not isinstance(leg, dict):
            raise SystemExit(f"第 {index} 个 leg_scenery 必须为对象")
        from_name = str(leg.get("from", "")).strip()
        to_name = str(leg.get("to", "")).strip()
        if from_name not in names or to_name not in names:
            raise SystemExit(
                f"第 {index} 个 leg_scenery 的 from/to 未匹配地点"
            )
        if from_name == to_name:
            raise SystemExit(f"第 {index} 个 leg_scenery 的 from/to 不能相同")
        from_index = names.index(from_name)
        to_index = names.index(to_name)
        key = f"{min(from_index, to_index)}-{max(from_index, to_index)}"
        if key in leg_scenery:
            raise SystemExit(
                f"leg_scenery 重复描述同一路段：{from_name}—{to_name}"
            )
        item = {
            "fromName": from_name,
            "toName": to_name,
            "sceneryScore": str(leg.get("scenery_score", "")).strip(),
            "description": str(leg.get("description", "")).strip(),
            "roadAdvice": str(leg.get("road_advice", "")).strip(),
            "sources": [
                {
                    "title": str(source.get("title", "")).strip(),
                    "url": str(source.get("url", "")).strip(),
                }
                for source in leg.get("sources", [])
                if isinstance(source, dict)
                and str(source.get("title", "")).strip()
                and str(source.get("url", "")).strip()
            ],
        }
        missing = [
            field
            for field in ("sceneryScore", "description", "roadAdvice")
            if not item[field]
        ]
        if missing or not item["sources"]:
            raise SystemExit(
                f"第 {index} 个 leg_scenery 缺少字段或 sources："
                f"{', '.join(missing) if missing else 'sources'}"
            )
        leg_scenery[key] = item
    for preset in route_presets:
        for previous, current in zip(preset["order"], preset["order"][1:]):
            key = f"{min(previous, current)}-{max(previous, current)}"
            if key not in leg_scenery:
                raise SystemExit(
                    f"route_preset {preset['key']} 缺少相邻路段风景描述："
                    f"{names[previous]}—{names[current]}"
                )

    result = {
        "title": title,
        "subtitle": str(raw.get("subtitle", "")).strip(),
        "includeElevation": include_elevation,
        "regionHint": str(raw.get("region_hint", "")).strip(),
        "travelTimeFactor": float(raw.get("travel_time_factor", 1.0)),
        "routeOverview": str(raw.get("route_overview", "")).strip(),
        "routeHighlights": route_highlights,
        "routePresets": route_presets,
        "legScenery": leg_scenery,
        "stops": cleaned,
    }
    if start_index is not None and end_index is not None:
        result.update(
            {
                "startIndex": start_index,
                "endIndex": end_index,
                "startName": start_name,
                "endName": end_name,
            }
        )
    if not 0.5 <= result["travelTimeFactor"] <= 3:
        raise SystemExit("travel_time_factor 必须在 0.5 到 3 之间")
    if len(cleaned) > 14:
        raise SystemExit(
            "候选地点超过 14 个；完整两两路线会过大，请拆成区域或控制在 14 个以内"
        )
    if sum(1 for stop in cleaned if stop["selected"]) < 2:
        raise SystemExit("至少需要两个默认选中的地点；为 stop 设置 selected: true")
    return result


def load_cache(path: Path) -> dict[str, Any]:
    if not path.exists():
        return {}
    try:
        return json.loads(path.read_text(encoding="utf-8"))
    except (OSError, json.JSONDecodeError):
        return {}


def geocode_stops(trip: dict[str, Any], cache_path: Path, no_network: bool) -> None:
    cache = load_cache(cache_path)
    unresolved: list[str] = []
    changed = False
    for stop in trip["stops"]:
        if "lat" in stop:
            stop["source"] = stop["source"] or "输入坐标"
            continue
        query = stop["query"] or ", ".join(
            part for part in (stop["name"], trip["regionHint"]) if part
        )
        cached = cache.get(query)
        if cached:
            stop.update(cached)
            continue
        if no_network:
            unresolved.append(stop["name"])
            continue
        params = urllib.parse.urlencode(
            {
                "q": query,
                "format": "jsonv2",
                "limit": 1,
                "countrycodes": "cn",
            }
        )
        try:
            results = http_json(f"https://nominatim.openstreetmap.org/search?{params}")
        except Exception as exc:  # network errors need a user-facing fallback
            print(f"警告：{stop['name']} 地理编码失败：{exc}", file=sys.stderr)
            results = []
        if not results:
            unresolved.append(stop["name"])
        else:
            first = results[0]
            resolved = {
                "lat": float(first["lat"]),
                "lon": float(first["lon"]),
                "source": "OSM Nominatim",
                "resolvedName": first.get("display_name", ""),
            }
            stop.update(resolved)
            cache[query] = resolved
            changed = True
        time.sleep(1.1)
    if changed:
        cache_path.write_text(
            json.dumps(cache, ensure_ascii=False, indent=2),
            encoding="utf-8",
        )
    if unresolved:
        names = "、".join(unresolved)
        raise SystemExit(f"以下地点无法可靠定位，请补充 lat/lon：{names}")


def simplify_coordinates(
    coordinates: list[list[float]], max_points: int = 90
) -> list[list[float]]:
    if len(coordinates) <= max_points:
        return coordinates
    indices = sorted(
        {
            round(index * (len(coordinates) - 1) / (max_points - 1))
            for index in range(max_points)
        }
    )
    return [coordinates[index] for index in indices]


def route_cache_key(a: dict[str, Any], b: dict[str, Any]) -> str:
    return (
        f"{a['lon']:.6f},{a['lat']:.6f}|"
        f"{b['lon']:.6f},{b['lat']:.6f}"
    )


def fetch_route_pair(
    a: dict[str, Any],
    b: dict[str, Any],
    no_network: bool,
) -> dict[str, Any]:
    fallback = [[a["lon"], a["lat"]], [b["lon"], b["lat"]]]
    fallback_distance = round(haversine_m(fallback[0], fallback[1]))
    if no_network:
        return {
            "coordinates": fallback,
            "distanceMeters": fallback_distance,
            "durationSeconds": None,
            "status": "straight-line",
            "message": "未联网，使用两点直线连接",
        }
    coordinates = f"{a['lon']},{a['lat']};{b['lon']},{b['lat']}"
    params = urllib.parse.urlencode(
        {"overview": "simplified", "geometries": "geojson", "steps": "false"}
    )
    url = f"https://router.project-osrm.org/route/v1/driving/{coordinates}?{params}"
    try:
        payload = http_json(url, timeout=90)
        if payload.get("code") != "Ok" or not payload.get("routes"):
            raise RuntimeError(payload.get("code", "empty response"))
        route = payload["routes"][0]
        return {
            "coordinates": simplify_coordinates(route["geometry"]["coordinates"]),
            "distanceMeters": route.get("distance"),
            "durationSeconds": route.get("duration"),
            "status": "osrm",
            "message": "OSRM driving",
        }
    except Exception as exc:
        print(f"警告：OSRM 路线获取失败，改用直线连接：{exc}", file=sys.stderr)
        return {
            "coordinates": fallback,
            "distanceMeters": fallback_distance,
            "durationSeconds": None,
            "status": "straight-line",
            "message": f"OSRM 不可用：{exc}",
        }


def build_pair_routes(
    trip: dict[str, Any],
    cache_path: Path,
    no_network: bool,
    pair_delay: float,
) -> dict[str, dict[str, Any]]:
    cache = load_cache(cache_path)
    changed = False
    pairs: dict[str, dict[str, Any]] = {}
    stops = trip["stops"]
    total = len(stops) * (len(stops) - 1) // 2
    completed = 0
    for left in range(len(stops)):
        for right in range(left + 1, len(stops)):
            completed += 1
            a, b = stops[left], stops[right]
            cache_key = route_cache_key(a, b)
            pair = cache.get(cache_key)
            if not pair:
                pair = fetch_route_pair(a, b, no_network)
                if not no_network:
                    cache[cache_key] = pair
                    changed = True
                    print(
                        f"路线矩阵 {completed}/{total}：{a['name']} → {b['name']}",
                        file=sys.stderr,
                    )
                    if pair_delay > 0:
                        time.sleep(pair_delay)
            pairs[f"{left}-{right}"] = pair
    if changed:
        cache_path.write_text(
            json.dumps(cache, ensure_ascii=False, separators=(",", ":")),
            encoding="utf-8",
        )
    return pairs


def build_route_matrix(
    trip: dict[str, Any], pairs: dict[str, dict[str, Any]]
) -> dict[str, list[list[float | None]]]:
    count = len(trip["stops"])
    distances: list[list[float | None]] = [
        [0 if row == column else None for column in range(count)]
        for row in range(count)
    ]
    durations: list[list[float | None]] = [
        [0 if row == column else None for column in range(count)]
        for row in range(count)
    ]
    factor = trip["travelTimeFactor"]
    for key, pair in pairs.items():
        left, right = map(int, key.split("-"))
        distance = pair.get("distanceMeters")
        duration = pair.get("durationSeconds")
        distances[left][right] = distances[right][left] = distance
        durations[left][right] = durations[right][left] = (
            round(float(duration) * factor, 1) if duration is not None else None
        )
    return {"distancesMeters": distances, "durationsSeconds": durations}


def route_from_order(
    order: list[int], pairs: dict[str, dict[str, Any]]
) -> dict[str, Any]:
    coordinates: list[list[float]] = []
    distance = 0.0
    duration = 0.0
    duration_available = True
    statuses: list[str] = []
    for left, right in zip(order, order[1:]):
        key = f"{min(left, right)}-{max(left, right)}"
        pair = pairs[key]
        segment = pair["coordinates"]
        if left > right:
            segment = list(reversed(segment))
        if coordinates and segment:
            segment = segment[1:]
        coordinates.extend(segment)
        distance += float(pair.get("distanceMeters") or 0)
        if pair.get("durationSeconds") is None:
            duration_available = False
        else:
            duration += float(pair["durationSeconds"])
        statuses.append(pair.get("status", "straight-line"))
    return {
        "coordinates": coordinates,
        "distanceMeters": round(distance, 1),
        "durationSeconds": round(duration, 1) if duration_available else None,
        "status": "osrm" if statuses and all(s == "osrm" for s in statuses) else "mixed",
        "message": "预计算 OSRM 两两路线" if statuses else "未选择路线",
    }


def haversine_m(a: list[float], b: list[float]) -> float:
    lon1, lat1 = map(math.radians, a)
    lon2, lat2 = map(math.radians, b)
    dlon = lon2 - lon1
    dlat = lat2 - lat1
    value = (
        math.sin(dlat / 2) ** 2
        + math.cos(lat1) * math.cos(lat2) * math.sin(dlon / 2) ** 2
    )
    return 6371000 * 2 * math.atan2(math.sqrt(value), math.sqrt(1 - value))


def sample_route(
    coordinates: list[list[float]], max_points: int = 90
) -> list[dict[str, float]]:
    if not coordinates:
        return []
    cumulative = [0.0]
    for previous, current in zip(coordinates, coordinates[1:]):
        cumulative.append(cumulative[-1] + haversine_m(previous, current))
    if len(coordinates) <= max_points:
        indices = list(range(len(coordinates)))
    else:
        indices = sorted(
            {
                round(index * (len(coordinates) - 1) / (max_points - 1))
                for index in range(max_points)
            }
        )
    return [
        {
            "lon": coordinates[index][0],
            "lat": coordinates[index][1],
            "distanceKm": round(cumulative[index] / 1000, 2),
        }
        for index in indices
    ]


def fetch_open_elevation_values(
    points: list[dict[str, float]]
) -> list[float | None]:
    body = json.dumps(
        {
            "locations": [
                {"latitude": point["lat"], "longitude": point["lon"]}
                for point in points
            ]
        }
    ).encode("utf-8")
    request = urllib.request.Request(
        "https://api.open-elevation.com/api/v1/lookup",
        data=body,
        headers={
            "User-Agent": USER_AGENT,
            "Accept": "application/json",
            "Content-Type": "application/json",
        },
        method="POST",
    )
    with urllib.request.urlopen(request, timeout=90) as response:
        payload = json.load(response)
    return [result.get("elevation") for result in payload.get("results", [])]


def fetch_elevation_values(points: list[dict[str, float]]) -> list[float | None]:
    global ELEVATION_SOURCE
    values: list[float | None] = []
    for offset in range(0, len(points), 100):
        chunk = points[offset : offset + 100]
        if os.environ.get("TRIP_ELEVATION_PROVIDER") == "open-elevation":
            ELEVATION_SOURCE = "Open-Elevation API"
            values.extend(fetch_open_elevation_values(chunk))
        else:
            params = urllib.parse.urlencode(
                {
                    "latitude": ",".join(str(point["lat"]) for point in chunk),
                    "longitude": ",".join(str(point["lon"]) for point in chunk),
                }
            )
            payload = http_json(f"https://api.open-meteo.com/v1/elevation?{params}")
            values.extend(payload.get("elevation", [None] * len(chunk)))
        if offset + len(chunk) < len(points):
            time.sleep(2.0)
    return values


def fetch_elevation(
    trip: dict[str, Any],
    pairs: dict[str, dict[str, Any]],
    no_network: bool,
) -> dict[str, Any]:
    if no_network:
        return {
            "enabled": True,
            "source": "未获取",
        }
    try:
        missing_stops = [
            stop for stop in trip["stops"] if stop.get("elevation") is None
        ]
        if missing_stops:
            stop_points = [
                {"lat": stop["lat"], "lon": stop["lon"]} for stop in missing_stops
            ]
            stop_elevations = fetch_elevation_values(stop_points)
            for stop, elevation in zip(missing_stops, stop_elevations):
                stop["elevation"] = elevation
        pair_points: list[dict[str, float]] = []
        ranges: dict[str, tuple[int, int]] = {}
        for key, pair in pairs.items():
            if pair.get("elevationProfile"):
                continue
            sampled = sample_route(pair["coordinates"], max_points=28)
            start = len(pair_points)
            pair_points.extend(sampled)
            ranges[key] = (start, len(pair_points))
        if pair_points:
            pair_elevations = fetch_elevation_values(pair_points)
            for point, elevation in zip(pair_points, pair_elevations):
                point["elevation"] = elevation
            for key, (start, end) in ranges.items():
                pairs[key]["elevationProfile"] = pair_points[start:end]
        return {
            "enabled": True,
            "source": ELEVATION_SOURCE,
        }
    except Exception as exc:
        print(f"警告：海拔数据获取失败：{exc}", file=sys.stderr)
        return {
            "enabled": True,
            "source": f"获取失败：{exc}",
        }


def copy_template(output: Path) -> None:
    output.mkdir(parents=True, exist_ok=True)
    for filename in ("index.html", "styles.css", "app.js"):
        shutil.copy2(TEMPLATE_DIR / filename, output / filename)


def write_output(trip: dict[str, Any], output: Path) -> None:
    payload = json.dumps(trip, ensure_ascii=False, separators=(",", ":"))
    safe_payload = payload.replace("</", "<\\/")
    index_path = output / "index.html"
    index_template = index_path.read_text(encoding="utf-8")
    if "__TRIP_DATA__" not in index_template:
        raise SystemExit("地图模板缺少 __TRIP_DATA__ 占位符")
    html = index_template.replace("__TRIP_DATA__", safe_payload)
    css = (output / "styles.css").read_text(encoding="utf-8")
    app_js = (output / "app.js").read_text(encoding="utf-8")
    html = html.replace(
        '<link rel="stylesheet" href="./styles.css?v=trip-map-v10">',
        f"<style>\n{css}\n</style>",
        1,
    )
    html = html.replace(
        '<script src="./app.js?v=trip-map-v10"></script>',
        f"<script>\n{app_js}\n</script>",
        1,
    )
    html = html.replace(
        "路线矩阵已预载 · 页面只加载地图瓦片",
        "静态单文件 · 路线与海拔已内嵌 · 页面只联网加载地图底图",
        1,
    )
    if "./styles.css" in html or "./app.js" in html:
        raise SystemExit("单文件地图仍包含本地 CSS/JS 依赖")
    index_path.write_text(html, encoding="utf-8")
    (output / "data.js").write_text(
        f"window.TRIP_DATA = {payload};\n",
        encoding="utf-8",
    )
    (output / "trip-data.json").write_text(
        json.dumps(trip, ensure_ascii=False, indent=2),
        encoding="utf-8",
    )


def main() -> None:
    args = parse_args()
    raw = read_json(args.input)
    trip = validate_trip(raw)
    args.output.mkdir(parents=True, exist_ok=True)
    cache_path = args.output / ".geocode-cache.json"
    geocode_stops(trip, cache_path, args.no_network)
    pair_cache_path = args.output / ".route-pair-cache.json"
    trip["pairRoutes"] = build_pair_routes(
        trip, pair_cache_path, args.no_network, args.pair_delay
    )
    trip["routeMatrix"] = build_route_matrix(trip, trip["pairRoutes"])
    default_order = [
        index for index, stop in enumerate(trip["stops"]) if stop["selected"]
    ]
    trip["route"] = route_from_order(default_order, trip["pairRoutes"])
    trip["elevation"] = (
        fetch_elevation(trip, trip["pairRoutes"], args.no_network)
        if trip["includeElevation"]
        else {"enabled": False, "source": ""}
    )
    if trip["includeElevation"] and not trip["elevation"].get(
        "source", ""
    ).startswith("获取失败"):
        pair_cache = load_cache(pair_cache_path)
        for left in range(len(trip["stops"])):
            for right in range(left + 1, len(trip["stops"])):
                pair_cache[route_cache_key(
                    trip["stops"][left], trip["stops"][right]
                )] = trip["pairRoutes"][f"{left}-{right}"]
        pair_cache_path.write_text(
            json.dumps(pair_cache, ensure_ascii=False, separators=(",", ":")),
            encoding="utf-8",
        )
    trip["generatedAt"] = date.today().isoformat()
    trip["routeSource"] = trip["route"]["message"]
    trip["elevationSource"] = trip["elevation"]["source"]
    copy_template(args.output)
    write_output(trip, args.output)
    print(f"已生成：{args.output / 'index.html'}")
    print(
        f"地点：{len(trip['stops'])}；两两路线：{len(trip['pairRoutes'])}；"
        f"默认路线状态：{trip['route']['status']}"
    )


if __name__ == "__main__":
    main()
