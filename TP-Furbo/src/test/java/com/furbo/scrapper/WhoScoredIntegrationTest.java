package com.furbo.scrapper;

import com.microsoft.playwright.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;
import java.nio.file.Paths;

public class WhoScoredIntegrationTest {

    @Test
    @Disabled("Solo para ejecución manual. Requiere acceso real y puede ser bloqueado.")
    void manualAccessTest() {
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
            Page page = browser.newPage();
            
            page.setExtraHTTPHeaders(java.util.Map.of(
                "User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
            ));

            System.out.println("Navegando a WhoScored...");
            Response response = page.navigate("https://www.whoscored.com/Regions/252/Tournaments/2/Seasons/9684/Stages/22036/PlayerStatistics/England-Premier-League-2024-2025");
            
            System.out.println("URL final: " + page.url());
            System.out.println("Status: " + response.status());
            
            browser.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
