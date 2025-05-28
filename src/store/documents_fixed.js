import { defineStore } from 'pinia';
import { ref } from 'vue';
import { useActivityLogsStore } from './activityLogs';
import { api, apiService, API_BASE_URL } from '@/services/api';

export const useDocumentsStore = defineStore('documents', () => {
  const documents = ref([]);
  const currentDocument = ref(null);
  const loading = ref(false);
  const error = ref(null);
  const documentTypes = ref([]);
  const tags = ref([]);
  const accessLevels = ref(['Privado', 'Reservado', 'Secreto']);
  const activityLogsStore = useActivityLogsStore();
  const previewCache = new Map();

  // Helper para obtener la URL de descarga de documentos
  const getDocumentDownloadUrl = (id) => {
    return `${API_BASE_URL}/documents/download/${id}`;
  };

  // Getters
  function getDocuments() {
    return documents.value;
  }

  function getCurrentDocument() {
    return currentDocument.value;
  }

  function isLoading() {
    return loading.value;
  }

  function getError() {
    return error.value;
  }

  function getDocumentTypes() {
    return documentTypes.value;
  }

  function getTags() {
    return tags.value;
  }
  
  function getAccessLevels() {
    return accessLevels.value;
  }
  
  function getSecurityLevelColor(level) {
    switch (level) {
      case 'Secreto':
        return 'red';
      case 'Reservado':
        return 'orange';
      case 'Privado':
      default:
        return 'green';
    }
  }

  // Estado para paginación
  const pagination = ref({
    page: 0,
    size: 10,
    totalItems: 0,
    totalPages: 0
  });

  // Opciones de ordenamiento
  const sortOptions = ref({
    sortBy: 'id',
    direction: 'asc'
  });

  // Actions
  async function fetchDocuments() {
    try {
      loading.value = true;
      const data = await api.get('/documents', { debug: true });
      documents.value = data || [];
    } catch (err) {
      error.value = err.message;
      console.error('fetchDocuments Error:', err);
      documents.value = [];
    } finally {
      loading.value = false;
    }
  }

  /**
   * Obtiene documentos con paginación del servidor
   * @param {Object} options - Opciones de paginación y filtrado
   * @returns {Promise} - Datos paginados
   */
  async function fetchPaginatedDocuments(options = {}) {
    try {
      loading.value = true;
      
      const {
        page = pagination.value.page,
        size = pagination.value.size,
        sortBy = sortOptions.value.sortBy,
        direction = sortOptions.value.direction,
        filters = {}
      } = options;
      
      // Actualizar opciones en el store
      pagination.value.page = page;
      pagination.value.size = size;
      sortOptions.value.sortBy = sortBy;
      sortOptions.value.direction = direction;
      
      const response = await api.getPaginated('/documents/paginated', {
        page,
        pageSize: size,
        sortBy,
        sortDirection: direction,
        filters,
        debug: true
      });
      
      // Actualizar el estado con los resultados
      documents.value = response.content || [];
      pagination.value.totalItems = response.totalElements || 0;
      pagination.value.totalPages = response.totalPages || 0;
      
      return {
        items: documents.value,
        pagination: { ...pagination.value }
      };
    } catch (err) {
      error.value = err.message;
      console.error('fetchPaginatedDocuments Error:', err);
      documents.value = [];
      return {
        items: [],
        pagination: { ...pagination.value, totalItems: 0, totalPages: 0 }
      };
    } finally {
      loading.value = false;
    }
  }

  /**
   * Búsqueda avanzada de documentos con paginación
   * @param {Object} searchParams - Parámetros de búsqueda
   * @param {Object} paginationOptions - Opciones de paginación
   * @returns {Promise} - Resultados de búsqueda paginados
   */
  async function searchDocumentsPaginated(searchParams = {}, paginationOptions = {}) {
    try {
      loading.value = true;
      
      const {
        title,
        description,
        author,
        fromDate,
        toDate,
        documentTypeId,
        tags
      } = searchParams;
      
      const {
        page = pagination.value.page,
        size = pagination.value.size,
        sortBy = sortOptions.value.sortBy,
        direction = sortOptions.value.direction
      } = paginationOptions;
      
      // Construir filtros para la búsqueda
      const filters = {};
      if (title) filters.title = title;
      if (description) filters.description = description;
      if (author) filters.author = author;
      if (fromDate) filters.fromDate = fromDate;
      if (toDate) filters.toDate = toDate;
      if (documentTypeId) filters.documentTypeId = documentTypeId;
      if (tags && tags.length > 0) filters.tags = tags;
      
      // Usar la función de paginación con los filtros
      const response = await api.getPaginated('/documents/search', {
        page,
        pageSize: size,
        sortBy,
        sortDirection: direction,
        filters,
        debug: true
      });
      
      // Actualizar el estado con los resultados
      documents.value = response.content || [];
      pagination.value = {
        page,
        size,
        totalItems: response.totalElements || 0,
        totalPages: response.totalPages || 0
      };
      
      return {
        items: documents.value,
        pagination: { ...pagination.value }
      };
    } catch (err) {
      error.value = err.message;
      console.error('searchDocumentsPaginated Error:', err);
      documents.value = [];
      return {
        items: [],
        pagination: { ...pagination.value, totalItems: 0, totalPages: 0 }
      };
    } finally {
      loading.value = false;
    }
  }

  async function fetchDocumentById(id) {
    try {
      loading.value = true;
      const document = await api.get(`/documents/${id}`, { debug: true });
      currentDocument.value = document;
      return document;
    } catch (err) {
      error.value = err.message;
      console.error('fetchDocumentById Error:', err);
      throw err;
    } finally {
      loading.value = false;
    }
  }

  async function createDocument(documentData) {
    try {
      loading.value = true;
      
      // Crear un FormData para enviar los datos multipart
      const formData = new FormData();
      
      // Agregar datos básicos
      formData.append('title', documentData.title);
      formData.append('description', documentData.description || '');
      
      // Agregar tipo de documento
      if (documentData.documentTypeId) {
        formData.append('documentTypeId', documentData.documentTypeId);
      }
      
      // Agregar etiquetas (cada una como un item separado)
      if (documentData.tags && Array.isArray(documentData.tags)) {
        documentData.tags.forEach(tag => {
          formData.append('tags', tag);
        });
      }
      
      // Agregar archivo
      if (documentData.file) {
        formData.append('file', documentData.file);
      }
      
      // Configuración de seguridad
      if (documentData.security) {
        formData.append('security', JSON.stringify(documentData.security));
      }
      
      console.log('Enviando documento a la API...');
      
      const newDocument = await api.post('/documents', formData, {
        isFormData: true,
        debug: true,
      });
      
      console.log('Documento creado:', newDocument);
      
      // Registrar la actividad
      await activityLogsStore.createActivityLog(
        'CREATE_DOCUMENT',
        `Documento "${newDocument.title}" creado`,
        newDocument.id
      );
      
      return newDocument;
    } catch (err) {
      error.value = err.message;
      console.error('createDocument Error:', err);
      throw err;
    } finally {
      loading.value = false;
    }
  }

  async function updateDocument(id, documentData) {
    try {
      loading.value = true;
      
      // Si documentData ya es un FormData, usarlo directamente
      const formData = documentData instanceof FormData ? documentData : new FormData();
      
      // Si no es FormData, crear uno nuevo (mantener compatibilidad con código existente)
      if (!(documentData instanceof FormData)) {
        Object.keys(documentData).forEach(key => {
          if (key === 'file') {
            if (documentData.file) {
              formData.append('file', documentData.file);
            }
          } else if (key === 'tags' && Array.isArray(documentData.tags)) {
            // CORRECCIÓN: Agregar cada tag como un elemento independiente en la lista
            // en lugar de JSON.stringify que causa problemas con el controlador
            documentData.tags.forEach(tag => {
              formData.append('tags', tag);
            });
          } else if (key === 'security') {
            // CORRECCIÓN: Manejar objeto security como JSON string completo
            // para que el controlador lo reciba correctamente
            if (documentData.security && documentData.security.accessLevel) {
              const securityJson = JSON.stringify(documentData.security);
              formData.append('security', securityJson);
            }
          } else {
            formData.append(key, documentData[key]);
          }
        });
      }
      
      // Ya no necesitamos esto porque ahora enviamos 'security' como JSON
      // Eliminar este bloque que intentaba manejar 'security.accessLevel' por separado
      /* 
      if (!formData.has('security.accessLevel') && currentDocument.value && currentDocument.value.security) {
        formData.append('security.accessLevel', currentDocument.value.security.accessLevel);
      } else if (!formData.has('security.accessLevel')) {
        formData.append('security.accessLevel', 'Privado');
      }
      */

      // Asegurarse de que siempre haya un nivel de acceso por defecto si no se especificó
      if (!formData.has('security') && currentDocument.value && currentDocument.value.security) {
        // Mantener el nivel actual si existe
        formData.append('security', JSON.stringify(currentDocument.value.security));
      }

      const updatedDocument = await api.put(`/documents/${id}`, formData, {
        isFormData: true,
        debug: true
      });
      
      // Registrar la actividad
      await activityLogsStore.createActivityLog(
        'UPDATE_DOCUMENT',
        `Documento "${updatedDocument.title}" actualizado`,
        updatedDocument.id
      );
      return updatedDocument;
    } catch (err) {
      error.value = err.message;
      console.error('[updateDocument] Error:', err);
      throw err;
    } finally {
      loading.value = false;
    }
  }

  async function deleteDocument(id) {
    try {
      loading.value = true;
      const document = currentDocument.value;
      
      await api.delete(`/documents/${id}`, { debug: true });
      
      // Registrar la actividad
      await activityLogsStore.createActivityLog(
        'DELETE_DOCUMENT',
        `Documento "${document?.title || id}" eliminado`,
        id
      );
      
      // Eliminar el documento del array local
      documents.value = documents.value.filter(doc => doc.id !== id);
      if (currentDocument.value && currentDocument.value.id === id) {
        currentDocument.value = null;
      }
      
      return true;
    } catch (err) {
      error.value = err.message;
      console.error('deleteDocument Error:', err);
      throw err;
    } finally {
      loading.value = false;
    }
  }

  async function downloadDocument(id) {
    try {
      loading.value = true;
      
      // Obtener enlace para descargar el archivo
      const downloadUrl = getDocumentDownloadUrl(id);
      
      // Intentar descargar con la API de apiService para manejar auth
      try {
        const response = await apiService.get(downloadUrl, {
          responseType: 'blob',
          debug: true
        });
        
        // Crear un blob y un URL para descargarlo
        const blob = new Blob([response.data], { 
          type: response.headers['content-type'] || 'application/octet-stream' 
        });
        const url = window.URL.createObjectURL(blob);
        
        // Obtener nombre de archivo del header o usar ID
        let filename = 'document.pdf';
        const contentDisposition = response.headers['content-disposition'];
        if (contentDisposition) {
          const filenameMatch = contentDisposition.match(/filename="?([^"]*)"?/);
          if (filenameMatch && filenameMatch[1]) {
            filename = filenameMatch[1];
          }
        }
        
        // Crear enlace de descarga y simular clic
        const a = document.createElement('a');
        a.href = url;
        a.download = filename;
        document.body.appendChild(a);
        a.click();
        window.URL.revokeObjectURL(url);
        document.body.removeChild(a);
        
        // Registrar la actividad
        await activityLogsStore.createActivityLog(
          'DOWNLOAD_DOCUMENT',
          `Documento ID=${id} descargado`,
          id
        );
        
        return true;
      } catch (err) {
        console.error('Error al descargar usando API:', err);
        // Falló la descarga con API, intentar con enlace directo
        window.open(downloadUrl, '_blank');
        return true;
      }
    } catch (err) {
      error.value = err.message;
      console.error('downloadDocument Error:', err);
      throw err;
    } finally {
      loading.value = false;
    }
  }

  // Función para obtener la URL de previsualización del documento
  async function getFilePreview(id) {
    try {
      // Verificar si ya tenemos una URL en caché
      if (previewCache.has(id)) {
        return previewCache.get(id);
      }
      
      loading.value = true;
      
      const previewUrl = `${API_BASE_URL}/documents/preview/${id}`;
      
      try {
        // Obtener el archivo como blob
        const response = await apiService.get(previewUrl, {
          responseType: 'blob',
          debug: true
        });
        
        // Crear un blob URL para mostrar en el componente
        const blob = new Blob([response.data], { 
          type: response.headers['content-type'] || 'application/pdf' 
        });
        const url = window.URL.createObjectURL(blob);
        
        // Guardar en caché
        previewCache.set(id, url);
        
        return url;
      } catch (err) {
        console.error('Error al obtener vista previa:', err);
        return null;
      }
    } catch (err) {
      console.error('getFilePreview Error:', err);
      return null;
    } finally {
      loading.value = false;
    }
  }

  // Liberar recursos de URL cuando ya no se necesiten
  function revokePreviewUrl(id) {
    if (previewCache.has(id)) {
      const url = previewCache.get(id);
      window.URL.revokeObjectURL(url);
      previewCache.delete(id);
    }
  }

  async function fetchDocumentTypes() {
    try {
      loading.value = true;
      const data = await api.get('/documents/types', { debug: true });
      documentTypes.value = data || [];
      return documentTypes.value;
    } catch (err) {
      error.value = err.message;
      console.error('fetchDocumentTypes Error:', err);
      documentTypes.value = [];
      return [];
    } finally {
      loading.value = false;
    }
  }

  async function fetchTags() {
    try {
      loading.value = true;
      const data = await api.get('/documents/tags', { debug: true });
      tags.value = data || [];
      return tags.value;
    } catch (err) {
      error.value = err.message;
      console.error('fetchTags Error:', err);
      tags.value = [];
      return [];
    } finally {
      loading.value = false;
    }
  }

  return {
    // State
    documents,
    currentDocument,
    loading,
    error,
    documentTypes,
    tags,
    accessLevels,
    pagination,
    sortOptions,
    
    // Getters
    getDocuments,
    getCurrentDocument,
    isLoading,
    getError,
    getDocumentTypes,
    getTags,
    getAccessLevels,
    getSecurityLevelColor,
    
    // Actions
    fetchDocuments,
    fetchPaginatedDocuments,
    searchDocumentsPaginated,
    fetchDocumentById,
    createDocument,
    updateDocument,
    deleteDocument,
    downloadDocument,
    getDocumentDownloadUrl,
    getFilePreview,
    revokePreviewUrl,
    fetchDocumentTypes,
    fetchTags
  };
});
