import { configureStore } from '@reduxjs/toolkit'
import authReducer from './slices/authSlice'
import postReducer from './slices/postSlice'

/**
 * Root Redux store.
 * Manages auth state and post state across the application.
 */
export const store = configureStore({
  reducer: {
    auth: authReducer,
    posts: postReducer,
  },
  middleware: (getDefaultMiddleware) =>
    getDefaultMiddleware({
      // Disable serializable check for dates inside post objects
      serializableCheck: {
        ignoredActions: ['posts/setCurrentPost'],
        ignoredPaths: ['posts.currentPost.createdAt', 'posts.currentPost.updatedAt'],
      },
    }),
})

export default store
