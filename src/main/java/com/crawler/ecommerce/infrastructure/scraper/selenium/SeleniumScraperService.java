package com.crawler.ecommerce.infrastructure.scraper.selenium;

import io.github.bonigarcia.wdm.WebDriverManager;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.stereotype.Service;

import jakarta.annotation.PreDestroy;
import java.time.Duration;

/**
 * Servicio Selenium para manejo de Lazy Loading en sitios e-commerce.
 *
 * Implementa automatización de navegador para sitios con carga dinámica:
 * - Soporte para scroll infinito (Paris.cl y similares)
 * - WebDriver con configuración anti-detección
 * - Manejo de recursos y ciclo de vida del navegador
 *
 * ------------------------------------------------------------------------
 * NOTA ARQUITECTÓNICA — SELENIUM SERVICE
 *
 * Este servicio sigue patrones de diseño robustos:
 *
 * - LAZY INITIALIZATION: Inicialización bajo demanda para optimizar recursos
 * - ANTI-DETECTION: Configuración headless y user-agent realista
 * - RESOURCE MANAGEMENT: Limpieza automática de procesos y memoria
 * - ERROR HANDLING: Manejo robusto de timeouts y excepciones
 * - INTEGRACIÓN: Compatibilidad con estrategias Jsoup existentes
 *
 * Las características implementadas son:
 * - Scroll programable con detección de estabilización
 * - Configuración de Chrome para evadir detección automatizada
 * - Tiempos de espera optimizados para carga de contenido dinámico
 * - Logging detallado para debugging de comportamientos del navegador
 * - Ciclo de vida controlado con @PreDestroy
 * ------------------------------------------------------------------------
 */
@Service
@Slf4j
public class SeleniumScraperService {

    private WebDriver driver;

    /**
     * Inicialización lazy del WebDriver.
     * Solo se crea cuando se necesita por primera vez para optimizar recursos.
     * 
     * Configuración específica para evitar detección:
     * - Headless mode (opcional)
     * - User-Agent realista
     * - Sin sandboxing (menos sospechoso)
     */
    private synchronized void ensureDriverInitialized() {
        if (driver == null) {
            log.info("Inicializando ChromeDriver (lazy)");
            WebDriverManager.chromedriver().setup();

            ChromeOptions options = new ChromeOptions();
            options.addArguments("--headless", "--no-sandbox", "--disable-dev-shm-usage");
            options.addArguments("--disable-blink-features=AutomationControlled");
            options.addArguments("--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
            options.setPageLoadTimeout(Duration.ofSeconds(30));

            driver = new ChromeDriver(options);
            log.info("ChromeDriver listo");
        }
    }

    /**
     * Scroll infinito hasta cargar todos los items de Paris.cl.
     * 
     * Algoritmo de scroll:
     * 1. Scroll al final de página
     * 2. Esperar carga de nuevos items (1.5s)
     * 3. Contar items actuales
     * 4. Repetir hasta que no aparezcan nuevos items (3 intentos)
     * 5. Máximo 50 scrolls para evitar loops infinitos
     * 
     * @param url URL de la categoría a scrapear
     * @param itemsSelector Selector CSS para contar items
     * @return Document Jsoup con HTML completo
     * @throws RuntimeException Si falla el scraping
     */
    public Document scrapeWithInfiniteScroll(String url, String itemsSelector) {
        try {
            ensureDriverInitialized(); // Lazy init
            log.info("Selenium → {}", url);
            driver.get(url);
            Thread.sleep(3000); // Carga inicial

            JavascriptExecutor js = (JavascriptExecutor) driver;
            int previousCount = 0;
            int stableScrolls = 0;
            int maxScrolls = 50;

            for (int i = 0; i < maxScrolls; i++) {
                js.executeScript("window.scrollTo(0, document.body.scrollHeight);");
                Thread.sleep(1500);

                String countScript = "return document.querySelectorAll('div[data-cnstrc-item-id][role=\"gridcell\"]').length;";
                long currentCount = (Long) js.executeScript(countScript);

                log.debug("Scroll {}/{}: {} items", i + 1, maxScrolls, currentCount);

                if (currentCount == previousCount) {
                    stableScrolls++;
                    if (stableScrolls >= 3)
                        break;
                } else {
                    stableScrolls = 0;
                }
                previousCount = (int) currentCount;
            }

            String html = driver.getPageSource();
            log.info("Selenium completo: {} items", previousCount);
            return Jsoup.parse(html, url);

        } catch (Exception e) {
            log.error("Selenium error: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * Limpieza de recursos WebDriver.
     * 
     * Importante para liberar memoria y procesos Chrome zombies.
     * Se ejecuta automáticamente al detener la aplicación (@PreDestroy).
     */
    @PreDestroy
    public void close() {
        if (driver != null) {
            log.info("Cerrando ChromeDriver");
            driver.quit();
        }
    }
}
