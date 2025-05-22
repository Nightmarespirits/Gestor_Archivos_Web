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

  // Actions
  async function fetchDocuments() {
    try {
      loading.value = true;
      const data = await api.get('/documents', { debug: true });
      documents.value = data;
    } catch (err) {
      error.value = err.message;
      console.error('fetchDocuments Error:', err);
      throw err;
    } finally {
      loading.value = false;
    }
  }

  async function fetchDocumentById(id) {
    try {
      loading.value = true;
      const data = await api.get(`/documents/${id}`);
      currentDocument.value = data;
      return data;
    } catch (err) {
      error.value = err.message;
      throw err;
    } finally {
      loading.value = false;
    }
  }

  async function createDocument(documentData) {
    try {
      loading.value = true;
      
      // Si documentData ya es un FormData, usarlo directamente
      const formData = documentData instanceof FormData ? documentData : new FormData();
      
      // Si no es FormData, crear uno nuevo (mantener compatibilidad con código existente)
      if (!(documentData instanceof FormData)) {
        Object.keys(documentData).forEach(key => {
          if (key === 'file') {
            formData.append('file', documentData.file);
          } else if (key === 'tags' && Array.isArray(documentData.tags)) {
            formData.append('tags', JSON.stringify(documentData.tags));
          } else if (key === 'security') {
            // Manejar objeto security específicamente
            if (documentData.security && documentData.security.accessLevel) {
              formData.append('security.accessLevel', documentData.security.accessLevel);
            }
          } else {
            formData.append(key, documentData[key]);
          }
        });
      }
      
      // Asegurarse de que siempre haya un nivel de acceso por defecto si no se especificó
      if (!formData.has('security.accessLevel')) {
        formData.append('security.accessLevel', 'Privado');
      }

      const newDocument = await api.post('/documents', formData, {
        isFormData: true
      });
      
      // Registrar la actividad
      await activityLogsStore.createActivityLog(
        'CREATE_DOCUMENT',
        `Documento "${newDocument.title}" creado`,
        newDocument.id
      );

      return newDocument;
    } catch (err) {
      error.value = err.message;
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
            formData.append('tags', JSON.stringify(documentData.tags));
          } else if (key === 'security') {
            // Manejar objeto security específicamente
            if (documentData.security && documentData.security.accessLevel) {
              formData.append('security.accessLevel', documentData.security.accessLevel);
            }
          } else {
            formData.append(key, documentData[key]);
          }
        });
      }
      
      // Asegurarse de que siempre haya un nivel de acceso por defecto si no se especificó
      if (!formData.has('security.accessLevel') && currentDocument.value && currentDocument.value.security) {
        // Mantener el nivel actual si existe
        formData.append('security.accessLevel', currentDocument.value.security.accessLevel);
      } else if (!formData.has('security.accessLevel')) {
        // Nivel por defecto
        formData.append('security.accessLevel', 'Privado');
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
      const document = await fetchDocumentById(id);
      
      await api.delete(`/documents/${id}`);
      
      // Registrar la actividad
      await activityLogsStore.createActivityLog(
        'DELETE_DOCUMENT',
        `Documento "${document.title}" eliminado`,
        id
      );

      await fetchDocuments(); // Actualizar lista
      return true;
    } catch (err) {
      error.value = err.message;
      throw err;
    } finally {
      loading.value = false;
    }
  }

  async function downloadDocument(id) {
    try {
      loading.value = true;
      
      // Primero obtener los datos del documento si no están disponibles
      let documentData = currentDocument.value;
      if (!documentData || documentData.id !== id) {
        await fetchDocumentById(id);
        documentData = currentDocument.value;
      }
      
      // Usar fetch directamente para obtener el blob
      const response = await fetch(`${API_BASE_URL}/documents/download/${id}`, { 
        method: 'GET',
        headers: apiService.getAuthHeaders(false)
      });

      if (!response.ok) throw new Error(`Error ${response.status}: ${response.statusText}`);
      
      // Registrar la actividad de descarga
      await activityLogsStore.createActivityLog(
        'DOWNLOAD_DOCUMENT',
        `Documento "${documentData.title}" descargado`,
        id
      );
      
      // Procesar la descarga del archivo
      const blob = await response.blob();
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = documentData.title || 'documento';
      document.body.appendChild(a);
      a.click();
      a.remove();
      window.URL.revokeObjectURL(url);
      
      return true;
    } catch (err) {
      error.value = err.message;
      throw err;
    } finally {
      loading.value = false;
    }
  }

  // Función para obtener la URL de previsualización del documento
  async function getFilePreview(id) {
    try {
      loading.value = true;

      if (previewCache.has(id)) {
        return previewCache.get(id);
      }

      const response = await fetch(`${API_BASE_URL}/documents/download/${id}`, {
        method: 'GET',
        headers: apiService.getAuthHeaders(false)
      });

      if (!response.ok) throw new Error(`Error ${response.status}: ${response.statusText}`);

      const blob = await response.blob();
      const mimeType = blob.type || response.headers.get('content-type') || 'application/octet-stream';
      const url = URL.createObjectURL(blob);

      const previewData = { url, mimeType };
      previewCache.set(id, previewData);
      return previewData;
    } catch (err) {
      error.value = err.message;
      throw err;
    } finally {
      loading.value = false;
    }
  }

  function revokePreviewUrl(id) {
    const cached = previewCache.get(id);
    if (cached) {
      URL.revokeObjectURL(cached.url);
      previewCache.delete(id);
    }
  }

  async function fetchDocumentTypes() {
    try {
      loading.value = true;
      const response = await api.get('/document-types');
      documentTypes.value = response;
      return response;
    } catch (err) {
      error.value = err.message;
      throw err;
    } finally {
      loading.value = false;
    }
  }

  async function fetchTags() {
    try {
      loading.value = true;
      const response = await api.get('/tags');
      tags.value = response;
      return response;
    } catch (err) {
      error.value = err.message;
      throw err;
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
    previewCache,
    accessLevels,

    // Getters
    getDocuments,
    getCurrentDocument,
    isLoading,
    getError,
    getDocumentTypes,
    getTags,
    getDocumentDownloadUrl,
    getAccessLevels,
    getSecurityLevelColor,

    // Actions
    fetchDocuments,
    fetchDocumentById,
    createDocument,
    updateDocument,
    deleteDocument,
    downloadDocument,
    getFilePreview,
    revokePreviewUrl,
    fetchDocumentTypes,
    fetchTags
  };
});
