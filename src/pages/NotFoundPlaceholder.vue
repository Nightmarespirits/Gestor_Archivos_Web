<template>
  <v-container class="fill-height" fluid>
    <v-row align="center" justify="center">
      <v-col cols="12" sm="8" md="6">
        <v-card elevation="4">
          <v-card-title class="headline text-center blue-grey lighten-4 py-4">
            <v-icon large color="blue-grey darken-2" class="mr-2">mdi-alert</v-icon>
            Página No Encontrada (404)
          </v-card-title>
          <v-card-text class="text-center pa-5">
            <v-img
              src="https://cdn.vuetifyjs.com/images/logos/vuetify-logo-dark.png"
              alt="Logo"
              class="mx-auto my-4"
              max-width="100"
              contain
            ></v-img>
            <p class="text-h5 mb-4">¡Ups! No pudimos encontrar la página que buscas.</p>
            <p>La página solicitada no existe o ha sido movida a otra ubicación.</p>
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
  console.log('NotFoundPlaceholder component mounted');
});
</script>

<style scoped>
/* Estilos específicos si son necesarios */
</style>
