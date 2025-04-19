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
      let errorMessage = '';
      
      // Try to get detailed error message from response
      try {
        const errorData = await response.json();
        errorMessage = errorData.message || errorData.error || JSON.stringify(errorData);
      } catch {
        // If we can't parse JSON, use status text
        errorMessage = response.statusText;
      }

      // Specific error handling based on status codes
      switch (response.status) {
        case 400:
          throw new Error(`Error en la solicitud: ${errorMessage}`);
        case 401:
          throw new Error('Sesión expirada. Por favor, vuelva a iniciar sesión.');
        case 403:
          throw new Error('No tiene permisos para realizar esta acción.');
        case 404:
          throw new Error('El recurso solicitado no existe.');
        case 413:
          throw new Error('El archivo es demasiado grande.');
        case 415:
          throw new Error('Tipo de archivo no soportado.');
        case 429:
          throw new Error('Demasiadas solicitudes. Por favor, espere un momento.');
        case 500:
          throw new Error('Error interno del servidor. Por favor, intente más tarde.');
        default:
          throw new Error(`Error ${response.status}: ${errorMessage}`);
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
          if (typeof options.body === 'object') {
            config.body = JSON.stringify(options.body);
          } else {
            throw new Error('El body debe ser un objeto válido');
          }
        } else {
          if (options.body instanceof FormData) {
            config.body = options.body;
          } else {
            throw new Error('El body debe ser una instancia de FormData');
          }
        }
      }

      // Timeout para las peticiones
      const timeoutDuration = 30000; // 30 segundos
      const controller = new AbortController();
      const timeoutId = setTimeout(() => controller.abort(), timeoutDuration);
      config.signal = controller.signal;
      
      try {
        const response = await fetch(url, config);
        clearTimeout(timeoutId);
        
        // Si la respuesta es 204 No Content, devolver true
        if (response.status === 204) {
          return true;
        }
        
        return await handleFetchResponse(response);
      } catch (err) {
        if (err.name === 'AbortError') {
          throw new Error('La solicitud ha excedido el tiempo de espera.');
        }
        throw err;
      }
      
    } catch (err) {
      let errorMsg = 'Error en la solicitud';
      
      if (err.message) {
        errorMsg = err.message;
      } else if (err.data) {
        errorMsg = typeof err.data === 'object' ? JSON.stringify(err.data) : String(err.data);
      }
      
      error.value = errorMsg;
      console.error(`Error en solicitud a ${endpoint}:`, err, 'Detalles:', errorMsg);
      throw new Error(errorMsg);
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
  async function deleteDocument(id, password) {
    if (!password) {
      throw new Error('Se requiere contraseña para eliminar documentos');
    }

    // Validar contraseña antes de eliminar
    const authValidation = await fetchApi(`/auth/validate-password`, {
      method: 'POST',
      body: { password }
    });

    if (!authValidation.valid) {
      throw new Error('Contraseña incorrecta');
    }

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

  // 13. Obtener metadatos de un documento
  async function fetchMetadata(documentId) {
    try {
      const metadata = await fetchApi(`/metadata/document/${documentId}`);
      return Array.isArray(metadata) ? metadata[0] : metadata; // Asegurar que devolvemos un objeto único
    } catch (error) {
      console.error('Error al obtener metadatos:', error);
      return null; // Devolver null si hay error, permitiendo manejo graceful
    }
  }

  // 14. Crear metadatos para un documento
  async function createMetadata(documentId, metadata) {
    // Validar datos de entrada
    if (!metadata || typeof metadata !== 'object') {
      throw new Error('Los metadatos deben ser un objeto válido');
    }

    // Sanitizar datos antes de enviar
    const sanitizedMetadata = {
      keywords: (metadata.keywords || '').trim(),
      department: (metadata.department || '').trim(),
      expirationDate: metadata.expirationDate || null
    };

    return await fetchApi(`/metadata/document/${documentId}`, {
      method: 'POST',
      body: sanitizedMetadata
    });
  }

  // 15. Actualizar metadatos
  async function updateMetadata(id, metadata) {
    // Validar datos de entrada
    if (!metadata || typeof metadata !== 'object') {
      throw new Error('Los metadatos deben ser un objeto válido');
    }

    // Sanitizar datos antes de enviar
    const sanitizedMetadata = {
      keywords: (metadata.keywords || '').trim(),
      department: (metadata.department || '').trim(),
      expirationDate: metadata.expirationDate || null
    };

    return await fetchApi(`/metadata/${id}`, {
      method: 'PUT',
      body: sanitizedMetadata
    });
  }

  // 16. Eliminar metadatos
  async function deleteMetadata(id) {
    return await fetchApi(`/metadata/${id}`, { method: 'DELETE' });
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
    fetchMetadata,
    createMetadata,
    updateMetadata,
    deleteMetadata,
    clearError,
    clearCurrentDocument
  };
});
