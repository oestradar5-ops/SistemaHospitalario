package com.hospital.his.controller;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboards/farmacia")
public class DashboardFarmaciaController {

    private final JdbcTemplate jdbcTemplate;

    public DashboardFarmaciaController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // ==========================================
    // TOP 5 DE MEDICAMENTOS MÁS DESPACHADOS
    // ==========================================

    @GetMapping("/top")
    public List<Map<String, Object>> obtenerTopMedicamentos() {

        String sql = """
                SELECT
                    cm.id,
                    cm.nombre AS medicamento,
                    SUM(dd.cantidad) AS cantidad_despachada

                FROM public.detalle_despachos dd

                INNER JOIN public.catalogo_medicamentos cm
                    ON dd.medicamento_entregado_id = cm.id

                INNER JOIN public.despachos_medicamentos dm
                    ON dd.despacho_id = dm.id

                WHERE dm.estado = 'CONFIRMADO'

                GROUP BY cm.id, cm.nombre

                ORDER BY cantidad_despachada DESC

                LIMIT 5
                """;

        return jdbcTemplate.queryForList(sql);
    }

    // ==========================================
    // TOTAL DE UNIDADES DESPACHADAS
    // ==========================================

    @GetMapping("/total")
    public Map<String, Object> obtenerTotalDespachado() {

        String sql = """
                SELECT
                    COALESCE(SUM(dd.cantidad), 0) AS total_unidades

                FROM public.detalle_despachos dd

                INNER JOIN public.despachos_medicamentos dm
                    ON dd.despacho_id = dm.id

                WHERE dm.estado = 'CONFIRMADO'
                """;

        return jdbcTemplate.queryForMap(sql);
    }

    // ==========================================
    // MEDICAMENTO MÁS DESPACHADO
    // ==========================================

    @GetMapping("/mas-despachado")
    public List<Map<String, Object>> obtenerMasDespachado() {

        String sql = """
                SELECT
                    cm.nombre AS medicamento,
                    SUM(dd.cantidad) AS cantidad_despachada

                FROM public.detalle_despachos dd

                INNER JOIN public.catalogo_medicamentos cm
                    ON dd.medicamento_entregado_id = cm.id

                INNER JOIN public.despachos_medicamentos dm
                    ON dd.despacho_id = dm.id

                WHERE dm.estado = 'CONFIRMADO'

                GROUP BY cm.id, cm.nombre

                ORDER BY cantidad_despachada DESC

                LIMIT 1
                """;

        return jdbcTemplate.queryForList(sql);
    }

    // ==========================================
    // IDENTIFICAR COLUMNA DE FECHA
    // ==========================================

    private String obtenerColumnaFecha() {

        String sql = """
                SELECT column_name

                FROM information_schema.columns

                WHERE table_schema = 'public'

                  AND table_name = 'despachos_medicamentos'

                  AND data_type IN (
                      'timestamp without time zone',
                      'timestamp with time zone',
                      'date'
                  )

                ORDER BY
                    CASE column_name
                        WHEN 'fecha_confirmacion' THEN 1
                        WHEN 'fecha_despacho' THEN 2
                        WHEN 'fecha_entrega' THEN 3
                        WHEN 'fecha_creacion' THEN 4
                        WHEN 'fecha_registro' THEN 5
                        ELSE 6
                    END

                LIMIT 1
                """;

        List<String> columnas = jdbcTemplate.queryForList(
                sql,
                String.class
        );

        if (columnas.isEmpty()) {
            return null;
        }

        return columnas.get(0);
    }

    // ==========================================
    // HISTORIAL DE DESPACHOS
    // CON FECHA Y HORA
    // ==========================================

    @GetMapping("/historial")
    public List<Map<String, Object>> obtenerHistorialDespachos() {

        String columnaFecha = obtenerColumnaFecha();

        String expresionFecha = "NULL::timestamp";

        if (columnaFecha != null) {

            // Escapar el identificador SQL.
            String columnaSegura =
                    columnaFecha.replace("\"", "\"\"");

            expresionFecha =
                    "dm.\"" + columnaSegura + "\"";
        }

        String sql = """
                SELECT
                    dm.id AS id_despacho,

                    dd.id AS id_detalle,

                    cm.id AS id_medicamento,

                    cm.nombre AS medicamento,

                    dd.cantidad AS cantidad,

                    TO_CHAR(
                        %s,
                        'DD/MM/YYYY'
                    ) AS fecha,

                    TO_CHAR(
                        %s,
                        'HH12:MI:SS AM'
                    ) AS hora

                FROM public.detalle_despachos dd

                INNER JOIN public.catalogo_medicamentos cm
                    ON dd.medicamento_entregado_id = cm.id

                INNER JOIN public.despachos_medicamentos dm
                    ON dd.despacho_id = dm.id

                WHERE dm.estado = 'CONFIRMADO'

                ORDER BY
                    %s DESC NULLS LAST,
                    dm.id DESC,
                    dd.id DESC
                """.formatted(
                expresionFecha,
                expresionFecha,
                expresionFecha
        );

        return jdbcTemplate.queryForList(sql);
    }

    // ==========================================
    // VERIFICAR COLUMNA DE FECHA UTILIZADA
    // ==========================================

    @GetMapping("/verificar-fecha")
    public Map<String, Object> verificarColumnaFecha() {

        String columnaFecha = obtenerColumnaFecha();

        return Map.of(
                "tabla",
                "despachos_medicamentos",

                "columna_fecha",
                columnaFecha != null
                        ? columnaFecha
                        : "NO ENCONTRADA",

                "estado",
                columnaFecha != null
                        ? "COLUMNA DETECTADA"
                        : "SIN COLUMNA DE FECHA"
        );
    }
}