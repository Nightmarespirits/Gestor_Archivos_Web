import { PERMISSIONS } from '@/helpers/permissionsHelper';

export const inventoryRoutes = {
    path: 'inventarios',
    name: 'inventarios',
    children: [
        {
            path: '',
            name: 'inventario-list',
            component: () => import('@/pages/inventarios/index.vue'),
            meta: { requiredPermissions: [PERMISSIONS.DOC_READ] },
        },
        {
            path: 'crear',
            name: 'inventario-create',
            component: () => import('@/pages/inventarios/create.vue'),
            meta: { requiredPermissions: [PERMISSIONS.DOC_CREATE] }
        },
        {
            path: ':id',
            name: 'inventario-view',
            component: () => import('@/pages/inventarios/[id]/index.vue'),
            meta: { requiredPermissions: [PERMISSIONS.DOC_READ] }
        },
        {
            path: ':id/edit',
            name: 'inventario-edit',
            component: () => import('@/pages/inventarios/[id]/edit.vue'),
            meta: { requiredPermissions: [PERMISSIONS.DOC_UPDATE] }
        },
    ]
}
