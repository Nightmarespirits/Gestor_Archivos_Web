// DetalleCatalogoTransferenciaRepository.java
package com.ns.iestpffaaarchives.domain.repository.inventory;

import com.ns.iestpffaaarchives.domain.entity.inventory.CatalogoTransferencia;
import com.ns.iestpffaaarchives.domain.entity.inventory.DetalleCatalogoTransferencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DetalleCatalogoTransferenciaRepository extends JpaRepository<DetalleCatalogoTransferencia, Long> {
    // Buscar detalles por catálogo
    List<DetalleCatalogoTransferencia> findByCatalogoTransferencia(CatalogoTransferencia catalogoTransferencia);
    
    // Buscar detalles por número de ítem
    List<DetalleCatalogoTransferencia> findByNumeroItem(Integer numeroItem);
    
    // Buscar detalles por número de caja
    List<DetalleCatalogoTransferencia> findByNumeroCaja(String numeroCaja);
    
    // Buscar detalles por número de tomo o paquete
    List<DetalleCatalogoTransferencia> findByNumeroTomoPaquete(String numeroTomoPaquete);
    
    // Buscar detalles por número de unidad documental
    List<DetalleCatalogoTransferencia> findByNumeroUnidadDocumental(String numeroUnidadDocumental);
    
    // Eliminar todos los detalles asociados a un catálogo
    void deleteByCatalogoTransferencia(CatalogoTransferencia catalogoTransferencia);
}
