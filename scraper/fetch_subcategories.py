import json
import time
import argparse
import random
import os
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

def request_page(page, url):
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

def fetch_category(league_key, subcategory, cdp_url):
    with open("scraper/leagues.json", "r") as f:
        leagues = json.load(f)
    
    league_info = leagues[league_key]
    stage_id = league_info["stageId"]
    tournament_options = league_info.get("tournamentOptions", 2)
    
    os.makedirs(f"scraper/cache/{league_key}/{subcategory}", exist_ok=True)
    
    with sync_playwright() as p:
        browser = p.chromium.connect_over_cdp(cdp_url)
        page = [pg for ctx in browser.contexts for pg in ctx.pages if "whoscored.com" in pg.url][0]
        origin = page.evaluate("location.origin")
        
        initial_data = request_page(page, f"{origin}/statisticsfeed/1/getplayerstatistics?category=summary&subcategory={subcategory}&statsAccumulationType=0&isCurrent=true&stageId={stage_id}&tournamentOptions={tournament_options}&page=1&numberOfPlayersToPick=100&sortBy=Rating")
        if not initial_data: return
        
        total_pages = initial_data["paging"]["totalPages"]
        all_players = {}
        consecutive_failures = 0
        
        for p_num in range(1, total_pages + 1):
            cache_file = f"scraper/cache/{league_key}/{subcategory}/page_{p_num}.json"
            
            if os.path.exists(cache_file):
                with open(cache_file, "r", encoding="utf-8") as f:
                    try:
                        data = json.load(f)
                    except:
                        data = None
            else:
                data = request_page(page, f"{origin}/statisticsfeed/1/getplayerstatistics?category=summary&subcategory={subcategory}&statsAccumulationType=0&isCurrent=true&stageId={stage_id}&tournamentOptions={tournament_options}&page={p_num}&numberOfPlayersToPick=100&sortBy=Rating")
                if data:
                    with open(cache_file, "w", encoding="utf-8") as f:
                        json.dump(data, f, ensure_ascii=False)
                    consecutive_failures = 0
                else:
                    consecutive_failures += 1
                    if consecutive_failures >= 3: break
                    continue
            
            if data:
                for player in data.get("playerTableStats", []):
                    key = (player["playerId"], player["tournamentId"], player["seasonId"], player["teamId"])
                    all_players[key] = player
            
            time.sleep(random.uniform(4, 8))
        
        # Validation
        final_list = list(all_players.values())
        print(f"Liga {league_key} - Subcategory {subcategory}: Total uniques {len(final_list)} | Expected {initial_data['paging']['totalResults']}")

if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("--league")
    parser.add_argument("--subcategory")
    parser.add_argument("--cdp", default="http://localhost:9222")
    args = parser.parse_args()
    fetch_category(args.league, args.subcategory, args.cdp)
