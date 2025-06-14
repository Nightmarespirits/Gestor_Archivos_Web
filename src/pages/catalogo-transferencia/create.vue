<template>
  <v-container fluid>
    <v-card>
      <v-card-title class="d-flex justify-space-between">
        <span>Crear Catálogo de Transferencia</span>
        <v-btn
          color="error"
          variant="outlined"
          prepend-icon="mdi-arrow-left"
          :to="{ name: 'catalogo-transferencia-list' }"
          class="ml-2"
        >
          Cancelar
        </v-btn>
      </v-card-title>
      
      <v-card-text>
        <v-form ref="form" @submit.prevent="guardarCatalogo">
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
          
          <!-- Sección para Gestión de Detalles -->
          <v-divider class="my-6"></v-divider>
          
          <h3 class="text-h5 mb-4">Detalles del Catálogo</h3>
          
          <!-- Tabla de detalles existentes -->
          <v-card variant="outlined" class="mb-4">
            <v-card-title class="d-flex align-center">
              <span>Lista de Ítems</span>
              <v-spacer></v-spacer>
              <v-btn
                color="primary"
                variant="text"
                size="small"
                prepend-icon="mdi-plus"
                @click="modalDetalle = true"
              >
                Añadir Ítem
              </v-btn>
            </v-card-title>
            
            <v-card-text>
              <v-table v-if="catalogo.detalles.length > 0">
                <thead>
                  <tr>
                    <th>Nº Item</th>
                    <th>Nº Caja</th>
                    <th>Nº Tomo/Paquete</th>
                    <th>Nº Unidad Documental</th>
                    <th>Fecha</th>
                    <th>Contenido</th>
                    <th>Folios</th>
                    <th>Acciones</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="(detalle, index) in catalogo.detalles" :key="index">
                    <td>{{ detalle.numeroItem }}</td>
                    <td>{{ detalle.numeroCaja }}</td>
                    <td>{{ detalle.numeroTomoPaquete }}</td>
                    <td>{{ detalle.numeroUnidadDocumental }}</td>
                    <td>{{ detalle.fechaUnidadDocumental ? formatDate(detalle.fechaUnidadDocumental) : '-' }}</td>
                    <td>{{ detalle.alcanceContenido ? truncateText(detalle.alcanceContenido, 30) : '-' }}</td>
                    <td>{{ detalle.cantidadFolios }}</td>
                    <td>
                      <v-btn
                        icon="mdi-pencil"
                        size="x-small"
                        variant="text"
                        color="primary"
                        @click="editarDetalle(index)"
                      ></v-btn>
                      <v-btn
                        icon="mdi-delete"
                        size="x-small"
                        variant="text"
                        color="error"
                        @click="eliminarDetalle(index)"
                      ></v-btn>
                    </td>
                  </tr>
                </tbody>
              </v-table>
              <v-alert
                v-else
                type="info"
                text="No hay detalles agregados. Haga clic en 'Añadir Ítem' para agregar el primer detalle al catálogo."
                variant="tonal"
                class="mt-2"
              ></v-alert>
            </v-card-text>
          </v-card>
          
          <div class="d-flex flex-column flex-sm-row gap-2 mt-5">
            <v-btn
              color="primary"
              type="submit"
              size="large"
              :loading="loading"
              prepend-icon="mdi-content-save"
            >
              Guardar Catálogo
            </v-btn>
            
            <v-btn
              color="secondary"
              variant="outlined"
              size="large"
              :loading="loading"
              :to="{ name: 'catalogo-transferencia-list' }"
            >
              Cancelar
            </v-btn>
          </div>
        </v-form>
      </v-card-text>
    </v-card>
  </v-container>

  <!-- Modal para Añadir/Editar Detalle -->
  <v-dialog v-model="modalDetalle" max-width="800px">
    <v-card>
      <v-card-title>
        {{ detalleIndex >= 0 ? 'Editar' : 'Añadir' }} Ítem
      </v-card-title>
      
      <v-card-text>
        <v-form @submit.prevent="agregarDetalle">
          <v-row>
            <!-- Número de Item -->
            <v-col cols="12" md="4">
              <v-text-field
                v-model="detalleForm.numeroItem"
                label="Número de Item"
                type="number"
                :rules="[v => !!v || 'Número de item es requerido']"
                required
                variant="outlined"
              ></v-text-field>
            </v-col>
            
            <!-- Número de Caja -->
            <v-col cols="12" md="4">
              <v-text-field
                v-model="detalleForm.numeroCaja"
                label="Número de Caja"
                variant="outlined"
              ></v-text-field>
            </v-col>
            
            <!-- Número de Tomo/Paquete -->
            <v-col cols="12" md="4">
              <v-text-field
                v-model="detalleForm.numeroTomoPaquete"
                label="Número de Tomo/Paquete"
                variant="outlined"
              ></v-text-field>
            </v-col>
            
            <!-- Número de Unidad Documental -->
            <v-col cols="12" md="6">
              <v-text-field
                v-model="detalleForm.numeroUnidadDocumental"
                label="Número de Unidad Documental"
                variant="outlined"
              ></v-text-field>
            </v-col>
            
            <!-- Fecha de Unidad Documental -->
            <v-col cols="12" md="6">
              <v-text-field
                v-model="detalleForm.fechaUnidadDocumental"
                label="Fecha de Unidad Documental"
                type="date"
                variant="outlined"
              ></v-text-field>
            </v-col>
            
            <!-- Alcance y Contenido -->
            <v-col cols="12" md="6">
              <v-textarea
                v-model="detalleForm.alcanceContenido"
                label="Alcance y Contenido"
                variant="outlined"
                rows="3"
              ></v-textarea>
            </v-col>
            
            <!-- Información Adicional -->
            <v-col cols="12" md="6">
              <v-textarea
                v-model="detalleForm.informacionAdicional"
                label="Información Adicional"
                variant="outlined"
                rows="3"
              ></v-textarea>
            </v-col>
            
            <!-- Cantidad de Folios -->
            <v-col cols="12" md="6">
              <v-text-field
                v-model="detalleForm.cantidadFolios"
                label="Cantidad de Folios"
                type="number"
                variant="outlined"
              ></v-text-field>
            </v-col>
            
            <!-- Observaciones -->
            <v-col cols="12" md="6">
              <v-textarea
                v-model="detalleForm.observaciones"
                label="Observaciones"
                variant="outlined"
                rows="3"
              ></v-textarea>
            </v-col>
          </v-row>
        </v-form>
      </v-card-text>
      
      <v-card-actions>
        <v-spacer></v-spacer>
        <v-btn
          variant="text"
          @click="cerrarModalDetalle"
        >
          Cancelar
        </v-btn>
        <v-btn
          color="primary"
          @click="agregarDetalle"
        >
          Guardar
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { format } from 'date-fns';
import { useRouter } from 'vue-router';
import { useCatalogoTransferenciaStore } from '@/store/catalogo-transferencia';  
import { useAuthStore } from '@/store/auth';

// Stores
const router = useRouter();
const inventariosStore = useCatalogoTransferenciaStore();
const authStore = useAuthStore();

// Estado reactivo
const loading = computed(() => inventariosStore.isLoading());
const form = ref(null);
const modalDetalle = ref(false);
const detalleIndex = ref(-1); // -1 indica nuevo detalle, otro valor indica edición

// Datos para los selectores
const soportes = ['PAPEL', 'DIGITAL', 'AUDIOVISUAL', 'MICROFILM', 'OTRO'];

// Función para obtener valores predeterminados del catálogo
function getDefaultCatalogo() {
  const fechaActual = new Date().toLocaleDateString('es-ES');
  const anioActual = new Date().getFullYear();
  const usuario = authStore.user;
  
  return {
    nombreEntidad: 'Ministerio de Defensa',  // Valor predeterminado para la entidad
    unidadOrganizacion: 'Dirección de Tesorería',  // Valor predeterminado para la unidad
    seccion: 'Tesorería',
    nivelDescripcion: 'Pieza documental compuesta',  // Valor predeterminado común
    serieDocumental: 'Expediente de ',
    codigoReferencia: `REF-${anioActual}-`,  // Prefijo con año actual
    soporte: 'PAPEL',  // Ya tiene valor predeterminado
    volumenMetrosLineales: null,
    responsableSeccion: 'Jefe de Tesorería',  // Valor predeterminado para el responsable
    inventarioElaboradoPor: usuario?.fullName,
    numeroAnioRemision: `${anioActual}`,  // Año actual como valor predeterminado
    lugarFechaElaboracion: `Lima, ${fechaActual}`,  // Ubicación y fecha actual
    vistoBuenoResponsable: '',
    observaciones: '',
    detalles: [] // Array para almacenar los detalles
  };
}

// Datos del formulario
const catalogo = ref(getDefaultCatalogo());

// Datos del detalle en edición
const detalleForm = ref({
  id: null,
  numeroItem: null,
  numeroCaja: '',
  numeroTomoPaquete: '',
  numeroUnidadDocumental: '',
  fechaUnidadDocumental: null,
  alcanceContenido: '',
  informacionAdicional: '',
  cantidadFolios: null,
  observaciones: '',
});

// Métodos
async function guardarCatalogo() {
  if (!form.value.validate()) {
    return;
  }

  try {
    const resultado = await inventariosStore.createCatalogoTransferencia(catalogo.value);
    
    if (resultado && resultado.id) {
      // Mostrar notificación de éxito
      alert('Catálogo de transferencia creado con éxito');
      
      // Resetear el formulario para permitir crear otro catálogo
      resetForm();
      
      // Redireccionar a la lista de catálogos
      router.push({ name: 'catalogo-transferencia-list' });
    }
  } catch (error) {
    console.error('Error al guardar el catálogo:', error);
    alert('Error al guardar el catálogo: ' + error.message);
  }
}

// Métodos para gestión de detalles
function agregarDetalle() {
  if (detalleIndex.value >= 0) {
    // Editar detalle existente
    catalogo.value.detalles[detalleIndex.value] = {...detalleForm.value};
  } else {
    // Añadir nuevo detalle
    catalogo.value.detalles.push({...detalleForm.value});
  }
  
  cerrarModalDetalle();
}

function editarDetalle(index) {
  detalleIndex.value = index;
  detalleForm.value = {...catalogo.value.detalles[index]};
  modalDetalle.value = true;
}

function eliminarDetalle(index) {
  if (confirm('¿Está seguro de eliminar este detalle?')) {
    catalogo.value.detalles.splice(index, 1);
  }
}

function cerrarModalDetalle() {
  modalDetalle.value = false;
  detalleIndex.value = -1;
  resetDetalleForm();
}

function getEmptyDetalle() {
  const anioActual = new Date().getFullYear();
  const siguienteNumero = catalogo.value.detalles.length + 1;
  
  return {
    numeroItem: siguienteNumero,
    numeroCaja: `C-${siguienteNumero.toString().padStart(3, '0')}`, // Formato: C-001, C-002, etc.
    numeroTomoPaquete: `T-${siguienteNumero.toString().padStart(3, '0')}`,
    numeroUnidadDocumental: `UD-${anioActual}-${siguienteNumero.toString().padStart(3, '0')}`,
    fechaUnidadDocumental: null,
    alcanceContenido: '',
    informacionAdicional: '',
    cantidadFolios: 1, // Valor predeterminado de 1 folio
    observaciones: '',
  };
}

function resetDetalleForm() {
  detalleForm.value = getEmptyDetalle();
}

function formatDate(date) {
  if (!date) return '-';
  
  try {
    if (typeof date === 'string') {
      return format(new Date(date), 'dd/MM/yyyy');
    }
    return format(date, 'dd/MM/yyyy');
  } catch (e) {
    return date;
  }
}

function truncateText(text, maxLength) {
  if (!text) return '';
  return text.length <= maxLength ? text : `${text.slice(0, maxLength)}...`;
}

// Función para resetear el formulario a valores predeterminados
function resetForm() {
  catalogo.value = getDefaultCatalogo();
  if (form.value) {
    form.value.resetValidation();
  }
}
</script>
