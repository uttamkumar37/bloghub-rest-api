import apiClient from './apiClient'

const authService = {
  /** Register a new user */
  register: async (data) => {
    const response = await apiClient.post('/auth/register', data)
    return response.data
  },

  /** Login and receive JWT token */
  login: async (credentials) => {
    const response = await apiClient.post('/auth/login', credentials)
    return response.data
  },

  /** Get current user's profile */
  getCurrentUser: async () => {
    const response = await apiClient.get('/users/me')
    return response.data
  },

  /** Update user profile */
  updateProfile: async (userId, data) => {
    const response = await apiClient.put(`/users/${userId}`, data)
    return response.data
  },
}

export default authService
