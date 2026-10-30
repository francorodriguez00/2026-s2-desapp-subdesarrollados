import json
import os
import glob
from collections import defaultdict

# Mapa de columnas: (nombre_entidad, json_key, categoria)
mappings = [
    ("whoscoredPlayerId", "playerId", "Summary"),
    ("rating", "rating", "Summary"),
    ("goals", "goal", "Summary"),
    ("assists", "assistTotal", "Summary"),
    ("yellowCards", "yellowCard", "Summary"),
    ("redCards", "redCard", "Summary"),
    ("shotsPerGame", "shotsPerGame", "Summary"),
    ("aerialWonPerGame", "aerialWonPerGame", "Summary"),
    ("manOfTheMatch", "manOfTheMatch", "Summary"),
    ("keyPassPerGame", "keyPassPerGame", "Offensive"),
    ("dribbleWonPerGame", "dribbleWonPerGame", "Offensive"),
    ("foulGivenPerGame", "foulGivenPerGame", "Offensive"),
    ("dispossessedPerGame", "dispossessedPerGame", "Offensive"),
    ("turnoverPerGame", "turnoverPerGame", "Offensive"),
    ("tacklePerGame", "tacklePerGame", "Defensive"),
    ("interceptionPerGame", "interceptionPerGame", "Defensive"),
    ("foulsPerGame", "foulsPerGame", "Defensive"),
    ("clearancePerGame", "clearancePerGame", "Defensive"),
    ("wasDribbledPerGame", "wasDribbledPerGame", "Defensive"),
    ("outfielderBlockPerGame", "outfielderBlockPerGame", "Defensive"),
    ("totalPassesPerGame", "totalPassesPerGame", "Passing"),
    ("accurateCrossesPerGame", "accurateCrossesPerGame", "Passing"),
    ("accurateLongPassPerGame", "accurateLongPassPerGame", "Passing"),
    ("accurateThroughBallPerGame", "accurateThroughBallPerGame", "Passing")
]

results = {col: {"found": 0, "total": 0} for col, _, _ in mappings}

# Recorrer archivos
for path in glob.glob("scraper/cache/**/*.json", recursive=True):
    try:
        with open(path, 'r', encoding='utf-8') as f:
            data = json.load(f)
            players = data.get("playerTableStats", [])
            for p in players:
                for col, key, _ in mappings:
                    results[col]["total"] += 1
                    if p.get(key) is not None:
                        results[col]["found"] += 1
    except Exception as e:
        continue

print(f"{'Columna Entidad':<30} | {'JSON Key':<25} | {'Categoria':<15} | {'Existencia en JSON (%)':<20}")
print("-" * 100)
for col, key, cat in mappings:
    total = results[col]["total"]
    found = results[col]["found"]
    perc = (found / total * 100) if total > 0 else 0
    print(f"{col:<30} | {key:<25} | {cat:<15} | {perc:.1f}%")
