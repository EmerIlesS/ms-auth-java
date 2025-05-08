package com.auth.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ProductService {

    private final WebClient webClient;
    
    public ProductService(WebClient.Builder webClientBuilder, @Value("${products.service.url}") String productsServiceUrl) {
        this.webClient = webClientBuilder
                .baseUrl(productsServiceUrl)
                .build();
    }
    
    /**
     * Verifica si un producto existe en el microservicio de productos
     * @param productId ID del producto a verificar
     * @return true si el producto existe, false en caso contrario
     */
    public boolean productExists(String productId) {
        try {
            // Llamada al endpoint de verificación de producto en ms-products-orders
            Boolean exists = webClient.get()
                    .uri("/api/products/{id}/exists", productId)
                    .retrieve()
                    .bodyToMono(Boolean.class)
                    .block();
            
            return exists != null && exists;
        } catch (WebClientResponseException ex) {
            if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
                return false;
            }
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, 
                    "No se pudo conectar con el servicio de productos", ex);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                    "Error al verificar la existencia del producto", ex);
        }
    }
}