package com.biblioteca.util;

import com.biblioteca.exception.ValidacionException;

public final class PathIdUtil {

    private PathIdUtil() {
    }

    public static Integer extractId(String pathInfo) {
        if (pathInfo == null || pathInfo.isEmpty() || pathInfo.equals("/")) {
            throw new ValidacionException("id", "El ID de la ruta es obligatorio.");
        }

        int segmentStart = pathInfo.charAt(0) == '/' ? 1 : 0;
        int segmentEnd = pathInfo.indexOf('/', segmentStart);
        if (segmentEnd == -1) {
            segmentEnd = pathInfo.length();
        }

        String idSegment = pathInfo.substring(segmentStart, segmentEnd);
        if (idSegment.isEmpty()) {
            throw new ValidacionException("id", "El ID de la ruta es obligatorio.");
        }

        try {
            return Integer.valueOf(idSegment);
        } catch (NumberFormatException e) {
            throw new ValidacionException("id", "El ID de la ruta debe ser numérico.");
        }
    }
}