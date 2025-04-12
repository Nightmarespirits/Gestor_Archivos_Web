/**
 * router/index.ts
 *
 * Automatic routes for `./src/pages/*.vue`
 */

// Composables
import { createRouter, createWebHistory } from 'vue-router';
import DefaultLayout from '@/layouts/default.vue';
import { createAuthGuard } from './guards'; // Import the guard creator
import pinia from '@/store'; // Import the pinia instance

const routes = [
  {
    path: '/',
    component: DefaultLayout,
    children: [
      {
        path: '', // Root path for the default layout
        name: 'Dashboard',
        component: () => import(/* webpackChunkName: "dashboard" */ '@/pages/index.vue'), 
        meta: { requiresAuth: true } // Dashboard requires login
      },
      {
        path: 'documents',
        name: 'Documents',
        component: () => import(/* webpackChunkName: "documents" */ '@/pages/documents/index.vue'),
        meta: { requiresAuth: true, requiredRole: 'ADMIN' }, // Requires login and Admin role
      },
      {
        path: 'documents/create',
        name: 'CreateDocument',
        component: () => import(/* webpackChunkName: "documents" */ '@/pages/documents/create.vue'),
        meta: { requiresAuth: true, requiredRole: 'ADMIN' } // Explícitamente establecer permisos
      },
      {
        path: 'users',
        name: 'Users',
        component: () => import(/* webpackChunkName: "users" */ '@/pages/users/index.vue'),
        meta: { requiresAuth: true, requiredRole: 'ADMIN' } // Requires login and Admin role
      },
      {
        path: 'users/create',
        name: 'CreateUser',
        component: () => import(/* webpackChunkName: "users" */ '@/pages/users/create.vue'),
        meta: { requiresAuth: true, requiredRole: 'ADMIN' } // Requires login and Admin role
      },
      {
        path: 'users/:id/edit',
        name: 'EditUser',
        component: () => import(/* webpackChunkName: "users" */ '@/pages/users/[id]/edit.vue'),
        meta: { requiresAuth: true, requiredRole: 'ADMIN' } // Requires login and Admin role
      },
      {
        path: 'search-documents',
        name: 'SearchDocuments',
        component: () => import(/* webpackChunkName: "search" */ '@/pages/search-documents.vue'),
        meta: { requiresAuth: true } // Requires login, any role
      },
      {
        path: 'activity-log',
        name: 'ActivityLog',
        component: () => import(/* webpackChunkName: "activity" */ '@/pages/ActivityLogPlaceholder.vue'),
        meta: { requiresAuth: true, requiredRole: 'ADMIN' } // Requires login and Admin role
      },
      {
        path: 'access-control',
        name: 'AccessControl',
        component: () => import(/* webpackChunkName: "access" */ '@/pages/AccessControlPlaceholder.vue'),
        meta: { requiresAuth: true, requiredRole: 'ADMIN' } // Requires login and Admin role
      },
      // --- Add other authenticated routes here --- //
      
      // Rutas para documentos individuales
      {
        path: 'documents/:id',
        name: 'DocumentDetail',
        component: () => import(/* webpackChunkName: "documents" */ '@/pages/documents/[id].vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'documents/:id/edit',
        name: 'EditDocument',
        component: () => import(/* webpackChunkName: "documents" */ '@/pages/documents/[id]/edit.vue'),
        meta: { requiresAuth: true }
      },
    ],
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import(/* webpackChunkName: "login" */ '@/pages/login.vue'),
    // meta: { requiresAuth: false } // Default is false if not specified
  },
  {
    path: '/unauthorized',
    name: 'Unauthorized',
    component: () => import(/* webpackChunkName: "unauthorized" */ '@/pages/UnauthorizedPlaceholder.vue'),
  },
  {
    path: '/:catchAll(.*)',
    name: 'NotFound',
    component: () => import(/* webpackChunkName: "notfound" */ '@/pages/NotFoundPlaceholder.vue'),
  }
];

const router = createRouter({
  history: createWebHistory(process.env.BASE_URL),
  routes,
});

createAuthGuard(router, pinia);

export default router;
