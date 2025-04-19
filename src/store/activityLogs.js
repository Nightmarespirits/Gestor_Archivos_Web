import { defineStore } from 'pinia';

const handleResponse = async (response) => {
  if (!response.ok) {
    const error = await response.text();
    throw new Error(error || `HTTP error! status: ${response.status}`);
  }
  return response.json();
};

export const useActivityLogsStore = defineStore('activityLogs', {
  state: () => ({
    logs: [],
    currentLog: null,
    loading: false,
    error: null,
    pagination: {
      totalItems: 0,
      totalPages: 0,
      currentPage: 0,
      itemsPerPage: 20,
    },
  }),

  actions: {
    async fetchLogs({ page = 0, size = 20 } = {}) {
      this.loading = true;
      try {
        const response = await fetch(`/api/activity-logs/paged?page=${page}&size=${size}`, {
          headers: {
            'Accept': 'application/json',
          },
        });
        const data = await handleResponse(response);
        this.logs = data.content;
        this.pagination = {
          totalItems: data.totalElements,
          totalPages: data.totalPages,
          currentPage: data.number,
          itemsPerPage: data.size,
        };
      } catch (error) {
        this.error = error.message || 'Error al cargar los registros de actividad';
        throw error;
      } finally {
        this.loading = false;
      }
    },

    async fetchLogsByUser(userId) {
      this.loading = true;
      try {
        const response = await fetch(`/api/activity-logs/user/${userId}`, {
          headers: {
            'Accept': 'application/json',
          },
        });
        const data = await handleResponse(response);
        this.logs = data;
      } catch (error) {
        this.error = error.message || 'Error al cargar los registros del usuario';
        throw error;
      } finally {
        this.loading = false;
      }
    },

    async fetchLogsByActionType(actionType) {
      this.loading = true;
      try {
        const response = await fetch(`/api/activity-logs/action-type/${actionType}`, {
          headers: {
            'Accept': 'application/json',
          },
        });
        const data = await handleResponse(response);
        this.logs = data;
      } catch (error) {
        this.error = error.message || 'Error al cargar los registros por tipo de acción';
        throw error;
      } finally {
        this.loading = false;
      }
    },

    async fetchLogsByTimeRange(startTime, endTime) {
      this.loading = true;
      try {
        const url = new URL('/api/activity-logs/time-range', window.location.origin);
        url.searchParams.append('startTime', startTime);
        url.searchParams.append('endTime', endTime);
        
        const response = await fetch(url, {
          headers: {
            'Accept': 'application/json',
          },
        });
        const data = await handleResponse(response);
        this.logs = data;
      } catch (error) {
        this.error = error.message || 'Error al cargar los registros por rango de tiempo';
        throw error;
      } finally {
        this.loading = false;
      }
    },

    clearError() {
      this.error = null;
    }
  },
});