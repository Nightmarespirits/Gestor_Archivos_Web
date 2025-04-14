<template>
  <div>
    <v-row>
      <v-col cols="12">
        <v-card>
          <v-card-title class="d-flex align-center">
            <v-btn icon class="mr-4" to="/users">
              <v-icon>mdi-arrow-left</v-icon>
            </v-btn>
            <span class="text-h5">Editar Usuario</span>
          </v-card-title>
          
          <v-card-text>
            <div v-if="loading" class="d-flex justify-center align-center" style="min-height: 300px;">
              <v-progress-circular indeterminate color="primary"></v-progress-circular>
            </div>
            
            <v-form v-else ref="form" @submit.prevent="updateUser">
              <v-row>
                <v-col cols="12" md="6">
                  <v-text-field
                    v-model="user.username"
                    label="Nombre de Usuario"
                    disabled
                    hint="El nombre de usuario no puede ser modificado"
                    persistent-hint
                  ></v-text-field>
                </v-col>
                
                <v-col cols="12" md="6">
                  <v-text-field
                    v-model="user.fullName"
                    label="Nombre Completo"
                    :rules="[v => !!v || 'Nombre completo es obligatorio']"
                    required
                  ></v-text-field>
                </v-col>
                
                <v-col cols="12" md="6">
                  <v-text-field
                    v-model="user.email"
                    label="Correo Electrónico"
                    type="email"
                    :rules="[
                      v => !!v || 'Correo electrónico es obligatorio',
                      v => /.+@.+\..+/.test(v) || 'Correo electrónico debe ser válido'
                    ]"
                    required
                  ></v-text-field>
                </v-col>
                
                <v-col cols="12" md="6">
                  <v-select
                    v-model="user.roleId"
                    :items="roles"
                    item-title="displayName"
                    item-value="id"
                    label="Rol"
                    :rules="[v => !!v || 'Rol es obligatorio']"
                    required
                  ></v-select>
                </v-col>
                
                <v-col cols="12" md="6">
                  <v-switch
                    v-model="user.status"
                    color="success"
                    label="Estado: Activo/Inactivo"
                    hint="Los usuarios inactivos no pueden acceder al sistema"
                    persistent-hint
                  ></v-switch>
                </v-col>
              </v-row>
              
              <v-divider class="my-6"></v-divider>
              
              <v-row>
                <v-col cols="12">
                  <h3 class="text-h6 mb-4">Cambiar Contraseña (opcional)</h3>
                  
                  <v-text-field
                    v-model="newPassword"
                    label="Nueva Contraseña"
                    type="password"
                    autocomplete="new-password"
                    :rules="[
                      newPassword ? v => v.length >= 6 || 'La contraseña debe tener al menos 6 caracteres' : () => true
                    ]"
                    hint="Deja en blanco si no deseas cambiar la contraseña"
                    persistent-hint
                  ></v-text-field>
                </v-col>
              </v-row>
            </v-form>
          </v-card-text>
          
          <v-card-actions>
            <v-spacer></v-spacer>
            <v-btn color="error" variant="outlined" to="/users">
              Cancelar
            </v-btn>
            <v-btn 
              color="primary" 
              @click="updateUser"
              :loading="usersStore.loading"
              :disabled="loading || !formValid || usersStore.loading"
            >
              Actualizar Usuario
            </v-btn>
          </v-card-actions>
        </v-card>
      </v-col>
    </v-row>
    
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
import { useRouter, useRoute } from 'vue-router';
import { useUsersStore } from '@/store/users';
import { useAuthStore } from '@/store/auth';

// Stores y router
const usersStore = useUsersStore();
const authStore = useAuthStore();
const router = useRouter();
const route = useRoute();

// Referencias
const form = ref(null);
const formValid = computed(() => true); // Se actualizará con la validación del formulario

// Estado local
const loading = ref(true);
const user = ref({
  id: null,
  username: '',
  fullName: '',
  email: '',
  status: true,
  roleId: null
});
const newPassword = ref('');

const roles = ref([]);
const snackbar = ref({
  show: false,
  text: '',
  color: 'success',
  timeout: 3000
});

// Verificar permisos y cargar datos al montar el componente
onMounted(async () => {
  try {
    // Obtener el rol del usuario
    const userRoleObj = authStore.user?.role;
    console.log('Rol del usuario en edit.vue:', userRoleObj);
    
    // Extraer el nombre del rol, considerando diferentes estructuras posibles
    let userRole = '';
    if (typeof userRoleObj === 'string') {
      userRole = userRoleObj;
    } else if (userRoleObj && typeof userRoleObj === 'object') {
      userRole = userRoleObj.name || userRoleObj.roleName || '';
    }
    
    // Normalizar el rol para comparación (convertir a mayúsculas y eliminar prefijos comunes)
    const normalizedUserRole = userRole.toUpperCase().replace('ROLE_', '');
    
    // Verificar si el rol del usuario es admin (considerando diferentes formatos)
    const isAdmin = normalizedUserRole === 'ADMIN' || normalizedUserRole === 'ADMINISTRADOR';
    
    // Verificar que el usuario es administrador
    if (!isAdmin) {
      showSnackbar('No tienes permisos para acceder a esta página', 'error');
      router.push('/unauthorized');
      return;
    }
    
    // Obtener el ID del usuario de la URL
    const userId = route.params.id;
    
    // Cargar roles para el select
    await loadRoles();
    
    // Cargar los datos del usuario
    await fetchUser(userId);
  } catch (error) {
    showSnackbar(`Error al cargar datos: ${error.message}`, 'error');
    setTimeout(() => router.push('/users'), 2000);
  } finally {
    loading.value = false;
  }
});

// Cargar roles disponibles
async function loadRoles() {
  try {
    console.log('Cargando roles disponibles...');
    const rolesData = await usersStore.fetchRoles();
    console.log('Roles cargados desde API:', rolesData);
    
    // Añadir displayName para mostrar en el select con formato mejor
    roles.value = rolesData.map(role => ({
      ...role,
      displayName: role.name.charAt(0).toUpperCase() + role.name.slice(1).toLowerCase() + 
                  (role.description ? ` - ${role.description}` : '')
    }));
    
    console.log('Roles procesados para mostrar:', roles.value);
  } catch (error) {
    console.error('Error al cargar roles:', error);
    showSnackbar(`Error al cargar roles: ${error.message}`, 'error');
  }
}

// Cargar datos del usuario
async function fetchUser(userId) {
  try {
    console.log(`Intentando cargar usuario con ID ${userId}`);
    const userData = await usersStore.fetchUserById(userId);
    
    if (!userData) {
      throw new Error('No se encontró el usuario');
    }
    
    console.log('Datos del usuario recibidos:', userData);
    
    // Extraer el ID del rol de manera segura
    let roleId = null;
    if (userData.role) {
      if (typeof userData.role === 'object') {
        roleId = userData.role.id;
      } else if (typeof userData.role === 'number') {
        roleId = userData.role;
      }
    } else if (userData.roleId) {
      roleId = userData.roleId;
    }
    
    // Si no se pudo determinar el roleId, usar un valor por defecto
    if (roleId === null) {
      console.warn('No se pudo determinar el rol del usuario, usando rol por defecto');
      // Buscar el rol de administrador en los roles disponibles
      const adminRole = roles.value.find(r => 
        r.name.toUpperCase() === 'ADMIN' || 
        r.name.toUpperCase() === 'ADMINISTRADOR'
      );
      roleId = adminRole ? adminRole.id : 1; // Usar 1 como fallback
    }
    
    user.value = {
      id: userData.id,
      username: userData.username || '',
      fullName: userData.fullName || '',
      email: userData.email || '',
      status: userData.status !== undefined ? userData.status : true,
      roleId: roleId
    };
    
    console.log('Datos del usuario procesados:', user.value);
  } catch (error) {
    console.error(`Error al cargar usuario con ID ${userId}:`, error);
    showSnackbar(`Error al cargar usuario: ${error.message}`, 'error');
    throw error;
  }
}

// Actualizar usuario
async function updateUser() {
  try {
    const { valid } = await form.value.validate();
    
    if (!valid) {
      showSnackbar('Por favor, corrige los errores en el formulario', 'error');
      return;
    }
    
    // Preparar datos para actualización
    const updateData = {
      fullName: user.value.fullName,
      email: user.value.email,
      status: user.value.status,
      roleId: user.value.roleId
    };
    
    // Añadir contraseña solo si se ha especificado una nueva
    if (newPassword.value) {
      updateData.password = newPassword.value;
    }
    
    console.log('Enviando datos de actualización:', updateData);
    
    // Mostrar indicador de carga
    loading.value = true;
    
    // Enviar actualización
    try {
      const updatedUser = await usersStore.updateUser(user.value.id, updateData);
      console.log('Usuario actualizado correctamente:', updatedUser);
      showSnackbar('Usuario actualizado exitosamente', 'success');
      
      // Redirigir a la lista de usuarios después de un breve retraso
      setTimeout(() => {
        router.push('/users');
      }, 1500);
    } catch (apiError) {
      console.error('Error en la API al actualizar usuario:', apiError);
      showSnackbar(`Error al actualizar usuario: ${apiError.message || 'Error desconocido'}`, 'error');
    } finally {
      loading.value = false;
    }
  } catch (error) {
    console.error('Error general al actualizar usuario:', error);
    showSnackbar(`Error al actualizar usuario: ${error.message || 'Error desconocido'}`, 'error');
    loading.value = false;
  }
}

// Mostrar notificación
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
.v-card {
  border-radius: 8px;
}
</style>
