// CatalogoTransferenciaRepository.java
package com.ns.iestpffaaarchives.domain.repository.inventory;

import com.ns.iestpffaaarchives.domain.entity.inventory.CatalogoTransferencia;
import com.ns.iestpffaaarchives.domain.entity.inventory.InventarioGeneral.EstadoDocumento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface CatalogoTransferenciaRepository extends JpaRepository<CatalogoTransferencia, Long> {
    // Métodos de búsqueda por los nuevos campos
    List<CatalogoTransferencia> findByNombreEntidad(String nombreEntidad);
    List<CatalogoTransferencia> findByUnidadOrganizacion(String unidadOrganizacion);
    List<CatalogoTransferencia> findBySeccion(String seccion);
    List<CatalogoTransferencia> findBySerieDocumental(String serieDocumental);
    List<CatalogoTransferencia> findByCodigoReferencia(String codigoReferencia);
    List<CatalogoTransferencia> findBySoporte(String soporte);
    List<CatalogoTransferencia> findByNumeroAnioRemision(String numeroAnioRemision);
    List<CatalogoTransferencia> findByEstado(EstadoDocumento estado);
    // Método eliminado porque el campo 'titulo' ya no existe en la entidad CatalogoTransferencia
    
    // Búsqueda paginada
    Page<CatalogoTransferencia> findByEstado(EstadoDocumento estado, Pageable pageable);
    Page<CatalogoTransferencia> findByUnidadOrganizacion(String unidadOrganizacion, Pageable pageable);
    // Método eliminado porque el campo 'titulo' ya no existe en la entidad CatalogoTransferencia
    
    // Búsquedas combinadas
    List<CatalogoTransferencia> findByUnidadOrganizacionAndSeccion(String unidadOrganizacion, String seccion);
    List<CatalogoTransferencia> findByNombreEntidadAndNumeroAnioRemision(String nombreEntidad, String numeroAnioRemision);
    List<CatalogoTransferencia> findBySerieDocumentalAndSoporte(String serieDocumental, String soporte);
}