package com.biblioteca.controller;

import com.biblioteca.dto.CrearPrestamoRequest;
import com.biblioteca.dto.PrestamoDTO;
import com.biblioteca.service.PrestamoService;
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
 * Controller REST para /api/prestamos.
 */
@WebServlet("/api/prestamos/*")
public class PrestamoController extends HttpServlet {

    private final PrestamoService servicio = new PrestamoService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        if (pathInfo != null && !pathInfo.isEmpty() && !pathInfo.equals("/")) {
            Integer id = PathIdUtil.extractId(pathInfo);
            PrestamoDTO dto = servicio.buscarPorId(id);
            resp.setStatus(200);
            resp.setContentType("application/json; charset=UTF-8");
            resp.getWriter().write(JsonUtil.toJson(dto));
        } else {
            Integer socioId = null;
            String estado = null;
            String socioParam = req.getParameter("socio_id");
            if (socioParam != null && !socioParam.isEmpty()) {
                socioId = Integer.parseInt(socioParam);
            }
            estado = req.getParameter("estado");
            List<PrestamoDTO> lista = servicio.listar(socioId, estado);
            resp.setStatus(200);
            resp.setContentType("application/json; charset=UTF-8");
            resp.getWriter().write(JsonUtil.toJson(lista));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try (InputStream is = req.getInputStream()) {
            CrearPrestamoRequest reqDto = JsonUtil.fromJson(is.readAllBytes(), CrearPrestamoRequest.class);
            PrestamoDTO creado = servicio.crear(reqDto);
            resp.setStatus(201);
            resp.setHeader("Location", "/api/prestamos/" + creado.getId());
            resp.setContentType("application/json; charset=UTF-8");
            resp.getWriter().write(JsonUtil.toJson(creado));
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        if (pathInfo != null && pathInfo.contains("/devolucion")) {
            Integer id = PathIdUtil.extractId(pathInfo);
            PrestamoDTO devuelto = servicio.devolver(id);
            resp.setStatus(200);
            resp.setContentType("application/json; charset=UTF-8");
            resp.getWriter().write(JsonUtil.toJson(devuelto));
        }
    }
}
