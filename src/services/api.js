/**
 * Servicio centralizado para manejar todas las llamadas a la API
 * Este servicio implementa métodos para estandarizar las solicitudes HTTP
 * y manejar errores de forma consistente
 */

// URL base de la API desde variables de entorno
export const API_BASE_URL = import.meta.env.VITE_BASE_URL;

/**
 * Servicio de API que proporciona métodos para interactuar con el backend
 * Compatible con la documentación Swagger de la API
 */
export const apiService = {
  /**
   * Obtiene los headers de autorización necesarios para las llamadas a la API
   * @param {boolean} isFormData - Indica si la solicitud incluye FormData
   * @returns {Object} - Headers de la solicitud
   */
  getAuthHeaders(isFormData = false) {
    const token = localStorage.getItem('authToken');
    const headers = {};
    
    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }
    
    if (!isFormData) {
      headers['Content-Type'] = 'application/json';
    }
    
    return headers;
  },
  
  /**
   * Registra información de depuración sobre una llamada a la API
   * @param {string} endpoint - El endpoint que se está llamando
   * @param {Object} requestConfig - Configuración de la solicitud
   * @param {boolean} includeToken - Si se debe incluir información del token (truncada por seguridad)
   */
  logApiCall(endpoint, requestConfig, includeToken = false) {
    console.group(`🔍 API Debug: ${endpoint}`);
    
    if (includeToken) {
      const token = localStorage.getItem('authToken');
      if (token) {
        const tokenPreview = token.length > 10 
          ? `${token.substring(0, 5)}...${token.substring(token.length - 5)}`
          : '(token muy corto)';
        console.log(`🔑 Token preview: ${tokenPreview}`);
      }
    }
    
    console.log('📤 Config:', { 
      method: requestConfig.method, 
      headers: requestConfig.headers,
      body: requestConfig.body ? '[CONTENIDO]' : undefined
    });
    console.groupEnd();
  },
  
  /**
   * Método principal para realizar llamadas a la API
   * @param {string} endpoint - Endpoint a llamar (sin la URL base)
   * @param {Object} options - Opciones de la solicitud
   * @param {string} options.method - Método HTTP (GET, POST, PUT, DELETE)
   * @param {Object} options.body - Cuerpo de la solicitud
   * @param {boolean} options.isFormData - Si el cuerpo es FormData
   * @param {Object} options.params - Parámetros de query string
   * @param {boolean} options.debug - Si se debe registrar información de depuración
   * @returns {Promise} - Promesa con la respuesta
   */
  async fetch(endpoint, options = {}) {
    const { 
      method = 'GET', 
      body, 
      isFormData = false, 
      params,
      debug = false
    } = options;
    
    // Construir URL con parámetros de consulta
    let url = `${API_BASE_URL}${endpoint}`;
    if (params) {
      const queryParams = new URLSearchParams();
      Object.keys(params).forEach(key => {
        if (params[key] !== undefined && params[key] !== null) {
          queryParams.append(key, params[key]);
        }
      });
      const queryString = queryParams.toString();
      if (queryString) {
        url = `${url}?${queryString}`;
      }
    }
    
    // Configurar headers y solicitud
    const headers = this.getAuthHeaders(isFormData);
    const config = { method, headers };
    
    if (method !== 'GET' && body) {
      config.body = isFormData ? body : JSON.stringify(body);
    }
    
    // Registrar información de depuración si está habilitado
    if (debug) {
      this.logApiCall(endpoint, config, true);
    }
    
    try {
      // Ejecutar solicitud
      const response = await fetch(url, config);
      
      if (!response.ok) {
        const error = await this.handleErrorResponse(response);
        throw error;
      }
      
      // Manejar respuesta exitosa
      const data = await this.handleSuccessResponse(response);
      
      if (debug) {
        console.log('✅ API Response:', data);
      }
      
      return data;
    } catch (error) {
      if (debug) {
        console.error('❌ API Error:', error.message);
      }
      throw error;
    }
  },
  
  /**
   * Maneja respuestas de error de la API
   * @param {Response} response - Objeto Response de fetch
   * @returns {Error} - Error con información detallada
   */
  async handleErrorResponse(response) {
    try {
      const contentType = response.headers.get('content-type');
      
      if (contentType && contentType.includes('application/json')) {
        const errorData = await response.json();
        return new Error(errorData.message || errorData.error || `Error ${response.status}: ${response.statusText}`);
      } else {
        const text = await response.text();
        return new Error(`Error ${response.status}: ${text || response.statusText}`);
      }
    } catch (e) {
      return new Error(`Error ${response.status}: ${response.statusText}`);
    }
  },
  
  /**
   * Maneja respuestas exitosas de la API
   * @param {Response} response - Objeto Response de fetch
   * @returns {Object|string} - Datos de respuesta
   */
  async handleSuccessResponse(response) {
    const contentType = response.headers.get('content-type');
    
    if (contentType && contentType.includes('application/json')) {
      return await response.json();
    } else {
      return await response.text();
    }
  },
  
  /**
   * Método especializado para manejar paginación desde el servidor
   * @param {string} endpoint - Endpoint a llamar
   * @param {Object} options - Opciones de paginación
   * @returns {Promise} - Datos paginados
   */
  fetchPaginated(endpoint, options = {}) {
    const { 
      page = 1, 
      pageSize = 20, 
      sortBy = null,
      sortDirection = 'asc',
      filters = {},
      ...otherOptions 
    } = options;
    
    // Construir parámetros de paginación
    const params = {
      page,
      size: pageSize,
      ...filters
    };
    
    if (sortBy) {
      params.sort = `${sortBy},${sortDirection}`;
    }
    
    return this.fetch(endpoint, { 
      ...otherOptions, 
      params 
    });
  }
};

// Métodos de conveniencia para operaciones HTTP comunes
export const api = {
  /**
   * Realiza una solicitud GET
   * @param {string} endpoint - Endpoint a consultar
   * @param {Object} options - Opciones de la solicitud
   */
  get(endpoint, options = {}) {
    return apiService.fetch(endpoint, { ...options, method: 'GET' });
  },
  
  /**
   * Realiza una solicitud POST
   * @param {string} endpoint - Endpoint para enviar datos
   * @param {Object} data - Datos a enviar
   * @param {Object} options - Opciones adicionales
   */
  post(endpoint, data, options = {}) {
    return apiService.fetch(endpoint, { 
      ...options, 
      method: 'POST',
      body: data
    });
  },
  
  /**
   * Realiza una solicitud PUT
   * @param {string} endpoint - Endpoint para actualizar
   * @param {Object} data - Datos a enviar
   * @param {Object} options - Opciones adicionales
   */
  put(endpoint, data, options = {}) {
    return apiService.fetch(endpoint, { 
      ...options, 
      method: 'PUT',
      body: data
    });
  },
  
  /**
   * Realiza una solicitud DELETE
   * @param {string} endpoint - Endpoint para eliminar
   * @param {Object} options - Opciones adicionales
   */
  delete(endpoint, options = {}) {
    return apiService.fetch(endpoint, { 
      ...options, 
      method: 'DELETE' 
    });
  },
  
  /**
   * Obtiene datos con paginación
   * @param {string} endpoint - Endpoint a consultar
   * @param {Object} paginationOptions - Opciones de paginación
   */
  getPaginated(endpoint, paginationOptions = {}) {
    return apiService.fetchPaginated(endpoint, {
      ...paginationOptions,
      method: 'GET'
    });
  }
};
