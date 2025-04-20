import { defineStore } from 'pinia';
import { ref } from 'vue';
import { fetchApi } from '@/utils/apiDebug';

export const useActivityLogsStore = defineStore('activityLogs', () => {
  // Estado
  const logs = ref([]);
  const currentLog = ref(null);
  const loading = ref(false);
  const error = ref(null);
  const pagination = ref({
    totalItems: 0,
    totalPages: 0,
    currentPage: 0,
    itemsPerPage: 20,
  });

  // Getters
  function getLogs() {
    return logs.value;
  }

  function getCurrentLog() {
    return currentLog.value;
  }

  function isLoading() {
    return loading.value;
  }

  function getError() {
    return error.value;
  }

  function getPagination() {
    return pagination.value;
  }

  // Acciones
  async function fetchLogs({ page = 0, size = 20 } = {}) {
    try {
      loading.value = true;
      const response = await fetchApi(`/activity-logs/paged?page=${page}&size=${size}`);
      logs.value = response.content;
      pagination.value = {
        totalItems: response.totalElements,
        totalPages: response.totalPages,
        currentPage: response.number,
        itemsPerPage: response.size,
      };
      return response;
    } catch (err) {
      error.value = err.message;
      throw err;
    } finally {
      loading.value = false;
    }
  }

  async function fetchLogsByUser(userId) {
    try {
      loading.value = true;
      const response = await fetchApi(`/activity-logs/user/${userId}`);
      logs.value = response;
      return response;
    } catch (err) {
      error.value = err.message;
      throw err;
    } finally {
      loading.value = false;
    }
  }

  async function fetchLogsByActionType(actionType) {
    try {
      loading.value = true;
      const response = await fetchApi(`/activity-logs/action-type/${actionType}`);
      logs.value = response;
      return response;
    } catch (err) {
      error.value = err.message;
      throw err;
    } finally {
      loading.value = false;
    }
  }

  async function fetchLogsByTimeRange(startTime, endTime) {
    try {
      loading.value = true;
      const url = new URL('/activity-logs/time-range', import.meta.env.VITE_API_BASE_URL);
      url.searchParams.append('startTime', startTime);
      url.searchParams.append('endTime', endTime);
      const response = await fetchApi(url.pathname + url.search);
      logs.value = response;
      return response;
    } catch (err) {
      error.value = err.message;
      throw err;
    } finally {
      loading.value = false;
    }
  }

  // Función para crear logs de actividad
  async function createActivityLog(actionType, description, documentId = null) {
    try {
      loading.value = true;
      const user = JSON.parse(localStorage.getItem('authUser'));
      
      const logData = {
        user: {
          id: user.id
        },
        actionType: actionType,
        description: description || actionType,
        document: documentId ? { id: documentId } : null,
        ipAddress: window.location.hostname
      };

      const response = await fetchApi('/activity-logs', {
        method: 'POST',
        body: logData // fetchApi ya maneja la conversión a JSON
      });

      // Actualizar la lista de logs si estamos en la página de logs
      if (logs.value.length > 0) {
        await fetchLogs({ page: pagination.value.currentPage, size: pagination.value.itemsPerPage });
      }

      return response;
    } catch (err) {
      error.value = err.message;
      console.error('Error en createActivityLog:', err);
    } finally {
      loading.value = false;
    }
  }

  function clearError() {
    error.value = null;
  }

  return {
    // State
    logs,
    currentLog,
    loading,
    error,
    pagination,

    // Getters
    getLogs,
    getCurrentLog,
    isLoading,
    getError,
    getPagination,

    // Actions
    fetchLogs,
    fetchLogsByUser,
    fetchLogsByActionType,
    fetchLogsByTimeRange,
    createActivityLog,
    clearError
  };
});