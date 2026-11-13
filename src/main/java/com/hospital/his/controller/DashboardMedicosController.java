package com.hospital.his.controller;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboards/medicos")
public class DashboardMedicosController {

    private final JdbcTemplate jdbcTemplate;

    public DashboardMedicosController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // ==========================================
    // TOP 5 DE MÉDICOS CON MÁS CONSULTAS
    // ==========================================

    @GetMapping("/top")
    public List<Map<String, Object>> obtenerTopMedicos() {

        String sql = """
                SELECT
                    u.nombre_completo AS medico,
                    COUNT(cm.id) AS total_consultas

                FROM public.consultas_medicas cm

                INNER JOIN public.citas c
                    ON cm.cita_id = c.id

                INNER JOIN public.medicos m
                    ON c.medico_id = m.id

                INNER JOIN public.usuarios u
                    ON m.usuario_id = u.id

                GROUP BY
                    m.id,
                    u.nombre_completo

                ORDER BY total_consultas DESC

                LIMIT 5
                """;

        return jdbcTemplate.queryForList(sql);
    }

    // ==========================================
    // DÍAS DE MAYOR DEMANDA
    // ==========================================

    @GetMapping("/demanda")
    public List<Map<String, Object>> obtenerDiasMayorDemanda() {

        String sql = """
                SELECT
                    EXTRACT(
                        ISODOW FROM cm.fecha_creacion
                    )::INTEGER AS numero_dia,

                    CASE EXTRACT(
                        ISODOW FROM cm.fecha_creacion
                    )::INTEGER

                        WHEN 1 THEN 'Lunes'
                        WHEN 2 THEN 'Martes'
                        WHEN 3 THEN 'Miércoles'
                        WHEN 4 THEN 'Jueves'
                        WHEN 5 THEN 'Viernes'
                        WHEN 6 THEN 'Sábado'
                        WHEN 7 THEN 'Domingo'

                    END AS dia_semana,

                    COUNT(cm.id) AS total_consultas

                FROM public.consultas_medicas cm

                WHERE cm.fecha_creacion IS NOT NULL

                GROUP BY
                    EXTRACT(
                        ISODOW FROM cm.fecha_creacion
                    )

                ORDER BY numero_dia
                """;

        return jdbcTemplate.queryForList(sql);
    }

    // ==========================================
    // HISTORIAL DE CONSULTAS MÉDICAS
    // CON FECHA Y HORA
    // ==========================================

    @GetMapping("/historial")
    public List<Map<String, Object>> obtenerHistorialConsultas() {

        String sql = """
                SELECT
                    cm.id AS id_consulta,

                    u.nombre_completo AS medico,

                    TO_CHAR(
                        cm.fecha_creacion,
                        'DD/MM/YYYY'
                    ) AS fecha,

                    TO_CHAR(
                        cm.fecha_creacion,
                        'HH12:MI:SS AM'
                    ) AS hora,

                    EXTRACT(
                        ISODOW FROM cm.fecha_creacion
                    )::INTEGER AS numero_dia

                FROM public.consultas_medicas cm

                INNER JOIN public.citas c
                    ON cm.cita_id = c.id

                INNER JOIN public.medicos m
                    ON c.medico_id = m.id

                INNER JOIN public.usuarios u
                    ON m.usuario_id = u.id

                ORDER BY
                    cm.fecha_creacion DESC NULLS LAST,
                    cm.id DESC
                """;

        return jdbcTemplate.queryForList(sql);
    }

    // ==========================================
    // DIAGNÓSTICO DE CONEXIÓN
    // ==========================================

    @GetMapping("/diagnostico")
    public List<Map<String, Object>> diagnosticarConexion() {

        String sql = """
                SELECT
                    current_database() AS base_datos,
                    inet_server_addr() AS servidor,
                    inet_server_port() AS puerto,
                    pg_backend_pid() AS conexion_id,
                    id,
                    fecha_creacion

                FROM public.consultas_medicas

                WHERE id = 5
                """;

        return jdbcTemplate.queryForList(sql);
    }

    // ==========================================
    // VERIFICAR SERVIDOR
    // ==========================================

    @GetMapping("/servidor")
    public List<Map<String, Object>> verificarServidor() {

        String sql = """
                SELECT
                    inet_server_addr()::text AS servidor,
                    inet_server_port() AS puerto,
                    current_database() AS base_datos,
                    pg_postmaster_start_time() AS inicio_servidor
                """;

        return jdbcTemplate.queryForList(sql);
    }

    // ==========================================
    // VERIFICAR DATOS
    // ==========================================

    @GetMapping("/verificar-datos")
    public List<Map<String, Object>> verificarDatos() {

        String sql = """
                SELECT
                    current_database() AS base_datos,
                    current_schema() AS esquema,
                    inet_server_addr()::text AS servidor,
                    inet_server_port() AS puerto,
                    pg_postmaster_start_time() AS inicio_servidor,
                    current_setting('transaction_isolation') AS aislamiento,
                    id,
                    fecha_creacion

                FROM public.consultas_medicas

                WHERE id = 5
                """;

        return jdbcTemplate.queryForList(sql);
    }

    // ==========================================
    // VERIFICAR IDENTIDAD
    // ==========================================

    @GetMapping("/identidad")
    public List<Map<String, Object>> verificarIdentidad() {

        String sql = """
                SELECT
                    current_database() AS base_datos,
                    current_schema() AS esquema,
                    inet_server_addr()::text AS servidor,
                    inet_server_port() AS puerto,
                    pg_postmaster_start_time() AS inicio_servidor,
                    'public.consultas_medicas'::regclass::oid AS tabla_oid,
                    id,
                    fecha_creacion

                FROM public.consultas_medicas

                WHERE id = 5
                """;

        return jdbcTemplate.queryForList(sql);
    }

    // ==========================================
    // VERIFICAR VERSIÓN DEL REGISTRO
    // ==========================================

    @GetMapping("/version-registro")
    public List<Map<String, Object>> verificarVersionRegistro() {

        String sql = """
                SELECT
                    id,
                    fecha_creacion,
                    xmin::text AS version_registro,
                    ctid::text AS ubicacion_registro

                FROM public.consultas_medicas

                WHERE id = 5
                """;

        return jdbcTemplate.queryForList(sql);
    }

    // ==========================================
    // VERIFICAR IDENTIFICADOR DEL SERVIDOR
    // ==========================================

    @GetMapping("/identificador-servidor")
    public List<Map<String, Object>> verificarIdentificadorServidor() {

        String sql = """
                SELECT
                    system_identifier

                FROM pg_control_system()
                """;

        return jdbcTemplate.queryForList(sql);
    }
}