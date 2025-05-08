import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import { useActivityLogsStore } from './activityLogs';
import { api } from '@/services/api';

export const usePermissionsStore = defineStore('permissions', () => {
  const permissions = ref([]);
  const permissionTypes = ref([]);
  const loading = ref(false);
  const error = ref(null);
  const activityLogsStore = useActivityLogsStore();

  // No necesitamos estas funciones ya que usaremos el servicio api.js

  // Obtener todos los permisos
  async function fetchPermissions() {
    loading.value = true;
    error.value = null;
    
    try {
      console.log('Obteniendo permisos');
      const data = await api.get('/permissions', { debug: true });
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
      console.log('Obteniendo tipos de permisos');
      const data = await api.get('/permissions/types');
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
      console.log(`Obteniendo permiso con ID ${permissionId}`);
      const permissionData = await api.get(`/permissions/${permissionId}`, { debug: true });
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
      const newPermission = await api.post('/permissions', permissionData);
      
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
      console.log(`Actualizando permiso con ID ${permissionId}`, permissionData);
      
      const updatedPermission = await api.put(`/permissions/${permissionId}`, permissionData, {
        debug: true
      });
      
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
      await api.delete(`/permissions/${permissionId}`);

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
