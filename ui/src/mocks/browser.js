import { setupWorker } from 'msw';
import { handlers } from './handlers.js';

// Configura el Service Worker con los handlers definidos
export const worker = setupWorker(...handlers);

// No exportamos directamente start() para que main.js pueda controlarlo
