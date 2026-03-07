const API_BASE = import.meta.env.VITE_API_URL || 'http://localhost:8080';

class ApiClient {
  constructor() {
    this.baseUrl = API_BASE;
  }

  getHeaders() {
    const headers = { 'Content-Type': 'application/json' };
    const token = localStorage.getItem('accessToken');
    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }
    return headers;
  }

  async request(method, path, body = null) {
    const options = {
      method,
      headers: this.getHeaders(),
    };
    if (body) {
      options.body = JSON.stringify(body);
    }

    const response = await fetch(`${this.baseUrl}${path}`, options);

    if (response.status === 401) {
      // Try refresh token
      const refreshed = await this.refreshToken();
      if (refreshed) {
        options.headers = this.getHeaders();
        const retryResponse = await fetch(`${this.baseUrl}${path}`, options);
        return this.handleResponse(retryResponse);
      }
      // Logout on failure
      localStorage.clear();
      window.location.href = '/login';
      throw new Error('Session expired');
    }

    return this.handleResponse(response);
  }

  async handleResponse(response) {
    const data = await response.json();
    if (!response.ok) {
      throw new Error(data.message || 'Request failed');
    }
    return data;
  }

  async refreshToken() {
    const refreshToken = localStorage.getItem('refreshToken');
    if (!refreshToken) return false;
    try {
      const response = await fetch(`${this.baseUrl}/api/v1/auth/refresh`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ refreshToken }),
      });
      if (response.ok) {
        const data = await response.json();
        localStorage.setItem('accessToken', data.data.accessToken);
        return true;
      }
    } catch (e) { /* ignore */ }
    return false;
  }

  // Auth
  login(identifier, password) {
    return this.request('POST', '/api/v1/auth/login', { identifier, password });
  }
  register(data) {
    return this.request('POST', '/api/v1/auth/register', data);
  }

  // Dashboard
  getStats() {
    return this.request('GET', '/api/v1/dashboard/stats');
  }

  // Tickets
  getTickets(params = {}) {
    const query = new URLSearchParams(params).toString();
    return this.request('GET', `/api/v1/tickets?${query}`);
  }
  getTicket(ticketId) {
    return this.request('GET', `/api/v1/tickets/${ticketId}`);
  }
  createTicket(data) {
    return this.request('POST', '/api/v1/tickets', data);
  }
  updateTicketStatus(ticketId, status) {
    return this.request('PUT', `/api/v1/tickets/${ticketId}/status`, { status });
  }
  assignTicket(ticketId, staffId) {
    return this.request('PUT', `/api/v1/tickets/${ticketId}/assign`, { staffId });
  }

  // Staff
  getStaff(department) {
    const query = department ? `?department=${department}` : '';
    return this.request('GET', `/api/v1/staff${query}`);
  }
  createStaff(data) {
    return this.request('POST', '/api/v1/staff', data);
  }
  updateStaff(id, data) {
    return this.request('PUT', `/api/v1/staff/${id}`, data);
  }
  deleteStaff(id) {
    return this.request('DELETE', `/api/v1/staff/${id}`);
  }

  // Rooms
  getRooms() {
    return this.request('GET', '/api/v1/rooms');
  }
  createRoom(data) {
    return this.request('POST', '/api/v1/rooms', data);
  }
  deleteRoom(id) {
    return this.request('DELETE', `/api/v1/rooms/${id}`);
  }

  // Food Feedback
  getFoodFeedback(params = {}) {
    const query = new URLSearchParams(params).toString();
    return this.request('GET', `/api/v1/food-feedback?${query}`);
  }
  updateFoodFeedbackStatus(id, status) {
    return this.request('PUT', `/api/v1/food-feedback/${id}/status`, { status });
  }
}

const api = new ApiClient();
export default api;
