import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import { useActivityLogsStore } from './activityLogs';
import { api } from '@/services/api';

export const useRolesStore = defineStore('roles', () => {
  const roles = ref([]);
  const roleTypes = ref([]);
  const loading = ref(false);
  const error = ref(null);
  const activityLogsStore = useActivityLogsStore();

  // No necesitamos estas funciones ya que usaremos el servicio api.js

  // Obtener todos los roles
  async function fetchRoles() {
    loading.value = true;
    error.value = null;
    
    try {
      console.log('Obteniendo roles');
      const data = await api.get('/roles', { debug: true });
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
      console.log('Obteniendo tipos de roles');
      const data = await api.get('/roles/types');
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
      console.log(`Obteniendo rol con ID ${roleId}`);
      const roleData = await api.get(`/roles/${roleId}`, { debug: true });
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
      const newRole = await api.post('/roles', roleData);
      
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
      console.log(`Actualizando rol con ID ${roleId}`, roleData);
      
      const updatedRole = await api.put(`/roles/${roleId}`, roleData, {
        debug: true
      });
      
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
      await api.delete(`/roles/${roleId}`);

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
      
      const updatedRole = await api.post(`/roles/${roleId}/permissions`, { permissionIds }, {
        debug: true
      });
      
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
      
      const updatedRole = await api.delete(`/roles/${roleId}/permissions`, {
        body: { permissionIds },
        debug: true
      });
      
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
