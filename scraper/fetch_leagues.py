import time
import json
import os
from curl_cffi import requests

def log_intento(metodo, url, status, bloqueo, preview):
    with open("scraper/INTENTOS.md", "a", encoding="utf-8") as f:
        f.write(f"\n# INTENTO {int(time.time())}\n")
        f.write(f"- Método: {metodo}\n- URL: {url}\n- Status HTTP: {status}\n- Bloqueo: {bloqueo}\n- Fragmento: {preview}\n")

def fetch_leagues():
    leagues = {
        "epl": "/regions/252/tournaments/2",
        "laliga": "/regions/206/tournaments/4",
        "seriea": "/regions/108/tournaments/5",
        "bundesliga": "/regions/81/tournaments/3",
        "ligue1": "/regions/74/tournaments/22"
    }
    
    headers = {
        "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/129.0.0.0 Safari/537.36"
    }

    for name, path in leagues.items():
        url = f"https://www.whoscored.com{path}"
        print(f"Fetching {name}...")
        
        response = requests.get(url, headers=headers, impersonate="chrome")
        
        bloqueo = "Sí" if response.status_code != 200 else "No"
        log_intento(f"Etapa 1 - {name}", url, response.status_code, bloqueo, response.text[:200])
        
        if response.status_code == 200:
            with open(f"scraper/cache/{name}_home.html", "w", encoding="utf-8") as f:
                f.write(response.text)
        else:
            print(f"Bloqueado en {name}. Status: {response.status_code}")
            return # Detener si hay bloqueo
        
        time.sleep(7)

if __name__ == "__main__":
    fetch_leagues()
