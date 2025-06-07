// InventarioGeneralRepository.java
package com.ns.iestpffaaarchives.domain.repository.inventory;

import com.ns.iestpffaaarchives.domain.entity.inventory.InventarioGeneral;
import com.ns.iestpffaaarchives.domain.entity.inventory.InventarioGeneral.EstadoDocumento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.LocalDate;
import java.util.List;

public interface InventarioGeneralRepository extends JpaRepository<InventarioGeneral, Long> {
    // Métodos de búsqueda por los nuevos campos
    List<InventarioGeneral> findByUnidadAdministrativa(String unidadAdministrativa);
    List<InventarioGeneral> findBySeccion(String seccion);
    List<InventarioGeneral> findByFechaTransferencia(LocalDate fechaTransferencia);
    List<InventarioGeneral> findByNumeroAnioRemision(String numeroAnioRemision);
    List<InventarioGeneral> findByEstado(EstadoDocumento estado);
    
    // Búsqueda paginada
    Page<InventarioGeneral> findByEstado(EstadoDocumento estado, Pageable pageable);
    Page<InventarioGeneral> findByUnidadAdministrativa(String unidadAdministrativa, Pageable pageable);
    
    // Búsquedas combinadas
    List<InventarioGeneral> findByUnidadAdministrativaAndSeccion(String unidadAdministrativa, String seccion);
    List<InventarioGeneral> findByFechaTransferenciaBetween(LocalDate fechaInicio, LocalDate fechaFin);
}

