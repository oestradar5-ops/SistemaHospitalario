package com.hospital.his.controller;

import com.hospital.his.dto.VerificacionDpiResponse;
import com.hospital.his.service.UsuarioService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
public class PortalController {

    private final UsuarioService usuarioService;

    public PortalController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping({"/", "/portal"})
    public String mostrarPortal() {
        return "portal/index";
    }

    @ResponseBody
    @GetMapping("/api/verificar-dpi/{dpi}")
    public VerificacionDpiResponse verificarDpi(@PathVariable String dpi) {
        return usuarioService.verificarDpi(dpi);
    }
}