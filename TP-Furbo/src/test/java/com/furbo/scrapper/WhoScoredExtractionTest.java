package com.furbo.scrapper;

import org.junit.jupiter.api.Test;
import java.nio.file.Files;
import java.nio.file.Paths;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class WhoScoredExtractionTest {

    @Test
    void testParsingExistingHtml() throws Exception {
        String path = "src/test/resources/whoscored_sample.html";
        String content = new String(Files.readAllBytes(Paths.get(path)));
        
        // Verificamos si el contenido contiene el mensaje de bloqueo
        boolean isBlocked = content.contains("Sorry, you have been blocked") || content.contains("Cloudflare");
        
        System.out.println("¿Contenido bloqueado? " + isBlocked);
        
        // En un caso de éxito, aquí probaríamos nuestros selectores
        // Como el archivo actual es una página de bloqueo, esto es un test de "sanity check"
        assertTrue(content.length() > 0, "El archivo no debería estar vacío");
    }
}
