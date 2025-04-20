import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import { useActivityLogsStore } from './activityLogs';

// Define la URL base de la API
const API_BASE_URL = import.meta.env.VITE_BASE_URL;

export const useUsersStore = defineStore('users', () => {
  const users = ref([]);
  const roles = ref([]);
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

  // Obtener todos los usuarios
  async function fetchUsers() {
    loading.value = true;
    error.value = null;
    
    try {
      console.log('Obteniendo usuarios desde:', `${API_BASE_URL}/users`);
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
      console.log(`Obteniendo usuario con ID ${userId} desde: ${API_BASE_URL}/users/${userId}`);
      
      const response = await fetch(`${API_BASE_URL}/users/${userId}`, {
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

      const userData = await response.json();
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
      console.log(`Actualizando usuario con ID ${userId} en ${API_BASE_URL}/users/${userId}`, userData);
      
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
      
      // Verificar la conexión antes de intentar la actualización
      try {
        const testConnection = await fetch(`${API_BASE_URL}/users/${userId}`, {
          method: 'GET',
          headers: getAuthHeaders()
        });
        console.log('Estado de conexión a la API:', testConnection.status, testConnection.statusText);
      } catch (connErr) {
        console.error('Error al comprobar la conexión a la API:', connErr);
      }
      
      // Realizar la solicitud de actualización
      const response = await fetch(`${API_BASE_URL}/users/${userId}`, {
        method: 'PUT',
        headers: getAuthHeaders(),
        body: JSON.stringify(dataToSend),
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
            const textError = await response.text();
            if (textError && textError.trim()) {
              errorMessage = textError;
            }
            console.log('Texto de respuesta no-JSON:', textError);
          }
        } catch (parseErr) {
          console.error('Error al procesar respuesta de error:', parseErr);
        }
        
        // Si todo falla, proporcionar un mensaje de error específico basado en el código HTTP
        if (response.status === 404) {
          errorMessage = `No se encontró el usuario con ID ${userId}. Posiblemente ha sido eliminado o no existe.`;
        } else if (response.status === 403) {
          errorMessage = 'No tienes permisos suficientes para actualizar este usuario.';
        } else if (response.status === 400) {
          errorMessage = 'Datos de actualización inválidos. Verifica la información proporcionada.';
        } else if (response.status >= 500) {
          errorMessage = 'Error del servidor. Intenta nuevamente más tarde.';
        }
        
        console.error(`Error al actualizar usuario (${response.status}):`, errorMessage);
        throw new Error(errorMessage);
      }

      // Parsear la respuesta JSON
      let updatedUser;
      try {
        const responseText = await response.text();
        console.log('Texto de respuesta exitosa:', responseText);
        
        if (responseText && responseText.trim()) {
          updatedUser = JSON.parse(responseText);
          console.log('Usuario actualizado (desde respuesta):', updatedUser);
        } else {
          console.log('Respuesta vacía del servidor');
        }
      } catch (jsonErr) {
        console.error('Error al parsear respuesta JSON exitosa:', jsonErr);
      }
      
      // Si no se recibió una respuesta con datos, hacer una solicitud adicional para obtener el usuario actualizado
      if (!updatedUser) {
        console.log('Obteniendo datos actualizados del usuario...');
        try {
          updatedUser = await fetchUserById(userId);
          console.log('Usuario actualizado (desde fetchUserById):', updatedUser);
        } catch (fetchErr) {
          console.error('Error al obtener usuario actualizado:', fetchErr);
          // Si falla, crear un objeto con los datos enviados para tener algo que devolver
          updatedUser = { id: userId, ...dataToSend };
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
      const response = await fetch(`${API_BASE_URL}/users/${userId}`, {
        method: 'DELETE',
        headers: getAuthHeaders(),
      });

      if (!response.ok) {
        throw new Error(`Error ${response.status}: ${response.statusText}`);
      }

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
    
    return new Promise((resolve, reject) => {
      try {
        console.log('Obteniendo roles desde:', `${API_BASE_URL}/roles`);
        
        // Usar XMLHttpRequest en lugar de fetch para tener más control
        const xhr = new XMLHttpRequest();
        xhr.open('GET', `${API_BASE_URL}/roles`, true);
        
        // Configurar headers
        const headers = getAuthHeaders();
        Object.keys(headers).forEach(key => {
          xhr.setRequestHeader(key, headers[key]);
        });
        
        // No enviar credenciales de forma explícita
        xhr.withCredentials = false;
        
        xhr.onload = function() {
          if (xhr.status >= 200 && xhr.status < 300) {
            try {
              const data = JSON.parse(xhr.responseText);
              console.log('Roles obtenidos correctamente:', data);
              roles.value = data;
              loading.value = false;
              resolve(data);
            } catch (parseError) {
              console.error('Error al parsear la respuesta JSON:', parseError);
              error.value = 'Error en formato de respuesta';
              loading.value = false;
              reject(parseError);
            }
          } else {
            console.error(`Error del servidor (${xhr.status}):`, xhr.responseText);
            error.value = `Error al obtener roles: ${xhr.status}`;
            loading.value = false;
            reject(new Error(`Error al obtener roles: ${xhr.status}`));
          }
        };
        
        xhr.onerror = function() {
          console.error('Error de red al obtener roles');
          error.value = 'Error de red al conectar con el servidor';
          loading.value = false;
          reject(new Error('Error de red al obtener roles'));
        };
        
        xhr.send();
      } catch (err) {
        console.error('Error al obtener roles:', err);
        error.value = err.message;
        loading.value = false;
        reject(err);
      }
    });
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
