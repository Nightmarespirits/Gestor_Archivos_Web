// Define la URL base de la API
const API_BASE_URL = import.meta.env.VITE_BASE_URL;

// Obtener token de autenticación del localStorage
const getAuthToken = () => {
  const token = localStorage.getItem('authToken');
  if (!token) {
    console.warn('No se encontró token de autenticación');
  }
  return token;
};

// Función para crear headers de autenticación
const getAuthHeaders = (isFormData = false) => {
  const token = getAuthToken();
  const headers = {
    'Authorization': token ? `Bearer ${token}` : '',
  };

  if (!isFormData) {
    headers['Content-Type'] = 'application/json';
  }

  return headers;
};

export const fetchApi = async (endpoint, options = {}) => {
  const { method = 'GET', body, isFormData} = options;

  try {
    const url = `${API_BASE_URL}${endpoint}`;

    console.log('URL de la petición:', url);
    console.log('Método:', method);
    console.log('Datos enviados:', isFormData ? 'FormData Object' : body);

    const headers = getAuthHeaders(isFormData);
    const config = {
      method,
      headers,
    };

    // Solo agregar body si no es GET
    if (method !== 'GET' && body) {
      config.body = isFormData ? body : JSON.stringify(body);
    }

    console.log('Configuración completa:', {
      url,
      method,
      headers,
      bodyType: body ? (isFormData ? 'FormData' : 'JSON') : 'none'
    });

    const response = await fetch(url, config);
    
    // Log detallado de la respuesta
    console.log('Respuesta del servidor:', {
      status: response.status,
      statusText: response.statusText,
      headers: Object.fromEntries(response.headers.entries())
    });

    // Si la respuesta no es exitosa, intentar obtener el mensaje de error detallado
    if (!response.ok) {
      const contentType = response.headers.get('content-type');
      let errorMessage = `Error ${response.status}: ${response.statusText}`;

      if (contentType && contentType.includes('application/json')) {
        try {
          const errorData = await response.json();
          errorMessage = errorData.message || errorData.error || errorMessage;
          console.error('Error detallado del servidor:', errorData);
        } catch (e) {
          console.error('Error al parsear respuesta JSON de error:', e);
        }
      } else {
        try {
          const textError = await response.text();
          errorMessage = textError || errorMessage;
          console.error('Error en texto plano del servidor:', textError);
        } catch (e) {
          console.error('Error al leer respuesta de texto:', e);
        }
      }

      throw new Error(errorMessage);
    }

    // Si la respuesta es exitosa pero no hay contenido (204)
    if (response.status === 204) {
      return null;
    }

    // Para respuestas exitosas con contenido
    const contentType = response.headers.get('content-type');
    if (contentType && contentType.includes('application/json')) {
      try {
        const data = await response.json();
        console.log('Datos recibidos:', data);
        return data;
      } catch (e) {
        console.error('Error al parsear respuesta JSON:', e);
        throw new Error('Error al procesar la respuesta del servidor');
      }
    } else {
      const textResponse = await response.text();
      console.log('Respuesta en texto plano:', textResponse);
      return textResponse;
    }
  } catch (error) {
    console.error('Error en fetchApi:', {
      message: error.message,
      stack: error.stack
    });
    throw error;
  }
};

/**
 * Imprime información de depuración sobre una solicitud API
 * @param {string} endpoint - El endpoint que se está llamando
 * @param {Object} headers - Los headers que se están enviando
 * @param {Object} authStore - La instancia del store de autenticación
 */
export function debugApiCall(endpoint, headers, authStore) {
  console.group(`🔍 API Debug: ${endpoint}`);
  console.log('🔑 Token presente:', !!authStore.token);
  if (authStore.token) {
    // Solo mostrar los primeros y últimos caracteres del token por seguridad
    const tokenPreview = authStore.token.length > 10 
      ? `${authStore.token.substring(0, 5)}...${authStore.token.substring(authStore.token.length - 5)}`
      : '(token muy corto)';
    console.log(`🔑 Token preview: ${tokenPreview}`);
  }
  console.log('👤 Usuario autenticado:', !!authStore.user);
  if (authStore.user) {
    console.log('👤 Rol de usuario:', authStore.user.role?.name);
  }
  console.log('📤 Headers:', headers);
  console.groupEnd();
}

/**
 * Crea headers para las solicitudes API con autenticación
 * @param {Object} authStore - La instancia del store de autenticación
 * @param {boolean} isFormData - Si la solicitud contiene FormData
 * @returns {Object} - Los headers para la solicitud
 */
export function createAuthHeaders(authStore, isFormData = false) {
  const headers = {};
  
  if (authStore.token) {
    // Probar diferentes formatos de autorización
    headers['Authorization'] = `Bearer ${authStore.token}`;
    // Algunos backends esperan el token sin el prefijo "Bearer"
    headers['X-Auth-Token'] = authStore.token;
  }
  
  if (!isFormData) {
    headers['Content-Type'] = 'application/json';
    headers['Accept'] = 'application/json';
  }
  
  return headers;
}
