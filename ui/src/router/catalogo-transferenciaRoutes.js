import { PERMISSIONS } from '@/helpers/permissionsHelper';

export const catalogoTransferenciaRoutes = {
    path: 'catalogo-transferencia',
    name: 'catalogo-transferencia',
    children: [
        {
            path: '',
            name: 'catalogo-transferencia-list',
            component: () => import('@/pages/catalogo-transferencia/index.vue'),
            meta: { requiredPermissions: [PERMISSIONS.DOC_READ] }
        },
        {
            path: 'crear',
            name: 'catalogo-transferencia-create',
            component: () => import('@/pages/catalogo-transferencia/create.vue'),
            meta: { requiredPermissions: [PERMISSIONS.DOC_CREATE] }
        },
        {
            path: ':id',
            name: 'catalogo-transferencia-detalle',
            component: () => import('@/pages/catalogo-transferencia/[id]/index.vue'),
            meta: { requiredPermissions: [PERMISSIONS.DOC_READ] }
        },
        {
            path: ':id/edit',
            name: 'catalogo-transferencia-edit',
            component: () => import('@/pages/catalogo-transferencia/[id]/edit.vue'),
            meta: { requiredPermissions: [PERMISSIONS.DOC_UPDATE] }
        },
        {
            path: ':id/detalles',
            name: 'catalogo-transferencia-detalles',
            component: () => import('@/pages/catalogo-transferencia/[id]/detalles.vue'),
            meta: { requiredPermissions: [PERMISSIONS.DOC_UPDATE] }
        }
    ]
}