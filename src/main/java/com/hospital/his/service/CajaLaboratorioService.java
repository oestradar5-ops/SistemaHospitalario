package com.hospital.his.service;

import com.hospital.his.entity.OrdenLaboratorio;
import com.hospital.his.entity.Pago;
import com.hospital.his.repository.OrdenLaboratorioRepository;
import com.hospital.his.repository.PagoRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Locale;

@Service
public class CajaLaboratorioService {

    private final OrdenLaboratorioRepository ordenRepository;
    private final PagoRepository pagoRepository;

    public CajaLaboratorioService(
            OrdenLaboratorioRepository ordenRepository,
            PagoRepository pagoRepository) {

        this.ordenRepository = ordenRepository;
        this.pagoRepository = pagoRepository;
    }

    // ==========================================
    // CU-10: REGISTRAR PAGO DE LABORATORIO
    // ==========================================

    @Transactional
    public Pago registrarPagoLaboratorio(
            Long ordenId,
            String metodoPago,
            BigDecimal montoRecibido,
            String referencia) {

        // ======================================
        // 1. VALIDAR NÚMERO DE ORDEN
        // ======================================

        if (ordenId == null || ordenId <= 0) {
            throw new IllegalArgumentException(
                    "Debe indicar una orden de laboratorio válida."
            );
        }

        // ======================================
        // 2. BUSCAR ORDEN
        // ======================================

        OrdenLaboratorio orden = ordenRepository.findById(ordenId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "No existe la orden de laboratorio: " + ordenId
                        )
                );

        // ======================================
        // 3. VALIDAR ESTADO
        // ======================================

        if (!"PENDIENTE".equalsIgnoreCase(orden.getEstado())) {

            throw new IllegalStateException(
                    "Solo se pueden cobrar órdenes de laboratorio pendientes."
            );
        }

        // ======================================
        // 4. EVITAR PAGOS DUPLICADOS
        // ======================================

        if (orden.getFechaPago() != null) {

            throw new IllegalStateException(
                    "Esta orden de laboratorio ya tiene un pago confirmado."
            );
        }

        if (pagoRepository.existsByOrdenLaboratorioId(ordenId)) {

            throw new IllegalStateException(
                    "Ya existe un registro de pago para esta orden."
            );
        }

        // ======================================
        // 5. VALIDAR MONTO
        // ======================================

        BigDecimal monto = orden.getMontoTotal();

        if (monto == null ||
                monto.compareTo(BigDecimal.ZERO) <= 0) {

            throw new IllegalStateException(
                    "La orden no tiene un monto válido para cobrar."
            );
        }

        // ======================================
        // 6. VALIDAR MÉTODO DE PAGO
        // ======================================

        if (metodoPago == null || metodoPago.isBlank()) {

            throw new IllegalArgumentException(
                    "Debe seleccionar un método de pago."
            );
        }

        String metodo = metodoPago.trim()
                .toUpperCase(Locale.ROOT);

        boolean esEfectivo = metodo.equals("EFECTIVO");

        boolean esTarjeta =
                metodo.equals("VISA") ||
                        metodo.equals("MASTERCARD") ||
                        metodo.equals("DEBITO");

        if (!esEfectivo && !esTarjeta) {

            throw new IllegalArgumentException(
                    "Método de pago no permitido."
            );
        }

        // ======================================
        // 7. VALIDAR EFECTIVO O TARJETA
        // ======================================

        BigDecimal recibido;
        BigDecimal cambio;
        String referenciaFinal = null;

        if (esEfectivo) {

            if (montoRecibido == null ||
                    montoRecibido.compareTo(monto) < 0) {

                throw new IllegalArgumentException(
                        "El monto recibido es insuficiente."
                );
            }

            recibido = montoRecibido;
            cambio = recibido.subtract(monto);

        } else {

            // Formato temporal:
            // 1234|AUT-123456
            //
            // 1234 = últimos cuatro dígitos
            // AUT-123456 = referencia de autorización externa

            if (referencia == null || referencia.isBlank()) {

                throw new IllegalArgumentException(
                        "Debe ingresar los últimos cuatro dígitos " +
                                "y la referencia de autorización."
                );
            }

            String[] partes = referencia.trim().split("\\|", -1);

            if (partes.length != 2) {

                throw new IllegalArgumentException(
                        "Los datos de la tarjeta están incompletos."
                );
            }

            String ultimos4 = partes[0].trim();
            String autorizacion = partes[1].trim();

            if (!ultimos4.matches("[0-9]{4}")) {

                throw new IllegalArgumentException(
                        "Debe ingresar exactamente los últimos " +
                                "cuatro dígitos de la tarjeta."
                );
            }

            if (autorizacion.isBlank()) {

                throw new IllegalArgumentException(
                        "Debe ingresar la referencia de autorización " +
                                "de la transacción."
                );
            }

            if (autorizacion.length() > 70) {

                throw new IllegalArgumentException(
                        "La referencia de autorización es demasiado larga."
                );
            }

            recibido = monto;
            cambio = BigDecimal.ZERO;

            // Solo almacenamos los últimos cuatro dígitos
            // y la referencia externa.
            // Nunca almacenamos el número completo de tarjeta.

            referenciaFinal = ultimos4 + "|" + autorizacion;
        }

        // ======================================
        // 8. CREAR REGISTRO DE PAGO
        // ======================================

        LocalDateTime fechaPago = LocalDateTime.now();

        Pago pago = new Pago();

        pago.setCita(null);
        pago.setOrdenLaboratorio(orden);
        pago.setMonto(monto);
        pago.setMetodoPago(metodo);

        // APROBADO representa el registro de una
        // transacción que el cajero confirma como autorizada.
        // No representa una autorización bancaria automática.
        pago.setEstado("APROBADO");

        pago.setReferencia(referenciaFinal);

        pago.setMontoRecibido(recibido);
        pago.setCambio(cambio);
        pago.setFechaPago(fechaPago);

        // ======================================
        // 9. GUARDAR PAGO
        // ======================================

        Pago pagoGuardado = pagoRepository.saveAndFlush(pago);

        // ======================================
        // 10. ACTUALIZAR ORDEN
        // ======================================

        orden.setFechaPago(fechaPago);
        orden.setEstado("EN_PROCESO");

        ordenRepository.save(orden);

        // ======================================
        // 11. RETORNAR RESULTADO
        // ======================================

        return pagoGuardado;
    }
}