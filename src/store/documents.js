import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import { useAuthStore } from '@/store/auth';

// Define la URL base de tu API
const API_BASE_URL = import.meta.env.VITE_BASE_URL || 'http://localhost:8080/api';

export const useDocumentsStore = defineStore('documents', () => {
  // Estado
  const documents = ref([]);
  const currentDocument = ref(null);
  const loading = ref(false);
  const error = ref(null);
  const documentTypes = ref([]);
  const tags = ref([]);

  // Getters
  const getDocuments = computed(() => documents.value);
  const getCurrentDocument = computed(() => currentDocument.value);
  const isLoading = computed(() => loading.value);
  const getError = computed(() => error.value);
  const getDocumentTypes = computed(() => documentTypes.value);
  const getTags = computed(() => tags.value);

  // Función para obtener los headers con autenticación
  function getAuthHeaders(isFormData = false) {
    const authStore = useAuthStore();
    const headers = {};
    
    if (authStore.token) {
      headers['Authorization'] = `Bearer ${authStore.token}`;
    }
    
    if (!isFormData) {
      headers['Content-Type'] = 'application/json';
      headers['Accept'] = 'application/json';
    }
    
    return headers;
  }

  // Función utilitaria para manejar respuestas fetch con mejor manejo de errores
  async function handleFetchResponse(response) {
    if (!response.ok) {
      // Intentamos leer el cuerpo de error como JSON primero
      try {
        const errorData = await response.json();
        throw { status: response.status, data: errorData };
      } catch (parseError) {
        // Si no es JSON, obtenemos el texto
        try {
          const errorText = await response.text();
          // Verificar si es una respuesta HTML (indicador de que estamos apuntando al lugar equivocado)
          if (errorText.includes('<!DOCTYPE html>')) {
            console.error('Recibiendo HTML en lugar de JSON. Verificar URL de API:', API_BASE_URL);
            throw { status: response.status, data: 'Error de conexión con la API. Posible URL incorrecta.' };
          }
          throw { status: response.status, data: errorText || response.statusText };
        } catch (textError) {
          // Si todo falla, usamos el statusText
          throw { status: response.status, data: response.statusText };
        }
      }
    }
    return response.json();
  }

  // Funciones utilitarias para peticiones API
  async function fetchApi(endpoint, options = {}) {
    try {
      loading.value = true;
      error.value = null;
      
      const url = endpoint.startsWith('http') ? endpoint : `${API_BASE_URL}${endpoint}`;
      const config = {
        method: options.method || 'GET',
        headers: getAuthHeaders(options.isFormData),
      };
      
      // Si hay un body y no es FormData, convertirlo a JSON
      if (options.body) {
        if (!options.isFormData) {
          config.body = JSON.stringify(options.body);
        } else {
          config.body = options.body;
        }
      }
      
      const response = await fetch(url, config);
      
      // Si la respuesta es 204 No Content, devolver true
      if (response.status === 204) {
        return true;
      }
      
      return await handleFetchResponse(response);
    } catch (err) {
      error.value = err.message || (err.data ? JSON.stringify(err.data) : 'Error en la solicitud');
      console.error(`Error en solicitud a ${endpoint}:`, err);
      throw err;
    } finally {
      loading.value = false;
    }
  }

  // Acciones
  // 1. Obtener todos los documentos
  async function fetchAllDocuments() {
    documents.value = await fetchApi('/documents');
    return documents.value;
  }

  // 2. Obtener documento por ID
  async function fetchDocumentById(id) {
    currentDocument.value = await fetchApi(`/documents/${id}`);
    return currentDocument.value;
  }

  // 3. Buscar documentos por título
  async function searchDocumentsByTitle(title) {
    documents.value = await fetchApi(`/documents/search?title=${encodeURIComponent(title)}`);
    return documents.value;
  }

  // 4. Obtener documentos por etiqueta
  async function fetchDocumentsByTag(tagName) {
    documents.value = await fetchApi(`/documents/tag/${encodeURIComponent(tagName)}`);
    return documents.value;
  }

  // 5. Obtener documentos por autor
  async function fetchDocumentsByAuthor(authorId) {
    documents.value = await fetchApi(`/documents/author/${authorId}`);
    return documents.value;
  }

  // 6. Crear documento
  async function createDocument(formData) {
    const newDocument = await fetchApi('/documents', {
      method: 'POST',
      body: formData,
      isFormData: true
    });
    
    documents.value.push(newDocument);
    return newDocument;
  }

  // 7. Descargar documento
  function getDocumentDownloadUrl(id) {
    const authStore = useAuthStore();
    return `${API_BASE_URL}/documents/download/${id}?token=${authStore.token}`;
  }

  // 8. Actualizar documento
  async function updateDocument(id, documentData) {
    const updatedDocument = await fetchApi(`/documents/${id}`, {
      method: 'PUT',
      body: documentData
    });
    
    // Actualizar en la lista si existe
    const index = documents.value.findIndex(doc => doc.id === updatedDocument.id);
    if (index !== -1) {
      documents.value[index] = updatedDocument;
    }
    
    // Actualizar documento actual si es el mismo
    if (currentDocument.value && currentDocument.value.id === updatedDocument.id) {
      currentDocument.value = updatedDocument;
    }
    
    return updatedDocument;
  }

  // 9. Eliminar documento (soft delete)
  async function deleteDocument(id) {
    await fetchApi(`/documents/${id}`, { method: 'DELETE' });
    
    // Eliminar de la lista local
    documents.value = documents.value.filter(doc => doc.id !== id);
    
    // Limpiar documento actual si es el mismo
    if (currentDocument.value && currentDocument.value.id === id) {
      currentDocument.value = null;
    }
    
    return true;
  }

  // 10. Eliminar documento permanentemente
  async function deleteDocumentPermanently(id) {
    await fetchApi(`/documents/permanent/${id}`, { method: 'DELETE' });
    
    // Eliminar de la lista local
    documents.value = documents.value.filter(doc => doc.id !== id);
    
    // Limpiar documento actual si es el mismo
    if (currentDocument.value && currentDocument.value.id === id) {
      currentDocument.value = null;
    }
    
    return true;
  }

  // 11. Obtener todos los tipos de documentos
  async function fetchDocumentTypes() {
    documentTypes.value = await fetchApi('/document-types');
    return documentTypes.value;
  }

  // 12. Obtener todas las etiquetas
  async function fetchTags() {
    tags.value = await fetchApi('/tags');
    return tags.value;
  }

  // Limpiar errores
  function clearError() {
    error.value = null;
  }

  // Limpiar documento actual
  function clearCurrentDocument() {
    currentDocument.value = null;
  }

  return {
    // Estado
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
    
    // Acciones
    fetchAllDocuments,
    fetchDocumentById,
    searchDocumentsByTitle,
    fetchDocumentsByTag,
    fetchDocumentsByAuthor,
    createDocument,
    getDocumentDownloadUrl,
    updateDocument,
    deleteDocument,
    deleteDocumentPermanently,
    fetchDocumentTypes,
    fetchTags,
    clearError,
    clearCurrentDocument
  };
});
