import apiClient from './apiClient'

const postService = {
  /** Fetch paginated, sorted list of all posts */
  getAllPosts: async ({ page = 0, size = 10, sortBy = 'createdAt', sortDir = 'desc' } = {}) => {
    const response = await apiClient.get('/posts', { params: { page, size, sortBy, sortDir } })
    return response.data
  },

  /** Fetch a single post by ID */
  getPostById: async (postId) => {
    const response = await apiClient.get(`/posts/${postId}`)
    return response.data
  },

  /** Create a new post */
  createPost: async (data) => {
    const response = await apiClient.post('/posts', data)
    return response.data
  },

  /** Update an existing post */
  updatePost: async (postId, data) => {
    const response = await apiClient.put(`/posts/${postId}`, data)
    return response.data
  },

  /** Delete a post */
  deletePost: async (postId) => {
    await apiClient.delete(`/posts/${postId}`)
  },

  /** Toggle like/unlike on a post */
  toggleLike: async (postId) => {
    const response = await apiClient.post(`/posts/${postId}/like`)
    return response.data
  },

  /** Full-text search */
  searchPosts: async (query, page = 0, size = 10) => {
    const response = await apiClient.get('/posts/search', { params: { query, page, size } })
    return response.data
  },

  /** Filter posts by category */
  getPostsByCategory: async (category, page = 0, size = 10) => {
    const response = await apiClient.get(`/posts/category/${category}`, { params: { page, size } })
    return response.data
  },

  /** Get posts by a specific user */
  getPostsByUser: async (userId, page = 0, size = 10) => {
    const response = await apiClient.get(`/posts/user/${userId}`, { params: { page, size } })
    return response.data
  },
}

export default postService
