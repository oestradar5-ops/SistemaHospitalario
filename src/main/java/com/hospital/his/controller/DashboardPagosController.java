package com.hospital.his.controller;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboards/pagos")
public class DashboardPagosController {

    private final JdbcTemplate jdbcTemplate;

    public DashboardPagosController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // ==========================================
    // CANTIDAD DE PAGOS POR MÉTODO
    // ==========================================

    @GetMapping("/metodos")
    public List<Map<String, Object>> obtenerMetodosPago() {

        String sql = """
                SELECT
                    CASE
                        WHEN UPPER(TRIM(metodo_pago)) = 'VISA'
                            THEN 'Visa'

                        WHEN UPPER(TRIM(metodo_pago)) = 'MASTERCARD'
                            THEN 'Mastercard'

                        WHEN UPPER(TRIM(metodo_pago)) = 'EFECTIVO'
                            THEN 'Efectivo'

                        WHEN UPPER(TRIM(metodo_pago)) = 'TARJETA'
                            THEN 'Tarjeta sin especificar'

                        WHEN UPPER(TRIM(metodo_pago)) = 'DEBITO'
                            THEN 'Débito'

                        ELSE 'Otros'
                    END AS metodo,

                    COUNT(*) AS total_pagos

                FROM public.pagos

                WHERE estado = 'APROBADO'

                GROUP BY
                    CASE
                        WHEN UPPER(TRIM(metodo_pago)) = 'VISA'
                            THEN 'Visa'

                        WHEN UPPER(TRIM(metodo_pago)) = 'MASTERCARD'
                            THEN 'Mastercard'

                        WHEN UPPER(TRIM(metodo_pago)) = 'EFECTIVO'
                            THEN 'Efectivo'

                        WHEN UPPER(TRIM(metodo_pago)) = 'TARJETA'
                            THEN 'Tarjeta sin especificar'

                        WHEN UPPER(TRIM(metodo_pago)) = 'DEBITO'
                            THEN 'Débito'

                        ELSE 'Otros'
                    END

                ORDER BY total_pagos DESC
                """;

        return jdbcTemplate.queryForList(sql);
    }

    // ==========================================
    // TOTAL DE PAGOS APROBADOS
    // ==========================================

    @GetMapping("/total")
    public List<Map<String, Object>> obtenerTotalPagos() {

        String sql = """
                SELECT
                    COUNT(*) AS total_pagos
                FROM public.pagos
                WHERE estado = 'APROBADO'
                """;

        return jdbcTemplate.queryForList(sql);
    }

    // ==========================================
    // TOTAL RECAUDADO
    // ==========================================

    @GetMapping("/recaudacion")
    public List<Map<String, Object>> obtenerRecaudacion() {

        String sql = """
                SELECT
                    COALESCE(SUM(monto), 0) AS total_recaudado
                FROM public.pagos
                WHERE estado = 'APROBADO'
                """;

        return jdbcTemplate.queryForList(sql);
    }

    // ==========================================
    // HISTORIAL DE PAGOS CON FECHA Y HORA
    // ==========================================

    @GetMapping("/historial")
    public List<Map<String, Object>> obtenerHistorialPagos() {

        String sql = """
                SELECT
                    id,

                    CASE
                        WHEN UPPER(TRIM(metodo_pago)) = 'VISA'
                            THEN 'Visa'

                        WHEN UPPER(TRIM(metodo_pago)) = 'MASTERCARD'
                            THEN 'Mastercard'

                        WHEN UPPER(TRIM(metodo_pago)) = 'EFECTIVO'
                            THEN 'Efectivo'

                        WHEN UPPER(TRIM(metodo_pago)) = 'TARJETA'
                            THEN 'Tarjeta sin especificar'

                        WHEN UPPER(TRIM(metodo_pago)) = 'DEBITO'
                            THEN 'Débito'

                        ELSE 'Otros'
                    END AS metodo,

                    monto,

                    TO_CHAR(fecha_pago, 'DD/MM/YYYY') AS fecha,

                    TO_CHAR(fecha_pago, 'HH12:MI:SS AM') AS hora

                FROM public.pagos

                WHERE estado = 'APROBADO'

                ORDER BY fecha_pago DESC NULLS LAST, id DESC
                """;

        return jdbcTemplate.queryForList(sql);
    }

}