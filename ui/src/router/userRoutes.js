import { PERMISSIONS } from '@/helpers/permissionsHelper';

export const userRoutes = {
    path: 'users',
    name: 'Users',
    children: [ 
        {
            path: '',
            component: () => import('@/pages/users/index.vue'),
            meta: { requiredPermissions: [PERMISSIONS.USER_READ] }, 
        },
        {
            path: 'create',
            name: 'CreateUser',
            component: () => import('@/pages/users/create.vue'),
            meta: { requiredPermissions: [PERMISSIONS.USER_CREATE] }
        },
        {
            path: ':id/edit',
            name: 'EditUser',
            component: () => import('@/pages/users/[id]/edit.vue'),
            meta: { requiredPermissions: [PERMISSIONS.USER_UPDATE] }
        },
    ]
}
