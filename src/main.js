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

  // --- Authentication Check --- //
  // Get the auth store instance using the Pinia instance
  const authStore = useAuthStore(pinia);

  try {
    console.log('main.js: Attempting initial auth check...');
    // Call checkAuth (ensure it updates the store state and returns a promise)
    await authStore.checkAuth();
    console.log('main.js: Initial auth check completed. State:', authStore.isAuthenticated);
  } catch (error) {
    console.error('main.js: Initial authentication check failed:', error);
    // Decide if the app should still mount or show an error state
    // For now, we'll proceed to mount, the guard will handle redirects
  }
  // --- End Authentication Check --- //

  // Mount the app only after the check is attempted
  app.mount('#app');
  console.log('main.js: App mounted.');
}

initializeApp();
