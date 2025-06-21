<template>
  <v-container fluid>
    <v-card>
      <v-card-title class="d-flex justify-space-between align-center">
        <span>Crear Nuevo Inventario</span>
        <v-btn 
          color="secondary" 
          prepend-icon="mdi-arrow-left"
          :to="{ name: 'inventario-list' }"
        >
          Volver
        </v-btn>
      </v-card-title>

      <v-card-text>
        <v-form ref="form" v-model="isFormValid" @submit.prevent="submitForm">
          <!-- Información general del inventario -->
          <v-row>
            <v-col cols="12" sm="6">
              <v-text-field
                v-model="inventario.titulo"
                label="Título del inventario"
                :rules="[v => !!v || 'El título es requerido']"
                required
                variant="outlined"
                density="comfortable"
              ></v-text-field>
            </v-col>
            <v-col cols="12" sm="6">
              <v-text-field
                v-model="inventario.unidadAdministrativa"
                label="Unidad Administrativa"
                :rules="[v => !!v || 'La unidad administrativa es requerida']"
                required
                variant="outlined"
                density="comfortable"
              ></v-text-field>
            </v-col>
          </v-row>

          <!-- Información basada en el DTO de Inventario -->
          <v-row>
            <v-col cols="12" sm="6">
              <v-text-field
                v-model="inventario.numeroAnioRemision"
                label="Número y Año de Remisión"
                hint="Formato: Número-Año"
                persistent-hint
                variant="outlined"
                density="comfortable"
              ></v-text-field>
            </v-col>
            <v-col cols="12" sm="6">
              <v-text-field
                v-model="inventario.seccion"
                label="Sección"
                hint="Sección a la que pertenece el inventario"
                persistent-hint
                variant="outlined"
                density="comfortable"
              ></v-text-field>
            </v-col>
          </v-row>

          <v-row>
            <v-col cols="12" sm="6">
              <v-menu
                v-model="menuFechaTransferencia"
                :close-on-content-click="false"
                transition="scale-transition"
                min-width="auto"
              >
                <template v-slot:activator="{ props }">
                  <v-text-field
                    v-model="fechaTransferenciaFormatted"
                    label="Fecha de Transferencia"
                    prepend-icon="mdi-calendar"
                    readonly
                    v-bind="props"
                    variant="outlined"
                    density="comfortable"
                  ></v-text-field>
                </template>
                <v-date-picker
                  v-model="inventario.fechaTransferencia"
                  @update:model-value="menuFechaTransferencia = false"
                ></v-date-picker>
              </v-menu>
            </v-col>
            <v-col cols="12" sm="6">
              <v-text-field
                v-model.number="inventario.totalVolumen"
                label="Volumen Total (m3)"
                type="number"
                step="0.01"
                variant="outlined"
                density="comfortable"
                :rules="[
                  v => v !== null || 'El volumen total es requerido',
                  v => (v >= 0) || 'El volumen debe ser un número positivo'
                ]"
                required
              ></v-text-field>
            </v-col>
          </v-row>

          <v-row>
            <v-col cols="12" sm="6">
              <v-menu
                v-model="menuFechaInicial"
                :close-on-content-click="false"
                transition="scale-transition"
                min-width="auto"
              >
                <template v-slot:activator="{ props }">
                  <v-text-field
                    v-model="fechaInicialFormatted"
                    label="Fecha Inicial"
                    prepend-icon="mdi-calendar"
                    readonly
                    variant="outlined"
                    density="comfortable"
                    v-bind="props"
                    :rules="[v => !!inventario.fechaInicial || 'La fecha inicial es requerida']"
                    required
                  ></v-text-field>
                </template>
                <v-date-picker
                  v-model="inventario.fechaInicial"
                  @update:model-value="menuFechaInicial = false"
                ></v-date-picker>
              </v-menu>
            </v-col>
            <v-col cols="12" sm="6">
              <v-menu
                v-model="menuFechaFinal"
                :close-on-content-click="false"
                transition="scale-transition"
                min-width="auto"
              >
                <template v-slot:activator="{ props }">
                  <v-text-field
                    v-model="fechaFinalFormatted"
                    label="Fecha Final"
                    prepend-icon="mdi-calendar"
                    readonly
                    variant="outlined"
                    density="comfortable"
                    v-bind="props"
                    :rules="[
                      v => !!inventario.fechaFinal || 'La fecha final es requerida',
                      v => !inventario.fechaInicial || inventario.fechaFinal >= inventario.fechaInicial || 'La fecha final debe ser posterior a la fecha inicial'
                    ]"
                    required
                  ></v-text-field>
                </template>
                <v-date-picker
                  v-model="inventario.fechaFinal"
                  @update:model-value="menuFechaFinal = false"
                ></v-date-picker>
              </v-menu>
            </v-col>
          </v-row>

          <v-row>
            <v-col cols="12" sm="6">
              <v-text-field
                v-model="inventario.lugarFechaEntrega"
                label="Lugar y Fecha de Entrega"
                hint="Ej: Lima, 01 de junio de 2023"
                persistent-hint
                variant="outlined"
                density="comfortable"
              ></v-text-field>
            </v-col>
            <v-col cols="12" sm="6">
              <v-text-field
                v-model="inventario.lugarFechaRecepcion"
                label="Lugar y Fecha de Recepción"
                variant="outlined"
                density="comfortable"
              ></v-text-field>
            </v-col>
          </v-row>

          <!-- Información adicional -->
          <v-row>
            <v-col cols="12" sm="6">
              <v-text-field
                v-model="inventario.firmaSelloAutoridadEntrega"
                label="Firma/Sello de Autoridad que Entrega"
                variant="outlined"
                density="comfortable"
              ></v-text-field>
            </v-col>
            <v-col cols="12" sm="6">
              <v-text-field
                v-model="inventario.firmaSelloAutoridadRecibe"
                label="Firma/Sello de Autoridad que Recibe"
                variant="outlined"
                density="comfortable"
              ></v-text-field>
            </v-col>
          </v-row>

          <v-row>
            <v-col cols="12" sm="6">
              <v-text-field
                v-model="inventario.estadoConservacion"
                label="Estado de Conservación"
                variant="outlined"
                density="comfortable"
              ></v-text-field>
            </v-col>
            <v-col cols="12" sm="6">
              <v-textarea
                v-model="inventario.observaciones"
                label="Observaciones"
                rows="3"
                auto-grow
                variant="outlined"
                density="comfortable"
              ></v-textarea>
            </v-col>
          </v-row>

          <!-- Botones de acción -->
          <v-row class="mt-4">
            <v-col cols="12" class="d-flex justify-end">
              <v-btn
                color="secondary"
                class="mr-4"
                :to="{ name: 'inventario-list' }"
              >
                Cancelar
              </v-btn>
              <v-btn
                color="primary"
                type="submit"
                :loading="loading"
                :disabled="!isFormValid"
              >
                Guardar Inventario
              </v-btn>
            </v-col>
          </v-row>
        </v-form>
      </v-card-text>
    </v-card>
  </v-container>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import { useInventariosStore } from '@/store/inventarios';
import { useUserPermissionsStore } from '@/store/userPermissions'; 
import { useAuthStore } from '@/store/auth';

// Stores y composables
const inventariosStore = useInventariosStore();
const authStore = useAuthStore();
const userPermissions = useUserPermissionsStore();
const router = useRouter();
const snackbar = ref({
  show: false,
  text: '',
  color: 'success',
  timeout: 3000
});

// Estado
const loading = ref(false);
const isFormValid = ref(false);
const menuFechaTransferencia = ref(false);
const menuFechaInicial = ref(false);
const menuFechaFinal = ref(false);

// Formateo de fechas para mostrar en UI
const fechaTransferenciaFormatted = computed(() => {
  return inventario.fechaTransferencia ? 
    new Date(inventario.fechaTransferencia).toLocaleDateString() : '';
});

const fechaInicialFormatted = computed(() => {
  return inventario.fechaInicial ? 
    new Date(inventario.fechaInicial).toLocaleDateString() : '';
});

const fechaFinalFormatted = computed(() => {
  return inventario.fechaFinal ? 
    new Date(inventario.fechaFinal).toLocaleDateString() : '';
});
const form = ref(null);
const menuFechaInicio = ref(false);
const menuFechaFin = ref(false);

// Verificar permisos
onMounted(() => {
  if (!userPermissions.hasPermission('DOCUMENT_UPDATE')) {
    snackbar.value.show = true;
    snackbar.value.text = 'No tienes permiso para crear inventarios';
    snackbar.value.color = 'error';
    router.push({ name: 'inventario-list' });
  }
  
  // Prellenar el responsable con el usuario actual si está disponible
  if (authStore.user) {
    inventario.responsable = `${authStore.user.nombre} ${authStore.user.apellido}`;
    inventario.cargoResponsable = authStore.user.cargo || '';
  }
  
  // Prellenar año actual
  inventario.anio = new Date().getFullYear();
});

// Datos del inventario
const inventario = reactive({
  titulo: '',
  unidadAdministrativa: '', // Cambiado de dependencia a unidadAdministrativa
  numeroAnioRemision: '', // Cambiado para coincidir con DTO
  seccion: '',
  fechaTransferencia: null,
  totalVolumen: 0,
  lugarFechaEntrega: '',
  lugarFechaRecepcion: '',
  firmaSelloAutoridadEntrega: '',
  firmaSelloAutoridadRecibe: '',
  fechaInicial: null, // Cambiado de fechaInicio
  fechaFinal: null, // Cambiado de fechaFin
  estadoConservacion: '',
  observaciones: '',
  estado: 'BORRADOR', // Estado por defecto
  documentoIds: [], // Lista vacía para evitar NullPointerException
  detalles: [] // Se agregarán después de crear el inventario
});

// Métodos
async function submitForm() {
  if (!form.value.validate()) return;
  
  try {
    loading.value = true;
    
    // Crear el inventario
    const createdInventario = await inventariosStore.createInventario(inventario);
    
    if (createdInventario && createdInventario.id) {
      snackbar.value.show = true;
      snackbar.value.text = 'Inventario creado correctamente';
      snackbar.value.color = 'success';
      
      // Redirigir a la página de edición para agregar detalles
      router.push({ 
        name: 'inventario-edit', 
        params: { id: createdInventario.id } 
      });
    } else {
      snackbar.value.show = true;
      snackbar.value.text = 'Error al crear el inventario';
      snackbar.value.color = 'error';
    }
  } catch (error) {
    snackbar.value.show = true;
    snackbar.value.text = 'Error: ' + (error.message || 'No se pudo crear el inventario');
    snackbar.value.color = 'error';
    console.error('Error al crear inventario:', error);
  } finally {
    loading.value = false;
  }
}
</script>
