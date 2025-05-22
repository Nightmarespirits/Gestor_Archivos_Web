package com.ns.iestpffaaarchives.application.service;

import com.ns.iestpffaaarchives.domain.entity.Document;
import com.ns.iestpffaaarchives.domain.entity.User;
import com.ns.iestpffaaarchives.domain.entity.DocumentVersion;
import com.ns.iestpffaaarchives.domain.repository.DocumentRepository;
import com.ns.iestpffaaarchives.domain.repository.DocumentVersionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
// Imports para seguridad
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.util.StringUtils;
import org.springframework.data.jpa.domain.Specification;
import java.util.stream.Collectors;

@Service
public class DocumentService {

    private static final Logger logger = LoggerFactory.getLogger(DocumentService.class);

    // Constantes para niveles de acceso
    private static final String ACCESS_LEVEL_PUBLICO = "Publico";
    private static final String ACCESS_LEVEL_PRIVADO = "Privado";
    private static final String ACCESS_LEVEL_RESERVADO = "Reservado";
    private static final String ACCESS_LEVEL_SECRETO = "Secreto";

    // Constantes para roles (deben coincidir con la configuración de Spring Security)
    private static final String ROLE_USER = "ROLE_USER";
    private static final String ROLE_ARCHIVIST = "ROLE_ARCHIVIST";
    private static final String ROLE_MANAGER = "ROLE_MANAGER";
    private static final String ROLE_ADMIN = "ROLE_ADMIN";
    private static final String ROLE_SUPERADMIN = "ROLE_SUPERADMIN";

    private final DocumentRepository documentRepository;
    private final DocumentVersionRepository documentVersionRepository;

    @Autowired
    public DocumentService(DocumentRepository documentRepository, DocumentVersionRepository documentVersionRepository) {
        this.documentRepository = documentRepository;
        this.documentVersionRepository = documentVersionRepository;
    }

    /**
     * Obtiene todos los documentos no eliminados, filtrados por el nivel de acceso
     * permitido para el usuario autenticado actualmente.
     * @return Lista de documentos a los que el usuario tiene acceso.
     */
    @Transactional(readOnly = true)
    public List<Document> getAllDocuments() {
        // Obtener niveles de acceso permitidos para el usuario actual
        Set<String> permissibleAccessLevels = getPermissibleAccessLevels();
        
        if (permissibleAccessLevels.isEmpty()) {
            logger.warn("[getAllDocuments] El usuario no tiene niveles de acceso definidos para ver documentos.");
            return List.of(); // Devuelve lista vacía si no hay niveles permitidos
        }
        
        logger.info("[getAllDocuments] Obteniendo documentos para los niveles de acceso: {}", permissibleAccessLevels);
        // Usar el nuevo método de repositorio que filtra por niveles de acceso
        return documentRepository.findByIsDeletedFalseAndSecurityAccessLevelIn(permissibleAccessLevels);
    }

    /**
     * Método original pero con advertencia de que no filtra por seguridad.
     * Se mantiene para compatibilidad con código existente.
     * @deprecated Use getAllDocuments() para obtener documentos con filtrado de seguridad.
     */
    @Transactional(readOnly = true)
    public List<Document> getAllDocumentsNoSecurityFilter() {
        logger.warn("[getAllDocumentsNoSecurityFilter] PRECAUCIÓN: Este método no aplica filtros de seguridad por nivel de acceso.");
        return documentRepository.findByIsDeletedFalse();
    }

    /**
     * Obtiene un documento por su ID, solo si el usuario autenticado tiene permiso
     * para ver el nivel de acceso de dicho documento.
     */
    @Transactional(readOnly = true)
    public Optional<Document> getDocumentById(Long id) {
        Optional<Document> documentOptional = documentRepository.findById(id);
        
        if (documentOptional.isEmpty()) {
            return Optional.empty();
        }
        
        Document document = documentOptional.get();
        Set<String> permissibleAccessLevels = getPermissibleAccessLevels();
        
        // Verificar si el nivel de acceso del documento está entre los permitidos para el usuario
        if (document.getSecurity() != null && 
            document.getSecurity().getAccessLevel() != null && 
            permissibleAccessLevels.contains(document.getSecurity().getAccessLevel())) {
            logger.info("[getDocumentById] Usuario tiene acceso al documento id={} con nivel {}", id, document.getSecurity().getAccessLevel());
            return documentOptional;
        } else {
            logger.warn("[getDocumentById] Usuario NO tiene acceso al documento id={} con nivel {}. Niveles permitidos: {}", 
                      id, (document.getSecurity() != null ? document.getSecurity().getAccessLevel() : "N/A"), permissibleAccessLevels);
            return Optional.empty(); // Simula que no se encontró para este usuario
        }
    }

    /**
     * Método original pero con advertencia de que no filtra por seguridad.
     * Se mantiene para compatibilidad con código existente.
     * @deprecated Use getDocumentById(Long id) para obtener documentos con filtrado de seguridad.
     */
    @Transactional(readOnly = true)
    public Optional<Document> getDocumentByIdNoSecurityFilter(Long id) {
        logger.warn("[getDocumentByIdNoSecurityFilter] PRECAUCIÓN: Este método no aplica filtros de seguridad por nivel de acceso para el documento id={}", id);
        return documentRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Document> getDocumentsByAuthor(User author) {
        // Obtener documentos por autor
        List<Document> documents = documentRepository.findByAuthor(author);
        // Aplicar filtro de seguridad post-consulta
        return filterDocumentsByAccessLevel(documents);
    }

    @Transactional(readOnly = true)
    public List<Document> searchDocumentsByTitle(String title) {
        // Obtener documentos por título
        List<Document> documents = documentRepository.findByTitleContainingIgnoreCase(title);
        // Aplicar filtro de seguridad post-consulta
        return filterDocumentsByAccessLevel(documents);
    }

    @Transactional(readOnly = true)
    public List<Document> getDocumentsByTag(String tagName) {
        // Obtener documentos por etiqueta
        List<Document> documents = documentRepository.findByTagName(tagName);
        // Aplicar filtro de seguridad post-consulta
        return filterDocumentsByAccessLevel(documents);
    }

    /**
     * Método auxiliar para filtrar una lista de documentos según el nivel de acceso del usuario.
     * Se usa para filtrar los resultados post-consulta en métodos que no pueden usar JPQL directo.
     */
    private List<Document> filterDocumentsByAccessLevel(List<Document> documents) {
        Set<String> permissibleAccessLevels = getPermissibleAccessLevels();
        
        if (permissibleAccessLevels.isEmpty()) {
            logger.warn("[filterDocumentsByAccessLevel] El usuario no tiene niveles de acceso definidos para ver documentos.");
            return List.of();
        }
        
        return documents.stream()
                .filter(doc -> doc.getSecurity() != null && 
                               doc.getSecurity().getAccessLevel() != null && 
                               permissibleAccessLevels.contains(doc.getSecurity().getAccessLevel()))
                .collect(Collectors.toList());
    }

    @Transactional
    public Document createDocument(Document document) {
        // Asegurar que el documento tenga configuración de seguridad
        ensureDocumentHasSecurity(document);
        
        document.setUploadDate(LocalDateTime.now());
        document.setIsDeleted(false);
        document.setVersionNumber(1);
        return documentRepository.save(document);
    }

    /**
     * Asegura que un documento tenga configuración de seguridad con un nivel de acceso válido.
     * Si no tiene, se crea con nivel por defecto "Privado".
     */
    private void ensureDocumentHasSecurity(Document document) {
        if (document.getSecurity() == null) {
            // Si no tiene objeto de seguridad, crear uno nuevo con nivel por defecto
            com.ns.iestpffaaarchives.domain.entity.DocumentSecurity security = new com.ns.iestpffaaarchives.domain.entity.DocumentSecurity();
            security.setAccessLevel(ACCESS_LEVEL_PRIVADO); // Por defecto "Privado"
            security.setDocument(document);
            document.setSecurity(security);
            logger.info("[ensureDocumentHasSecurity] Creada configuración de seguridad con nivel por defecto '{}'", ACCESS_LEVEL_PRIVADO);
        } else if (document.getSecurity().getAccessLevel() == null || document.getSecurity().getAccessLevel().isEmpty()) {
            // Si tiene objeto de seguridad pero sin nivel, establecer nivel por defecto
            document.getSecurity().setAccessLevel(ACCESS_LEVEL_PRIVADO);
            logger.info("[ensureDocumentHasSecurity] Establecido nivel de acceso por defecto '{}'", ACCESS_LEVEL_PRIVADO);
        }
    }

    @Transactional
    public Document updateDocument(Document document) {
        Document existingDocument = documentRepository.findById(document.getId())
            .orElseThrow(() -> new RuntimeException("Document not found"));

        if (document.getTitle() != null) {
            existingDocument.setTitle(document.getTitle());
        }
        if (document.getDescription() != null) {
            existingDocument.setDescription(document.getDescription());
        }
        if (document.getType() != null) {
            existingDocument.setType(document.getType());
        }
        if (document.getTags() != null) {
            existingDocument.setTags(document.getTags());
        }
        if (document.getFilePath() != null) {
            existingDocument.setFilePath(document.getFilePath());
        }
        if (document.getFormat() != null) {
            existingDocument.setFormat(document.getFormat());
        }
        if (document.getVersionNumber() > 0) {
            existingDocument.setVersionNumber(document.getVersionNumber());
        }
        
        // Actualizar nivel de acceso si se proporciona
        if (document.getSecurity() != null && document.getSecurity().getAccessLevel() != null) {
            if (existingDocument.getSecurity() == null) {
                // Si no tiene objeto de seguridad, crear uno nuevo
                com.ns.iestpffaaarchives.domain.entity.DocumentSecurity security = new com.ns.iestpffaaarchives.domain.entity.DocumentSecurity();
                security.setAccessLevel(document.getSecurity().getAccessLevel());
                security.setDocument(existingDocument);
                existingDocument.setSecurity(security);
            } else {
                // Si ya tiene objeto de seguridad, actualizar nivel
                existingDocument.getSecurity().setAccessLevel(document.getSecurity().getAccessLevel());
            }
            logger.info("[updateDocument] Actualizado nivel de acceso a '{}'", document.getSecurity().getAccessLevel());
        }

        return documentRepository.save(existingDocument);
    }

    @Transactional
    public void softDeleteDocument(Long id) {
        documentRepository.findById(id).ifPresent(document -> {
            document.setIsDeleted(true);
            documentRepository.save(document);
        });
    }

    @Transactional
    public void hardDeleteDocument(Long id) {
        documentRepository.deleteById(id);
    }

    @Transactional
    public DocumentVersion saveDocumentVersion(DocumentVersion version) {
        return documentVersionRepository.save(version);
    }

    /**
     * Actualiza un documento con versionado. Si hay archivo nuevo, crea una versión antigua antes de actualizar.
     * Devuelve el documento actualizado o lanza excepción si algo falla.
     */
    @Transactional
    public Document updateDocumentWithVersioning(Document updatedData, MultipartFile file, String uploadDir) {
        logger.info("[updateDocumentWithVersioning] Iniciando actualización para documento id={}", updatedData.getId());
        Document existingDocument = documentRepository.findById(updatedData.getId())
            .orElseThrow(() -> new RuntimeException("Documento no encontrado: id=" + updatedData.getId()));

        // Guardar versión antigua si hay archivo nuevo
        if (file != null && !file.isEmpty()) {
            logger.info("[updateDocumentWithVersioning] Archivo nuevo detectado para documento id={}", existingDocument.getId());
            DocumentVersion version = new DocumentVersion();
            version.setDocument(existingDocument);
            version.setVersionNumber(existingDocument.getVersionNumber());
            version.setChanges("Actualización de archivo");
            version.setAuthor(existingDocument.getAuthor());
            version.setFilePath(existingDocument.getFilePath());
            saveDocumentVersion(version);
            logger.info("[updateDocumentWithVersioning] Versión antigua guardada para documento id={}", existingDocument.getId());
        }

        // Actualizar campos básicos
        if (updatedData.getTitle() != null) {
            existingDocument.setTitle(updatedData.getTitle());
        }
        if (updatedData.getDescription() != null) {
            existingDocument.setDescription(updatedData.getDescription());
        }
        if (updatedData.getType() != null) {
            existingDocument.setType(updatedData.getType());
        }
        if (updatedData.getTags() != null) {
            existingDocument.setTags(updatedData.getTags());
        }
        
        // Actualizar nivel de acceso si se proporciona
        if (updatedData.getSecurity() != null && updatedData.getSecurity().getAccessLevel() != null) {
            if (existingDocument.getSecurity() == null) {
                // Si no tiene objeto de seguridad, crear uno nuevo
                com.ns.iestpffaaarchives.domain.entity.DocumentSecurity security = new com.ns.iestpffaaarchives.domain.entity.DocumentSecurity();
                security.setAccessLevel(updatedData.getSecurity().getAccessLevel());
                security.setDocument(existingDocument);
                existingDocument.setSecurity(security);
            } else {
                // Si ya tiene objeto de seguridad, actualizar nivel
                existingDocument.getSecurity().setAccessLevel(updatedData.getSecurity().getAccessLevel());
            }
            logger.info("[updateDocumentWithVersioning] Actualizado nivel de acceso a '{}'", updatedData.getSecurity().getAccessLevel());
        }

        // Si hay archivo nuevo, actualizar filePath, formato y versionNumber
        if (file != null && !file.isEmpty()) {
            try {
                java.nio.file.Path uploadPath = java.nio.file.Paths.get(uploadDir).toAbsolutePath().normalize();
                java.nio.file.Files.createDirectories(uploadPath);
                String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
                String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
                String uniqueFilename = java.util.UUID.randomUUID().toString() + fileExtension;
                java.nio.file.Path filePath = uploadPath.resolve(uniqueFilename);
                java.nio.file.Files.copy(file.getInputStream(), filePath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                existingDocument.setFilePath(uniqueFilename);
                existingDocument.setFormat(file.getContentType());
                existingDocument.setVersionNumber(existingDocument.getVersionNumber() + 1);
                logger.info("[updateDocumentWithVersioning] Archivo actualizado y campos filePath, format, versionNumber actualizados para documento id={}", existingDocument.getId());
            } catch (Exception e) {
                logger.error("[updateDocumentWithVersioning] Error al guardar archivo actualizado para documento id={}", existingDocument.getId(), e);
                throw new RuntimeException("Error al guardar archivo actualizado", e);
            }
        }

        Document saved = documentRepository.save(existingDocument);
        logger.info("[updateDocumentWithVersioning] Documento actualizado correctamente id={}", saved.getId());
        return saved;
    }

    // --- MÉTODO ADVANCED SEARCH CON SPECIFICATION ---
    @Transactional(readOnly = true)
    public List<Document> advancedSearch(String title, String author, String fromDate, String toDate, Long documentTypeId, List<String> tagNames) {
        java.time.LocalDateTime from = null;
        java.time.LocalDateTime to = null;
        if (fromDate != null && !fromDate.isEmpty()) {
            from = java.time.LocalDate.parse(fromDate).atStartOfDay();
        }
        if (toDate != null && !toDate.isEmpty()) {
            to = java.time.LocalDate.parse(toDate).atTime(23, 59, 59);
        }
        
        // Obtener niveles de acceso permitidos para el usuario actual
        Set<String> permissibleAccessLevels = getPermissibleAccessLevels();
        
        if (permissibleAccessLevels.isEmpty()) {
            logger.warn("[advancedSearch] El usuario no tiene niveles de acceso definidos para buscar documentos.");
            return List.of(); // Devuelve lista vacía si no hay niveles permitidos
        }
        
        logger.info("[advancedSearch] Buscando documentos con niveles de acceso: {}", permissibleAccessLevels);
        
        Specification<com.ns.iestpffaaarchives.domain.entity.Document> spec = DocumentSpecification.advancedSearch(
            (title != null && !title.isEmpty()) ? title : null,
            (author != null && !author.isEmpty()) ? author : null,
            from,
            to,
            documentTypeId,
            (tagNames != null && !tagNames.isEmpty()) ? tagNames : null,
            permissibleAccessLevels // Pasar niveles permitidos
        );
        return documentRepository.findAll(spec);
    }
    
    /**
     * Método de búsqueda avanzada sin filtro de seguridad.
     * Se mantiene para compatibilidad con código existente.
     * @deprecated Use advancedSearch para realizar búsquedas con filtrado de seguridad.
     */
    @Transactional(readOnly = true)
    public List<Document> advancedSearchNoSecurityFilter(String title, String author, String fromDate, String toDate, Long documentTypeId, List<String> tagNames) {
        logger.warn("[advancedSearchNoSecurityFilter] PRECAUCIÓN: Este método no aplica filtros de seguridad por nivel de acceso.");
        java.time.LocalDateTime from = null;
        java.time.LocalDateTime to = null;
        if (fromDate != null && !fromDate.isEmpty()) {
            from = java.time.LocalDate.parse(fromDate).atStartOfDay();
        }
        if (toDate != null && !toDate.isEmpty()) {
            to = java.time.LocalDate.parse(toDate).atTime(23, 59, 59);
        }
        Specification<com.ns.iestpffaaarchives.domain.entity.Document> spec = DocumentSpecification.advancedSearch(
            (title != null && !title.isEmpty()) ? title : null,
            (author != null && !author.isEmpty()) ? author : null,
            from,
            to,
            documentTypeId,
            (tagNames != null && !tagNames.isEmpty()) ? tagNames : null
        );
        return documentRepository.findAll(spec);
    }
    
    /**
     * Determina el conjunto de niveles de acceso a documentos permitidos para el usuario actual,
     * basándose en sus roles/autoridades.
     * @return Un Set de Strings representando los niveles de acceso permitidos (ej. "Privado", "Reservado").
     */
    private Set<String> getPermissibleAccessLevels() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            logger.warn("[getPermissibleAccessLevels] No hay usuario autenticado actualmente.");
            return Set.of(); // Sin usuario autenticado, sin niveles permitidos
        }
        
        return getPermissibleAccessLevels(authentication.getAuthorities());
    }
    
    /**
     * Método auxiliar que mapea roles a niveles de acceso permitidos.
     * @param authorities Colección de GrantedAuthority del usuario.
     * @return Set de niveles de acceso permitidos.
     */
    private Set<String> getPermissibleAccessLevels(Collection<? extends GrantedAuthority> authorities) {
        Set<String> levels = new HashSet<>();
        if (authorities == null || authorities.isEmpty()) {
            logger.warn("[getPermissibleAccessLevels] Usuario sin autoridades/roles definidos.");
            return levels; // Sin autoridades, sin niveles específicos
        }
        
        // Extraer nombres de roles de los objetos GrantedAuthority
        Set<String> userRoles = authorities.stream()
                                           .map(GrantedAuthority::getAuthority)
                                           .collect(Collectors.toSet());
        
        logger.debug("[getPermissibleAccessLevels] Roles del usuario: {}", userRoles);
        
        // Aplicar lógica basada en roles. Los roles superiores generalmente incluyen permisos de roles inferiores.
        // ROLE_SUPERADMIN puede ver todos los niveles incluido los secretos
        if( userRoles.contains(ROLE_SUPERADMIN)) {
            levels.add(ACCESS_LEVEL_PUBLICO);
            levels.add(ACCESS_LEVEL_PRIVADO);
            levels.add(ACCESS_LEVEL_RESERVADO);
            levels.add(ACCESS_LEVEL_SECRETO);
            logger.debug("[getPermissibleAccessLevels] Usuario es SUPERADMIN   , acceso total concedido.");
            return levels;
        }
        // ROLE_ADMIN puede ver todos los niveles excepto los secretos.
        if (userRoles.contains(ROLE_ADMIN)) {
            levels.add(ACCESS_LEVEL_PUBLICO);
            levels.add(ACCESS_LEVEL_PRIVADO);
            levels.add(ACCESS_LEVEL_RESERVADO);
            logger.debug("[getPermissibleAccessLevels] Usuario es ADMIN, acceso parcial concedido.");
            return levels; // Si es admin, tiene todos los permisos.
        }
        
        // Si no es admin, verificar GESTOR.
        if (userRoles.contains(ROLE_MANAGER)) {
            levels.add(ACCESS_LEVEL_PUBLICO);
            levels.add(ACCESS_LEVEL_PRIVADO);
            logger.debug("[getPermissibleAccessLevels] Usuario es GESTOR, acceso parcial concedido.");
        }
        
        // Verificar USER. Asegura que "Privado" se agregue si el usuario tiene ROLE_USER,
        // incluso si también tiene GESTOR (donde ya se habría agregado).
        if (userRoles.contains(ROLE_USER)) {
            levels.add(ACCESS_LEVEL_PRIVADO);
            levels.add(ACCESS_LEVEL_PUBLICO);
            logger.debug("[getPermissibleAccessLevels] Usuario es USER, acceso parcial concedido.");
        }
        
        logger.debug("[getPermissibleAccessLevels] Niveles de acceso determinados: {} para roles de usuario: {}", levels, userRoles);
        return levels;
    }
}
