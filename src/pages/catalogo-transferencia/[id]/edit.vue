<template>
  <v-container fluid>
    <v-card>
      <v-card-title class="d-flex justify-space-between">
        <span>Editar Catálogo de Transferencia</span>
        <v-row class="mt-4">
          <v-col cols="12" class="d-flex justify-space-between">
            <v-btn
              color="info"
              variant="outlined"
              :to="{ name: 'catalogo-transferencia-detalles', params: { id } }"
              prepend-icon="mdi-table-edit"
            >
              Gestionar Detalles
            </v-btn>
            
            <div>
              <v-btn
                color="error"
                variant="outlined"
                :to="{ name: 'catalogo-transferencia-detalle', params: { id } }"
                class="ml-2"
              >
                Cancelar
              </v-btn>
            </div>
          </v-col>
        </v-row>
      </v-card-title>
      
      <v-card-text>
        <v-alert
          v-if="error"
          type="error"
          title="Error"
          :text="error"
          class="mb-4"
        ></v-alert>
        <!--
        <v-skeleton-loader
          v-if="loading && !catalogoExistente"
          type="card-heading, form"
        ></v-skeleton-loader>
        -->
        <v-form v-else ref="form" @submit.prevent="actualizarCatalogo">
          <v-row>
            <!-- Nombre Entidad -->
            <v-col cols="12" md="6">
              <v-text-field
                v-model="catalogo.nombreEntidad"
                label="Nombre Entidad"
                :rules="[v => !!v || 'Nombre de entidad es requerido']"
                required
                variant="outlined"
              ></v-text-field>
            </v-col>
            
            <!-- Unidad Organización -->
            <v-col cols="12" md="6">
              <v-text-field
                v-model="catalogo.unidadOrganizacion"
                label="Unidad de Organización"
                variant="outlined"
              ></v-text-field>
            </v-col>
            
            <!-- Sección -->
            <v-col cols="12" md="6">
              <v-text-field
                v-model="catalogo.seccion"
                label="Sección"
                variant="outlined"
              ></v-text-field>
            </v-col>
            
            <!-- Nivel Descripción -->
            <v-col cols="12" md="6">
              <v-text-field
                v-model="catalogo.nivelDescripcion"
                label="Nivel de Descripción"
                variant="outlined"
              ></v-text-field>
            </v-col>

            <!-- Serie Documental -->
            <v-col cols="12" md="6">
              <v-text-field
                v-model="catalogo.serieDocumental"
                label="Serie Documental"
                variant="outlined"
              ></v-text-field>
            </v-col>
            
            <!-- Código de Referencia -->
            <v-col cols="12" md="6">
              <v-text-field
                v-model="catalogo.codigoReferencia"
                label="Código de Referencia"
                required
                :rules="[v => !!v || 'Código de referencia es requerido']"
                variant="outlined"
              ></v-text-field>
            </v-col>

            <!-- Soporte -->
            <v-col cols="12" md="6">
              <v-select
                v-model="catalogo.soporte"
                :items="soportes"
                label="Soporte"
                variant="outlined"
              ></v-select>
            </v-col>

            <!-- Volumen en Metros Lineales -->
            <v-col cols="12" md="6">
              <v-text-field
                v-model="catalogo.volumenMetrosLineales"
                label="Volumen (metros lineales)"
                type="number"
                step="0.01"
                variant="outlined"
              ></v-text-field>
            </v-col>
            
            <!-- Responsable Sección -->
            <v-col cols="12" md="6">
              <v-text-field
                v-model="catalogo.responsableSeccion"
                label="Responsable de Sección"
                variant="outlined"
              ></v-text-field>
            </v-col>        
            
            <!-- Inventario Elaborado Por -->
            <v-col cols="12" md="6">
              <v-text-field
                v-model="catalogo.inventarioElaboradoPor"
                label="Elaborado por"
                variant="outlined"
              ></v-text-field>
            </v-col>
            
            <!-- Número/Año Remisión -->
            <v-col cols="12" md="6">
              <v-text-field
                v-model="catalogo.numeroAnioRemision"
                label="Número/Año Remisión"
                variant="outlined"
              ></v-text-field>
            </v-col>
            
            <!-- Lugar y Fecha Elaboración -->
            <v-col cols="12" md="6">
              <v-text-field
                v-model="catalogo.lugarFechaElaboracion"
                label="Lugar y fecha de elaboración"
                variant="outlined"
              ></v-text-field>
            </v-col>
            
            <!-- Visto Bueno Responsable -->
            <v-col cols="12" md="6">
              <v-text-field
                v-model="catalogo.vistoBuenoResponsable"
                label="Visto Bueno Responsable"
                variant="outlined"
              ></v-text-field>
            </v-col>
            
            <!-- Observaciones -->
            <v-col cols="12">
              <v-textarea
                v-model="catalogo.observaciones"
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
              :to="{ name: 'catalogo-transferencia-detalle', params: { id } }"
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
import { useCatalogoTransferenciaStore } from '@/store/catalogo-transferencia';  
import { useAuthStore } from '@/store/auth';

// Stores
const route = useRoute();
const router = useRouter();
const inventariosStore = useCatalogoTransferenciaStore();
const authStore = useAuthStore();

// Estado reactivo
const id = computed(() => route.params.id);
const loading = computed(() => inventariosStore.isLoading());
const error = ref(null);
const form = ref(null);
const catalogoExistente = computed(() => inventariosStore.getCurrentCatalogoTransferencia());

// Estructura del catálogo para editar
const catalogo = ref({
  nombreEntidad: '',
  unidadOrganizacion: '',
  seccion: '',
  nivelDescripcion: '',
  serieDocumental: '',
  codigoReferencia: '',
  volumenMetrosLineales: 0,
  responsableSeccion: '',
  inventarioElaboradoPor: '',
  numeroAnioRemision: '',
  lugarFechaElaboracion: '',
  vistoBuenoResponsable: '',
  soporte: 'PAPEL',
  observaciones: '',
});

// Datos para los selectores
const soportes = ['PAPEL', 'DIGITAL', 'AUDIOVISUAL', 'MICROFILM', 'OTRO'];

// Cargar el catálogo al montar el componente
onMounted(async () => {
  try {
    await inventariosStore.fetchCatalogoTransferenciaById(id.value);
    if (!catalogoExistente.value) {
      error.value = 'No se encontró el catálogo de transferencia solicitado';
    }
  } catch (err) {
    error.value = `Error al cargar el catálogo: ${err.message}`;
  }
});

// Observar cambios en el catálogo existente para actualizar el formulario
watch(catalogoExistente, (newVal) => {
  if (newVal) {
    // Copiar propiedades para evitar modificar directamente el store
    catalogo.value = { ...newVal };
  }
}, { immediate: true });

// Métodos
async function actualizarCatalogo() {
  if (!form.value.validate()) {
    return;
  }

  try {
    const resultado = await inventariosStore.updateCatalogoTransferencia(id.value, catalogo.value);
    
    if (resultado) {
      // Mostrar notificación de éxito
      alert('Catálogo de transferencia actualizado con éxito');
      
      // Redireccionar a la vista de detalle
      router.push({ name: 'catalogo-transferencia-detalle', params: { id: id.value } });
    }
  } catch (err) {
    error.value = `Error al actualizar el catálogo: ${err.message}`;
    console.error('Error:', err);
  }
}
</script>
