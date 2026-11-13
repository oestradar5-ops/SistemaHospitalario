package com.hospital.his.service;

import com.hospital.his.dto.MovimientoInventarioRequest;
import com.hospital.his.entity.InventarioMedicamento;
import com.hospital.his.entity.MovimientoInventario;
import com.hospital.his.entity.Usuario;

import com.hospital.his.repository.InventarioMedicamentoRepository;
import com.hospital.his.repository.MovimientoInventarioRepository;
import com.hospital.his.repository.UsuarioRepository;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class MovimientoInventarioService {

    // ==========================================
    // REPOSITORIOS Y SERVICIOS
    // ==========================================

    private final MovimientoInventarioRepository movimientoRepository;

    private final InventarioMedicamentoRepository inventarioRepository;

    private final UsuarioRepository usuarioRepository;

    private final FarmaciaSesionService farmaciaSesionService;

    // ==========================================
    // CONSTRUCTOR
    // ==========================================

    public MovimientoInventarioService(
            MovimientoInventarioRepository movimientoRepository,
            InventarioMedicamentoRepository inventarioRepository,
            UsuarioRepository usuarioRepository,
            FarmaciaSesionService farmaciaSesionService) {

        this.movimientoRepository = movimientoRepository;
        this.inventarioRepository = inventarioRepository;
        this.usuarioRepository = usuarioRepository;
        this.farmaciaSesionService = farmaciaSesionService;
    }

    // ==========================================
    // OBTENER USUARIO AUTENTICADO
    // ==========================================

    private Usuario obtenerUsuarioAutenticado(HttpSession sesion) {

        if (sesion == null) {
            throw new SecurityException(
                    "Debe iniciar sesión."
            );
        }

        Object nombreUsuario = sesion.getAttribute("usuario");
        Object rolSesion = sesion.getAttribute("rol");

        if (!(nombreUsuario instanceof String nombre)
                || nombre.isBlank()
                || !(rolSesion instanceof String rol)) {

            throw new SecurityException(
                    "La sesión no es válida."
            );
        }

        Usuario usuario;

        if ("FARMACIA".equalsIgnoreCase(rol)) {

            usuario = farmaciaSesionService.obtenerUsuario(sesion);

        } else if ("ADMIN".equalsIgnoreCase(rol)
                || "ADMINISTRADOR".equalsIgnoreCase(rol)) {

            usuario = usuarioRepository
                    .findByNombreUsuario(nombre)
                    .orElseThrow(() ->
                            new SecurityException(
                                    "El usuario no existe."
                            )
                    );

            if (!Boolean.TRUE.equals(usuario.getActivo())) {
                throw new SecurityException(
                        "El usuario está inactivo."
                );
            }

            if (!"ADMIN".equalsIgnoreCase(usuario.getRol())
                    && !"ADMINISTRADOR".equalsIgnoreCase(usuario.getRol())) {

                throw new SecurityException(
                        "El usuario no tiene permisos de administrador."
                );
            }

        } else {

            throw new SecurityException(
                    "No tiene permisos para registrar movimientos."
            );
        }

        return usuario;
    }

    // ==========================================
    // REGISTRAR MOVIMIENTO MANUAL
    // ==========================================

    @Transactional
    public MovimientoInventario registrarMovimiento(
            MovimientoInventarioRequest solicitud,
            HttpSession sesion) {

        // ======================================
        // VALIDAR SOLICITUD
        // ======================================

        if (solicitud == null) {
            throw new IllegalArgumentException(
                    "La solicitud no puede estar vacía."
            );
        }

        if (solicitud.getMedicamentoId() == null
                || solicitud.getSucursalId() == null) {

            throw new IllegalArgumentException(
                    "Debe seleccionar un medicamento y una sucursal."
            );
        }

        Integer tipo = solicitud.getTipoMovimiento();
        Integer cantidad = solicitud.getCantidad();

        if (tipo == null || tipo < 0 || tipo > 5) {
            throw new IllegalArgumentException(
                    "El tipo de movimiento no es válido."
            );
        }

        if (cantidad == null || cantidad <= 0) {
            throw new IllegalArgumentException(
                    "La cantidad debe ser mayor que cero."
            );
        }

        if (solicitud.getNumeroReferencia() != null
                && solicitud.getNumeroReferencia().length() > 100) {

            throw new IllegalArgumentException(
                    "El número de referencia no puede superar 100 caracteres."
            );
        }

        if (solicitud.getMotivoNotas() != null
                && solicitud.getMotivoNotas().length() > 500) {

            throw new IllegalArgumentException(
                    "Las notas no pueden superar 500 caracteres."
            );
        }

        BigDecimal costo = solicitud.getCostoUnitario();

        if (costo != null
                && (costo.compareTo(BigDecimal.ZERO) < 0
                || costo.scale() > 2
                || costo.precision() - costo.scale() > 8)) {

            throw new IllegalArgumentException(
                    "El costo unitario no es válido."
            );
        }

        // ======================================
        // IDENTIFICAR RESPONSABLE
        // ======================================

        Usuario usuario = obtenerUsuarioAutenticado(sesion);

        // ======================================
        // VALIDAR SUCURSAL DE FARMACIA
        // ======================================

        if ("FARMACIA".equalsIgnoreCase(usuario.getRol())) {

            Long sucursalPermitida =
                    farmaciaSesionService.obtenerSucursal(sesion).getId();

            if (!sucursalPermitida.equals(solicitud.getSucursalId())) {

                throw new SecurityException(
                        "No puede registrar movimientos en otra sucursal."
                );
            }
        }

        // ======================================
        // BUSCAR Y BLOQUEAR INVENTARIO
        // ======================================

        InventarioMedicamento inventario =
                inventarioRepository.buscarParaDespacho(
                        solicitud.getMedicamentoId(),
                        solicitud.getSucursalId()
                ).orElseThrow(() ->
                        new IllegalArgumentException(
                                "No existe inventario para el medicamento "
                                        + "en la sucursal seleccionada."
                        )
                );

        if (!Boolean.TRUE.equals(inventario.getActivo())) {
            throw new IllegalStateException(
                    "El inventario está inactivo."
            );
        }

        // ======================================
        // CALCULAR EXISTENCIAS
        // ======================================

        int stockAnterior = inventario.getCantidadDisponible();

        boolean esEntrada = tipo == 0 || tipo == 1 || tipo == 4;

        int stockNuevo;

        if (esEntrada) {

            try {
                stockNuevo = Math.addExact(stockAnterior, cantidad);
            } catch (ArithmeticException e) {
                throw new IllegalArgumentException(
                        "La cantidad supera el límite permitido."
                );
            }

        } else {

            if (cantidad > stockAnterior) {
                throw new IllegalArgumentException(
                        "Existencias insuficientes. Disponible: "
                                + stockAnterior
                );
            }

            stockNuevo = stockAnterior - cantidad;
        }

        // ======================================
        // ACTUALIZAR INVENTARIO
        // ======================================

        inventario.setCantidadDisponible(stockNuevo);

        inventarioRepository.save(inventario);

        // ======================================
        // CREAR REGISTRO EN BITÁCORA
        // ======================================

        MovimientoInventario movimiento =
                new MovimientoInventario();

        movimiento.setInventario(inventario);
        movimiento.setTipoMovimiento(tipo);
        movimiento.setCantidad(cantidad);
        movimiento.setStockAnterior(stockAnterior);
        movimiento.setStockNuevo(stockNuevo);

        movimiento.setCostoUnitario(
                costo != null
                        ? costo
                        : inventario.getPrecioUnitario()
        );

        movimiento.setNumeroReferencia(
                solicitud.getNumeroReferencia()
        );

        movimiento.setMotivoNotas(
                solicitud.getMotivoNotas()
        );

        movimiento.setUsuario(usuario);
        movimiento.setActivo(true);

        // ======================================
        // GUARDAR MOVIMIENTO
        // ======================================

        return movimientoRepository.save(movimiento);
    }

    // ==========================================
    // CONSULTAR HISTORIAL COMPLETO
    // ==========================================

    @Transactional(readOnly = true)
    public List<MovimientoInventario> listarMovimientos() {

        return movimientoRepository.findAllConDetalles();
    }

    // ==========================================
    // CONSULTAR MOVIMIENTOS POR INVENTARIO
    // ==========================================

    @Transactional(readOnly = true)
    public List<MovimientoInventario> listarPorInventario(
            Long inventarioId) {

        return movimientoRepository
                .findByInventarioConDetalles(inventarioId);
    }

    // ==========================================
    // CONSULTAR MOVIMIENTOS POR TIPO
    // ==========================================

    @Transactional(readOnly = true)
    public List<MovimientoInventario> listarPorTipo(
            Integer tipoMovimiento) {

        return movimientoRepository
                .findByTipoConDetalles(tipoMovimiento);
    }

    // ==========================================
    // ACTIVAR O DESACTIVAR REGISTRO
    // ==========================================

    @Transactional
    public void cambiarEstado(
            Long movimientoId,
            boolean activo,
            HttpSession sesion) {

        Usuario usuario = obtenerUsuarioAutenticado(sesion);

        if (!"ADMIN".equalsIgnoreCase(usuario.getRol())
                && !"ADMINISTRADOR".equalsIgnoreCase(usuario.getRol())) {

            throw new SecurityException(
                    "Solo un administrador puede cambiar el estado."
            );
        }

        MovimientoInventario movimiento =
                movimientoRepository.findById(movimientoId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "El movimiento no existe."
                                )
                        );

        movimiento.setActivo(activo);

        movimientoRepository.save(movimiento);
    }
}