// RegistroTransferenciaRepository.java
package com.ns.iestpffaaarchives.domain.repository.inventory;

import com.ns.iestpffaaarchives.domain.entity.inventory.RegistroTransferencia;
import com.ns.iestpffaaarchives.domain.entity.inventory.InventarioGeneral.EstadoDocumento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.LocalDate;
import java.util.List;

public interface RegistroTransferenciaRepository extends JpaRepository<RegistroTransferencia, Long> {
    // Métodos de búsqueda por los nuevos campos
    List<RegistroTransferencia> findByNombreEntidad(String nombreEntidad);
    List<RegistroTransferencia> findByUnidadOrganizacion(String unidadOrganizacion);
    List<RegistroTransferencia> findBySeccion(String seccion);
    List<RegistroTransferencia> findBySerieDocumental(String serieDocumental);
    List<RegistroTransferencia> findBySoporte(String soporte);
    List<RegistroTransferencia> findByNumeroAnioRemision(String numeroAnioRemision);
    List<RegistroTransferencia> findByFechaTransferencia(LocalDate fechaTransferencia);
    List<RegistroTransferencia> findByEstado(EstadoDocumento estado);
    
    // Búsqueda paginada
    Page<RegistroTransferencia> findByEstado(EstadoDocumento estado, Pageable pageable);
    Page<RegistroTransferencia> findByUnidadOrganizacion(String unidadOrganizacion, Pageable pageable);
    
    // Búsquedas combinadas
    List<RegistroTransferencia> findByUnidadOrganizacionAndSeccion(String unidadOrganizacion, String seccion);
    List<RegistroTransferencia> findByFechaTransferenciaBetween(LocalDate fechaInicio, LocalDate fechaFin);
    List<RegistroTransferencia> findByNombreEntidadAndNumeroAnioRemision(String nombreEntidad, String numeroAnioRemision);
}
