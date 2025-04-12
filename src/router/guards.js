// src/router/guards.js
import { useAuthStore } from '@/store/auth';
import { storeToRefs } from 'pinia'; // Needed to properly get reactive state outside components

export function createAuthGuard(router, piniaInstance) {
  router.beforeEach(async (to, from, next) => { // Make the guard async
    // It's crucial to get a fresh instance of the store *inside* the guard
    // or pass the pinia instance and use it
    const authStore = useAuthStore(piniaInstance);
    const { isAuthenticated, user, isInitialized } = storeToRefs(authStore); // Use storeToRefs for reactivity

    const requiresAuth = to.meta.requiresAuth;
    const requiredRole = to.meta.requiredRole;

    // 1. Ensure the auth state is initialized (e.g., checked localStorage)
    // We rely on main.js calling checkAuth before mounting
    // If checkAuth isn't finished yet, this might still be false initially.
    // A more robust solution might involve a loading state or delaying navigation.
    if (!isInitialized.value && requiresAuth) {
        console.log('Guard: Auth not initialized, attempting checkAuth...');
        try {
            // Attempt to re-check auth state if not initialized
            // This assumes checkAuth updates isInitialized and isAuthenticated
            await authStore.checkAuth();
            console.log('Guard: checkAuth completed. Initialized:', isInitialized.value, 'Authenticated:', isAuthenticated.value);
        } catch (error) {
            console.error('Guard: Error during initial checkAuth:', error);
            // Decide how to handle auth check failure - redirect to login?
            next({ name: 'Login', query: { redirect: to.fullPath } });
            return;
        }
    }

    // Re-evaluate after potential checkAuth call
    const currentIsAuthenticated = isAuthenticated.value;
    const currentUserRole = user.value?.role?.name;

    // 2. Check if route requires authentication
    if (requiresAuth) {
      if (!currentIsAuthenticated) {
        // Not authenticated, redirect to login
        console.log('Guard: Not authenticated, redirecting to login for path:', to.fullPath);
        next({ name: 'Login', query: { redirect: to.fullPath } });
        return;
      }

      // 3. Check if route requires a specific role
      if (requiredRole) {
        if (!currentUserRole || currentUserRole !== requiredRole) {
          // Authenticated but wrong role, redirect to unauthorized or dashboard
          console.warn(`Guard: Unauthorized access attempt to ${to.path}. Required role: ${requiredRole}, User role: ${currentUserRole}`);
          // Option 1: Redirect to dashboard (if Unauthorized page doesn't exist yet)
           next({ name: 'Dashboard' });
          // Option 2: Redirect to a dedicated 'Unauthorized' page
          // next({ name: 'Unauthorized' });
          return;
        }
      }
    // 4. Handle access to login page when already authenticated
    } else if (to.name === 'Login' && currentIsAuthenticated) {
       // Optional: If user is logged in and tries to access Login page, redirect to dashboard
       console.log('Guard: Authenticated user accessing Login page, redirecting to Dashboard.');
       next({ name: 'Dashboard' });
       return;
    }

    // If all checks pass, proceed to the route
    console.log(`Guard: Access granted to ${to.path}`);
    next();
  });
}
