<template>
  <v-container class="fill-height d-flex flex-column justify-center align-center">
    <v-icon size="96" color="error">mdi-alert-circle-outline</v-icon>
    <h1 class="mt-2">404 - Página no encontrada</h1>
    <p>La ruta que buscas no existe o ha sido movida.</p>
    <v-btn color="primary" @click="$router.push('/login')">Ir al Login</v-btn>
  </v-container>
</template>

<script setup>
</script>

<style scoped>
h1 {
  font-weight: 700;
}
</style>
