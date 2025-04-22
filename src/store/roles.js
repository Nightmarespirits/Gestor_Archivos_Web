import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import { useActivityLogsStore } from './activityLogs';

// Define la URL base de la API
const API_BASE_URL = import.meta.env.VITE_BASE_URL;

export const useRolesStore = defineStore('roles', () => {
  const roles = ref([]);
  const roleTypes = ref([]);
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

  // Obtener todos los roles
  async function fetchRoles() {
    loading.value = true;
    error.value = null;
    
    try {
      console.log('Obteniendo roles desde:', `${API_BASE_URL}/roles`);
      const response = await fetch(`${API_BASE_URL}/roles`, {
        method: 'GET',
        headers: getAuthHeaders(),
      });

      if (!response.ok) {
        throw new Error(`Error ${response.status}: ${response.statusText}`);
      }

      const data = await response.json();
      roles.value = data;
    } catch (err) {
      console.error('Error al obtener roles:', err);
      error.value = err.message;
    } finally {
      loading.value = false;
    }
  }

  // Obtener tipos de roles disponibles
  async function fetchRoleTypes() {
    loading.value = true;
    error.value = null;
    
    try {
      console.log('Obteniendo tipos de roles desde:', `${API_BASE_URL}/roles/types`);
      const response = await fetch(`${API_BASE_URL}/roles/types`, {
        method: 'GET',
        headers: getAuthHeaders(),
      });

      if (!response.ok) {
        throw new Error(`Error ${response.status}: ${response.statusText}`);
      }

      const data = await response.json();
      roleTypes.value = data;
    } catch (err) {
      console.error('Error al obtener tipos de roles:', err);
      error.value = err.message;
    } finally {
      loading.value = false;
    }
  }

  // Obtener un rol por su ID
  async function fetchRoleById(roleId) {
    loading.value = true;
    error.value = null;
    
    try {
      console.log(`Obteniendo rol con ID ${roleId} desde: ${API_BASE_URL}/roles/${roleId}`);
      
      const response = await fetch(`${API_BASE_URL}/roles/${roleId}`, {
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

      const roleData = await response.json();
      console.log('Datos de rol recibidos:', roleData);
      
      return roleData;
    } catch (err) {
      console.error(`Error al obtener rol con ID ${roleId}:`, err);
      error.value = err.message;
      return null;
    } finally {
      loading.value = false;
    }
  }

  // Crear un nuevo rol
  async function createRole(roleData) {
    loading.value = true;
    error.value = null;
    
    try {
      const response = await fetch(`${API_BASE_URL}/roles`, {
        method: 'POST',
        headers: getAuthHeaders(),
        body: JSON.stringify(roleData),
      });

      if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.message || `Error ${response.status}: ${response.statusText}`);
      }

      const newRole = await response.json();
      
      // Registrar la actividad
      await activityLogsStore.createActivityLog(
        'CREATE_ROLE',
        `Rol "${newRole.name}" creado`
      );

      await fetchRoles(); // Refrescar la lista de roles
      return newRole;
    } catch (err) {
      console.error('Error al crear rol:', err);
      error.value = err.message;
      throw err;
    } finally {
      loading.value = false;
    }
  }

  // Actualizar un rol existente
  async function updateRole(roleId, roleData) {
    loading.value = true;
    error.value = null;
    
    try {
      console.log(`Actualizando rol con ID ${roleId} en ${API_BASE_URL}/roles/${roleId}`, roleData);
      
      const response = await fetch(`${API_BASE_URL}/roles/${roleId}`, {
        method: 'PUT',
        headers: getAuthHeaders(),
        body: JSON.stringify(roleData),
      });

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

      const updatedRole = await response.json();
      
      // Registrar la actividad
      await activityLogsStore.createActivityLog(
        'UPDATE_ROLE',
        `Rol "${updatedRole.name}" actualizado`
      );

      await fetchRoles(); // Refrescar la lista de roles
      return updatedRole;
    } catch (err) {
      console.error(`Error al actualizar rol con ID ${roleId}:`, err);
      error.value = err.message;
      throw err;
    } finally {
      loading.value = false;
    }
  }

  // Eliminar un rol
  async function deleteRole(roleId) {
    loading.value = true;
    error.value = null;
    
    try {
      const response = await fetch(`${API_BASE_URL}/roles/${roleId}`, {
        method: 'DELETE',
        headers: getAuthHeaders(),
      });

      if (!response.ok) {
        throw new Error(`Error ${response.status}: ${response.statusText}`);
      }

      // Registrar la actividad
      await activityLogsStore.createActivityLog(
        'DELETE_ROLE',
        `Rol con ID ${roleId} eliminado`
      );

      await fetchRoles(); // Refrescar la lista de roles
      return true;
    } catch (err) {
      console.error('Error al eliminar rol:', err);
      error.value = err.message;
      throw err;
    } finally {
      loading.value = false;
    }
  }

  // Añadir permisos a un rol
  async function addPermissionsToRole(roleId, permissionIds) {
    loading.value = true;
    error.value = null;
    
    try {
      console.log(`Añadiendo permisos al rol con ID ${roleId}`, permissionIds);
      
      const response = await fetch(`${API_BASE_URL}/roles/${roleId}/permissions`, {
        method: 'POST',
        headers: getAuthHeaders(),
        body: JSON.stringify({ permissionIds }),
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

      const updatedRole = await response.json();
      
      // Registrar la actividad
      await activityLogsStore.createActivityLog(
        'ADD_PERMISSIONS_TO_ROLE',
        `Permisos añadidos al rol "${updatedRole.name}"`
      );

      await fetchRoles(); // Refrescar la lista de roles
      return updatedRole;
    } catch (err) {
      console.error(`Error al añadir permisos al rol con ID ${roleId}:`, err);
      error.value = err.message;
      throw err;
    } finally {
      loading.value = false;
    }
  }

  // Eliminar permisos de un rol
  async function removePermissionsFromRole(roleId, permissionIds) {
    loading.value = true;
    error.value = null;
    
    try {
      console.log(`Eliminando permisos del rol con ID ${roleId}`, permissionIds);
      
      const response = await fetch(`${API_BASE_URL}/roles/${roleId}/permissions`, {
        method: 'DELETE',
        headers: getAuthHeaders(),
        body: JSON.stringify({ permissionIds }),
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

      const updatedRole = await response.json();
      
      // Registrar la actividad
      await activityLogsStore.createActivityLog(
        'REMOVE_PERMISSIONS_FROM_ROLE',
        `Permisos eliminados del rol "${updatedRole.name}"`
      );

      await fetchRoles(); // Refrescar la lista de roles
      return updatedRole;
    } catch (err) {
      console.error(`Error al eliminar permisos del rol con ID ${roleId}:`, err);
      error.value = err.message;
      throw err;
    } finally {
      loading.value = false;
    }
  }

  return {
    roles,
    roleTypes,
    loading,
    error,
    fetchRoles,
    fetchRoleTypes,
    fetchRoleById,
    createRole,
    updateRole,
    deleteRole,
    addPermissionsToRole,
    removePermissionsFromRole
  };
});
