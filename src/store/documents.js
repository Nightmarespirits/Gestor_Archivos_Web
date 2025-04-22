import { defineStore } from 'pinia';
import { ref } from 'vue';
import { useActivityLogsStore } from './activityLogs';
import { fetchApi } from '@/utils/apiDebug';

const API_BASE_URL = import.meta.env.VITE_BASE_URL;

export const useDocumentsStore = defineStore('documents', () => {
  const documents = ref([]);
  const currentDocument = ref(null);
  const loading = ref(false);
  const error = ref(null);
  const documentTypes = ref([]);
  const tags = ref([]);
  const activityLogsStore = useActivityLogsStore();

  // Helpers
  const getAuthHeaders = (includeContentType = true) => {
    const headers = {
      'Authorization': `Bearer ${localStorage.getItem('authToken')}`
    };
    
    if (includeContentType) {
      headers['Content-Type'] = 'application/json';
    }
    
    return headers;
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

  // Actions
  async function fetchDocuments() {
    try {
      loading.value = true;
      const response = await fetch(`${API_BASE_URL}/documents`, {
        headers: getAuthHeaders()
      });

      if (!response.ok) throw new Error(`Error ${response.status}: ${response.statusText}`);
      
      const data = await response.json();
      documents.value = data;
    } catch (err) {
      error.value = err.message;
      throw err;
    } finally {
      loading.value = false;
    }
  }

  async function fetchDocumentById(id) {
    try {
      loading.value = true;
      const response = await fetch(`${API_BASE_URL}/documents/${id}`, {
        headers: getAuthHeaders()
      });

      if (!response.ok) throw new Error(`Error ${response.status}: ${response.statusText}`);
      
      const data = await response.json();
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
          } else {
            formData.append(key, documentData[key]);
          }
        });
      }

      const response = await fetch(`${API_BASE_URL}/documents`, {
        method: 'POST',
        headers: getAuthHeaders(false),
        body: formData
      });

      if (!response.ok) throw new Error(`Error ${response.status}: ${response.statusText}`);
      
      const newDocument = await response.json();
      
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
      const formData = new FormData();
      // Log de los datos originales
      console.log('[updateDocument] Datos originales:', documentData);
      Object.keys(documentData).forEach(key => {
        if (key === 'file' && documentData.file) {
          formData.append('file', documentData.file);
        } else if (key === 'tags' && Array.isArray(documentData.tags)) {
          documentData.tags.forEach(tag => {
            formData.append('tags', typeof tag === 'object' ? tag.name : tag);
          });
        } else if ((key === 'type' || key === 'documentTypeId') && documentData[key]) {
          // Mapear correctamente el ID de tipo de documento
          let docTypeId = documentData[key];
          if (typeof docTypeId === 'object' && docTypeId.id) {
            docTypeId = docTypeId.id;
          } else if (typeof docTypeId === 'string') {
            const docType = documentTypes.value.find(dt => dt.name === docTypeId || dt.id == docTypeId);
            if (docType) docTypeId = docType.id;
          }
          formData.append('documentTypeId', docTypeId);
        } else if (documentData[key] !== undefined) {
          formData.append(key, documentData[key]);
        }
      });
      // Log de los datos enviados
      for (let [k, v] of formData.entries()) {
        console.log(`[updateDocument] FormData: ${k} =`, v);
      }
      const response = await fetch(`${API_BASE_URL}/documents/${id}`, {
        method: 'PUT',
        headers: getAuthHeaders(false),
        body: formData
      });
      console.log('[updateDocument] Respuesta HTTP:', response);
      if (!response.ok) throw new Error(`Error ${response.status}: ${response.statusText}`);
      const updatedDocument = await response.json();
      console.log('[updateDocument] Documento actualizado recibido:', updatedDocument);
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
      
      const response = await fetch(`${API_BASE_URL}/documents/${id}`, {
        method: 'DELETE',
        headers: getAuthHeaders()
      });

      if (!response.ok) throw new Error(`Error ${response.status}: ${response.statusText}`);
      
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
      const document = await fetchDocumentById(id);
      
      const response = await fetch(`${API_BASE_URL}/documents/${id}/download`, {
        headers: getAuthHeaders()
      });

      if (!response.ok) throw new Error(`Error ${response.status}: ${response.statusText}`);
      
      // Registrar la actividad
      await activityLogsStore.createActivityLog(
        'DOWNLOAD_DOCUMENT',
        `Documento "${document.title}" descargado`,
        id
      );

      const blob = await response.blob();
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = document.fileName || 'documento';
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

  async function fetchDocumentTypes() {
    try {
      loading.value = true;
      const response = await fetchApi('/document-types');
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
      const response = await fetchApi('/tags');
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

    // Getters
    getDocuments,
    getCurrentDocument,
    isLoading,
    getError,
    getDocumentTypes,
    getTags,

    // Actions
    fetchDocuments,
    fetchDocumentById,
    createDocument,
    updateDocument,
    deleteDocument,
    downloadDocument,
    fetchDocumentTypes,
    fetchTags
  };
});
