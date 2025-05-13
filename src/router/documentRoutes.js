import { PERMISSIONS } from '@/helpers/permissionsHelper';
export const documentRoutes = {
    path: 'documents',
    name: 'Documents', 
    children: [
        {
            path: '',
            component: () => import('@/pages/documents/index.vue'), 
            meta: { requiredPermissions: [PERMISSIONS.DOC_READ] },
        },
        {
            path: 'search',
            name: 'SearchDocuments',
            component: () => import('@/pages/documents/search.vue'),
            meta: { requiredPermissions: [PERMISSIONS.DOC_READ] }
        },
        {
            path: 'create',
            name: 'CreateDocument',
            component: () => import('@/pages/documents/create.vue'),
            meta: { requiredPermissions: [PERMISSIONS.DOC_CREATE] }
        },
        {
            path: ':id/edit',
            name: 'EditDocument',
            component: () => import('@/pages/documents/[id]/edit.vue'),
            meta: { requiredPermissions: [PERMISSIONS.DOC_UPDATE] }
        },
        {
            path: ':id',
            name: 'DocumentDetail',
            component: () => import('@/pages/documents/[id].vue'),
            meta: { requiredPermissions: [PERMISSIONS.DOC_READ] }
        }
    ]
}
