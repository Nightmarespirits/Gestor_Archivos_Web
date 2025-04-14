/**
 * router/index.ts
 *
 * Automatic routes for `./src/pages/*.vue`
 */

// Composables
import { createRouter, createWebHistory } from 'vue-router';
import DefaultLayout from '@/layouts/default.vue';
import BlankLayout from '@/layouts/blank.vue';
import { createAuthGuard } from './guards'; // Import the guard creator
import pinia from '@/store'; // Import the pinia instance

const routes = [
  // Rutas que requieren autenticación (Layout Default)
  {
    path: '/',
    component: DefaultLayout,
    meta: { requiresAuth: true },
    children: [
      {
        path: '', // Root path for the default layout
        name: 'Dashboard',
        component: () => import(/* webpackChunkName: "dashboard" */ '@/pages/index.vue'),
      },
      {
        path: 'documents',
        name: 'Documents',
        component: () => import(/* webpackChunkName: "documents" */ '@/pages/documents/index.vue'),
        meta: { requiredRole: 'ADMIN' },
      },
      {
        path: 'documents/create',
        name: 'CreateDocument',
        component: () => import(/* webpackChunkName: "documents" */ '@/pages/documents/create.vue'),
        meta: { requiredRole: 'ADMIN' }
      },
      {
        path: 'users',
        name: 'Users',
        component: () => import(/* webpackChunkName: "users" */ '@/pages/users/index.vue'),
        meta: { requiredRole: 'ADMIN' }
      },
      {
        path: 'users/create',
        name: 'CreateUser',
        component: () => import(/* webpackChunkName: "users" */ '@/pages/users/create.vue'),
        meta: { requiredRole: 'ADMIN' }
      },
      {
        path: 'users/:id/edit',
        name: 'EditUser',
        component: () => import(/* webpackChunkName: "users" */ '@/pages/users/[id]/edit.vue'),
        meta: { requiredRole: 'ADMIN' }
      },
      {
        path: 'search-documents',
        name: 'SearchDocuments',
        component: () => import(/* webpackChunkName: "search" */ '@/pages/search-documents.vue'),
      },
      {
        path: 'activity-log',
        name: 'ActivityLog',
        component: () => import(/* webpackChunkName: "activity" */ '@/pages/ActivityLogPlaceholder.vue'),
        meta: { requiredRole: 'ADMIN' }
      },
      {
        path: 'access-control',
        name: 'AccessControl',
        component: () => import(/* webpackChunkName: "access" */ '@/pages/AccessControlPlaceholder.vue'),
        meta: { requiredRole: 'ADMIN' }
      },
      {
        path: 'tags-types',
        name: 'TagsAndTypes',
        component: () => import(/* webpackChunkName: "tags-types" */ '@/pages/tags-types/index.vue'),
        meta: { requiredRole: 'ADMIN' }
      },
      // Rutas para documentos individuales
      {
        path: 'documents/:id',
        name: 'DocumentDetail',
        component: () => import(/* webpackChunkName: "documents" */ '@/pages/documents/[id].vue'),
      },
      {
        path: 'documents/:id/edit',
        name: 'EditDocument',
        component: () => import(/* webpackChunkName: "documents" */ '@/pages/documents/[id]/edit.vue'),
      },
    ],
  },
  
  // Rutas públicas (Layout Blank)
  {
    path: '/login',
    component: BlankLayout,
    children: [
      {
        path: '',
        name: 'Login',
        component: () => import(/* webpackChunkName: "login" */ '@/pages/login.vue'),
      }
    ]
  },
  
  // Otras rutas (Layout Blank)
  {
    path: '/',
    component: BlankLayout,
    children: [
      {
        path: 'unauthorized',
        name: 'Unauthorized',
        component: () => import(/* webpackChunkName: "unauthorized" */ '@/pages/UnauthorizedPlaceholder.vue'),
      },
      {
        path: ':catchAll(.*)',
        name: 'NotFound',
        component: () => import(/* webpackChunkName: "notfound" */ '@/pages/NotFoundPlaceholder.vue'),
      }
    ]
  }
];

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL || '/'),
  routes,
});

createAuthGuard(router, pinia);

export default router;
