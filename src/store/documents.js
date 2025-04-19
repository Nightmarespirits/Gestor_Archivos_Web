import { defineStore } from 'pinia';
import { ref } from 'vue';
import { fetchApi } from '@/utils/apiDebug';

export const useDocumentsStore = defineStore('documents', () => {
  // Estado
  const documents = ref([]);
  const currentDocument = ref(null);
  const loading = ref(false);
  const error = ref(null);
  const documentTypes = ref([]);
  const tags = ref([]);

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

  // Acciones
  async function fetchAllDocuments() {
    try {
      loading.value = true;
      const response = await fetchApi('/documents');
      console.log(
        'fetchAllDocuments: Respuesta de la API:', response
      )
      documents.value = response;
      return response;
    } catch (err) {
      error.value = err.message;
      throw err;
    } finally {
      loading.value = false;
    }
  }

  async function fetchDocumentById(id) {
    try {
      if (!id) throw new Error('Document ID is required');
      loading.value = true;
      const response = await fetchApi(`/documents/${id}`);
      currentDocument.value = response;
      return response;
    } catch (err) {
      error.value = err.message;
      throw err;
    } finally {
      loading.value = false;
    }
  }

  async function searchDocumentsByTitle(title) {
    try {
      if (!title) throw new Error('Search title is required');
      loading.value = true;
      const response = await fetchApi(`/documents/search?title=${encodeURIComponent(title)}`);
      return response;
    } catch (err) {
      error.value = err.message;
      throw err;
    } finally {
      loading.value = false;
    }
  }

  async function fetchDocumentsByTag(tagName) {
    try {
      if (!tagName) throw new Error('Tag name is required');
      loading.value = true;
      const response = await fetchApi(`/documents/by-tag/${encodeURIComponent(tagName)}`);
      return response;
    } catch (err) {
      error.value = err.message;
      throw err;
    } finally {
      loading.value = false;
    }
  }

  async function createDocument(formData) {
    try {
      loading.value = true;
      const response = await fetchApi('/documents', {
        method: 'POST',
        body: formData,
        isFormData: true
      });
      return response;
    } catch (err) {
      error.value = err.message;
      throw err;
    } finally {
      loading.value = false;
    }
  }

  function getDocumentDownloadUrl(id) {
    if (!id) throw new Error('Document ID is required');
    return `${import.meta.env.VITE_API_BASE_URL}/documents/download/${id}`;
  }

  async function updateDocument(id, document) {
    try {
      if (!id) throw new Error('Document ID is required');
      loading.value = true;
      const response = await fetchApi(`/documents/${id}`, {
        method: 'PUT',
        body: document
      });
      return response;
    } catch (err) {
      error.value = err.message;
      throw err;
    } finally {
      loading.value = false;
    }
  }

  async function deleteDocument(id) {
    try {
      if (!id) throw new Error('Document ID is required');
      loading.value = true;
      await fetchApi(`/documents/${id}`, { method: 'DELETE' });
    } catch (err) {
      error.value = err.message;
      throw err;
    } finally {
      loading.value = false;
    }
  }

  async function deleteDocumentPermanently(id) {
    try {
      if (!id) throw new Error('Document ID is required');
      loading.value = true;
      await fetchApi(`/documents/permanent/${id}`, { method: 'DELETE' });
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
    fetchAllDocuments,
    fetchDocumentById,
    searchDocumentsByTitle,
    fetchDocumentsByTag,
    createDocument,
    getDocumentDownloadUrl,
    updateDocument,
    deleteDocument,
    deleteDocumentPermanently,
    fetchDocumentTypes,
    fetchTags
  };
});
