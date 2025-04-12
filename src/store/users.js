import { defineStore } from 'pinia';
import { ref, computed } from 'vue';

// Define la URL base de la API
const API_BASE_URL = import.meta.env.VITE_BASE_URL;

export const useUsersStore = defineStore('users', () => {
  const users = ref([]);
  const roles = ref([]);
  const loading = ref(false);
  const error = ref(null);

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

  // Obtener todos los usuarios
  async function fetchUsers() {
    loading.value = true;
    error.value = null;
    
    try {
      const response = await fetch(`${API_BASE_URL}/users`, {
        method: 'GET',
        headers: getAuthHeaders(),
      });

      if (!response.ok) {
        throw new Error(`Error ${response.status}: ${response.statusText}`);
      }

      const data = await response.json();
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
      const response = await fetch(`${API_BASE_URL}/users/${userId}`, {
        method: 'GET',
        headers: getAuthHeaders(),
      });

      if (!response.ok) {
        throw new Error(`Error ${response.status}: ${response.statusText}`);
      }

      return await response.json();
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
      const response = await fetch(`${API_BASE_URL}/auth/register`, {
        method: 'POST',
        headers: getAuthHeaders(),
        body: JSON.stringify(userData),
      });

      if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.message || `Error ${response.status}: ${response.statusText}`);
      }

      const newUser = await response.json();
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
      const response = await fetch(`${API_BASE_URL}/users/${userId}`, {
        method: 'PUT',
        headers: getAuthHeaders(),
        body: JSON.stringify(userData),
      });

      if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.message || `Error ${response.status}: ${response.statusText}`);
      }

      const updatedUser = await response.json();
      await fetchUsers(); // Refrescar la lista de usuarios
      return updatedUser;
    } catch (err) {
      console.error(`Error al actualizar usuario con ID ${userId}:`, err);
      error.value = err.message;
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
      const response = await fetch(`${API_BASE_URL}/users/${userId}`, {
        method: 'DELETE',
        headers: getAuthHeaders(),
      });

      if (!response.ok) {
        throw new Error(`Error ${response.status}: ${response.statusText}`);
      }

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
      const response = await fetch(`${API_BASE_URL}/roles`, {
        method: 'GET',
        headers: getAuthHeaders(),
      });

      if (!response.ok) {
        throw new Error(`Error ${response.status}: ${response.statusText}`);
      }

      const data = await response.json();
      roles.value = data;
      return data;
    } catch (err) {
      console.error('Error al obtener roles:', err);
      error.value = err.message;
      // En caso de error al obtener roles, creamos unos por defecto básicos
      roles.value = [
        { id: 1, name: 'Administrador' },
        { id: 2, name: 'Usuario' }
      ];
      return roles.value;
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
