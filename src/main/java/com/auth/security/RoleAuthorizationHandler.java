package com.auth.security;

import com.auth.model.User;
import org.springframework.graphql.execution.DataFetcherExceptionResolverAdapter;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import graphql.schema.DataFetchingEnvironment;

import java.util.Map;

import java.util.Arrays;

/**
 * Manejador centralizado para la validación de roles y permisos en ms-auth-java.
 * Proporciona métodos para verificar si un usuario tiene los roles necesarios para acceder a recursos.
 */
@Component
public class RoleAuthorizationHandler extends DataFetcherExceptionResolverAdapter {

    /**
     * Verifica si el usuario actual está autenticado.
     * @return El usuario autenticado
     * @throws ResponseStatusException con código 401 si el usuario no está autenticado
     */
    public static User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || 
            !(authentication.getPrincipal() instanceof User)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, 
                "No autenticado. Por favor, inicie sesión para continuar.");
        }
        return (User) authentication.getPrincipal();
    }

    /**
     * Verifica si el usuario actual tiene alguno de los roles especificados.
     * @param roles Lista de roles permitidos
     * @return El usuario autenticado si tiene alguno de los roles especificados
     * @throws AccessDeniedException si el usuario no tiene ninguno de los roles especificados
     */
    public static User checkRoles(String... roles) {
        User user = getCurrentUser();
        boolean hasRole = Arrays.stream(roles)
            .anyMatch(role -> user.getRole().equalsIgnoreCase(role));
        
        if (!hasRole) {
            throw new AccessDeniedException(
                String.format("No autorizado. Se requiere uno de los siguientes roles: %s.", 
                String.join(", ", roles)));
        }
        
        return user;
    }

    /**
     * Verifica si el usuario es administrador.
     * @return El usuario autenticado si es administrador
     */
    public static User checkAdmin() {
        return checkRoles("ADMIN");
    }

    /**
     * Verifica si el usuario es vendedor.
     * @return El usuario autenticado si es vendedor
     */
    public static User checkSeller() {
        return checkRoles("SELLER");
    }

    /**
     * Verifica si el usuario es cliente.
     * @return El usuario autenticado si es cliente
     */
    public static User checkCustomer() {
        return checkRoles("CUSTOMER");
    }

    /**
     * Verifica si el usuario es administrador o vendedor.
     * @return El usuario autenticado si es administrador o vendedor
     */
    public static User checkAdminOrSeller() {
        return checkRoles("ADMIN", "SELLER");
    }

    /**
     * Verifica si el usuario es propietario del recurso o administrador.
     * @param resourceUserId ID del propietario del recurso
     * @return El usuario autenticado si es propietario o administrador
     */
    public static User checkOwnerOrAdmin(String resourceUserId) {
        User user = getCurrentUser();
        if (!user.getRole().equalsIgnoreCase("ADMIN") && 
            !user.getId().equals(resourceUserId)) {
            throw new AccessDeniedException(
                "No autorizado. Solo puede acceder a sus propios recursos.");
        }
        return user;
    }

    /**
     * Convierte excepciones de autorización en errores GraphQL.
     */
    @Override
    protected GraphQLError resolveToSingleError(Throwable ex, DataFetchingEnvironment env) {
        if (ex instanceof AccessDeniedException) {
            return GraphqlErrorBuilder.newError()
                .message(ex.getMessage())
                .path(env.getExecutionStepInfo().getPath())
                .location(env.getField().getSourceLocation())
                .extensions(Map.of("code", "FORBIDDEN"))
                .build();
        } else if (ex instanceof ResponseStatusException && 
                  ((ResponseStatusException) ex).getStatusCode() == HttpStatus.UNAUTHORIZED) {
            return GraphqlErrorBuilder.newError()
                .message(ex.getMessage())
                .path(env.getExecutionStepInfo().getPath())
                .location(env.getField().getSourceLocation())
                .extensions(Map.of("code", "UNAUTHENTICATED"))
                .build();
        }
        return null;
    }
}