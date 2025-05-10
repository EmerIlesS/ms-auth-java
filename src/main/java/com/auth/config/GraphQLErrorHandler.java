package com.auth.config;

import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import graphql.schema.DataFetchingEnvironment;
import org.springframework.graphql.execution.DataFetcherExceptionResolver;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Manejador centralizado de errores para GraphQL.
 * Convierte excepciones de Java en errores GraphQL con mensajes descriptivos.
 */
@Component
public class GraphQLErrorHandler implements DataFetcherExceptionResolver {

    @Override
    public List<GraphQLError> resolveException(Throwable exception, DataFetchingEnvironment environment) {
        String message;
        String errorCode;
        
        if (exception instanceof AccessDeniedException) {
            message = exception.getMessage() != null ? 
                    exception.getMessage() : 
                    "No autorizado. No tiene permisos suficientes para realizar esta acción.";
            errorCode = "FORBIDDEN";
        } else if (exception instanceof AuthenticationException) {
            message = "No autenticado. Por favor, inicie sesión para continuar.";
            errorCode = "UNAUTHENTICATED";
        } else if (exception instanceof ResponseStatusException) {
            ResponseStatusException responseException = (ResponseStatusException) exception;
            message = responseException.getReason() != null ? 
                    responseException.getReason() : 
                    "Error en la solicitud: " + responseException.getStatusCode().toString();
            
            if (responseException.getStatusCode().value() == 401) {
                errorCode = "UNAUTHENTICATED";
            } else if (responseException.getStatusCode().value() == 403) {
                errorCode = "FORBIDDEN";
            } else if (responseException.getStatusCode().value() == 404) {
                errorCode = "NOT_FOUND";
            } else if (responseException.getStatusCode().value() == 400) {
                errorCode = "BAD_USER_INPUT";
            } else {
                errorCode = "INTERNAL_SERVER_ERROR";
            }
        } else if (exception instanceof IllegalArgumentException) {
            message = exception.getMessage() != null ? 
                    exception.getMessage() : 
                    "Datos de entrada inválidos. Por favor, verifique la información proporcionada.";
            errorCode = "BAD_USER_INPUT";
        } else {
            // Para errores internos, no exponer detalles técnicos al cliente en producción
            message = "Error interno del servidor. Por favor, inténtelo de nuevo más tarde.";
            errorCode = "INTERNAL_SERVER_ERROR";
            
            // Registrar el error completo para depuración
            exception.printStackTrace();
        }
        
        return Collections.singletonList(
            GraphqlErrorBuilder.newError()
                .message(message)
                .path(environment.getExecutionStepInfo().getPath())
                .location(environment.getField().getSourceLocation())
                .extensions(Map.of("code", errorCode))
                .build()
        );
    }
}