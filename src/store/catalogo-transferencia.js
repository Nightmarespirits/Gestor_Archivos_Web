import { defineStore } from 'pinia';
import { ref } from 'vue';
import { useActivityLogsStore } from './activityLogs';
import { api, apiService, API_BASE_URL } from '@/services/api';

export const useCatalogoTransferenciaStore = defineStore('catalogoTransferencia', () => {
    const catalogosTransferencia = ref([]);
    const currentCatalogoTransferencia = ref(null);

    const loading = ref(false);
    const error = ref(null);
    const activityLogsStore = useActivityLogsStore();
    
    function getCatalogosTransferencia() {
    return catalogosTransferencia.value;
    }

    function getCurrentCatalogoTransferencia() {
    return currentCatalogoTransferencia.value;
    }
    function isLoading() {
    return loading.value;
    }

    function getError() {
    return error.value;
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
   * Obtiene todos los catálogos de transferencia
   * @returns {Promise} - Lista de catálogos de transferencia
   */
  async function fetchCatalogosTransferencia() {
    try {
      loading.value = true;
      const data = await api.get('/inventarios/catalogo-transferencia', { debug: true });
      catalogosTransferencia.value = data || [];
      return data;
    } catch (err) {
      error.value = err.message;
      console.error('fetchCatalogosTransferencia Error:', err);
      catalogosTransferencia.value = [];
      return [];
    } finally {
      loading.value = false;
    }
  }

  /**
   * Obtiene un catálogo de transferencia por su ID
   * @param {Number} id - ID del catálogo a obtener
   * @returns {Promise} - Catálogo de transferencia obtenido
   */
  async function fetchCatalogoTransferenciaById(id) {
    try {
      loading.value = true;
      const data = await api.get(`/inventarios/catalogo-transferencia/${id}`);
      currentCatalogoTransferencia.value = data;
      return data;
    } catch (err) {
      error.value = err.message;
      console.error('fetchCatalogoTransferenciaById Error:', err);
      return null;
    } finally {
      loading.value = false;
    }
  }

  /**
   * Crea un nuevo catálogo de transferencia
   * @param {Object} catalogoData - Datos del catálogo a crear
   * @returns {Promise} - Catálogo de transferencia creado
   */
  async function createCatalogoTransferencia(catalogoData) {
    try {
      loading.value = true;
      
      // Enviar la solicitud al servidor
      const createdCatalogo = await api.post('/inventarios/catalogo-transferencia', catalogoData);
      
      // Actualizar la lista si fue exitoso
      if (createdCatalogo && createdCatalogo.id) {
        await fetchCatalogosTransferencia();
        
        // Registrar actividad
        await activityLogsStore.logActivity({
          action: 'CREATE',
          entityType: 'CATALOGO_TRANSFERENCIA',
          entityId: createdCatalogo.id,
          details: `Catálogo de Transferencia "${createdCatalogo.numero || createdCatalogo.codigo}" creado`
        });
      }
      
      return createdCatalogo;
    } catch (err) {
      error.value = err.message;
      console.error('createCatalogoTransferencia Error:', err);
      throw err;
    } finally {
      loading.value = false;
    }
  }

  /**
   * Actualiza un catálogo de transferencia existente
   * @param {Number} id - ID del catálogo a actualizar
   * @param {Object} catalogoData - Datos actualizados del catálogo
   * @returns {Promise} - Catálogo de transferencia actualizado
   */
  async function updateCatalogoTransferencia(id, catalogoData) {
    try {
      loading.value = true;
      
      // Obtener datos actuales para comparación
      const currentData = currentCatalogoTransferencia.value || await fetchCatalogoTransferenciaById(id);
      const oldState = currentData ? currentData.estado : null;
      
      // Enviar solicitud de actualización
      const updatedCatalogo = await api.put(`/inventarios/catalogo-transferencia/${id}`, catalogoData);
      
      // Actualizar la caché local si fue exitoso
      if (updatedCatalogo) {
        // Si el catálogo actual está cargado, actualizarlo
        if (currentCatalogoTransferencia.value && currentCatalogoTransferencia.value.id === id) {
          currentCatalogoTransferencia.value = updatedCatalogo;
        }
        
        // Actualizar en la lista si existe
        const index = catalogosTransferencia.value.findIndex(item => item.id === id);
        if (index !== -1) {
          catalogosTransferencia.value[index] = updatedCatalogo;
        }
        
        // Registro de actividad con detalles del cambio de estado
        let activityDetails = `Catálogo de Transferencia "${updatedCatalogo.numero || updatedCatalogo.codigo}" actualizado`;
        if (oldState && updatedCatalogo.estado && oldState !== updatedCatalogo.estado) {
          activityDetails += `. Estado cambiado de ${oldState} a ${updatedCatalogo.estado}`;
        }
        
        await activityLogsStore.logActivity({
          action: 'UPDATE',
          entityType: 'CATALOGO_TRANSFERENCIA',
          entityId: id,
          details: activityDetails
        });
      }
      
      return updatedCatalogo;
    } catch (err) {
      error.value = err.message;
      console.error('updateCatalogoTransferencia Error:', err);
      throw err;
    } finally {
      loading.value = false;
    }
  }

  /**
   * Elimina un catálogo de transferencia
   * @param {Number} id - ID del catálogo a eliminar
   * @returns {Promise<boolean>} - true si se eliminó correctamente
   */
  async function deleteCatalogoTransferencia(id) {
    try {
      loading.value = true;
      
      // Obtener datos para el registro de actividad
      const catalogoToDelete = catalogosTransferencia.value.find(cat => cat.id === id) || 
                                (currentCatalogoTransferencia.value && currentCatalogoTransferencia.value.id === id ? 
                                 currentCatalogoTransferencia.value : null);
      
      // Enviar solicitud de eliminación
      await api.delete(`/inventarios/catalogo-transferencia/${id}`);
      
      // Actualizar el estado local
      catalogosTransferencia.value = catalogosTransferencia.value.filter(cat => cat.id !== id);
      
      // Si el catálogo actual es el que se eliminó, limpiarlo
      if (currentCatalogoTransferencia.value && currentCatalogoTransferencia.value.id === id) {
        currentCatalogoTransferencia.value = null;
      }
      
      // Registrar actividad
      if (catalogoToDelete) {
        await activityLogsStore.logActivity({
          action: 'DELETE',
          entityType: 'CATALOGO_TRANSFERENCIA',
          entityId: id,
          details: `Catálogo de Transferencia "${catalogoToDelete.numero || catalogoToDelete.codigo || id}" eliminado`
        });
      }
      
      return true;
    } catch (err) {
      error.value = err.message;
      console.error('deleteCatalogoTransferencia Error:', err);
      return false;
    } finally {
      loading.value = false;
    }
  }
  
  return{
    catalogosTransferencia,
    currentCatalogoTransferencia,
    loading,
    error,
    activityLogsStore,
    getCatalogosTransferencia,
    getCurrentCatalogoTransferencia,
    isLoading,
    getError,
    getInventarioStates,
    getInventarioStateColor,
    getInventarioDownloadUrl,
    fetchCatalogosTransferencia,
    fetchCatalogoTransferenciaById,
    createCatalogoTransferencia,
    updateCatalogoTransferencia,
    deleteCatalogoTransferencia
  }


});