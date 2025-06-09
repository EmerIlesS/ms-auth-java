package com.auth.controller;

import com.auth.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class HealthController implements HealthIndicator {

    @Autowired
    private MongoTemplate mongoTemplate;
    
    @Autowired
    private ProductService productService;

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        
        // Verificar estado general
        boolean isHealthy = true;
        
        // Verificar conexión a MongoDB
        boolean dbConnected = checkMongoConnection();
        response.put("database", dbConnected ? "connected" : "disconnected");
        if (!dbConnected) isHealthy = false;
        
        // Verificar conexión al servicio de productos
        boolean productsServiceConnected = productService.isServiceAvailable();
        response.put("products_service", productsServiceConnected ? "connected" : "disconnected");
        // No marcamos como no saludable si falla el servicio de productos, ya que usamos circuit breaker
        
        // Información general
        response.put("status", isHealthy ? "healthy" : "unhealthy");
        response.put("timestamp", System.currentTimeMillis());
        response.put("service", "auth-service");
        response.put("version", "1.0");
        
        // Información del sistema
        Map<String, Object> systemInfo = new HashMap<>();
        systemInfo.put("jvm_memory_free_mb", Runtime.getRuntime().freeMemory() / (1024 * 1024));
        systemInfo.put("jvm_memory_total_mb", Runtime.getRuntime().totalMemory() / (1024 * 1024));
        systemInfo.put("processors", Runtime.getRuntime().availableProcessors());
        systemInfo.put("uptime_seconds", java.lang.management.ManagementFactory.getRuntimeMXBean().getUptime() / 1000);
        response.put("system", systemInfo);
        
        return ResponseEntity.ok(response);
    }
    
    @Override
    public Health health() {
        boolean dbConnected = checkMongoConnection();
        
        if (dbConnected) {
            return Health.up()
                    .withDetail("database", "connected")
                    .withDetail("products_service", productService.isServiceAvailable() ? "connected" : "disconnected")
                    .build();
        }
        
        return Health.down()
                .withDetail("database", "disconnected")
                .build();
    }
    
    private boolean checkMongoConnection() {
        try {
            // Intenta ejecutar un ping a la base de datos
            mongoTemplate.executeCommand("{ ping: 1 }");
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
