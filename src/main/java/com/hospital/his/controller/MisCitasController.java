package com.hospital.his.controller;

import com.hospital.his.entity.Cita;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class MisCitasController {

    @PersistenceContext
    private EntityManager entityManager;

    // ==========================================
    // MOSTRAR CITAS DEL PACIENTE
    // ==========================================

    @GetMapping("/paciente/mis-citas")
    @Transactional(readOnly = true)
    public String mostrarMisCitas(
            HttpSession session,
            Model model) {

        String usuario =
                (String) session.getAttribute("usuario");

        String rol =
                (String) session.getAttribute("rol");

        // ==========================================
        // VALIDAR SESIÓN
        // ==========================================

        if (usuario == null ||
                !"PACIENTE".equalsIgnoreCase(rol)) {

            return "redirect:/login";
        }

        // ==========================================
        // CONSULTAR CITAS DEL PACIENTE
        // ==========================================

        List<Cita> citas = entityManager.createQuery(
                        """
                        SELECT c
                        FROM Cita c
                        JOIN FETCH c.paciente p
                        JOIN FETCH c.medico m
                        JOIN FETCH m.usuario
                        JOIN FETCH c.sucursal
                        JOIN FETCH c.especialidad
                        JOIN FETCH c.horario
                        WHERE LOWER(p.nombreUsuario) = LOWER(:usuario)
                        ORDER BY c.fechaCreacion DESC
                        """,
                        Cita.class
                )
                .setParameter("usuario", usuario)
                .getResultList();

        // ==========================================
        // ENVIAR DATOS A LA VISTA
        // ==========================================

        model.addAttribute("citas", citas);
        model.addAttribute("usuario", usuario);

        return "paciente/mis-citas";
    }
}