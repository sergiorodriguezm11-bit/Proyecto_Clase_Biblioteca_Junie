package com.biblioteca.util;

import com.biblioteca.exception.ConflictoException;
import com.biblioteca.exception.RecursoNoEncontradoException;
import com.biblioteca.exception.ServicioException;
import com.biblioteca.exception.ValidacionException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ErrorHandlingFilterTest {

    @Test
    void mapeaExcepcionesDeDominioARespuestasHttp() throws Exception {
        assertStatus(new ValidacionException("isbn", "ISBN inválido"), 400, "ISBN inválido");
        assertStatus(new RecursoNoEncontradoException("No encontrado"), 404, "No encontrado");
        assertStatus(new ConflictoException("Conflicto"), 409, "Conflicto");
        assertStatus(new ServicioException("Fallo interno"), 500, "Error interno del servidor.");
    }

    private void assertStatus(RuntimeException error, int status, String mensaje) throws Exception {
        ServletRequest request = mock(ServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);
        StringWriter body = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(body));
        doThrow(error).when(chain).doFilter(request, response);

        new ErrorHandlingFilter().doFilter(request, response, chain);

        verify(response).setStatus(status);
        assertTrue(body.toString().contains(mensaje));
    }
}