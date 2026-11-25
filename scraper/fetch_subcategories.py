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

CATEGORIES = {
    "Summary": "all",
    "Offensive": "offensive",
    "Defensive": "defensive",
    "Passing": "passing"
}

def request_page(page, url):
    for attempt in range(3):
        try:
            res = page.evaluate(JS_FUNC, url)
            if res['status'] == 200:
                try:
                    data = json.loads(res['text'])
                    if "playerTableStats" in data:
                        return data
                except:
                    pass
        except Exception as e:
            print(f"Excepcion en fetch: {e}")
        
        if attempt < 2:
            time.sleep(random.uniform(10, 20))
    return None

def fetch_category(league_key, cat_name, subcategory, cdp_url):
    with open("scraper/leagues.json", "r") as f:
        leagues = json.load(f)
    
    league_info = leagues[league_key]
    stage_id = league_info["stageId"]
    tournament_options = league_info.get("tournamentOptions", 2)
    
    cat_dir = f"scraper/cache/{league_key}/{cat_name}"
    os.makedirs(cat_dir, exist_ok=True)
    
    with sync_playwright() as p:
        browser = p.chromium.connect_over_cdp(cdp_url)
        page = [pg for ctx in browser.contexts for pg in ctx.pages if "whoscored.com" in pg.url][0]
        origin = page.evaluate("location.origin")
        
        # Petición inicial para obtener total de páginas
        base_url = f"{origin}/statisticsfeed/1/getplayerstatistics?category=summary&subcategory={subcategory}&statsAccumulationType=0&isCurrent=true&stageId={stage_id}&tournamentOptions={tournament_options}&page=1&numberOfPlayersToPick=100&sortBy=Rating"
        initial_data = request_page(page, base_url)
        if not initial_data: 
            print(f"Error inicial en {league_key} - {cat_name}")
            return
        
        total_pages = initial_data["paging"]["totalPages"]
        print(f"Descargando {league_key} - {cat_name}: {total_pages} paginas")
        
        for p_num in range(1, total_pages + 1):
            cache_file = f"{cat_dir}/page_{p_num}.json"
            
            if os.path.exists(cache_file):
                continue
            
            data = request_page(page, base_url.replace("page=1", f"page={p_num}"))
            if data:
                with open(cache_file, "w", encoding="utf-8") as f:
                    json.dump(data, f, ensure_ascii=False)
            
            time.sleep(random.uniform(5, 10))

if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("--league")
    parser.add_argument("--cdp", default="http://localhost:9222")
    args = parser.parse_args()
    
    for cat_name, subcat in CATEGORIES.items():
        fetch_category(args.league, cat_name, subcat, args.cdp)
