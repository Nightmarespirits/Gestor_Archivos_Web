// apiDebug.js - Utilidades para depurar las llamadas a la API

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
