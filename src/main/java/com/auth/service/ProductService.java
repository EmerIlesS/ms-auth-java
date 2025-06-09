package com.auth.service;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import java.util.function.Supplier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;
import java.time.Duration;

@Service
public class ProductService {

    private final WebClient webClient;
    private final CircuitBreaker circuitBreaker;
    
    public ProductService(WebClient.Builder webClientBuilder, 
                         @Value("${products.service.url}") String productsServiceUrl,
                         CircuitBreaker productServiceCircuitBreaker) {
        this.webClient = webClientBuilder
                .baseUrl(productsServiceUrl)
                .build();
        this.circuitBreaker = productServiceCircuitBreaker;
    }
    
    /**
     * Verifica si un producto existe en el microservicio de productos
     * @param productId ID del producto a verificar
     * @return true si el producto existe, false en caso contrario
     */
    /**
     * Verifica si el servicio de productos está disponible
     * @return true si el servicio está disponible, false en caso contrario
     */
    public boolean isServiceAvailable() {
        try {
            // Realizamos una consulta simple para verificar conectividad
            String healthQuery = "{\"query\": \"query { __typename }\"}";
            
            String response = webClient.post()
                    .uri("/graphql")
                    .header("Content-Type", "application/json")
                    .bodyValue(healthQuery)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(3)) // Timeout corto para verificación
                    .block();
            
            return response != null && response.contains("__typename");
        } catch (Exception ex) {
            System.err.println("Servicio de productos no disponible: " + ex.getMessage());
            return false;
        }
    }
    
    public boolean productExists(String productId) {
        // Usamos Circuit Breaker para proteger la llamada al servicio de productos
        Supplier<Boolean> productExistsSupplier = () -> {
            try {
                // Llamada GraphQL para verificar si el producto existe con timeout y reintentos
                String graphqlQuery = "{\"query\": \"query { product(id: \\\"" + productId + "\\\") { id name price } }\"}";
                
                // Intentar con reintentos (máximo 3) con intervalo entre intentos
                int maxRetries = 3;
                int currentRetry = 0;
                
                while (currentRetry < maxRetries) {
                    try {
                        String response = webClient.post()
                                .uri("/graphql")
                                .header("Content-Type", "application/json")
                                .bodyValue(graphqlQuery)
                                .retrieve()
                                .bodyToMono(String.class)
                                .timeout(java.time.Duration.ofSeconds(5)) // Timeout de 5 segundos
                                .block();
                        
                        // Si obtenemos una respuesta y no contiene errores, el producto existe
                        return response != null && !response.contains("errors") && response.contains(productId);
                    } catch (Exception e) {
                        currentRetry++;
                        if (currentRetry >= maxRetries) {
                            throw e; // Re-lanzar la excepción si hemos agotado los intentos
                        }
                        System.out.println("Intento " + currentRetry + " fallido. Reintentando en 1 segundo...");
                        try {
                            Thread.sleep(1000); // Esperar 1 segundo antes de reintentar
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt(); // Restaurar el estado de interrupción
                            throw new RuntimeException("Thread fue interrumpido durante el reintento", ie);
                        }
                    }
                }
                
                return false; // No deberíamos llegar aquí, pero por si acaso
            } catch (WebClientResponseException ex) {
                // En caso de error HTTP, registramos el problema con más detalles
                System.err.println("Error HTTP al verificar producto " + productId + ": " + 
                                  "Código: " + ex.getRawStatusCode() + ", " +
                                  "Mensaje: " + ex.getMessage());
                throw ex; // Relanzamos para que el circuit breaker lo detecte
            } catch (Exception ex) {
                // En caso de cualquier otro error, registramos el problema con más detalles
                System.err.println("Error inesperado al verificar producto " + productId + ": " + 
                                  "Tipo: " + ex.getClass().getName() + ", " +
                                  "Mensaje: " + ex.getMessage());
                throw ex; // Relanzamos para que el circuit breaker lo detecte
            }
        };
        
        // Aplicamos el circuit breaker y manejamos el caso de circuito abierto
        try {
            return circuitBreaker.decorateSupplier(productExistsSupplier).get();
        } catch (Exception e) {
            // Si el circuito está abierto o hay un error, proporcionamos un valor por defecto
            System.err.println("Circuit breaker activo o error al verificar producto " + productId + ": " + e.getMessage());
            System.err.println("Estado del circuit breaker: " + circuitBreaker.getState());
            return false; // Valor por defecto cuando el servicio no está disponible
        }
    }
    }