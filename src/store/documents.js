import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import { useAuthStore } from '@/store/auth'; // Importar el store de autenticación
import { debugApiCall, createAuthHeaders } from '@/utils/apiDebug'; // Importar utilidades de depuración

// Define la URL base de tu API
const API_BASE_URL = import.meta.env.VITE_BASE_URL;

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
    const headers = createAuthHeaders(authStore, isFormData);
    
    // Depuración
    console.log('Token en el store:', authStore.token);
    console.log('Usuario en el store:', authStore.user);
    
    return headers;
  }

  // Acciones
  // 1. Obtener todos los documentos
  async function fetchAllDocuments() {
    const authStore = useAuthStore();
    try {
      loading.value = true;
      const headers = getAuthHeaders();
      
      // Debug info
      debugApiCall(`${API_BASE_URL}/documents`, headers, authStore);
      
      const response = await fetch(`${API_BASE_URL}/documents`, {
        headers,
        credentials: 'include' // Incluir cookies si el backend usa autenticación basada en cookies
      });
      
      if (!response.ok) {
        const errorText = await response.text();
        console.error('Error response:', errorText);
        throw new Error(`Error ${response.status}: ${response.statusText || errorText}`);
      }
      
      documents.value = await response.json();
      return documents.value;
    } catch (err) {
      error.value = err.message;
      console.error('Error al obtener documentos:', err);
      throw err;
    } finally {
      loading.value = false;
    }
  }

  // 2. Obtener documento por ID
  async function fetchDocumentById(id) {
    const authStore = useAuthStore();
    try {
      loading.value = true;
      const headers = getAuthHeaders();
      
      // Debug info
      debugApiCall(`${API_BASE_URL}/documents/${id}`, headers, authStore);
      
      const response = await fetch(`${API_BASE_URL}/documents/${id}`, {
        headers,
        credentials: 'include'
      });
      
      if (!response.ok) {
        throw new Error(`Error ${response.status}: ${response.statusText}`);
      }
      
      currentDocument.value = await response.json();
      return currentDocument.value;
    } catch (err) {
      error.value = err.message;
      console.error(`Error al obtener documento con ID ${id}:`, err);
      throw err;
    } finally {
      loading.value = false;
    }
  }

  // 3. Buscar documentos por título
  async function searchDocumentsByTitle(title) {
    const authStore = useAuthStore();
    try {
      loading.value = true;
      const headers = getAuthHeaders();
      
      // Debug info
      debugApiCall(`${API_BASE_URL}/documents/search?title=${title}`, headers, authStore);
      
      const response = await fetch(`${API_BASE_URL}/documents/search?title=${encodeURIComponent(title)}`, {
        headers,
        credentials: 'include'
      });
      
      if (!response.ok) {
        throw new Error(`Error ${response.status}: ${response.statusText}`);
      }
      
      documents.value = await response.json();
      return documents.value;
    } catch (err) {
      error.value = err.message;
      console.error('Error al buscar documentos por título:', err);
      throw err;
    } finally {
      loading.value = false;
    }
  }

  // 4. Obtener documentos por etiqueta
  async function fetchDocumentsByTag(tagName) {
    try {
      loading.value = true;
      const response = await fetch(`${API_BASE_URL}/documents/tag/${encodeURIComponent(tagName)}`, {
        headers: getAuthHeaders()
      });
      
      if (!response.ok) {
        throw new Error(`Error ${response.status}: ${response.statusText}`);
      }
      
      documents.value = await response.json();
      return documents.value;
    } catch (err) {
      error.value = err.message;
      console.error(`Error al obtener documentos con etiqueta ${tagName}:`, err);
      throw err;
    } finally {
      loading.value = false;
    }
  }

  // 5. Obtener documentos por autor
  async function fetchDocumentsByAuthor(authorId) {
    try {
      loading.value = true;
      const response = await fetch(`${API_BASE_URL}/documents/author/${authorId}`, {
        headers: getAuthHeaders()
      });
      
      if (!response.ok) {
        throw new Error(`Error ${response.status}: ${response.statusText}`);
      }
      
      documents.value = await response.json();
      return documents.value;
    } catch (err) {
      error.value = err.message;
      console.error(`Error al obtener documentos del autor ${authorId}:`, err);
      throw err;
    } finally {
      loading.value = false;
    }
  }

  // 6. Crear documento
  async function createDocument(formData) {
    try {
      loading.value = true;
      const response = await fetch(`${API_BASE_URL}/documents`, {
        method: 'POST',
        headers: getAuthHeaders(true), // true porque es FormData
        body: formData, // FormData para subir archivos
      });
      
      if (!response.ok) {
        throw new Error(`Error ${response.status}: ${response.statusText}`);
      }
      
      const newDocument = await response.json();
      documents.value.push(newDocument);
      return newDocument;
    } catch (err) {
      error.value = err.message;
      console.error('Error al crear documento:', err);
      throw err;
    } finally {
      loading.value = false;
    }
  }

  // 7. Descargar documento
  function getDocumentDownloadUrl(id) {
    const authStore = useAuthStore();
    return `${API_BASE_URL}/documents/download/${id}?token=${authStore.token}`;
  }

  // 8. Actualizar documento
  async function updateDocument(id, documentData) {
    try {
      loading.value = true;
      const response = await fetch(`${API_BASE_URL}/documents/${id}`, {
        method: 'PUT',
        headers: getAuthHeaders(),
        body: JSON.stringify(documentData),
      });
      
      if (!response.ok) {
        throw new Error(`Error ${response.status}: ${response.statusText}`);
      }
      
      const updatedDocument = await response.json();
      
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
    } catch (err) {
      error.value = err.message;
      console.error(`Error al actualizar documento con ID ${id}:`, err);
      throw err;
    } finally {
      loading.value = false;
    }
  }

  // 9. Eliminar documento (soft delete)
  async function deleteDocument(id) {
    try {
      loading.value = true;
      const response = await fetch(`${API_BASE_URL}/documents/${id}`, {
        method: 'DELETE',
        headers: getAuthHeaders()
      });
      
      if (!response.ok) {
        throw new Error(`Error ${response.status}: ${response.statusText}`);
      }
      
      // Eliminar de la lista local
      documents.value = documents.value.filter(doc => doc.id !== id);
      
      // Limpiar documento actual si es el mismo
      if (currentDocument.value && currentDocument.value.id === id) {
        currentDocument.value = null;
      }
      
      return true;
    } catch (err) {
      error.value = err.message;
      console.error(`Error al eliminar documento con ID ${id}:`, err);
      throw err;
    } finally {
      loading.value = false;
    }
  }

  // 10. Eliminar documento permanentemente
  async function deleteDocumentPermanently(id) {
    try {
      loading.value = true;
      const response = await fetch(`${API_BASE_URL}/documents/permanent/${id}`, {
        method: 'DELETE',
        headers: getAuthHeaders()
      });
      
      if (!response.ok) {
        throw new Error(`Error ${response.status}: ${response.statusText}`);
      }
      
      // Eliminar de la lista local
      documents.value = documents.value.filter(doc => doc.id !== id);
      
      // Limpiar documento actual si es el mismo
      if (currentDocument.value && currentDocument.value.id === id) {
        currentDocument.value = null;
      }
      
      return true;
    } catch (err) {
      error.value = err.message;
      console.error(`Error al eliminar permanentemente documento con ID ${id}:`, err);
      throw err;
    } finally {
      loading.value = false;
    }
  }

  // 11. Obtener todos los tipos de documentos
  async function fetchDocumentTypes() {
    const authStore = useAuthStore();
    try {
      loading.value = true;
      const headers = getAuthHeaders();
      
      // Debug info
      debugApiCall(`${API_BASE_URL}/document-types`, headers, authStore);
      
      const response = await fetch(`${API_BASE_URL}/document-types`, {
        headers,
        credentials: 'include'
      });
      
      if (!response.ok) {
        throw new Error(`Error ${response.status}: ${response.statusText}`);
      }
      
      documentTypes.value = await response.json();
      return documentTypes.value;
    } catch (err) {
      error.value = err.message;
      console.error('Error al obtener tipos de documentos:', err);
      throw err;
    } finally {
      loading.value = false;
    }
  }

  // 12. Obtener todas las etiquetas
  async function fetchTags() {
    const authStore = useAuthStore();
    try {
      loading.value = true;
      const headers = getAuthHeaders();
      
      // Debug info
      debugApiCall(`${API_BASE_URL}/tags`, headers, authStore);
      
      const response = await fetch(`${API_BASE_URL}/tags`, {
        headers,
        credentials: 'include'
      });
      
      if (!response.ok) {
        throw new Error(`Error ${response.status}: ${response.statusText}`);
      }
      
      tags.value = await response.json();
      return tags.value;
    } catch (err) {
      error.value = err.message;
      console.error('Error al obtener etiquetas:', err);
      throw err;
    } finally {
      loading.value = false;
    }
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
