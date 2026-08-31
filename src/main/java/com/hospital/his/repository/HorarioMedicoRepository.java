package com.hospital.his.repository;

import com.hospital.his.entity.HorarioMedico;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface HorarioMedicoRepository
        extends JpaRepository<HorarioMedico, Long> {

    List<HorarioMedico>
    findByMedicoIdAndDisponibleTrueOrderByFechaAscHoraAsc(
            Long medicoId
    );

    List<HorarioMedico>
    findByMedicoIdAndFechaAndDisponibleTrueOrderByHoraAsc(
            Long medicoId,
            LocalDate fecha
    );
}