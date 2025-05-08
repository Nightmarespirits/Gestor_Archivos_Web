import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import { useActivityLogsStore } from './activityLogs';
import { api } from '@/services/api';

export const useUsersStore = defineStore('users', () => {
  const users = ref([]);
  const roles = ref([]);
  const loading = ref(false);
  const error = ref(null);
  const activityLogsStore = useActivityLogsStore();

  // No necesitamos estas funciones ya que usaremos el servicio api.js

  // Obtener todos los usuarios
  async function fetchUsers() {
    loading.value = true;
    error.value = null;
    
    try {
      const data = await api.get('/users', { debug: true });
      users.value = data;
    } catch (err) {
      console.error('Error al obtener usuarios:', err);
      error.value = err.message;
    } finally {
      loading.value = false;
    }
  }

  // Obtener un usuario por su ID
  async function fetchUserById(userId) {
    loading.value = true;
    error.value = null;
    
    try {
      console.log(`Obteniendo usuario con ID ${userId}`);
      
      const userData = await api.get(`/users/${userId}`, { debug: true });
      console.log('Datos de usuario recibidos:', userData);
      
      return userData;
    } catch (err) {
      console.error(`Error al obtener usuario con ID ${userId}:`, err);
      error.value = err.message;
      return null;
    } finally {
      loading.value = false;
    }
  }

  // Crear un nuevo usuario
  async function createUser(userData) {
    loading.value = true;
    error.value = null;
    
    try {
      const newUser = await api.post('/auth/register', userData);
      
      // Registrar la actividad
      await activityLogsStore.createActivityLog(
        'CREATE_USER',
        `Usuario "${newUser.username}" creado`
      );

      await fetchUsers(); // Refrescar la lista de usuarios
      return newUser;
    } catch (err) {
      console.error('Error al crear usuario:', err);
      error.value = err.message;
      throw err;
    } finally {
      loading.value = false;
    }
  }

  // Actualizar un usuario existente
  async function updateUser(userId, userData) {
    loading.value = true;
    error.value = null;
    
    try {
      console.log(`Actualizando usuario con ID ${userId}`, userData);
      
      // Preparar los datos según la documentación de la API
      const dataToSend = {
        fullName: userData.fullName,
        email: userData.email,
        status: userData.status !== undefined ? userData.status : true,
        roleId: userData.roleId
      };
      
      // Añadir contraseña solo si se ha especificado una
      if (userData.password) {
        dataToSend.password = userData.password;
      }
      
      console.log('Datos enviados para actualización:', JSON.stringify(dataToSend));
      
      let updatedUser;
      try {
        // Usar el servicio API para actualizar el usuario
        updatedUser = await api.put(`/users/${userId}`, dataToSend, { debug: true });
        console.log('Usuario actualizado (desde respuesta):', updatedUser);
      } catch (apiErr) {
        console.error('Error al actualizar usuario a través de API:', apiErr);
        
        // Si la actualización falla pero no hay excepción, intentar obtener los datos actualizados
        console.log('Obteniendo datos actualizados del usuario...');
        try {
          updatedUser = await fetchUserById(userId);
          console.log('Usuario actualizado (desde fetchUserById):', updatedUser);
        } catch (fetchErr) {
          console.error('Error al obtener usuario actualizado:', fetchErr);
          // Si falla, crear un objeto con los datos enviados para tener algo que devolver
          updatedUser = { id: userId, ...dataToSend };
          throw apiErr; // Re-lanzar el error original
        }
      }
      
      // Registrar la actividad
      await activityLogsStore.createActivityLog(
        'UPDATE_USER',
        `Usuario "${updatedUser.username}" actualizado`
      );

      // Refrescar la lista de usuarios
      try {
        await fetchUsers();
      } catch (fetchErr) {
        console.error('Error al refrescar la lista de usuarios:', fetchErr);
      }
      
      return updatedUser;
    } catch (err) {
      const errorMessage = err.message || 'Error desconocido al actualizar el usuario';
      console.error(`Error al actualizar usuario con ID ${userId}:`, errorMessage);
      error.value = errorMessage;
      throw err;
    } finally {
      loading.value = false;
    }
  }

  // Eliminar un usuario
  async function deleteUser(userId) {
    loading.value = true;
    error.value = null;
    
    try {
      const user = await fetchUserById(userId);
      await api.delete(`/users/${userId}`);

      // Registrar la actividad
      await activityLogsStore.createActivityLog(
        'DELETE_USER',
        `Usuario "${user.username}" eliminado`
      );

      await fetchUsers(); // Refrescar la lista de usuarios
      return true;
    } catch (err) {
      console.error(`Error al eliminar usuario con ID ${userId}:`, err);
      error.value = err.message;
      throw err;
    } finally {
      loading.value = false;
    }
  }

  // Obtener los roles disponibles
  async function fetchRoles() {
    loading.value = true;
    error.value = null;
    
    try {
      console.log('Obteniendo roles');
      const data = await api.get('/roles', { debug: true });
      roles.value = data;
      return data;
    } catch (err) {
      console.error('Error al obtener roles:', err);
      error.value = err.message;
      throw err;
    } finally {
      loading.value = false;
    }
  }

  return {
    users,
    roles,
    loading,
    error,
    fetchUsers,
    fetchUserById,
    createUser,
    updateUser,
    deleteUser,
    fetchRoles
  };
});
