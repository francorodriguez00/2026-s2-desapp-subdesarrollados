import json
import time
import argparse
import random
import os
import py_compile
from playwright.sync_api import sync_playwright

JS_FUNC = """
async (url) => {
    try {
        const r = await fetch(url, { headers: { 'x-requested-with': 'XMLHttpRequest' } });
        const text = await r.text();
        return { status: r.status, text: text };
    } catch (e) {
        return { status: 500, text: e.toString() };
    }
}
"""

def get_whoscored_page(browser):
    for context in browser.contexts:
        for page in context.pages:
            if "whoscored.com" in page.url:
                return page
    return None

def fetch_players(league_key, test_pages, cdp_url):
    with open("scraper/leagues.json", "r") as f:
        leagues = json.load(f)
    
    league_info = leagues[league_key]
    stage_id = league_info["stageId"]
    
    with sync_playwright() as p:
        browser = p.chromium.connect_over_cdp(cdp_url)
        page = get_whoscored_page(browser)
        if not page:
            print("Error: No se encontro pestana whoscored.com.")
            return

        origin = page.evaluate("location.origin")
        os.makedirs(f"scraper/cache/{league_key}", exist_ok=True)
        
        def request_page(p_num):
            url = f"{origin}/statisticsfeed/1/getplayerstatistics?category=summary&subcategory=all&statsAccumulationType=0&isCurrent=true&stageId={stage_id}&page={p_num}&numberOfPlayersToPick=10"
            for attempt in range(3):
                try:
                    res = page.evaluate(JS_FUNC, url)
                    if res['status'] == 200:
                        try:
                            return json.loads(res['text'])
                        except:
                            pass
                except Exception as e:
                    print(f"Excepcion en fetch: {e}")
                
                if attempt < 2:
                    wait = random.uniform(45, 90)
                    time.sleep(wait)
            return None

        initial_data = request_page(1)
        if not initial_data: return
            
        total_pages = initial_data["paging"]["totalPages"]
        all_players = {}
        consecutive_failures = 0
        
        # Determinar rango
        pages_to_fetch = range(17, 19) if test_pages else range(1, total_pages + 1)
        
        for p_num in pages_to_fetch:
            cache_file = f"scraper/cache/{league_key}/page_{p_num}.json"
            
            if os.path.exists(cache_file):
                with open(cache_file, "r", encoding="utf-8") as f:
                    data = json.load(f)
            else:
                data = request_page(p_num)
                if data:
                    with open(cache_file, "w", encoding="utf-8") as f:
                        json.dump(data, f, ensure_ascii=False)
                    consecutive_failures = 0
                else:
                    consecutive_failures += 1
                    if consecutive_failures >= 3: break
                    continue
            
            for player in data.get("playerTableStats", []):
                all_players[player["playerId"]] = player
            
            time.sleep(random.uniform(4, 8))

        print(f"Liga {league_key} finalizada.")

if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("league")
    parser.add_argument("--test", action="store_true")
    parser.add_argument("--cdp", default="http://localhost:9222")
    args = parser.parse_args()
    fetch_players(args.league, args.test, args.cdp)
