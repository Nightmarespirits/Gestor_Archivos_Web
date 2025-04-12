<template>
  <div>
    <v-row class="mb-4">
      <v-col cols="8">
        <h1 class="text-h4">Gestión de Usuarios</h1>
      </v-col>
      <v-col cols="4" class="d-flex justify-end">
        <v-btn 
          color="primary" 
          prepend-icon="mdi-account-plus" 
          @click="openCreateDialog"
        >
          Nuevo Usuario
        </v-btn>
      </v-col>
    </v-row>

    <!-- Tabla de usuarios -->
    <v-card>
      <v-card-text>
        <v-text-field
          v-model="search"
          prepend-inner-icon="mdi-magnify"
          label="Buscar usuario"
          single-line
          hide-details
          clearable
          variant="outlined"
          density="compact"
          class="mb-4"
        ></v-text-field>

        <v-data-table
          :headers="headers"
          :items="usersStore.users"
          :search="search"
          :loading="usersStore.loading"
          :items-per-page="10"
          class="elevation-1"
        >
          <template v-slot:item.status="{ item }">
            <v-chip
              :color="item.status ? 'success' : 'error'"
              :text="item.status ? 'Activo' : 'Inactivo'"
              size="small"
            ></v-chip>
          </template>
          
          <template v-slot:item.role="{ item }">
            <v-chip
              v-if="item.role"
              :color="getRoleColor(item.role.name)"
              size="small"
            >
              {{ item.role.name }}
            </v-chip>
            <v-chip
              v-else
              color="grey"
              size="small"
            >
              Sin rol
            </v-chip>
          </template>
          
          <template v-slot:item.actions="{ item }">
            <v-tooltip text="Editar usuario" location="top">
              <template v-slot:activator="{ props }">
                <v-btn
                  icon
                  variant="text"
                  color="primary"
                  v-bind="props"
                  :to="`/users/${item.id}/edit`"
                  :disabled="usersStore.loading"
                >
                  <v-icon>mdi-pencil</v-icon>
                </v-btn>
              </template>
            </v-tooltip>
            
            <v-tooltip text="Cambiar estado" location="top">
              <template v-slot:activator="{ props }">
                <v-btn
                  icon
                  variant="text"
                  :color="item.status ? 'error' : 'success'"
                  v-bind="props"
                  @click="toggleUserStatus(item)"
                  :disabled="usersStore.loading"
                >
                  <v-icon>{{ item.status ? 'mdi-account-cancel' : 'mdi-account-check' }}</v-icon>
                </v-btn>
              </template>
            </v-tooltip>
          </template>
        </v-data-table>
      </v-card-text>
    </v-card>

    <!-- Dialog para crear usuario -->
    <v-dialog v-model="createDialog" max-width="600px">
      <v-card>
        <v-card-title class="text-h5">Crear Nuevo Usuario</v-card-title>
        <v-card-text>
          <v-form ref="createForm" @submit.prevent="createUser">
            <v-row>
              <v-col cols="12">
                <v-text-field
                  v-model="newUser.username"
                  label="Nombre de Usuario"
                  :rules="[v => !!v || 'Nombre de usuario es obligatorio']"
                  required
                ></v-text-field>
              </v-col>
              
              <v-col cols="12">
                <v-text-field
                  v-model="newUser.fullName"
                  label="Nombre Completo"
                  :rules="[v => !!v || 'Nombre completo es obligatorio']"
                  required
                ></v-text-field>
              </v-col>
              
              <v-col cols="12">
                <v-text-field
                  v-model="newUser.email"
                  label="Correo Electrónico"
                  type="email"
                  :rules="[
                    v => !!v || 'Correo electrónico es obligatorio',
                    v => /.+@.+\..+/.test(v) || 'Correo electrónico debe ser válido'
                  ]"
                  required
                ></v-text-field>
              </v-col>
              
              <v-col cols="12">
                <v-text-field
                  v-model="newUser.password"
                  label="Contraseña"
                  type="password"
                  :rules="[
                    v => !!v || 'Contraseña es obligatoria',
                    v => v.length >= 6 || 'La contraseña debe tener al menos 6 caracteres'
                  ]"
                  required
                ></v-text-field>
              </v-col>
              
              <v-col cols="12">
                <v-select
                  v-model="newUser.roleId"
                  :items="roles"
                  item-title="name"
                  item-value="id"
                  label="Rol"
                  :rules="[v => !!v || 'Rol es obligatorio']"
                  required
                ></v-select>
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
            @click="createUser"
            :loading="usersStore.loading"
          >
            Guardar
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- Dialog para editar usuario -->
    <v-dialog v-model="editDialog" max-width="600px">
      <v-card>
        <v-card-title class="text-h5">Editar Usuario</v-card-title>
        <v-card-text>
          <v-form ref="editForm" @submit.prevent="updateUser">
            <v-row>
              <v-col cols="12">
                <v-text-field
                  v-model="editedUser.username"
                  label="Nombre de Usuario"
                  disabled
                ></v-text-field>
              </v-col>
              
              <v-col cols="12">
                <v-text-field
                  v-model="editedUser.fullName"
                  label="Nombre Completo"
                  :rules="[v => !!v || 'Nombre completo es obligatorio']"
                  required
                ></v-text-field>
              </v-col>
              
              <v-col cols="12">
                <v-text-field
                  v-model="editedUser.email"
                  label="Correo Electrónico"
                  type="email"
                  :rules="[
                    v => !!v || 'Correo electrónico es obligatorio',
                    v => /.+@.+\..+/.test(v) || 'Correo electrónico debe ser válido'
                  ]"
                  required
                ></v-text-field>
              </v-col>
              
              <v-col cols="12">
                <v-select
                  v-model="editedUser.roleId"
                  :items="roles"
                  item-title="name"
                  item-value="id"
                  label="Rol"
                  :rules="[v => !!v || 'Rol es obligatorio']"
                  required
                ></v-select>
              </v-col>
              
              <v-col cols="12">
                <v-switch
                  v-model="editedUser.status"
                  color="success"
                  label="Estado: Activo/Inactivo"
                  hide-details
                ></v-switch>
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
            @click="updateUser"
            :loading="usersStore.loading"
          >
            Actualizar
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
          variant="text"
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
import { useUsersStore } from '@/store/users';
import { useAuthStore } from '@/store/auth';
import { useRouter } from 'vue-router';

// Stores
const usersStore = useUsersStore();
const authStore = useAuthStore();
const router = useRouter();

// Referencias a los formularios para validación
const createForm = ref(null);
const editForm = ref(null);

// Estado local
const search = ref('');
const createDialog = ref(false);
const editDialog = ref(false);
const roles = ref([]);

// Usuario en edición y creación
const newUser = ref({
  username: '',
  fullName: '',
  email: '',
  password: '',
  roleId: null
});

const editedUser = ref({
  id: null,
  username: '',
  fullName: '',
  email: '',
  status: true,
  roleId: null
});

// Notificaciones con Snackbar
const snackbar = ref({
  show: false,
  text: '',
  color: 'success',
  timeout: 3000
});

// Columnas de la tabla
const headers = [
  { title: 'ID', key: 'id', sortable: true },
  { title: 'Usuario', key: 'username', sortable: true },
  { title: 'Nombre Completo', key: 'fullName', sortable: true },
  { title: 'Correo Electrónico', key: 'email', sortable: true },
  { title: 'Rol', key: 'role', sortable: false },
  { title: 'Estado', key: 'status', sortable: true },
  { title: 'Fecha Creación', key: 'createdAt', sortable: true },
  { title: 'Acciones', key: 'actions', sortable: false, align: 'end' }
];

// Cargar datos al montar el componente
onMounted(async () => {
  try {
    // Verificar que el usuario es administrador
    if (authStore.user?.role?.name !== 'ADMIN') {
      showSnackbar('No tienes permisos para acceder a esta página', 'error');
      return;
    }
    
    // Cargar roles para los selects
    await loadRoles();
    
    // Cargar lista de usuarios
    await usersStore.fetchUsers();
  } catch (error) {
    showSnackbar(`Error al cargar datos: ${error.message}`, 'error');
  }
});

// Funciones de utilidad
function getRoleColor(roleName) {
  switch (roleName) {
    case 'ADMIN':
      return 'primary';
    case 'Usuario':
      return 'info';
    default:
      return 'grey';
  }
}

async function loadRoles() {
  try {
    console.log('Cargando roles desde store...');
    roles.value = await usersStore.fetchRoles();
    console.log('Roles cargados:', roles.value);
  } catch (error) {
    console.error('Error al cargar roles:', error);
    // Usar roles por defecto en caso de error
    roles.value = [
      { id: 1, name: 'ADMIN' },
      { id: 2, name: 'Usuario' }
    ];
  }
}

// Funciones para CRUD
function openCreateDialog() {
  // Verificar que el usuario tiene permisos para crear usuarios
  if (authStore.user?.role?.name === 'ADMIN') {
    console.log('Redirigiendo a pantalla de creación de usuario...');
    router.push('/users/create');
  } else {
    console.warn('Usuario intenta crear un usuario sin tener permisos de ADMIN');
    showSnackbar('No tienes permisos para crear usuarios', 'error');
  }
}

function openEditDialog(user) {
  editedUser.value = {
    id: user.id,
    username: user.username,
    fullName: user.fullName,
    email: user.email,
    status: user.status,
    roleId: user.role.id
  };
  editDialog.value = true;
}

async function createUser() {
  try {
    // Validar formulario
    const { valid } = await createForm.value.validate();
    if (!valid) return;
    
    // Crear usuario
    await usersStore.createUser(newUser.value);
    
    // Cerrar diálogo y mostrar notificación
    createDialog.value = false;
    showSnackbar('Usuario creado exitosamente', 'success');
  } catch (error) {
    showSnackbar(`Error al crear usuario: ${error.message}`, 'error');
  }
}

async function updateUser() {
  try {
    // Validar formulario
    const { valid } = await editForm.value.validate();
    if (!valid) return;
    
    // Actualizar usuario
    await usersStore.updateUser(editedUser.value.id, {
      fullName: editedUser.value.fullName,
      email: editedUser.value.email,
      status: editedUser.value.status,
      roleId: editedUser.value.roleId
    });
    
    // Cerrar diálogo y mostrar notificación
    editDialog.value = false;
    showSnackbar('Usuario actualizado exitosamente', 'success');
  } catch (error) {
    showSnackbar(`Error al actualizar usuario: ${error.message}`, 'error');
  }
}

async function toggleUserStatus(user) {
  try {
    // Actualizar estado del usuario
    await usersStore.updateUser(user.id, {
      fullName: user.fullName,
      email: user.email,
      status: !user.status,
      roleId: user.role.id
    });
    
    // Mostrar notificación
    showSnackbar(`Usuario ${user.status ? 'desactivado' : 'activado'} exitosamente`, 'success');
  } catch (error) {
    showSnackbar(`Error al cambiar estado del usuario: ${error.message}`, 'error');
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

.action-buttons {
  display: flex;
  justify-content: flex-end;
}
</style>
