/**
 * main.js
 *
 * Bootstraps Vuetify and other plugins then mounts the App`
 */

// Plugins
import { registerPlugins } from '@/plugins';
import pinia from '@/store'; // Import the created Pinia instance
import { useAuthStore } from '@/store/auth'; // Import the auth store composable

// Components
import App from './App.vue';

// Composables
import { createApp } from 'vue';

async function initializeApp() {
  const app = createApp(App);

  // Register plugins (including Pinia store)
  registerPlugins(app);

  
  const authStore = useAuthStore(pinia);

  try {
    await authStore.checkAuth();
  } catch (error) {
    console.error('main.js: Initial authentication check failed:', error);
  }


  // Mount the app only after the check is attempted
  app.mount('#app');
}

// Arranque con mocks en modo demo
async function bootstrap() {
  if (import.meta.env.VITE_USE_MOCK === 'true') {
    const { worker } = await import('./mocks/browser');
    await worker.start({
      // MSW 1.3.2: Configuración más permisiva para data:URLs
      onUnhandledRequest: 'bypass',
      serviceWorker: { url: '/mockServiceWorker.js' }
    });
    console.log('[MSW] Mock Service Worker iniciado');
  }
  await initializeApp();
}
bootstrap();
