import { rest } from 'msw';
import users from './data/users.js';
import inventarios from './data/inventarios.js';

function paginate(array, page = 1, size = 20) {
  const start = (page - 1) * size;
  return {
    content: array.slice(start, start + size),
    total: array.length,
  };
}

// Lista completa de permisos para el usuario demo
const allPermissions = [
  // Permisos de documentos
  'DOCUMENT_READ', 'DOCUMENT_CREATE', 'DOCUMENT_UPDATE', 'DOCUMENT_DELETE', 'FILE_DOWNLOAD',
  // Permisos de usuarios
  'USER_READ', 'USER_CREATE', 'USER_UPDATE', 'USER_DELETE',
  // Permisos de sistema
  'SYSTEM_CONFIG', 'LOG_READ',
  // Otros permisos especificados en permissionsHelper.js
  'TAG_MANAGE', 'TYPE_MANAGE',
  // Otros permisos específicos que pudieran necesitarse
  'INVENTORY_READ', 'INVENTORY_CREATE', 'INVENTORY_UPDATE', 'INVENTORY_DELETE',
  'TRANSFER_READ', 'TRANSFER_CREATE', 'TRANSFER_UPDATE', 'TRANSFER_DELETE',
  'EDITAR_REGISTROS', 'inventarios:view', 'inventarios:edit', 'inventarios:delete', 'inventarios:finalize',
  'inventarios:approve', 'inventarios:edit:any', 'inventarios:delete:any'
];

// Roles disponibles en el sistema
const allRoles = [
  {
    id: "role_superadmin",
    name: "SUPERADMIN",
    description: "Rol con acceso completo a todo el sistema",
    enabled: true
  },
  {
    id: "role_admin",
    name: "ADMIN",
    description: "Rol con acceso administrativo",
    enabled: true
  },
  {
    id: "role_manager",
    name: "MANAGER",
    description: "Rol con acceso de gestión",
    enabled: true
  },
  {
    id: "role_archivist",
    name: "ARCHIVIST",
    description: "Rol con acceso de archivista",
    enabled: true
  },
  {
    id: "role_viewer",
    name: "VIEWER",
    description: "Rol con acceso de solo lectura",
    enabled: true
  }
];

export const handlers = [
  // --- AUTH ---------------------------------------------------------------
  rest.post('/api/auth/login', async (req, res, ctx) => {
    const { username, password } = await req.json();
    if (username === 'demo' && password === 'demo') {
      const user = users[0];
      const token = btoa(`${user.id}:${Date.now()}`);
      return res(ctx.status(200), ctx.json({ token, user }));
    }
    return res(
      ctx.status(401),
      ctx.json({ message: 'Credenciales inválidas' })
    );
  }),

  // --- INVENTARIOS (ejemplo) ---------------------------------------------
  rest.get('/api/inventarios', (req, res, ctx) => {
    return res(ctx.status(200), ctx.json(inventarios));
  }),

  rest.get('/api/inventarios/paginated', (req, res, ctx) => {
    const page = Number(req.url.searchParams.get('page') || '1');
    const size = Number(req.url.searchParams.get('size') || '20');
    return res(ctx.status(200), ctx.json(paginate(inventarios, page, size)));
  }),

  // --- Simulación de API de roles y permisos --------------------------
  rest.get('/api/roles/:id', (req, res, ctx) => {
    const roleId = req.params.id;
    console.log(`[MSW] Obteniendo permisos para rol con ID: ${roleId} - SE ESTÁ LLAMANDO ESTE HANDLER`);
    
    return res(
      ctx.status(200),
      ctx.json({
        id: roleId,
        name: "SUPERADMIN",
        permissions: allPermissions.map(name => ({
          id: `perm_${name.toLowerCase().replace(/[^a-z0-9]/g, '_')}`,
          name,
          description: `Permiso para ${name.toLowerCase().replace(/_/g, ' ')}`,
          enabled: true
        }))
      })
    );
  }),

  // --- Validación de token de autenticación ----------------------------
  rest.get('/api/auth/validate-token', (req, res, ctx) => {
    console.log('[MSW] Validando token');
    return res(
      ctx.status(200),
      ctx.json({ valid: true })
    );
  }),

  // --- Simulación de lista de roles ----------------------------------
  rest.get('/api/roles', (req, res, ctx) => {
    console.log('[MSW] Obteniendo lista de roles');
    const name = req.url.searchParams.get('name');
    
    if (name) {
      // Si se está buscando por nombre, devolver solo el rol específico
      console.log(`[MSW] Buscando rol específico: ${name}`);
      const roleFound = allRoles.find(r => r.name === name);
      
      if (roleFound) {
        return res(ctx.status(200), ctx.json([roleFound]));
      }
      
      // Si no se encontró el rol, devolver el SUPERADMIN (fallback para demo)
      const superadminRole = allRoles.find(r => r.name === 'SUPERADMIN');
      return res(ctx.status(200), ctx.json([superadminRole]));
    }
    
    // Si no hay parámetro de búsqueda, devolver todos los roles
    return res(ctx.status(200), ctx.json(allRoles));
  }),

  // --- Simulación de usuario actual con permisos ------------------
  rest.get('/api/auth/me', (req, res, ctx) => {
    console.log('[MSW] Obteniendo información del usuario actual');
    
    return res(
      ctx.status(200),
      ctx.json({
        id: "demo_user",
        username: "demo",
        email: "demo@example.com",
        firstName: "Demo",
        lastName: "Usuario",
        role: {
          id: "role_superadmin",
          name: "SUPERADMIN"
        },
        enabled: true
      })
    );
  }),

  // --- Fallback genérico solo para rutas de API ------------------------
  rest.all('/api/**', async (req, res, ctx) => {
    // GET => []  | GET paginated/search => { content: [], total: 0 }
    if (req.method === 'GET') {
      const path = req.url.pathname;
      if (path.endsWith('/paginated') || path.endsWith('/search')) {
        return res(ctx.status(200), ctx.json({ content: [], total: 0 }));
      }
      return res(ctx.status(200), ctx.json([]));
    }

    // POST => eco con id
    if (req.method === 'POST') {
      let body = {};
      try {
        body = await req.json();
      } catch {}
      return res(ctx.status(201), ctx.json({ id: Date.now(), ...body }));
    }

    // PUT => eco
    if (req.method === 'PUT') {
      let body = {};
      try {
        body = await req.json();
      } catch {}
      return res(ctx.status(200), ctx.json(body));
    }

    // DELETE => 204
    if (req.method === 'DELETE') {
      return res(ctx.status(204));
    }

    return res(ctx.status(200), ctx.json({}));
  }),
];
