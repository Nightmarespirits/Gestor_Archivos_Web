import { defineStore } from 'pinia';
import { ref } from 'vue';
import { useActivityLogsStore } from './activityLogs';
import { api, apiService, API_BASE_URL } from '@/services/api';

export const useInventariosStore = defineStore('inventarios', () => {
  // Variables para Inventario General
  const inventarios = ref([]);
  const currentInventario = ref(null);
  
  // Variables para Registro de Transferencia
  const registrosTransferencia = ref([]);
  const currentRegistroTransferencia = ref(null);
  
  // Variables para Catálogo de Transferencia
  const catalogosTransferencia = ref([]);
  const currentCatalogoTransferencia = ref(null);
  
  // Variables comunes
  const loading = ref(false);
  const error = ref(null);
  const activityLogsStore = useActivityLogsStore();

  // Estados predefinidos para inventarios
  const inventarioStates = ref(['BORRADOR', 'FINALIZADO', 'APROBADO']);
  
  // URL para descargar inventarios
  const getInventarioDownloadUrl = (id, format) => {
    return `${API_BASE_URL}/inventarios/download/${id}?format=${format}`;
  };

  // Getters - Inventario General
  function getInventarios() {
    return inventarios.value;
  }

  function getCurrentInventario() {
    return currentInventario.value;
  }
  
  // Getters - Registro de Transferencia
  function getRegistrosTransferencia() {
    return registrosTransferencia.value;
  }

  function getCurrentRegistroTransferencia() {
    return currentRegistroTransferencia.value;
  }
  
  // Getters - Catálogo de Transferencia
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
  
  function getInventarioStates() {
    return inventarioStates.value;
  }
  
  function getInventarioStateColor(state) {
    switch (state) {
      case 'BORRADOR':
        return 'grey';
      case 'FINALIZADO':
        return 'blue';
      case 'APROBADO':
      default:
        return 'green';
    }
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

  // Actions
  async function fetchInventarios() {
    try {
      loading.value = true;
      const data = await api.get('/inventarios', { debug: true });
      // Garantizar que inventarios.value siempre sea un array
      inventarios.value = Array.isArray(data) ? data : [];
      console.log('fetchInventarios result:', inventarios.value);
    } catch (err) {
      error.value = err.message;
      console.error('fetchInventarios Error:', err);
      inventarios.value = [];
    } finally {
      loading.value = false;
    }
  }

  /**
   * Obtiene inventarios con paginación del servidor
   * @param {Object} options - Opciones de paginación y filtrado
   * @returns {Promise} - Datos paginados
   */
  async function fetchPaginatedInventarios(options = {}) {
    try {
      loading.value = true;
      
      const {
        page = pagination.value.page,
        size = pagination.value.size,
        sortBy = sortOptions.value.sortBy,
        direction = sortOptions.value.direction,
        filters = {}
      } = options;
      
      // Actualizar opciones en el store
      pagination.value.page = page;
      pagination.value.size = size;
      sortOptions.value.sortBy = sortBy;
      sortOptions.value.direction = direction;
      
      const response = await api.getPaginated('/inventarios/paginated', {
        page,
        pageSize: size,
        sortBy,
        sortDirection: direction,
        filters,
        debug: true
      });
      
      // Asegurarse de que response.content siempre sea un array
      const content = Array.isArray(response?.content) ? response.content : [];
      console.log('fetchPaginatedInventarios content:', content);
      
      // Actualizar el estado con los resultados
      inventarios.value = content;
      pagination.value.totalItems = response?.totalElements || 0;
      pagination.value.totalPages = response?.totalPages || 0;
      
      return {
        items: inventarios.value,
        pagination: { ...pagination.value }
      };
    } catch (err) {
      error.value = err.message;
      console.error('fetchPaginatedInventarios Error:', err);
      inventarios.value = [];
      return {
        items: [],
        pagination: { ...pagination.value, totalItems: 0, totalPages: 0 }
      };
    } finally {
      loading.value = false;
    }
  }

  /**
   * Búsqueda avanzada de inventarios con paginación
   * @param {Object} searchParams - Parámetros de búsqueda
   * @param {Object} paginationOptions - Opciones de paginación
   * @returns {Promise} - Resultados de búsqueda paginados
   */
  async function searchInventariosPaginated(searchParams = {}, paginationOptions = {}) {
    try {
      loading.value = true;
      
      const {
        titulo,
        dependencia,
        fechaInicio,
        fechaFin,
        estado
      } = searchParams;
      
      const {
        page = pagination.value.page,
        size = pagination.value.size,
        sortBy = sortOptions.value.sortBy,
        direction = sortOptions.value.direction
      } = paginationOptions;
      
      // Construir filtros para la búsqueda
      const filters = {};
      if (titulo) filters.titulo = titulo;
      if (dependencia) filters.dependencia = dependencia;
      if (fechaInicio) filters.fechaInicio = fechaInicio;
      if (fechaFin) filters.fechaFin = fechaFin;
      if (estado) filters.estado = estado;
      
      console.log('Searching with filters:', filters);
      
      // Usar la función de paginación con los filtros
      const response = await api.getPaginated('/inventarios/search', {
        page,
        pageSize: size,
        sortBy,
        sortDirection: direction,
        filters,
        debug: true
      });
      
      // Actualizar el estado con los resultados
      inventarios.value = response.content || [];
      pagination.value = {
        page,
        size,
        totalItems: response.totalElements || 0,
        totalPages: response.totalPages || 0
      };
      
      return {
        items: inventarios.value,
        pagination: { ...pagination.value }
      };
    } catch (err) {
      error.value = err.message;
      console.error('searchInventariosPaginated Error:', err);
      inventarios.value = [];
      return {
        items: [],
        pagination: { ...pagination.value, totalItems: 0, totalPages: 0 }
      };
    } finally {
      loading.value = false;
    }
  }

  /**
   * Obtiene un inventario por su ID
   * @param {Number} id - ID del inventario a obtener
   * @returns {Promise} - Inventario obtenido
   */
  async function fetchInventarioById(id) {
    try {
      loading.value = true;
      const data = await api.get(`/inventarios/${id}`);
      currentInventario.value = data;
      return data;
    } catch (err) {
      error.value = err.message;
      console.error('fetchInventarioById Error:', err);
      return null;
    } finally {
      loading.value = false;
    }
  }

  /**
   * Crea un nuevo inventario
   * @param {Object} inventarioData - Datos del inventario a crear
   * @returns {Promise} - Inventario creado
   */
  async function createInventario(inventarioData) {
    try {
      loading.value = true;
      
      // Enviar la solicitud al servidor
      const createdInventario = await api.post('/inventarios', inventarioData);
      
      // Actualizar la lista de inventarios si fue exitoso
      if (createdInventario && createdInventario.id) {
        // Opcional: refrescar la lista completa o agregar el nuevo inventario
        await fetchInventarios();
        
        // Registrar actividad
        await activityLogsStore.logActivity({
          action: 'CREATE',
          entityType: 'INVENTARIO',
          entityId: createdInventario.id,
          details: `Inventario "${createdInventario.titulo}" creado`
        });
      }
      
      return createdInventario;
    } catch (err) {
      error.value = err.message;
      console.error('createInventario Error:', err);
      throw err; // Re-lanzar el error para que el componente pueda manejarlo
    } finally {
      loading.value = false;
    }
  }

  /**
   * Actualiza un inventario existente
   * @param {Number} id - ID del inventario a actualizar
   * @param {Object} inventarioData - Datos actualizados del inventario
   * @returns {Promise} - Inventario actualizado
   */
  async function updateInventario(id, inventarioData) {
    try {
      loading.value = true;
      
      // Obtener datos actuales para comparación
      const currentData = currentInventario.value || await fetchInventarioById(id);
      const oldState = currentData ? currentData.estado : null;
      
      // Enviar solicitud de actualización
      const updatedInventario = await api.put(`/inventarios/${id}`, inventarioData);
      
      // Actualizar la caché local si fue exitoso
      if (updatedInventario) {
        // Si el inventario actual está cargado, actualizarlo
        if (currentInventario.value && currentInventario.value.id === id) {
          currentInventario.value = updatedInventario;
        }
        
        // Actualizar en la lista si existe
        const index = inventarios.value.findIndex(item => item.id === id);
        if (index !== -1) {
          inventarios.value[index] = updatedInventario;
        }
        
        // Registro de actividad con detalles del cambio de estado
        let activityDetails = `Inventario "${updatedInventario.titulo}" actualizado`;
        if (oldState && updatedInventario.estado && oldState !== updatedInventario.estado) {
          activityDetails += `. Estado cambiado de ${oldState} a ${updatedInventario.estado}`;
        }
        
        await activityLogsStore.logActivity({
          action: 'UPDATE',
          entityType: 'INVENTARIO',
          entityId: id,
          details: activityDetails
        });
      }
      
      return updatedInventario;
    } catch (err) {
      error.value = err.message;
      console.error('updateInventario Error:', err);
      throw err;
    } finally {
      loading.value = false;
    }
  }

  /**
   * Elimina un inventario
   * @param {Number} id - ID del inventario a eliminar
   * @returns {Promise<boolean>} - true si se eliminó correctamente
   */
  async function deleteInventario(id) {
    try {
      loading.value = true;
      
      // Obtener datos para el registro de actividad
      const inventarioToDelete = inventarios.value.find(inv => inv.id === id) || 
                              (currentInventario.value && currentInventario.value.id === id ? 
                               currentInventario.value : null);
      
      // Enviar solicitud de eliminación
      await api.delete(`/inventarios/${id}`);
      
      // Actualizar el estado local
      inventarios.value = inventarios.value.filter(inv => inv.id !== id);
      
      // Si el inventario actual es el que se eliminó, limpiarlo
      if (currentInventario.value && currentInventario.value.id === id) {
        currentInventario.value = null;
      }
      
      // Registrar actividad
      if (inventarioToDelete) {
        await activityLogsStore.logActivity({
          action: 'DELETE',
          entityType: 'INVENTARIO',
          entityId: id,
          details: `Inventario "${inventarioToDelete.titulo}" eliminado`
        });
      }
      
      return true;
    } catch (err) {
      error.value = err.message;
      console.error('deleteInventario Error:', err);
      return false;
    } finally {
      loading.value = false;
    }
  }

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
  
  /**
   * Descarga un inventario en formato Excel o PDF
   * @param {Number} id - ID del inventario a descargar
   * @param {String} format - Formato de descarga ('excel' o 'pdf')
   * @returns {Promise<boolean>} - true si la descarga fue exitosa
   */
  async function downloadInventario(id, format = 'excel') {
    try {
      loading.value = true;
      
      // Validar formato
      if (!['excel', 'pdf'].includes(format)) {
        throw new Error('Formato no válido. Debe ser "excel" o "pdf"');
      }
      
      // Obtener el inventario para el registro de actividad
      const inventarioInfo = inventarios.value.find(inv => inv.id === id) || 
                          (currentInventario.value && currentInventario.value.id === id ? 
                          currentInventario.value : await fetchInventarioById(id));
      
      // Construir la URL de descarga
      const downloadUrl = getInventarioDownloadUrl(id, format);
      
      // Iniciar la descarga como blob
      const response = await apiService.fetch(downloadUrl, {
        method: 'GET',
        responseType: 'blob'
      });
      
      // Verificar si la respuesta es válida
      if (!response || !response.data) {
        throw new Error('No se pudo obtener el archivo del servidor');
      }
      
      // Crear un objeto URL para la descarga
      const blob = new Blob([response.data]);
      const url = window.URL.createObjectURL(blob);
      
      // Crear elemento a para la descarga
      const a = document.createElement('a');
      a.href = url;
      a.download = `Inventario_${id}_${inventarioInfo?.titulo || 'descarga'}.${format === 'excel' ? 'xlsx' : 'pdf'}`;
      document.body.appendChild(a);
      a.click();
      
      // Limpiar
      window.URL.revokeObjectURL(url);
      document.body.removeChild(a);
      
      // Registrar actividad
      if (inventarioInfo) {
        await activityLogsStore.logActivity({
          action: 'DOWNLOAD',
          entityType: 'INVENTARIO',
          entityId: id,
          details: `Inventario "${inventarioInfo.titulo}" descargado en formato ${format.toUpperCase()}`
        });
      }
      
      return true;
    } catch (err) {
      error.value = err.message;
      console.error('downloadInventario Error:', err);
      return false;
    } finally {
      loading.value = false;
    }
  }

  /**
   * Cambia el estado de un inventario
   * @param {Number} id - ID del inventario
   * @param {String} newState - Nuevo estado ('BORRADOR', 'FINALIZADO', 'APROBADO')
   * @returns {Promise} - Inventario actualizado
   */
  async function changeInventarioState(id, newState) {
    try {
      if (!inventarioStates.value.includes(newState)) {
        throw new Error(`Estado no válido. Debe ser uno de: ${inventarioStates.value.join(', ')}`);
      }
      
      loading.value = true;
      
      // Obtener datos actuales para el registro de actividad
      const currentData = currentInventario.value && currentInventario.value.id === id ? 
                         currentInventario.value : 
                         await fetchInventarioById(id);
      
      if (!currentData) {
        throw new Error('No se pudo obtener información del inventario');
      }
      
      const oldState = currentData.estado;
      
      // Enviar solicitud para cambiar el estado
      const updatedInventario = await api.put(`/inventarios/${id}/estado`, { estado: newState });
      
      // Actualizar la caché local
      if (updatedInventario) {
        // Actualizar inventario actual si corresponde
        if (currentInventario.value && currentInventario.value.id === id) {
          currentInventario.value = updatedInventario;
        }
        
        // Actualizar en la lista si existe
        const index = inventarios.value.findIndex(item => item.id === id);
        if (index !== -1) {
          inventarios.value[index] = updatedInventario;
        }
        
        // Registrar actividad
        await activityLogsStore.logActivity({
          action: 'UPDATE_STATE',
          entityType: 'INVENTARIO',
          entityId: id,
          details: `Estado del inventario "${currentData.titulo}" cambiado de ${oldState || 'ninguno'} a ${newState}`
        });
      }
      
      return updatedInventario;
    } catch (err) {
      error.value = err.message;
      console.error('changeInventarioState Error:', err);
      throw err;
    } finally {
      loading.value = false;
    }
  }

  /**
   * Agrega un detalle al inventario actual
   * @param {Number} inventarioId - ID del inventario
   * @param {Object} detalleData - Datos del detalle a agregar
   * @returns {Promise} - Detalle agregado
   */
  async function addInventarioDetalle(inventarioId, detalleData) {
    try {
      loading.value = true;
      
      // Enviar solicitud para agregar el detalle
      const addedDetalle = await api.post(`/inventarios/${inventarioId}/detalles`, detalleData);
      
      // Actualizar el inventario actual si es el mismo
      if (currentInventario.value && currentInventario.value.id === inventarioId) {
        // Refrescar el inventario completo para obtener los detalles actualizados
        await fetchInventarioById(inventarioId);
      }
      
      return addedDetalle;
    } catch (err) {
      error.value = err.message;
      console.error('addInventarioDetalle Error:', err);
      throw err;
    } finally {
      loading.value = false;
    }
  }

  /**
   * Actualiza un detalle de inventario
   * @param {Number} inventarioId - ID del inventario
   * @param {Number} detalleId - ID del detalle
   * @param {Object} detalleData - Datos actualizados del detalle
   * @returns {Promise} - Detalle actualizado
   */
  async function updateInventarioDetalle(inventarioId, detalleId, detalleData) {
    try {
      loading.value = true;
      
      // Enviar solicitud de actualización
      const updatedDetalle = await api.put(`/inventarios/${inventarioId}/detalles/${detalleId}`, detalleData);
      
      // Actualizar el inventario actual si es el mismo
      if (currentInventario.value && currentInventario.value.id === inventarioId) {
        // Refrescar el inventario completo para obtener los detalles actualizados
        await fetchInventarioById(inventarioId);
      }
      
      return updatedDetalle;
    } catch (err) {
      error.value = err.message;
      console.error('updateInventarioDetalle Error:', err);
      throw err;
    } finally {
      loading.value = false;
    }
  }

  /**
   * Elimina un detalle de inventario
   * @param {Number} inventarioId - ID del inventario
   * @param {Number} detalleId - ID del detalle a eliminar
   * @returns {Promise<boolean>} - true si se eliminó correctamente
   */
  async function deleteInventarioDetalle(inventarioId, detalleId) {
    try {
      loading.value = true;
      
      // Enviar solicitud de eliminación
      await api.delete(`/inventarios/${inventarioId}/detalles/${detalleId}`);
      
      // Actualizar el inventario actual si es el mismo
      if (currentInventario.value && currentInventario.value.id === inventarioId) {
        // Refrescar el inventario completo para obtener los detalles actualizados
        await fetchInventarioById(inventarioId);
      }
      
      return true;
    } catch (err) {
      error.value = err.message;
      console.error('deleteInventarioDetalle Error:', err);
      return false;
    } finally {
      loading.value = false;
    }
  }

  return {
    // State - Inventario General
    inventarios,
    currentInventario,
    
    // State - Registro de Transferencia
    registrosTransferencia,
    currentRegistroTransferencia,
    
    // State - Catálogo de Transferencia
    catalogosTransferencia,
    currentCatalogoTransferencia,
    
    // State - Comunes
    loading,
    error,
    pagination,
    sortOptions,
    inventarioStates,
    
    // Getters - Inventario General
    getInventarios,
    getCurrentInventario,
    
    // Getters - Registro de Transferencia
    getRegistrosTransferencia,
    getCurrentRegistroTransferencia,
    
    // Getters - Catálogo de Transferencia
    getCatalogosTransferencia,
    getCurrentCatalogoTransferencia,
    
    // Getters - Comunes
    isLoading,
    getError,
    getInventarioStates,
    getInventarioStateColor,
    getInventarioDownloadUrl,
    
    // Actions - Inventario General
    fetchInventarios,
    fetchPaginatedInventarios,
    searchInventariosPaginated,
    fetchInventarioById,
    createInventario,
    updateInventario,
    deleteInventario,
    downloadInventario,
    changeInventarioState,
    //fetchInventarioDetalle,
    //createInventarioDetalle,
    //updateInventarioDetalle,
    //deleteInventarioDetalle,
    
    // Actions - Registro de Transferencia
    fetchRegistrosTransferencia,
    fetchRegistroTransferenciaById,
    createRegistroTransferencia,
    updateRegistroTransferencia,
    deleteRegistroTransferencia,
    
    // Actions - Catálogo de Transferencia
    fetchCatalogosTransferencia,
    fetchCatalogoTransferenciaById,
    createCatalogoTransferencia,
    updateCatalogoTransferencia,
    deleteCatalogoTransferencia
  };
});
