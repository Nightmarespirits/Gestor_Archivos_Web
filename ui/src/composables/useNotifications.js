import { ref, reactive } from 'vue'

// Estado global para las notificaciones
const snackbar = reactive({
  show: false,
  text: '',
  color: 'success',
  timeout: 3000
})

const confirmDialog = reactive({
  show: false,
  title: 'Confirmar acción',
  message: '',
  confirmText: 'Confirmar',
  cancelText: 'Cancelar',
  confirmColor: 'primary',
  onConfirm: null,
  onCancel: null
})

/**
 * Composable para manejar notificaciones y diálogos con Vuetify
 * Centraliza el manejo de snackbars y diálogos de confirmación
 */
export function useNotifications() {

  /**
   * Muestra una notificación de éxito
   * @param {string} message - Mensaje a mostrar
   * @param {number} timeout - Tiempo de duración en ms (por defecto 3000)
   */
  function showSuccess(message, timeout = 3000) {
    snackbar.show = true
    snackbar.text = message
    snackbar.color = 'success'
    snackbar.timeout = timeout
  }

  /**
   * Muestra una notificación de error
   * @param {string} message - Mensaje de error a mostrar
   * @param {number} timeout - Tiempo de duración en ms (por defecto 5000)
   */
  function showError(message, timeout = 5000) {
    snackbar.show = true
    snackbar.text = message
    snackbar.color = 'error'
    snackbar.timeout = timeout
  }

  /**
   * Muestra una notificación de advertencia
   * @param {string} message - Mensaje de advertencia a mostrar
   * @param {number} timeout - Tiempo de duración en ms (por defecto 4000)
   */
  function showWarning(message, timeout = 4000) {
    snackbar.show = true
    snackbar.text = message
    snackbar.color = 'warning'
    snackbar.timeout = timeout
  }

  /**
   * Muestra una notificación de información
   * @param {string} message - Mensaje informativo a mostrar
   * @param {number} timeout - Tiempo de duración en ms (por defecto 3000)
   */
  function showInfo(message, timeout = 3000) {
    snackbar.show = true
    snackbar.text = message
    snackbar.color = 'info'
    snackbar.timeout = timeout
  }

  /**
   * Muestra una notificación personalizada
   * @param {string} message - Mensaje a mostrar
   * @param {string} color - Color del snackbar (success, error, warning, info)
   * @param {number} timeout - Tiempo de duración en ms
   */
  function showNotification(message, color = 'success', timeout = 3000) {
    snackbar.show = true
    snackbar.text = message
    snackbar.color = color
    snackbar.timeout = timeout
  }

  /**
   * Muestra un diálogo de confirmación
   * @param {Object} options - Opciones del diálogo
   * @param {string} options.message - Mensaje de confirmación
   * @param {string} options.title - Título del diálogo (opcional)
   * @param {string} options.confirmText - Texto del botón de confirmar (opcional)
   * @param {string} options.cancelText - Texto del botón de cancelar (opcional)
   * @param {string} options.confirmColor - Color del botón de confirmar (opcional)
   * @returns {Promise<boolean>} - Promesa que resuelve a true si se confirma, false si se cancela
   */
  function showConfirm(options) {
    return new Promise((resolve) => {
      // Reset previos estados
      confirmDialog.show = false
      
      // Actualizamos propiedades y callbacks
      Object.assign(confirmDialog, {
        title: options.title || 'Confirmar acción',
        message: options.message,
        confirmText: options.confirmText || 'Confirmar',
        cancelText: options.cancelText || 'Cancelar',
        confirmColor: options.confirmColor || 'primary',
        onConfirm: () => {
          confirmDialog.show = false
          resolve(true)
        },
        onCancel: () => {
          confirmDialog.show = false
          resolve(false)
        }
      })
      
      // Mostramos el diálogo en el siguiente tick
      setTimeout(() => {
        confirmDialog.show = true
      }, 0)
    })
  }

  /**
   * Cierra el snackbar manualmente
   */
  function closeSnackbar() {
    snackbar.show = false
  }

  /**
   * Cierra el diálogo de confirmación manualmente
   */
  function closeConfirmDialog() {
    confirmDialog.show = false
  }

  return {
    // Estados reactivos
    snackbar,
    confirmDialog,
    
    // Métodos para notificaciones
    showSuccess,
    showError,
    showWarning,
    showInfo,
    showNotification,
    
    // Métodos para diálogos
    showConfirm,
    
    // Métodos de utilidad
    closeSnackbar,
    closeConfirmDialog
  }
}