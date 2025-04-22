import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import { useActivityLogsStore } from './activityLogs';

// Define la URL base de la API
const API_BASE_URL = import.meta.env.VITE_BASE_URL;

export const usePermissionsStore = defineStore('permissions', () => {
  const permissions = ref([]);
  const permissionTypes = ref([]);
  const loading = ref(false);
  const error = ref(null);
  const activityLogsStore = useActivityLogsStore();

  // Obtener token de autenticación del localStorage
  const getAuthToken = () => {
    const token = localStorage.getItem('authToken');
    if (!token) {
      console.warn('No se encontró token de autenticación');
    }
    return token;
  };

  // Función para crear headers de autenticación
  const getAuthHeaders = () => {
    const token = getAuthToken();
    return {
      'Authorization': token ? `Bearer ${token}` : '',
      'Content-Type': 'application/json',
    };
  };

  // Obtener todos los permisos
  async function fetchPermissions() {
    loading.value = true;
    error.value = null;
    
    try {
      console.log('Obteniendo permisos desde:', `${API_BASE_URL}/permissions`);
      const response = await fetch(`${API_BASE_URL}/permissions`, {
        method: 'GET',
        headers: getAuthHeaders(),
      });

      if (!response.ok) {
        throw new Error(`Error ${response.status}: ${response.statusText}`);
      }

      const data = await response.json();
      permissions.value = data;
    } catch (err) {
      console.error('Error al obtener permisos:', err);
      error.value = err.message;
    } finally {
      loading.value = false;
    }
  }

  // Obtener tipos de permisos disponibles
  async function fetchPermissionTypes() {
    loading.value = true;
    error.value = null;
    
    try {
      console.log('Obteniendo tipos de permisos desde:', `${API_BASE_URL}/permissions/types`);
      const response = await fetch(`${API_BASE_URL}/permissions/types`, {
        method: 'GET',
        headers: getAuthHeaders(),
      });

      if (!response.ok) {
        throw new Error(`Error ${response.status}: ${response.statusText}`);
      }

      const data = await response.json();
      permissionTypes.value = data;
    } catch (err) {
      console.error('Error al obtener tipos de permisos:', err);
      error.value = err.message;
    } finally {
      loading.value = false;
    }
  }

  // Obtener un permiso por su ID
  async function fetchPermissionById(permissionId) {
    loading.value = true;
    error.value = null;
    
    try {
      console.log(`Obteniendo permiso con ID ${permissionId} desde: ${API_BASE_URL}/permissions/${permissionId}`);
      
      const response = await fetch(`${API_BASE_URL}/permissions/${permissionId}`, {
        method: 'GET',
        headers: getAuthHeaders(),
      });

      if (!response.ok) {
        let errorMessage = `Error ${response.status}: ${response.statusText}`;
        const contentType = response.headers.get('content-type');
        
        if (contentType && contentType.includes('application/json')) {
          try {
            const errorData = await response.json();
            if (errorData && errorData.message) {
              errorMessage = errorData.message;
            }
          } catch (e) {
            console.error('Error al parsear respuesta JSON de error:', e);
          }
        }
        
        throw new Error(errorMessage);
      }

      const permissionData = await response.json();
      console.log('Datos de permiso recibidos:', permissionData);
      
      return permissionData;
    } catch (err) {
      console.error(`Error al obtener permiso con ID ${permissionId}:`, err);
      error.value = err.message;
      return null;
    } finally {
      loading.value = false;
    }
  }

  // Crear un nuevo permiso
  async function createPermission(permissionData) {
    loading.value = true;
    error.value = null;
    
    try {
      const response = await fetch(`${API_BASE_URL}/permissions`, {
        method: 'POST',
        headers: getAuthHeaders(),
        body: JSON.stringify(permissionData),
      });

      if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.message || `Error ${response.status}: ${response.statusText}`);
      }

      const newPermission = await response.json();
      
      // Registrar la actividad
      await activityLogsStore.createActivityLog(
        'CREATE_PERMISSION',
        `Permiso "${newPermission.name}" creado`
      );

      await fetchPermissions(); // Refrescar la lista de permisos
      return newPermission;
    } catch (err) {
      console.error('Error al crear permiso:', err);
      error.value = err.message;
      throw err;
    } finally {
      loading.value = false;
    }
  }

  // Actualizar un permiso existente
  async function updatePermission(permissionId, permissionData) {
    loading.value = true;
    error.value = null;
    
    try {
      console.log(`Actualizando permiso con ID ${permissionId} en ${API_BASE_URL}/permissions/${permissionId}`, permissionData);
      
      const response = await fetch(`${API_BASE_URL}/permissions/${permissionId}`, {
        method: 'PUT',
        headers: getAuthHeaders(),
        body: JSON.stringify(permissionData),
      });

      console.log('Respuesta de actualización:', response.status, response.statusText);
      
      if (!response.ok) {
        let errorMessage = `Error ${response.status}: ${response.statusText}`;
        const contentType = response.headers.get('content-type');
        
        try {
          if (contentType && contentType.includes('application/json')) {
            const responseText = await response.text();
            console.log('Texto de respuesta de error:', responseText);
            
            if (responseText && responseText.trim()) {
              try {
                const errorData = JSON.parse(responseText);
                if (errorData && errorData.message) {
                  errorMessage = errorData.message;
                } else {
                  errorMessage = JSON.stringify(errorData);
                }
              } catch (jsonErr) {
                errorMessage = responseText;
                console.error('Error al parsear JSON de error:', jsonErr);
              }
            }
          } else {
            errorMessage = await response.text() || errorMessage;
          }
        } catch (textErr) {
          console.error('Error al leer la respuesta de error:', textErr);
        }
        
        throw new Error(errorMessage);
      }

      const updatedPermission = await response.json();
      
      // Registrar la actividad
      await activityLogsStore.createActivityLog(
        'UPDATE_PERMISSION',
        `Permiso "${updatedPermission.name}" actualizado`
      );

      await fetchPermissions(); // Refrescar la lista de permisos
      return updatedPermission;
    } catch (err) {
      console.error(`Error al actualizar permiso con ID ${permissionId}:`, err);
      error.value = err.message;
      throw err;
    } finally {
      loading.value = false;
    }
  }

  // Eliminar un permiso
  async function deletePermission(permissionId) {
    loading.value = true;
    error.value = null;
    
    try {
      const response = await fetch(`${API_BASE_URL}/permissions/${permissionId}`, {
        method: 'DELETE',
        headers: getAuthHeaders(),
      });

      if (!response.ok) {
        throw new Error(`Error ${response.status}: ${response.statusText}`);
      }

      // Registrar la actividad
      await activityLogsStore.createActivityLog(
        'DELETE_PERMISSION',
        `Permiso con ID ${permissionId} eliminado`
      );

      await fetchPermissions(); // Refrescar la lista de permisos
      return true;
    } catch (err) {
      console.error('Error al eliminar permiso:', err);
      error.value = err.message;
      throw err;
    } finally {
      loading.value = false;
    }
  }

  return {
    permissions,
    permissionTypes,
    loading,
    error,
    fetchPermissions,
    fetchPermissionTypes,
    fetchPermissionById,
    createPermission,
    updatePermission,
    deletePermission
  };
});
