import { defineStore } from 'pinia';
import { ref } from 'vue';
import { useActivityLogsStore } from './activityLogs';
import { api, apiService, API_BASE_URL } from '@/services/api';

export const useInventariosStore = defineStore('inventarios', () => {
    const inventarios = ref([]);
    const currentInventario = ref(null);
    const loading = ref(false);
    const error = ref(null);
    const activityLogsStore = useActivityLogsStore();

    const inventarioStates = ref(['BORRADOR', 'FINALIZADO', 'APROBADO']);

    // URL para descargar inventarios (corregido para usar endpoints exactos)
    const getInventarioDownloadUrl = (id, format) => {
        // Usar las rutas correctas según el controlador
        if (format === 'pdf') {
            return `${API_BASE_URL}/inventarios/general/${id}/pdf`;
        } else if (format === 'excel') {
            return `${API_BASE_URL}/inventarios/general/${id}/excel`;
        } else {
            return `${API_BASE_URL}/inventarios/general/${id}/download`;
        }
    };
    
    const getInventarios = () => inventarios.value || [];
    const getCurrentInventario = () => currentInventario.value;

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

    async function fetchInventarios() {
        try {
            loading.value = true;
            // Actualizado para usar la ruta correcta definida en el controlador
            const data = await api.get('/inventarios/general', { debug: true });
            // Asegurar que inventarios.value siempre sea un array
            inventarios.value = Array.isArray(data) ? data : [];
            console.log('fetchInventarios result:', inventarios.value);
            return { items: inventarios.value }; // Formato esperado por los componentes
        } catch (err) {
            error.value = err.message;
            console.error('fetchInventarios Error:', err);
            inventarios.value = [];
            return { items: [] }; // Devolver formato consistente incluso en error
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
      
      // Ruta actualizada para usar /inventarios/general con parámetros de paginación
      const response = await api.get('/inventarios/general', {
        params: {
          page,
          size,
          sortBy,
          sortDirection: direction,
          ...filters
        },
        debug: true
      });
      
      // Cuando la API devuelve la lista directamente sin paginación
      let content = [];
      let totalElements = 0;
      let totalPages = 0;
      
      // Verificar el formato de respuesta y adaptarlo
      if (Array.isArray(response)) {
        // La API devolvió un array directamente
        content = response;
        totalElements = response.length;
        totalPages = 1;
      } else if (response && typeof response === 'object') {
        if (Array.isArray(response.content)) {
          // Formato paginado estándar
          content = response.content;
          totalElements = response.totalElements || response.content.length;
          totalPages = response.totalPages || 1;
        } else {
          // Otro formato de objeto, intentamos extraer datos útiles
          content = Array.isArray(response.items) ? response.items : [];
          totalElements = response.totalItems || content.length;
          totalPages = response.totalPages || 1;
        }
      }
      
      console.log('fetchPaginatedInventarios content:', content);
      
      // Actualizar el estado con los resultados
      inventarios.value = content;
      pagination.value.totalItems = totalElements;
      pagination.value.totalPages = totalPages;
      
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
        unidadAdministrativa, // Cambiado de 'dependencia' a 'unidadAdministrativa' para coincidir con el DTO
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
      
      // Construir parámetros para la búsqueda
      const params = {};
      if (titulo) params.titulo = titulo;
      if (unidadAdministrativa) params.unidadAdministrativa = unidadAdministrativa;
      if (fechaInicio) params.fechaInicio = fechaInicio;
      if (fechaFin) params.fechaFin = fechaFin;
      if (estado) params.estado = estado;
      
      // Añadir parámetros de paginación
      params.page = page;
      params.size = size;
      params.sortBy = sortBy;
      params.sortDirection = direction;
      
      console.log('Searching with params:', params);
      
      // Usar la función get normal con parámetros
      const response = await api.get('/inventarios/general', {
        params,
        debug: true
      });
      
      // Manejar diferentes tipos de respuesta
      let items = [];
      let totalItems = 0;
      let totalPages = 0;
      
      if (Array.isArray(response)) {
        // API devolvió un array directo
        items = response;
        totalItems = response.length;
        totalPages = 1;
      } else if (response && typeof response === 'object') {
        if (Array.isArray(response.content)) {
          // Formato paginado estándar
          items = response.content;
          totalItems = response.totalElements || response.content.length;
          totalPages = response.totalPages || 1; 
        } else if (Array.isArray(response)) {
          // Es un array directamente
          items = response;
          totalItems = items.length;
          totalPages = 1;
        } else {
          // Intentar extraer los datos de otra manera
          items = [];
          totalItems = 0;
          totalPages = 0;
          console.error('Formato de respuesta desconocido:', response);
        }
      } else {
        console.error('Respuesta vacía o no válida');
      }
      
      // Actualizar el estado con los resultados
      inventarios.value = items;
      pagination.value.page = page;
      pagination.value.size = size;
      pagination.value.totalItems = totalItems;
      pagination.value.totalPages = totalPages;
      
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
          // Actualizado para usar la ruta correcta
          const data = await api.get(`/inventarios/general/${id}`);
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
        
        // Enviar la solicitud al servidor (ruta actualizada)
        const createdInventario = await api.post('/inventarios/general', inventarioData);
        
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
        
        // Enviar solicitud de actualización con la ruta correcta
        const updatedInventario = await api.put(`/inventarios/general/${id}`, inventarioData);
        
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
            
            // Enviar solicitud de eliminación con la ruta correcta
            await api.delete(`/inventarios/general/${id}`);
            
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

    return {
        inventarios,
        currentInventario,
        loading,
        error,
        pagination,
        sortOptions,
        inventarioStates,
        getInventarios,
        getCurrentInventario,
        isLoading,
        getError,
        getInventarioStates,
        getInventarioStateColor,
        fetchInventarios,
        fetchPaginatedInventarios,
        searchInventariosPaginated,
        fetchInventarioById,
        createInventario,
        updateInventario,
        deleteInventario,
        getInventarioDownloadUrl
    }

})