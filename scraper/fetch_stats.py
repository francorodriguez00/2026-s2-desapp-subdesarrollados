from curl_cffi import requests
import json
import time

url = "https://www.whoscored.com/statisticsfeed/1/getplayerstatistics"
params = {
    "category": "summary",
    "subcategory": "all",
    "stageId": "25544",
    "field": "Overall",
    "page": "1",
    "isAggregate": "false",
    "d": "",
    "teamIds": "",
    "statsAccumulationType": "total"
}
headers = {
    "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/129.0.0.0 Safari/537.36",
    "Referer": "https://www.whoscored.com/Regions/252/Tournaments/2/Seasons/9684/Stages/25544/PlayerStatistics/England-Premier-League-2024-2025",
    "Accept": "application/json, text/javascript, */*; q=0.01",
    "X-Requested-With": "XMLHttpRequest"
}

response = requests.get(url, params=params, headers=headers, impersonate="chrome")
print(f"Status Code: {response.status_code}")

with open("scraper/cache/epl_stats.json", "w", encoding="utf-8") as f:
    f.write(response.text)
print("JSON guardado")
