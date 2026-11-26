package com.biblioteca.controller;

import com.biblioteca.dto.SocioDTO;
import com.biblioteca.service.SocioService;
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
 * Controller REST para /api/socios.
 */
@WebServlet("/api/socios/*")
public class SocioController extends HttpServlet {

    private final SocioService servicio = new SocioService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        if (pathInfo != null && !pathInfo.isEmpty() && !pathInfo.equals("/")) {
            SocioDTO dto = servicio.buscarPorId(PathIdUtil.extractId(pathInfo));
            resp.setStatus(200);
            resp.setContentType("application/json; charset=UTF-8");
            resp.getWriter().write(JsonUtil.toJson(dto));
        } else {
            List<SocioDTO> lista = servicio.listar();
            resp.setStatus(200);
            resp.setContentType("application/json; charset=UTF-8");
            resp.getWriter().write(JsonUtil.toJson(lista));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try (InputStream is = req.getInputStream()) {
            SocioDTO dto = JsonUtil.fromJson(is.readAllBytes(), SocioDTO.class);
            SocioDTO creado = servicio.crear(dto);
            resp.setStatus(201);
            resp.setHeader("Location", "/api/socios/" + creado.getId());
            resp.setContentType("application/json; charset=UTF-8");
            resp.getWriter().write(JsonUtil.toJson(creado));
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Integer id = PathIdUtil.extractId(req.getPathInfo());
        try (InputStream is = req.getInputStream()) {
            SocioDTO dto = JsonUtil.fromJson(is.readAllBytes(), SocioDTO.class);
            SocioDTO actualizado = servicio.actualizar(id, dto);
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
