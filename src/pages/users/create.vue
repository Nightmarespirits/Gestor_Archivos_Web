<template>
  <div>
    <v-row>
      <v-col cols="12">
        <v-card>
          <v-card-title class="d-flex align-center">
            <v-btn icon class="mr-4" to="/users">
              <v-icon>mdi-arrow-left</v-icon>
            </v-btn>
            <span class="text-h5">Crear Nuevo Usuario</span>
          </v-card-title>
          
          <v-card-text>
            <v-form ref="form" @submit.prevent="createUser">
              <v-row>
                <v-col cols="12" md="6">
                  <v-text-field
                    v-model="user.username"
                    label="Nombre de Usuario"
                    :rules="[v => !!v || 'Nombre de usuario es obligatorio']"
                    required
                    autocomplete="off"
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
                  <v-text-field
                    v-model="user.password"
                    label="Contraseña"
                    type="password"
                    :rules="[
                      v => !!v || 'Contraseña es obligatoria',
                      v => v.length >= 6 || 'La contraseña debe tener al menos 6 caracteres'
                    ]"
                    required
                    autocomplete="new-password"
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
              @click="createUser"
              :loading="usersStore.loading"
              :disabled="!formValid || usersStore.loading"
            >
              Guardar Usuario
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
import { useRouter } from 'vue-router';
import { useUsersStore } from '@/store/users';
import { useAuthStore } from '@/store/auth';

// Stores
const usersStore = useUsersStore();
const authStore = useAuthStore();
const router = useRouter();

// Referencias
const form = ref(null);
const formValid = computed(() => true); // Se actualizará con la validación del formulario

// Estado local
const user = ref({
  username: '',
  fullName: '',
  email: '',
  password: '',
  roleId: null
});

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
    // Cargar roles para el select
    await loadRoles();
  } catch (error) {
    showSnackbar(`Error al cargar datos: ${error.message}`, 'error');
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
  } catch (err) {
    console.error('Error al cargar roles:', err);
    showSnackbar(`Error al cargar roles: ${err.message}`, 'error');
  }
}

// Crear usuario
async function createUser() {
  try {
    if (!form.value) return;
    
    const isValid = await form.value.validate();
    if (!isValid.valid) {
      return showSnackbar('Por favor completa correctamente todos los campos requeridos', 'warning');
    }
    
    console.log('Creando usuario con datos:', user.value);
    
    // Verificar que existe un token válido antes de intentar crear el usuario
    const token = localStorage.getItem('authToken');
    if (!token) {
      showSnackbar('No tienes una sesión válida, vuelve a iniciar sesión', 'error');
      return authStore.logout();
    }
    
    // Crear el usuario
    await usersStore.createUser(user.value);
    showSnackbar('Usuario creado exitosamente', 'success');
    router.push('/users');
  } catch (err) {
    console.error('Error al crear usuario:', err);
    
    // Manejar específicamente el error 403
    if (err.message && err.message.includes('403')) {
      showSnackbar('No tienes permisos para crear usuarios. Contacta al administrador.', 'error');
    } else {
      showSnackbar(`Error al crear usuario: ${err.message}`, 'error');
    }
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
