<template>
  <v-container fluid>
    <v-card>
      <v-card-title class="d-flex justify-space-between">
        <span>Editar Registro de Transferencia</span>
        <v-btn
          color="error"
          variant="outlined"
          prepend-icon="mdi-arrow-left"
          :to="{ name: 'registro-transferencia-detalle', params: { id } }"
          class="ml-2"
        >
          Cancelar
        </v-btn>
      </v-card-title>
      
      <v-card-text>
        <v-alert
          v-if="error"
          type="error"
          title="Error"
          :text="error"
          class="mb-4"
        ></v-alert>
        
        <v-skeleton-loader
          v-if="loading && !registroExistente"
          type="card-heading, form"
        ></v-skeleton-loader>
        
        <v-form v-else ref="form" @submit.prevent="actualizarRegistro">
          <v-row>
            <!-- Código -->
            <v-col cols="12" md="6">
              <v-text-field
                v-model="registro.codigo"
                label="Código"
                :rules="[v => !!v || 'Código es requerido']"
                required
                variant="outlined"
              ></v-text-field>
            </v-col>
            
            <!-- Número -->
            <v-col cols="12" md="6">
              <v-text-field
                v-model="registro.numero"
                label="Número"
                :rules="[v => !!v || 'Número es requerido']"
                required
                variant="outlined"
              ></v-text-field>
            </v-col>
            
            <!-- Fecha -->
            <v-col cols="12" md="6">
              <v-text-field
                v-model="registro.fecha"
                label="Fecha"
                type="date"
                :rules="[v => !!v || 'Fecha es requerida']"
                required
                variant="outlined"
              ></v-text-field>
            </v-col>
            
            <!-- Dependencia Origen -->
            <v-col cols="12" md="6">
              <v-select
                v-model="registro.dependenciaOrigen"
                :items="dependencias"
                label="Dependencia Origen"
                item-title="nombre"
                item-value="id"
                :rules="[v => !!v || 'Dependencia origen es requerida']"
                required
                variant="outlined"
              ></v-select>
            </v-col>
            
            <!-- Dependencia Destino -->
            <v-col cols="12" md="6">
              <v-select
                v-model="registro.dependenciaDestino"
                :items="dependencias"
                label="Dependencia Destino"
                item-title="nombre"
                item-value="id"
                :rules="[v => !!v || 'Dependencia destino es requerida']"
                required
                variant="outlined"
              ></v-select>
            </v-col>
            
            <!-- Responsable -->
            <v-col cols="12" md="6">
              <v-text-field
                v-model="registro.responsable"
                label="Responsable"
                :rules="[v => !!v || 'Responsable es requerido']"
                required
                variant="outlined"
              ></v-text-field>
            </v-col>
            
            <!-- Estado (solo editable si tiene permisos) -->
            <v-col cols="12" md="6" v-if="tienePermisoParaModificarEstado">
              <v-select
                v-model="registro.estado"
                :items="estadosDisponibles"
                label="Estado"
                :rules="[v => !!v || 'Estado es requerido']"
                required
                variant="outlined"
              ></v-select>
            </v-col>
            
            <!-- Observaciones -->
            <v-col cols="12">
              <v-textarea
                v-model="registro.observaciones"
                label="Observaciones"
                variant="outlined"
                rows="3"
              ></v-textarea>
            </v-col>
          </v-row>
          
          <div class="d-flex flex-column flex-sm-row gap-2 mt-5">
            <v-btn
              color="primary"
              type="submit"
              size="large"
              :loading="loading"
              prepend-icon="mdi-content-save"
            >
              Guardar Cambios
            </v-btn>
            
            <v-btn
              color="secondary"
              variant="outlined"
              size="large"
              :to="{ name: 'registro-transferencia-detalle', params: { id } }"
            >
              Cancelar
            </v-btn>
          </div>
        </v-form>
      </v-card-text>
    </v-card>
  </v-container>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useAuthStore } from '@/store/auth';

import { useRegistroTransferenciaStore } from '@/store/registro-transferencia';
// Stores
const route = useRoute();
const router = useRouter();
const inventariosStore = useRegistroTransferenciaStore();
const authStore = useAuthStore();

// Estado reactivo
const id = computed(() => route.params.id);
const loading = computed(() => inventariosStore.isLoading());
const error = ref(null);
const form = ref(null);
const registroExistente = computed(() => inventariosStore.getCurrentRegistroTransferencia());

// Estructura del registro para editar
const registro = ref({
  codigo: '',
  numero: '',
  fecha: '',
  dependenciaOrigen: null,
  dependenciaDestino: null,
  responsable: '',
  totalDocumentos: 0,
  observaciones: '',
  estado: 'BORRADOR'
});

// Datos para el formulario
const dependencias = ref([
  { id: 1, nombre: 'Dirección General' },
  { id: 2, nombre: 'Departamento de Archivo' },
  { id: 3, nombre: 'Departamento de Logística' },
  { id: 4, nombre: 'Departamento de Administración' }
]);

// Permisos
const esAprobador = computed(() => authStore.hasPermission('APROBAR_REGISTROS'));
const tienePermisoParaModificarEstado = computed(() => 
  authStore.hasPermission('EDITAR_ESTADO_REGISTROS') || esAprobador.value
);

// Estados disponibles según permisos del usuario
const estadosDisponibles = computed(() => {
  const estados = ['BORRADOR', 'FINALIZADO'];
  
  if (esAprobador.value) {
    estados.push('APROBADO');
  }
  
  return estados;
});

// Cargar el registro al montar el componente
onMounted(async () => {
  try {
    await inventariosStore.fetchRegistroTransferenciaById(id.value);
    if (!registroExistente.value) {
      error.value = 'No se encontró el registro de transferencia solicitado';
    }
  } catch (err) {
    error.value = `Error al cargar el registro: ${err.message}`;
  }
});

// Observar cambios en el registro existente para actualizar el formulario
watch(registroExistente, (newVal) => {
  if (newVal) {
    // Copiar propiedades para evitar modificar directamente el store
    registro.value = { ...newVal };
    
    // Formatear fecha si es necesario (al formato yyyy-MM-dd para el input date)
    if (registro.value.fecha && typeof registro.value.fecha === 'string') {
      const dateObj = new Date(registro.value.fecha);
      if (!isNaN(dateObj.getTime())) {
        registro.value.fecha = dateObj.toISOString().substr(0, 10);
      }
    }
  }
}, { immediate: true });

// Métodos
async function actualizarRegistro() {
  if (!form.value.validate()) {
    return;
  }

  try {
    const resultado = await inventariosStore.updateRegistroTransferencia(id.value, registro.value);
    
    if (resultado) {
      // Mostrar notificación de éxito
      alert('Registro de transferencia actualizado con éxito');
      
      // Redireccionar a la vista de detalle
      router.push({ name: 'registro-transferencia-detalle', params: { id: id.value } });
    }
  } catch (err) {
    error.value = `Error al actualizar el registro: ${err.message}`;
    console.error('Error:', err);
  }
}
</script>
