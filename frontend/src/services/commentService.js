import apiClient from './apiClient'

const commentService = {
  /** Get all comments for a post */
  getComments: async (postId) => {
    const response = await apiClient.get(`/posts/${postId}/comments`)
    return response.data
  },

  /** Add a comment to a post */
  addComment: async (postId, body) => {
    const response = await apiClient.post(`/posts/${postId}/comments`, { body })
    return response.data
  },

  /** Update a comment */
  updateComment: async (postId, commentId, body) => {
    const response = await apiClient.put(`/posts/${postId}/comments/${commentId}`, { body })
    return response.data
  },

  /** Delete a comment */
  deleteComment: async (postId, commentId) => {
    await apiClient.delete(`/posts/${postId}/comments/${commentId}`)
  },
}

export default commentService
