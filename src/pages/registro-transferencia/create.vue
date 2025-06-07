<template>
  <v-container fluid>
    <v-card>
      <v-card-title class="d-flex justify-space-between">
        <span>Crear Registro de Transferencia</span>
        <v-btn
          color="error"
          variant="outlined"
          prepend-icon="mdi-arrow-left"
          :to="{ name: 'registro-transferencia-list' }"
          class="ml-2"
        >
          Cancelar
        </v-btn>
      </v-card-title>
      
      <v-card-text>
        <v-form ref="form" @submit.prevent="guardarRegistro">
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
              Guardar Registro
            </v-btn>
            
            <v-btn
              color="secondary"
              variant="outlined"
              size="large"
              :loading="loading"
              :to="{ name: 'registro-transferencia-list' }"
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
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/store/auth';
import { useRegistroTransferenciaStore } from '@/store/registro-transferencia';

// Stores
const router = useRouter();
const inventariosStore = useRegistroTransferenciaStore();
const authStore = useAuthStore(); 

// Estado reactivo
const loading = computed(() => inventariosStore.isLoading());
const form = ref(null);
const dependencias = ref([
  { id: 1, nombre: 'Dirección General' },
  { id: 2, nombre: 'Departamento de Archivo' },
  { id: 3, nombre: 'Departamento de Logística' },
  { id: 4, nombre: 'Departamento de Administración' }
]);

// Datos del formulario
const registro = ref({
  codigo: '',
  numero: '',
  fecha: new Date().toISOString().substr(0, 10),
  dependenciaOrigen: null,
  dependenciaDestino: null,
  responsable: authStore.getUserFullName() || '',
  totalDocumentos: 0,
  observaciones: '',
  estado: 'BORRADOR' // Estado inicial
});

// Métodos
async function guardarRegistro() {
  if (!form.value.validate()) {
    return;
  }

  try {
    const resultado = await inventariosStore.createRegistroTransferencia(registro.value);
    
    if (resultado && resultado.id) {
      // Mostrar notificación de éxito
      alert('Registro de transferencia creado con éxito');
      
      // Redireccionar a la lista de registros
      router.push({ name: 'registro-transferencia-list' });
    }
  } catch (error) {
    console.error('Error al guardar el registro:', error);
    alert('Error al guardar el registro: ' + error.message);
  }
}
</script>
