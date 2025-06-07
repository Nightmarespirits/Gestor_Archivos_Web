import { defineStore } from 'pinia';
import { ref } from 'vue';
import { useActivityLogsStore } from './activityLogs';
import { api, apiService, API_BASE_URL } from '@/services/api';
export const useRegistroTransferenciaStore = defineStore('registroTransferencia', () => {
    const registrosTransferencia = ref([]);
    const currentRegistroTransferencia = ref(null);
    
    const loading = ref(false);
    const error = ref(null);
    const activityLogsStore = useActivityLogsStore();

    function getRegistrosTransferencia() {
    return registrosTransferencia.value;
    }

    function getCurrentRegistroTransferencia() {
    return currentRegistroTransferencia.value;
    }

    function isLoading() {
    return loading.value;
    }

    function getError() {
    return error.value;
    }
    
    function getInventarioStates() {
    return inventarioStates.value;
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
        direction: 'desc'
    });
    
      /**
   * Obtiene todos los registros de transferencia
   * @returns {Promise} - Lista de registros de transferencia
   */
  async function fetchRegistrosTransferencia() {
    try {
      loading.value = true;
      const data = await api.get('/inventarios/registro-transferencia', { debug: true });
      registrosTransferencia.value = data || [];
      return data;
    } catch (err) {
      error.value = err.message;
      console.error('fetchRegistrosTransferencia Error:', err);
      registrosTransferencia.value = [];
      return [];
    } finally {
      loading.value = false;
    }
  }

  /**
   * Obtiene un registro de transferencia por su ID
   * @param {Number} id - ID del registro a obtener
   * @returns {Promise} - Registro de transferencia obtenido
   */
  async function fetchRegistroTransferenciaById(id) {
    try {
      loading.value = true;
      const data = await api.get(`/inventarios/registro-transferencia/${id}`);
      currentRegistroTransferencia.value = data;
      return data;
    } catch (err) {
      error.value = err.message;
      console.error('fetchRegistroTransferenciaById Error:', err);
      return null;
    } finally {
      loading.value = false;
    }
  }

  /**
   * Crea un nuevo registro de transferencia
   * @param {Object} registroData - Datos del registro a crear
   * @returns {Promise} - Registro de transferencia creado
   */
  async function createRegistroTransferencia(registroData) {
    try {
      loading.value = true;
      
      // Enviar la solicitud al servidor
      const createdRegistro = await api.post('/inventarios/registro-transferencia', registroData);
      
      // Actualizar la lista si fue exitoso
      if (createdRegistro && createdRegistro.id) {
        await fetchRegistrosTransferencia();
        
        // Registrar actividad
        await activityLogsStore.logActivity({
          action: 'CREATE',
          entityType: 'REGISTRO_TRANSFERENCIA',
          entityId: createdRegistro.id,
          details: `Registro de Transferencia "${createdRegistro.numero}" creado`
        });
      }
      
      return createdRegistro;
    } catch (err) {
      error.value = err.message;
      console.error('createRegistroTransferencia Error:', err);
      throw err;
    } finally {
      loading.value = false;
    }
  }

  /**
   * Actualiza un registro de transferencia existente
   * @param {Number} id - ID del registro a actualizar
   * @param {Object} registroData - Datos actualizados del registro
   * @returns {Promise} - Registro de transferencia actualizado
   */
  async function updateRegistroTransferencia(id, registroData) {
    try {
      loading.value = true;
      
      // Obtener datos actuales para comparación
      const currentData = currentRegistroTransferencia.value || await fetchRegistroTransferenciaById(id);
      const oldState = currentData ? currentData.estado : null;
      
      // Enviar solicitud de actualización
      const updatedRegistro = await api.put(`/inventarios/registro-transferencia/${id}`, registroData);
      
      // Actualizar la caché local si fue exitoso
      if (updatedRegistro) {
        // Si el registro actual está cargado, actualizarlo
        if (currentRegistroTransferencia.value && currentRegistroTransferencia.value.id === id) {
          currentRegistroTransferencia.value = updatedRegistro;
        }
        
        // Actualizar en la lista si existe
        const index = registrosTransferencia.value.findIndex(item => item.id === id);
        if (index !== -1) {
          registrosTransferencia.value[index] = updatedRegistro;
        }
        
        // Registro de actividad con detalles del cambio de estado
        let activityDetails = `Registro de Transferencia "${updatedRegistro.numero}" actualizado`;
        if (oldState && updatedRegistro.estado && oldState !== updatedRegistro.estado) {
          activityDetails += `. Estado cambiado de ${oldState} a ${updatedRegistro.estado}`;
        }
        
        await activityLogsStore.logActivity({
          action: 'UPDATE',
          entityType: 'REGISTRO_TRANSFERENCIA',
          entityId: id,
          details: activityDetails
        });
      }
      
      return updatedRegistro;
    } catch (err) {
      error.value = err.message;
      console.error('updateRegistroTransferencia Error:', err);
      throw err;
    } finally {
      loading.value = false;
    }
  }

  /**
   * Elimina un registro de transferencia
   * @param {Number} id - ID del registro a eliminar
   * @returns {Promise<boolean>} - true si se eliminó correctamente
   */
  async function deleteRegistroTransferencia(id) {
    try {
      loading.value = true;
      
      // Obtener datos para el registro de actividad
      const registroToDelete = registrosTransferencia.value.find(reg => reg.id === id) || 
                               (currentRegistroTransferencia.value && currentRegistroTransferencia.value.id === id ? 
                                currentRegistroTransferencia.value : null);
      
      // Enviar solicitud de eliminación
      await api.delete(`/inventarios/registro-transferencia/${id}`);
      
      // Actualizar el estado local
      registrosTransferencia.value = registrosTransferencia.value.filter(reg => reg.id !== id);
      
      // Si el registro actual es el que se eliminó, limpiarlo
      if (currentRegistroTransferencia.value && currentRegistroTransferencia.value.id === id) {
        currentRegistroTransferencia.value = null;
      }
      
      // Registrar actividad
      if (registroToDelete) {
        await activityLogsStore.logActivity({
          action: 'DELETE',
          entityType: 'REGISTRO_TRANSFERENCIA',
          entityId: id,
          details: `Registro de Transferencia "${registroToDelete.numero || id}" eliminado`
        });
      }
      
      return true;
    } catch (err) {
      error.value = err.message;
      console.error('deleteRegistroTransferencia Error:', err);
      return false;
    } finally {
      loading.value = false;
    }
  }

  return{
    registrosTransferencia,
    currentRegistroTransferencia,
    loading,
    error,
    fetchRegistrosTransferencia,
    fetchRegistroTransferenciaById,
    createRegistroTransferencia,
    updateRegistroTransferencia,
    deleteRegistroTransferencia,
    getRegistrosTransferencia,
    getCurrentRegistroTransferencia,
    isLoading,
    getError,
  }
})