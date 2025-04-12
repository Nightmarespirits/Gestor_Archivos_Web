<template>
  <v-container class="fill-height" fluid>
    <v-row align="center" justify="center">
      <v-col cols="12" sm="8" md="6">
        <v-card elevation="4">
          <v-card-title class="headline text-center red lighten-4 py-4">
            <v-icon large color="red darken-2" class="mr-2">mdi-alert-circle</v-icon>
            Acceso No Autorizado
          </v-card-title>
          <v-card-text class="text-center pa-5">
            <p class="text-h6 mb-4">Lo sentimos, no tienes permisos para acceder a esta sección.</p>
            <p>Este contenido está restringido a usuarios con roles específicos.</p>
            <p>Si crees que deberías tener acceso, por favor contacta al administrador del sistema.</p>
          </v-card-text>
          <v-card-actions class="justify-center pa-4">
            <v-btn color="primary" to="/" variant="outlined">
              <v-icon left>mdi-home</v-icon> Volver al inicio
            </v-btn>
          </v-card-actions>
        </v-card>
      </v-col>
    </v-row>
  </v-container>
</template>

<script setup>
// Este componente no requiere lógica adicional por ahora
import { onMounted } from 'vue';

onMounted(() => {
  console.log('UnauthorizedPlaceholder component mounted');
});
</script>

<style scoped>
/* Estilos específicos si son necesarios */
</style>
