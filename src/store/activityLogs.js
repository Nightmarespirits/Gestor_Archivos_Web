import { defineStore } from 'pinia';
import { ref } from 'vue';
import { api } from '@/services/api'; // Importar el servicio API centralizado

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
  
  /**
   * Obtiene un listado paginado de los registros de actividad
   * @param {Object} options - Opciones de paginación
   * @param {Number} options.page - Número de página (desde 0)
   * @param {Number} options.size - Tamaño de la página
   * @returns {Promise<Object>} - Respuesta paginada
   */
  async function fetchLogs() {
    try {
      loading.value = true;
      // Usar el método específico para paginación
      const response = await api.get('/activity-logs');
      
      logs.value = response;
      return response;
    } catch (err) {
      error.value = err.message;
      throw err;
    } finally {
      loading.value = false;
    }
  }

  /**
   * Obtiene los registros de actividad de un usuario específico
   * @param {Number} userId - ID del usuario
   */
  async function fetchLogsByUser(userId) {
    try {
      loading.value = true;
      const response = await api.get(`/activity-logs/user/${userId}`);
      logs.value = response;
      return response;
    } catch (err) {
      error.value = err.message;
      throw err;
    } finally {
      loading.value = false;
    }
  }

  /**
   * Obtiene los registros de actividad por tipo de acción
   * @param {String} actionType - Tipo de acción
   */
  async function fetchLogsByActionType(actionType) {
    try {
      loading.value = true;
      const response = await api.get(`/activity-logs/action-type/${actionType}`);
      logs.value = response;
      return response;
    } catch (err) {
      error.value = err.message;
      throw err;
    } finally {
      loading.value = false;
    }
  }

  /**
   * Obtiene los registros de actividad dentro de un rango de tiempo
   * @param {String} startTime - Fecha y hora de inicio
   * @param {String} endTime - Fecha y hora de fin
   */
  async function fetchLogsByTimeRange(startTime, endTime) {
    try {
      loading.value = true;
      
      // Usar parámetros en lugar de construir la URL manualmente
      const response = await api.get('/activity-logs/time-range', {
        params: {
          startTime,
          endTime
        }
      });
      
      logs.value = response;
      return response;
    } catch (err) {
      error.value = err.message;
      throw err;
    } finally {
      loading.value = false;
    }
  }

  /**
   * Registra una actividad del usuario en el sistema
   * @param {String} actionType - Tipo de acción realizada
   * @param {String} description - Descripción de la acción
   * @param {Number} documentId - ID del documento (opcional)
   */
  async function logActivity(actionType, description, documentId = null) {
    try {
      loading.value = true;
      const user = JSON.parse(localStorage.getItem('authUser') || '{}');
      
      const logData = {
        user: {
          id: user.id
        },
        actionType,
        description: description || actionType,
        document: documentId ? { id: documentId } : null,
        ipAddress: window.location.hostname
      };

      // Usar el método post del servicio API
      const response = await api.post('/activity-logs', logData);

      // Actualizar la lista de logs si estamos en la página de logs
      if (logs.value.length > 0) {
        await fetchLogs({ 
          page: pagination.value.currentPage, 
          size: pagination.value.itemsPerPage 
        });
      }

      return response;
    } catch (err) {
      error.value = err.message;
      console.error('Error al registrar actividad:', err);
      // No propagamos el error para no interrumpir el flujo principal
    } finally {
      loading.value = false;
    }
  }

  // Alias para mantener compatibilidad con código existente
  const createActivityLog = logActivity;

  /**
   * Limpia los errores del store
   */
  function clearError() {
    error.value = null;
  }

  return {
    // Estado
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

    // Acciones
    fetchLogs,
    fetchLogsByUser,
    fetchLogsByActionType,
    fetchLogsByTimeRange,
    logActivity,
    createActivityLog, // Mantener para compatibilidad
    clearError
  };
});
