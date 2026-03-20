import { createSlice, createAsyncThunk } from '@reduxjs/toolkit'
import postService from '../../services/postService'

// ── Async thunks ──────────────────────────────────────────────────────────────

export const fetchPosts = createAsyncThunk(
  'posts/fetchAll',
  async (params, { rejectWithValue }) => {
    try {
      return await postService.getAllPosts(params)
    } catch (err) {
      return rejectWithValue(err.response?.data?.message || 'Failed to fetch posts')
    }
  }
)

export const fetchPostById = createAsyncThunk(
  'posts/fetchById',
  async (postId, { rejectWithValue }) => {
    try {
      return await postService.getPostById(postId)
    } catch (err) {
      return rejectWithValue(err.response?.data?.message || 'Post not found')
    }
  }
)

export const createPost = createAsyncThunk(
  'posts/create',
  async (postData, { rejectWithValue }) => {
    try {
      return await postService.createPost(postData)
    } catch (err) {
      return rejectWithValue(err.response?.data?.message || 'Failed to create post')
    }
  }
)

export const updatePost = createAsyncThunk(
  'posts/update',
  async ({ postId, data }, { rejectWithValue }) => {
    try {
      return await postService.updatePost(postId, data)
    } catch (err) {
      return rejectWithValue(err.response?.data?.message || 'Failed to update post')
    }
  }
)

export const deletePost = createAsyncThunk(
  'posts/delete',
  async (postId, { rejectWithValue }) => {
    try {
      await postService.deletePost(postId)
      return postId
    } catch (err) {
      return rejectWithValue(err.response?.data?.message || 'Failed to delete post')
    }
  }
)

export const toggleLike = createAsyncThunk(
  'posts/toggleLike',
  async (postId, { rejectWithValue }) => {
    try {
      return await postService.toggleLike(postId)
    } catch (err) {
      return rejectWithValue(err.response?.data?.message || 'Failed to toggle like')
    }
  }
)

export const searchPosts = createAsyncThunk(
  'posts/search',
  async ({ query, page, size }, { rejectWithValue }) => {
    try {
      return await postService.searchPosts(query, page, size)
    } catch (err) {
      return rejectWithValue(err.response?.data?.message || 'Search failed')
    }
  }
)

// ── Slice ─────────────────────────────────────────────────────────────────────

const postSlice = createSlice({
  name: 'posts',
  initialState: {
    posts: [],
    currentPost: null,
    totalPages: 0,
    totalElements: 0,
    currentPage: 0,
    loading: false,
    error: null,
  },
  reducers: {
    clearCurrentPost(state) {
      state.currentPost = null
    },
    clearError(state) {
      state.error = null
    },
  },
  extraReducers: (builder) => {
    // Fetch all posts
    builder
      .addCase(fetchPosts.pending, (state) => { state.loading = true; state.error = null })
      .addCase(fetchPosts.fulfilled, (state, action) => {
        state.loading = false
        state.posts = action.payload.content
        state.totalPages = action.payload.totalPages
        state.totalElements = action.payload.totalElements
        state.currentPage = action.payload.page
      })
      .addCase(fetchPosts.rejected, (state, action) => { state.loading = false; state.error = action.payload })

    // Fetch single post
    builder
      .addCase(fetchPostById.pending, (state) => { state.loading = true })
      .addCase(fetchPostById.fulfilled, (state, action) => {
        state.loading = false
        state.currentPost = action.payload
      })
      .addCase(fetchPostById.rejected, (state, action) => { state.loading = false; state.error = action.payload })

    // Create post
    builder
      .addCase(createPost.fulfilled, (state, action) => {
        state.posts.unshift(action.payload)
      })

    // Update post
    builder
      .addCase(updatePost.fulfilled, (state, action) => {
        state.currentPost = action.payload
        const idx = state.posts.findIndex(p => p.id === action.payload.id)
        if (idx !== -1) state.posts[idx] = action.payload
      })

    // Delete post
    builder
      .addCase(deletePost.fulfilled, (state, action) => {
        state.posts = state.posts.filter(p => p.id !== action.payload)
        if (state.currentPost?.id === action.payload) state.currentPost = null
      })

    // Toggle like — optimistic update via returned post
    builder
      .addCase(toggleLike.fulfilled, (state, action) => {
        const idx = state.posts.findIndex(p => p.id === action.payload.id)
        if (idx !== -1) state.posts[idx] = action.payload
        if (state.currentPost?.id === action.payload.id) state.currentPost = action.payload
      })

    // Search
    builder
      .addCase(searchPosts.pending, (state) => { state.loading = true })
      .addCase(searchPosts.fulfilled, (state, action) => {
        state.loading = false
        state.posts = action.payload.content
        state.totalPages = action.payload.totalPages
        state.totalElements = action.payload.totalElements
      })
      .addCase(searchPosts.rejected, (state, action) => { state.loading = false; state.error = action.payload })
  },
})

export const { clearCurrentPost, clearError } = postSlice.actions
export default postSlice.reducer
