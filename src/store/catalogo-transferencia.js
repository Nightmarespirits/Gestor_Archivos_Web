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
      const data = await api.get('/catalogo-transferencia', { debug: true });
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
      const data = await api.get(`/catalogo-transferencia/${id}`);
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
      const createdCatalogo = await api.post('/catalogo-transferencia', catalogoData);
      
      // Actualizar la lista si fue exitoso
      if (createdCatalogo && createdCatalogo.id) {
        await fetchCatalogosTransferencia();
        
        // Registrar actividad
        await activityLogsStore.createActivityLog(
          'CREATE_CATALOG_TRANSFERENCY',
          `Catalgo de transferencia "${ createdCatalogo.id}" creado`
        );
      }
      console.log("Enviada a la api: ",JSON.stringify(catalogoData));
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
      const updatedCatalogo = await api.put(`/catalogo-transferencia/${id}`, catalogoData);
      
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
      await api.delete(`/catalogo-transferencia/${id}`);
      
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
  /**
   * Busca catálogos de transferencia con paginación y filtros
   * @param {Object} searchParams - Parámetros de búsqueda
   * @param {String} searchParams.unidad - Filtro por unidad/organización
   * @param {String} searchParams.titulo - Filtro por título o término
   * @param {String} searchParams.estado - Filtro por estado
   * @param {Number} searchParams.page - Número de página (0-indexed)
   * @param {Number} searchParams.size - Tamaño de página
   * @param {String} searchParams.sortBy - Campo para ordenar
   * @param {String} searchParams.direction - Dirección de ordenamiento (asc/desc)
   * @returns {Promise} - Página de catálogos que coinciden con los criterios
   */
  async function searchCatalogos(searchParams = {}) {
    try {
      loading.value = true;
      
      // Establecer valores por defecto para paginación
      const params = {
        page: searchParams.page !== undefined ? searchParams.page : pagination.value.page,
        size: searchParams.size !== undefined ? searchParams.size : pagination.value.size,
        sortBy: searchParams.sortBy || sortOptions.value.sortBy,
        direction: searchParams.direction || sortOptions.value.direction
      };
      
      // Añadir filtros opcionales solo si están presentes
      if (searchParams.unidad) params.unidad = searchParams.unidad;
      if (searchParams.titulo) params.titulo = searchParams.titulo;
      if (searchParams.estado) params.estado = searchParams.estado;
      
      // Realizar la solicitud con los parámetros de consulta
      const response = await api.get('/catalogo-transferencia/search', { 
        params,
        debug: true
      });
      
      // Actualizar estado de paginación con datos recibidos
      if (response && response.content) {
        catalogosTransferencia.value = response.content || [];
        pagination.value = {
          page: response.number || 0,
          size: response.size || 10,
          totalItems: response.totalElements || 0,
          totalPages: response.totalPages || 0
        };
        return response;
      }
      
      return { content: [], number: 0, size: pagination.value.size, totalElements: 0, totalPages: 0 };
    } catch (err) {
      error.value = err.message;
      console.error('searchCatalogos Error:', err);
      return { content: [], number: 0, size: pagination.value.size, totalElements: 0, totalPages: 0 };
    } finally {
      loading.value = false;
    }
  }
  
  /**
   * Obtiene todos los detalles de un catálogo por su ID
   * @param {Number} catalogoId - ID del catálogo
   * @returns {Promise} - Lista de detalles del catálogo
   */
  async function getDetallesByCatalogo(catalogoId) {
    try {
      loading.value = true;
      const response = await api.get(`/catalogo-transferencia/${catalogoId}/detalles`);
      console.log("Response---------------------------: ",response);
      return response || [];


    } catch (err) {
      error.value = err.message;
      console.error(`getDetallesByCatalogo Error para catálogo ID ${catalogoId}:`, err);
      return [];
    } finally {
      loading.value = false;
    }
  }

  /**
   * Añade un nuevo detalle a un catálogo existente
   * @param {Number} catalogoId - ID del catálogo
   * @param {Object} detalleData - Datos del detalle a crear
   * @returns {Promise} - Detalle creado
   */
  async function addDetalle(catalogoId, detalleData) {
    try {
      loading.value = true;
      const response = await api.post(`/catalogo-transferencia/${catalogoId}/detalles`, detalleData);
      return response;
    } catch (err) {
      error.value = err.message;
      console.error(`Error al añadir detalle al catálogo ID ${catalogoId}:`, err);
      throw err;
    } finally {
      loading.value = false;
    }
  }

  /**
   * Actualiza un detalle existente
   * @param {Number} catalogoId - ID del catálogo
   * @param {Number} detalleId - ID del detalle
   * @param {Object} detalleData - Datos actualizados del detalle
   * @returns {Promise} - Detalle actualizado
   */
  async function updateDetalle(catalogoId, detalleId, detalleData) {
    try {
      loading.value = true;
      const response = await api.put(`/catalogo-transferencia/${catalogoId}/detalles/${detalleId}`, detalleData);
      return response;
    } catch (err) {
      error.value = err.message;
      console.error(`Error al actualizar detalle ID ${detalleId} del catálogo ID ${catalogoId}:`, err);
      throw err;
    } finally {
      loading.value = false;
    }
  }
  
  /**
   * Exporta un catálogo y sus detalles a Excel
   * @param {Number} catalogoId - ID del catálogo a exportar
   * @returns {Promise} - URL para descargar el archivo
   */
  async function exportCatalogoToExcel(catalogoId) {
    try {
      loading.value = true;
      
      // Construir la URL para la descarga directa
      const downloadUrl = `${API_BASE_URL}/catalogo-transferencia/${catalogoId}/export-excel`;
      
      // Registrar actividad de exportación
      await activityLogsStore.logActivity({
        action: 'EXPORT',
        entityType: 'CATALOGO_TRANSFERENCIA',
        entityId: catalogoId,
        details: `Catálogo de Transferencia ID: ${catalogoId} exportado a Excel`
      });
      
      // Retornar la URL para que el componente pueda iniciar la descarga
      return downloadUrl;
    } catch (err) {
      error.value = err.message;
      console.error(`exportCatalogoToExcel Error para catálogo ID ${catalogoId}:`, err);
      throw err;
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
    //getInventarioStates,
    //getInventarioStateColor,
    //getInventarioDownloadUrl,
    fetchCatalogosTransferencia,
    fetchCatalogoTransferenciaById,
    createCatalogoTransferencia,
    updateCatalogoTransferencia,
    deleteCatalogoTransferencia,
    searchCatalogos,
    getDetallesByCatalogo,
    addDetalle,
    updateDetalle,
    exportCatalogoToExcel,
    pagination,
    sortOptions
  }


});