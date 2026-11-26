package com.biblioteca.util;

import com.biblioteca.dto.ErrorResponse;
import com.biblioteca.exception.*;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Collections;

/**
 * Filtro que intercepta excepciones y las convierte en respuestas JSON uniformes.
 */
@WebFilter("/*")
public class ErrorHandlingFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        try {
            chain.doFilter(request, response);
        } catch (RecursoNoEncontradoException e) {
            setErrorResponse(response, 404, "Not Found", e.getMessage());
        } catch (ValidacionException e) {
            setErrorResponse(response, 400, "Bad Request", e.getMessage());
        } catch (ConflictoException e) {
            setErrorResponse(response, 409, "Conflict", e.getMessage());
        } catch (ServicioException e) {
            setErrorResponse(response, 500, "Internal Server Error", "Error interno del servidor.");
        } catch (Exception e) {
            setErrorResponse(response, 500, "Internal Server Error", "Error inesperado: " + e.getMessage());
        }
    }

    private void setErrorResponse(ServletResponse response, int status, String error, String mensaje)
            throws IOException {
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        httpResponse.setStatus(status);
        httpResponse.setContentType("application/json; charset=UTF-8");

        ErrorResponse errorResponse = new ErrorResponse(status, error, mensaje, Collections.emptyList());
        httpResponse.getWriter().write(JsonUtil.toJson(errorResponse));
    }
}
