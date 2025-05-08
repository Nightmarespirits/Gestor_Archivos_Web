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

initializeApp();
