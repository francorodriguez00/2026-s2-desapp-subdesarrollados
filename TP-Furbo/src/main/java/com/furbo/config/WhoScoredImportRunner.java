package com.furbo.config;

import com.furbo.service.WhoScoredImportService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("whoscored-import")
public class WhoScoredImportRunner implements ApplicationRunner {

    private final WhoScoredImportService importService;
    private final String importDir;

    public WhoScoredImportRunner(WhoScoredImportService importService, 
                                @Value("${whoscored.import.dir:scraper/cache}") String importDir) {
        this.importService = importService;
        this.importDir = importDir;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        System.out.println("Iniciando importación desde: " + importDir);
        // Aquí iteraríamos sobre las carpetas de ligas según el requirement
        // Por ahora lanzamos la lógica de importLeague para el directorio base
        System.out.println("Directorio absoluto: " +
                new java.io.File(importDir).getAbsolutePath()); // sacar
        importService.importLeague(importDir);
        System.out.println("Importación finalizada.");
    }
}
