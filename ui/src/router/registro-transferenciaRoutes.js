import { PERMISSIONS } from '@/helpers/permissionsHelper';

export const registroTransferenciaRoutes = {
    path: 'registro-transferencia',
    name: 'registro-transferencia',
    children: [
        {
            path: '',
            name: 'registro-transferencia-list',
            component: () => import('@/pages/registro-transferencia/index.vue'),
            meta: { requiredPermissions: [PERMISSIONS.DOC_READ] }
        },
        {
            path: 'crear',
            name: 'registro-transferencia-create',
            component: () => import('@/pages/registro-transferencia/create.vue'),
            meta: { requiredPermissions: [PERMISSIONS.DOC_CREATE] }
        },
        {
            path: ':id',
            name: 'registro-transferencia-detalle',
            component: () => import('@/pages/registro-transferencia/[id]/index.vue'),
            meta: { requiredPermissions: [PERMISSIONS.DOC_READ] }
        },
        {
            path: ':id/edit',
            name: 'registro-transferencia-edit',
            component: () => import('@/pages/registro-transferencia/[id]/edit.vue'),
            meta: { requiredPermissions: [PERMISSIONS.DOC_UPDATE] }
        },
    ]
}