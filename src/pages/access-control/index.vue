<template>
  <div>
    <v-row class="mb-4">
      <v-col cols="8">
        <h1 class="text-h4">Gestión de Permisos y Accesos</h1>
      </v-col>
      <v-col cols="4" class="d-flex justify-end">
        <v-btn 
          color="primary" 
          prepend-icon="mdi-key-plus" 
          @click="openCreateDialog"
        >
          Nuevo Permiso
        </v-btn>
      </v-col>
    </v-row>

    <!-- Tabs para diferentes secciones -->
    <v-tabs v-model="activeTab" bg-color="secondary" color="white" class="mb-4">
      <v-tab value="permissions">Permisos</v-tab>
      <v-tab value="roles">Roles y Asignaciones</v-tab>
    </v-tabs>

    <v-window v-model="activeTab">
      <!-- Tab de Permisos -->
      <v-window-item value="permissions">
        <v-card>
          <v-card-text>
            <v-text-field
              v-model="search"
              prepend-inner-icon="mdi-magnify"
              label="Buscar permiso"
              single-line
              hide-details
              clearable
              variant="outlined"
              density="compact"
              class="mb-4"
            ></v-text-field>

            <v-data-table
              :headers="permissionHeaders"
              :items="permissionsStore.permissions"
              :search="search"
              :loading="permissionsStore.loading"
              :items-per-page="10"
              class="elevation-1"
            >
              <template v-slot:item.actions="{ item }">
                <v-tooltip text="Editar permiso" location="top">
                  <template v-slot:activator="{ props }">
                    <v-btn
                      icon
                      variant="text"
                      color="primary"
                      v-bind="props"
                      @click="openEditDialog(item)"
                      :disabled="permissionsStore.loading"
                    >
                      <v-icon>mdi-pencil</v-icon>
                    </v-btn>
                  </template>
                </v-tooltip>
                
                <v-tooltip text="Eliminar permiso" location="top">
                  <template v-slot:activator="{ props }">
                    <v-btn
                      icon
                      variant="text"
                      color="error"
                      v-bind="props"
                      @click="confirmDeletePermission(item)"
                      :disabled="permissionsStore.loading"
                    >
                      <v-icon>mdi-delete</v-icon>
                    </v-btn>
                  </template>
                </v-tooltip>
              </template>
            </v-data-table>
          </v-card-text>
        </v-card>
      </v-window-item>

      <!-- Tab de Roles y Asignaciones -->
      <v-window-item value="roles">
        <v-card>
          <v-card-text>
            <v-row>
              <v-col cols="12" md="6">
                <h2 class="text-h5 mb-3">Gestión de Roles</h2>
                
                <!-- Lista de roles -->
                <v-card variant="outlined" class="mb-4">
                  <v-list>
                    <v-list-subheader>Roles disponibles</v-list-subheader>
                    
                    <v-list-item
                      v-for="role in rolesStore.roles"
                      :key="role.id"
                      :title="role.name"
                      :subtitle="role.description"
                      @click="handleRoleSelect(role.id)"
                      :active="selectedRoleId === role.id"
                      :disabled="rolesStore.loading"
                    >
                      <template v-slot:append>
                        <v-btn 
                          icon="mdi-pencil" 
                          variant="text" 
                          color="primary" 
                          size="small"
                          @click.stop="openEditRoleDialog(role)"
                          :disabled="rolesStore.loading"
                        ></v-btn>
                        <v-btn 
                          icon="mdi-delete" 
                          variant="text" 
                          color="error" 
                          size="small"
                          @click.stop="confirmDeleteRole(role)"
                          :disabled="rolesStore.loading"
                        ></v-btn>
                      </template>
                    </v-list-item>
                    
                    <div class="pa-2">
                      <v-btn 
                        color="primary" 
                        block 
                        prepend-icon="mdi-account-multiple-plus" 
                        @click="openCreateRoleDialog"
                      >
                        Nuevo Rol
                      </v-btn>
                    </div>
                  </v-list>
                </v-card>
              </v-col>

              <v-col cols="12" md="6">
                <h2 class="text-h5 mb-3">Permisos del Rol</h2>
                
                <v-card v-if="selectedRole" variant="outlined" class="mb-4">
                  <v-card-title>{{ selectedRole.name }}</v-card-title>
                  <v-card-subtitle>{{ selectedRole.description }}</v-card-subtitle>
                  
                  <v-card-text>
                    <v-sheet class="mb-4">
                      <h3 class="text-subtitle-1">Permisos asignados</h3>
                      <div v-if="!selectedRole.permissions || !selectedRole.permissions.length" class="py-3 text-center">
                        <v-icon icon="mdi-alert" color="warning" size="large" class="mb-2"></v-icon>
                        <p>Este rol no tiene permisos asignados</p>
                      </div>
                      <v-chip-group v-else>
                        <v-chip
                          v-for="permission in selectedRole.permissions"
                          :key="permission.id"
                          closable
                          color="primary"
                          class="ma-1"
                          @click:close="removePermissionFromRole(permission)"
                        >
                          {{ permission.name }}
                        </v-chip>
                      </v-chip-group>
                    </v-sheet>
                    
                    <v-divider class="mb-4"></v-divider>
                    
                    <h3 class="text-subtitle-1 mb-2">Asignar nuevos permisos</h3>
                    <v-autocomplete
                      v-model="selectedPermissionIds"
                      :items="availablePermissions"
                      item-title="name"
                      item-value="id"
                      label="Seleccionar permisos"
                      multiple
                      chips
                      closable-chips
                      variant="outlined"
                      density="comfortable"
                    ></v-autocomplete>
                    
                    <v-btn 
                      color="primary" 
                      block
                      :disabled="!selectedPermissionIds.length || rolesStore.loading" 
                      @click="addPermissionsToRole"
                      class="mt-2"
                    >
                      Asignar Permisos
                    </v-btn>
                  </v-card-text>
                </v-card>
                
                <v-alert
                  v-if="!selectedRole"
                  type="info"
                  text="Seleccione un rol de la lista para gestionar sus permisos"
                  variant="tonal"
                ></v-alert>
              </v-col>
            </v-row>
          </v-card-text>
        </v-card>
      </v-window-item>
    </v-window>

    <!-- Dialog para crear permiso -->
    <v-dialog v-model="createDialog" max-width="600px">
      <v-card>
        <v-card-title class="text-h5">Crear Nuevo Permiso</v-card-title>
        <v-card-text>
          <v-form ref="createForm" @submit.prevent="createPermission">
            <v-row>
              <v-col cols="12">
                <v-select
                  v-model="newPermission.name"
                  :items="availablePermissionTypes"
                  item-title="displayName"
                  item-value="name"
                  label="Tipo de Permiso"
                  :rules="[v => !!v || 'Tipo de permiso es obligatorio']"
                  required
                >
                  <template v-slot:item="{ item, props }">
                    <v-list-item v-bind="props">
                      <v-list-item-title>{{ item.raw.displayName }}</v-list-item-title>
                      <v-list-item-subtitle>{{ item.raw.description }}</v-list-item-subtitle>
                    </v-list-item>
                  </template>
                </v-select>
              </v-col>
              
              <v-col cols="12">
                <v-textarea
                  v-model="newPermission.description"
                  label="Descripción"
                  :rules="[v => !!v || 'Descripción es obligatoria']"
                  required
                  rows="3"
                ></v-textarea>
              </v-col>
            </v-row>
          </v-form>
        </v-card-text>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn color="error" text @click="createDialog = false">Cancelar</v-btn>
          <v-btn 
            color="primary" 
            text 
            @click="createPermission"
            :loading="permissionsStore.loading"
          >
            Guardar
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- Dialog para editar permiso -->
    <v-dialog v-model="editDialog" max-width="600px">
      <v-card>
        <v-card-title class="text-h5">Editar Permiso</v-card-title>
        <v-card-text>
          <v-form ref="editForm" @submit.prevent="updatePermission">
            <v-row>
              <v-col cols="12">
                <v-select
                  v-model="editedPermission.name"
                  :items="availablePermissionTypes"
                  item-title="displayName"
                  item-value="name"
                  label="Tipo de Permiso"
                  :rules="[v => !!v || 'Tipo de permiso es obligatorio']"
                  required
                  disabled
                >
                  <template v-slot:item="{ item, props }">
                    <v-list-item v-bind="props">
                      <v-list-item-title>{{ item.raw.displayName }}</v-list-item-title>
                      <v-list-item-subtitle>{{ item.raw.description }}</v-list-item-subtitle>
                    </v-list-item>
                  </template>
                </v-select>
              </v-col>
              
              <v-col cols="12">
                <v-textarea
                  v-model="editedPermission.description"
                  label="Descripción"
                  :rules="[v => !!v || 'Descripción es obligatoria']"
                  required
                  rows="3"
                ></v-textarea>
              </v-col>
            </v-row>
          </v-form>
        </v-card-text>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn color="error" text @click="editDialog = false">Cancelar</v-btn>
          <v-btn 
            color="primary" 
            text 
            @click="updatePermission"
            :loading="permissionsStore.loading"
          >
            Guardar
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- Dialog de confirmación para eliminar permiso -->
    <v-dialog v-model="deleteDialog" max-width="400px">
      <v-card>
        <v-card-title class="text-h5">Confirmar Eliminación</v-card-title>
        <v-card-text>
          ¿Está seguro que desea eliminar este permiso? Esta acción no se puede deshacer.
        </v-card-text>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn color="primary" text @click="deleteDialog = false">Cancelar</v-btn>
          <v-btn 
            color="error" 
            text 
            @click="deletePermission"
            :loading="permissionsStore.loading"
          >
            Eliminar
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- Dialog para crear rol -->
    <v-dialog v-model="createRoleDialog" max-width="600px">
      <v-card>
        <v-card-title class="text-h5">Crear Nuevo Rol</v-card-title>
        <v-card-text>
          <v-form ref="createRoleForm" @submit.prevent="createRole">
            <v-row>
              <v-col cols="12">
                <v-select
                  v-model="newRole.name"
                  :items="availableRoleTypes"
                  item-title="displayName"
                  item-value="name"
                  label="Tipo de Rol"
                  :rules="[v => !!v || 'Tipo de rol es obligatorio']"
                  required
                >
                  <template v-slot:item="{ item, props }">
                    <v-list-item v-bind="props">
                      <v-list-item-title>{{ item.raw.displayName }}</v-list-item-title>
                      <v-list-item-subtitle>{{ item.raw.description }}</v-list-item-subtitle>
                    </v-list-item>
                  </template>
                </v-select>
              </v-col>
              
              <v-col cols="12">
                <v-textarea
                  v-model="newRole.description"
                  label="Descripción"
                  :rules="[v => !!v || 'Descripción es obligatoria']"
                  required
                  rows="3"
                ></v-textarea>
              </v-col>
            </v-row>
          </v-form>
        </v-card-text>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn color="error" text @click="createRoleDialog = false">Cancelar</v-btn>
          <v-btn 
            color="primary" 
            text 
            @click="createRole"
            :loading="rolesStore.loading"
          >
            Guardar
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- Dialog para editar rol -->
    <v-dialog v-model="editRoleDialog" max-width="600px">
      <v-card>
        <v-card-title class="text-h5">Editar Rol</v-card-title>
        <v-card-text>
          <v-form ref="editRoleForm" @submit.prevent="updateRole">
            <v-row>
              <v-col cols="12">
                <v-select
                  v-model="editedRole.name"
                  :items="availableRoleTypes"
                  item-title="displayName"
                  item-value="name"
                  label="Tipo de Rol"
                  :rules="[v => !!v || 'Tipo de rol es obligatorio']"
                  required
                  disabled
                >
                  <template v-slot:item="{ item, props }">
                    <v-list-item v-bind="props">
                      <v-list-item-title>{{ item.raw.displayName }}</v-list-item-title>
                      <v-list-item-subtitle>{{ item.raw.description }}</v-list-item-subtitle>
                    </v-list-item>
                  </template>
                </v-select>
              </v-col>
              
              <v-col cols="12">
                <v-textarea
                  v-model="editedRole.description"
                  label="Descripción"
                  :rules="[v => !!v || 'Descripción es obligatoria']"
                  required
                  rows="3"
                ></v-textarea>
              </v-col>
            </v-row>
          </v-form>
        </v-card-text>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn color="error" text @click="editRoleDialog = false">Cancelar</v-btn>
          <v-btn 
            color="primary" 
            text 
            @click="updateRole"
            :loading="rolesStore.loading"
          >
            Guardar
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- Dialog de confirmación para eliminar rol -->
    <v-dialog v-model="deleteRoleDialog" max-width="400px">
      <v-card>
        <v-card-title class="text-h5">Confirmar Eliminación</v-card-title>
        <v-card-text>
          ¿Está seguro que desea eliminar este rol? Esta acción no se puede deshacer y podría afectar a usuarios con este rol asignado.
        </v-card-text>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn color="primary" text @click="deleteRoleDialog = false">Cancelar</v-btn>
          <v-btn 
            color="error" 
            text 
            @click="deleteRole"
            :loading="rolesStore.loading"
          >
            Eliminar
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- Snackbar para notificaciones -->
    <v-snackbar
      v-model="snackbar.show"
      :color="snackbar.color"
      :timeout="snackbar.timeout"
    >
      {{ snackbar.text }}
      <template v-slot:actions>
        <v-btn
          color="white"
          text
          @click="snackbar.show = false"
        >
          Cerrar
        </v-btn>
      </template>
    </v-snackbar>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { usePermissionsStore } from '@/store/permissions';
import { useRolesStore } from '@/store/roles';

// Configuración del store
const permissionsStore = usePermissionsStore();
const rolesStore = useRolesStore();

// Estados reactivos
const search = ref('');
const activeTab = ref('permissions');
const createDialog = ref(false);
const editDialog = ref(false);
const deleteDialog = ref(false);
const snackbar = ref({
  show: false,
  text: '',
  color: 'success',
  timeout: 3000
});
const newPermission = ref({
  name: '',
  description: ''
});
const editedPermission = ref({
  id: null,
  name: '',
  description: ''
});
const permissionToDelete = ref(null);

const roleSearch = ref('');
const createRoleDialog = ref(false);
const editRoleDialog = ref(false);
const deleteRoleDialog = ref(false);
const newRole = ref({
  name: '',
  description: ''
});
const editedRole = ref({
  id: null,
  name: '',
  description: ''
});
const roleToDelete = ref(null);
const selectedRoleId = ref(null);
const selectedRole = ref(null);
const selectedPermissionIds = ref([]);
const availablePermissions = computed(() => permissionsStore.permissions);

// Headers para las tablas
const permissionHeaders = [
  { title: 'ID', key: 'id', sortable: true },
  { title: 'Nombre', key: 'name', sortable: true },
  { title: 'Descripción', key: 'description', sortable: true },
  { title: 'Acciones', key: 'actions', sortable: false, align: 'end' }
];

const roleHeaders = [
  { title: 'ID', key: 'id', sortable: true },
  { title: 'Nombre', key: 'name', sortable: true },
  { title: 'Descripción', key: 'description', sortable: true },
  { title: 'Acciones', key: 'actions', sortable: false, align: 'end' }
];

// Computed properties
const availablePermissionTypes = computed(() => permissionsStore.permissionTypes);
const availableRoleTypes = computed(() => rolesStore.roleTypes);

// Cargar datos al montar el componente
onMounted(async () => {
  await loadPermissions();
  await loadPermissionTypes();
  await loadRoles();
  await loadRoleTypes();
});

// Funciones de utilidad
async function loadPermissions() {
  try {
    await permissionsStore.fetchPermissions();
  } catch (error) {
    showSnackbar(`Error al cargar permisos: ${error.message}`, 'error');
  }
}

async function loadPermissionTypes() {
  try {
    await permissionsStore.fetchPermissionTypes();
  } catch (error) {
    showSnackbar(`Error al cargar tipos de permisos: ${error.message}`, 'error');
  }
}

async function loadRoles() {
  try {
    await rolesStore.fetchRoles();
  } catch (error) {
    showSnackbar(`Error al cargar roles: ${error.message}`, 'error');
  }
}

async function loadRoleTypes() {
  try {
    await rolesStore.fetchRoleTypes();
  } catch (error) {
    showSnackbar(`Error al cargar tipos de roles: ${error.message}`, 'error');
  }
}

// Funciones para CRUD
function openCreateDialog() {
  newPermission.value = {
    name: '',
    description: ''
  };
  createDialog.value = true;
}

function openEditDialog(permission) {
  editedPermission.value = {
    id: permission.id,
    name: permission.name,
    description: permission.description
  };
  editDialog.value = true;
}

function confirmDeletePermission(permission) {
  permissionToDelete.value = permission;
  deleteDialog.value = true;
}

async function createPermission() {
  try {
    if (!newPermission.value.name || !newPermission.value.description) {
      return showSnackbar('Por favor complete todos los campos requeridos', 'error');
    }

    await permissionsStore.createPermission(newPermission.value);
    createDialog.value = false;
    showSnackbar('Permiso creado exitosamente', 'success');
  } catch (error) {
    showSnackbar(`Error al crear permiso: ${error.message}`, 'error');
  }
}

async function updatePermission() {
  try {
    if (!editedPermission.value.description) {
      return showSnackbar('Por favor complete todos los campos requeridos', 'error');
    }

    await permissionsStore.updatePermission(editedPermission.value.id, editedPermission.value);
    editDialog.value = false;
    showSnackbar('Permiso actualizado exitosamente', 'success');
  } catch (error) {
    showSnackbar(`Error al actualizar permiso: ${error.message}`, 'error');
  }
}

async function deletePermission() {
  try {
    if (!permissionToDelete.value) return;
    
    await permissionsStore.deletePermission(permissionToDelete.value.id);
    deleteDialog.value = false;
    showSnackbar('Permiso eliminado exitosamente', 'success');
  } catch (error) {
    showSnackbar(`Error al eliminar permiso: ${error.message}`, 'error');
  }
}

function openCreateRoleDialog() {
  newRole.value = {
    name: '',
    description: ''
  };
  createRoleDialog.value = true;
}

function openEditRoleDialog(role) {
  editedRole.value = {
    id: role.id,
    name: role.name,
    description: role.description
  };
  editRoleDialog.value = true;
}

function confirmDeleteRole(role) {
  roleToDelete.value = role;
  deleteRoleDialog.value = true;
}

async function createRole() {
  try {
    if (!newRole.value.name || !newRole.value.description) {
      return showSnackbar('Por favor complete todos los campos requeridos', 'error');
    }

    await rolesStore.createRole(newRole.value);
    createRoleDialog.value = false;
    showSnackbar('Rol creado exitosamente', 'success');
  } catch (error) {
    showSnackbar(`Error al crear rol: ${error.message}`, 'error');
  }
}

async function updateRole() {
  try {
    if (!editedRole.value.description) {
      return showSnackbar('Por favor complete todos los campos requeridos', 'error');
    }

    await rolesStore.updateRole(editedRole.value.id, editedRole.value);
    editRoleDialog.value = false;
    showSnackbar('Rol actualizado exitosamente', 'success');
  } catch (error) {
    showSnackbar(`Error al actualizar rol: ${error.message}`, 'error');
  }
}

async function deleteRole() {
  try {
    if (!roleToDelete.value) return;
    
    await rolesStore.deleteRole(roleToDelete.value.id);
    deleteRoleDialog.value = false;
    showSnackbar('Rol eliminado exitosamente', 'success');
  } catch (error) {
    showSnackbar(`Error al eliminar rol: ${error.message}`, 'error');
  }
}

async function handleRoleSelect(roleId) {
  try {
    console.log('Seleccionando rol:', roleId);
    
    if (!roleId) {
      console.error('Error: El rol seleccionado no tiene ID válido', roleId);
      showSnackbar('Error al seleccionar rol: ID no válido', 'error');
      return;
    }
    
    // Cargar los datos completos del rol, incluyendo sus permisos
    console.log(`Cargando datos completos del rol con ID: ${roleId}`);
    const fullRoleData = await rolesStore.fetchRoleById(roleId);
    
    if (fullRoleData) {
      console.log('Datos completos del rol cargados:', fullRoleData);
      selectedRoleId.value = roleId;
      selectedRole.value = fullRoleData;
      // Limpiar la selección de permisos
      selectedPermissionIds.value = [];
    } else {
      console.error('No se pudo obtener datos completos del rol');
      showSnackbar('No se pudo cargar la información completa del rol', 'error');
    }
  } catch (error) {
    console.error('Error al cargar detalles del rol:', error);
    showSnackbar(`Error al cargar detalles del rol: ${error.message}`, 'error');
  }
}

async function removePermissionFromRole(permission) {
  try {
    if (!selectedRole.value || !selectedRole.value.id) {
      return showSnackbar('No hay un rol seleccionado válido', 'error');
    }
    
    rolesStore.removePermissionsFromRole(selectedRole.value.id, [permission.id])
      .then(() => {
        showSnackbar(`Permiso ${permission.name} eliminado del rol`, 'success');
        // Actualizar el rol seleccionado con los datos más recientes
        return rolesStore.fetchRoleById(selectedRole.value.id);
      })
      .then(updatedRole => {
        if (updatedRole) {
          selectedRole.value = updatedRole;
        }
      })
      .catch(error => {
        showSnackbar(`Error al eliminar permiso: ${error.message}`, 'error');
      });
  } catch (error) {
    showSnackbar(`Error al eliminar permiso: ${error.message}`, 'error');
  }
}

async function addPermissionsToRole() {
  try {
    if (!selectedRole.value || !selectedRole.value.id) {
      return showSnackbar('No hay un rol seleccionado válido', 'error');
    }
    
    if (!selectedPermissionIds.value.length) {
      return showSnackbar('Seleccione al menos un permiso para asignar', 'warning');
    }
    
    await rolesStore.addPermissionsToRole(selectedRole.value.id, selectedPermissionIds.value);
    
    // Actualizar el rol seleccionado con los datos más recientes
    const updatedRole = await rolesStore.fetchRoleById(selectedRole.value.id);
    if (updatedRole) {
      selectedRole.value = updatedRole;
    }
    
    // Limpiar la selección
    selectedPermissionIds.value = [];
    
    showSnackbar('Permisos asignados exitosamente', 'success');
  } catch (error) {
    showSnackbar(`Error al asignar permisos: ${error.message}`, 'error');
  }
}

function showSnackbar(text, color = 'success', timeout = 3000) {
  snackbar.value = {
    show: true,
    text,
    color,
    timeout
  };
}
</script>

<style scoped>
.v-data-table {
  border-radius: 8px;
}

.v-tab {
  text-transform: none;
  font-size: 1rem;
}
</style>
