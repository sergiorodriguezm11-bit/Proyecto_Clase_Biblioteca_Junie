package com.biblioteca.controller;

import com.biblioteca.dto.ActualizarLibroRequest;
import com.biblioteca.dto.CrearLibroRequest;
import com.biblioteca.dto.LibroDTO;
import com.biblioteca.service.LibroService;
import com.biblioteca.util.JsonUtil;
import com.biblioteca.util.PathIdUtil;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Controller REST para /api/libros con filtros.
 */
@WebServlet("/api/libros/*")
public class LibroController extends HttpServlet {

    private final LibroService servicio = new LibroService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        if (pathInfo != null && !pathInfo.isEmpty() && !pathInfo.equals("/")) {
            LibroDTO dto = servicio.buscarPorId(PathIdUtil.extractId(pathInfo));
            resp.setStatus(200);
            resp.setContentType("application/json; charset=UTF-8");
            resp.getWriter().write(JsonUtil.toJson(dto));
        } else {
            String titulo = req.getParameter("titulo");
            String autor = req.getParameter("autor");
            String categoria = req.getParameter("categoria");
            List<LibroDTO> lista = servicio.listar(titulo, autor, categoria);
            resp.setStatus(200);
            resp.setContentType("application/json; charset=UTF-8");
            resp.getWriter().write(JsonUtil.toJson(lista));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try (InputStream is = req.getInputStream()) {
            CrearLibroRequest reqDto = JsonUtil.fromJson(is.readAllBytes(), CrearLibroRequest.class);
            LibroDTO creado = servicio.crear(reqDto);
            resp.setStatus(201);
            resp.setHeader("Location", "/api/libros/" + creado.getId());
            resp.setContentType("application/json; charset=UTF-8");
            resp.getWriter().write(JsonUtil.toJson(creado));
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Integer id = PathIdUtil.extractId(req.getPathInfo());
        try (InputStream is = req.getInputStream()) {
            ActualizarLibroRequest reqDto = JsonUtil.fromJson(is.readAllBytes(), ActualizarLibroRequest.class);
            LibroDTO actualizado = servicio.actualizar(id, reqDto);
            resp.setStatus(200);
            resp.setContentType("application/json; charset=UTF-8");
            resp.getWriter().write(JsonUtil.toJson(actualizado));
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Integer id = PathIdUtil.extractId(req.getPathInfo());
        servicio.eliminar(id);
        resp.setStatus(204);
    }
}
