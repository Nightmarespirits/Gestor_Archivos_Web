import { createRouter, createWebHistory } from 'vue-router';
import DefaultLayout from '@/layouts/default.vue';
import BlankLayout from '@/layouts/blank.vue';
import { createAuthGuard } from './guards';
import pinia from '@/store';
import { PERMISSIONS } from '@/helpers/permissionsHelper';
import { documentRoutes } from './documentRoutes';
import { userRoutes } from './userRoutes';
import { ROLES } from '@/helpers/rolesHelper';
import { inventoryRoutes } from './inventoryRoutes';
import { catalogoTransferenciaRoutes } from './catalogo-transferenciaRoutes';
import {registroTransferenciaRoutes } from './registro-transferenciaRoutes';
const routes = [
    {
        path:'/',
        component: DefaultLayout,
        meta: {requiresAuth: true},
        children: [
            {
                path: '', 
                name: 'Dashboard',
                component: () => import('@/pages/index.vue'),
            },
            documentRoutes,
            userRoutes,
            {
                path: 'activity-logs', 
                name: 'ActivityLogs', 
                component: () => import('@/pages/activity-logs/index.vue'),
                meta: { requiredRoles: [ROLES.SUPERADMIN, ROLES.ADMIN] } 
            },
            {
                path: 'access-control',
                name: 'AccessControl',
                component: () => import('@/pages/access-control/index.vue'),
                meta: { requiredPermissions: [PERMISSIONS.ACCESS_MANAGE] } // O roles: [ROLES.ADMIN]
            },
            {
                path: 'tags-types',
                name: 'TagsAndTypes',
                component: () => import('@/pages/tags-types/index.vue'),
                meta: { requiredPermissions: [PERMISSIONS.TAG_MANAGE, PERMISSIONS.TYPE_MANAGE] } 
            },
            inventoryRoutes,
            catalogoTransferenciaRoutes,
            registroTransferenciaRoutes,
            {
                path: 'test', 
                component: () => import('@/components/examples/ActionButtonsExample.vue'),
            },
        ]
    },
    {
        path: '/login',
        component: BlankLayout,
        children: [
            { 
                path: '',
                name: 'Login',
                component: () => import('@/pages/login.vue'),            
            }
        ]
    },

    // Rutas de login error y otras (Layout Blank)
  {
    path: '/',
    component: BlankLayout,    
    children: [
        {
            path: '/unauthorized', // Mover catchAll al final
            name: 'Unauthorized',
            component: () => import('@/pages/Unauthorized.vue'),
        },
        {
            path: '/:catchAll(.*)*',
            name: 'NotFound',
            component: () => import('@/pages/NotFound.vue'),
        },
        

    ]
  }
];

const router = createRouter({
    history: createWebHistory(import.meta.env.BASE_URL),
    routes,
    scrollBehavior(to, from, savedPosition) {
        if (savedPosition) {
            return savedPosition;
        } else {
            return { top: 0 };
        }
    },
});

createAuthGuard(router, pinia);

export default router;