import React, { useEffect, useState } from 'react'
import { useDispatch, useSelector } from 'react-redux'
import { useNavigate } from 'react-router-dom'
import {
  Container, Box, Typography, Grid, Button, TextField,
  InputAdornment, Pagination, CircularProgress, Alert, Chip, Stack
} from '@mui/material'
import SearchIcon from '@mui/icons-material/Search'
import AddIcon from '@mui/icons-material/Add'
import { fetchPosts, searchPosts } from '../redux/slices/postSlice'
import PostCard from '../components/PostCard'

export default function DashboardPage() {
  const dispatch = useDispatch()
  const navigate = useNavigate()
  const { posts, totalPages, currentPage, loading, error } = useSelector((s) => s.posts)
  const { isAuthenticated } = useSelector((s) => s.auth)

  const [searchQuery, setSearchQuery] = useState('')
  const [page, setPage] = useState(1)

  // Fetch posts on mount and when page changes
  useEffect(() => {
    if (searchQuery) {
      dispatch(searchPosts({ query: searchQuery, page: page - 1, size: 9 }))
    } else {
      dispatch(fetchPosts({ page: page - 1, size: 9, sortBy: 'createdAt', sortDir: 'desc' }))
    }
  }, [dispatch, page])

  const handleSearch = (e) => {
    e.preventDefault()
    setPage(1)
    if (searchQuery.trim()) {
      dispatch(searchPosts({ query: searchQuery, page: 0, size: 9 }))
    } else {
      dispatch(fetchPosts({ page: 0, size: 9 }))
    }
  }

  const handlePageChange = (_, value) => setPage(value)

  return (
    <Container maxWidth="lg" sx={{ py: 4 }}>
      {/* Hero section */}
      <Box sx={{ mb: 5, textAlign: 'center' }}>
        <Typography variant="h3" fontWeight={700} gutterBottom>
          Explore Stories
        </Typography>
        <Typography color="text.secondary" variant="h6" sx={{ mb: 3 }}>
          Discover insightful articles from our community
        </Typography>

        {/* Search bar */}
        <Box component="form" onSubmit={handleSearch} sx={{ maxWidth: 600, mx: 'auto', display: 'flex', gap: 1 }}>
          <TextField
            fullWidth
            placeholder="Search posts..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            InputProps={{
              startAdornment: (
                <InputAdornment position="start">
                  <SearchIcon color="action" />
                </InputAdornment>
              ),
            }}
            size="medium"
          />
          <Button type="submit" variant="contained" sx={{ px: 3 }}>Search</Button>
        </Box>
      </Box>

      {/* Action bar */}
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Typography variant="h6" color="text.secondary">
          {posts.length > 0 ? `${posts.length} posts found` : ''}
        </Typography>
        {isAuthenticated && (
          <Button
            variant="contained"
            startIcon={<AddIcon />}
            onClick={() => navigate('/posts/new')}
          >
            Write Post
          </Button>
        )}
      </Box>

      {/* Error state */}
      {error && <Alert severity="error" sx={{ mb: 3 }}>{error}</Alert>}

      {/* Loading spinner */}
      {loading ? (
        <Box sx={{ display: 'flex', justifyContent: 'center', py: 8 }}>
          <CircularProgress size={48} />
        </Box>
      ) : posts.length === 0 ? (
        <Box sx={{ textAlign: 'center', py: 8 }}>
          <Typography variant="h6" color="text.secondary">
            No posts found. {isAuthenticated ? 'Be the first to write one!' : ''}
          </Typography>
        </Box>
      ) : (
        <>
          <Grid container spacing={3}>
            {posts.map((post) => (
              <Grid item xs={12} sm={6} md={4} key={post.id}>
                <PostCard post={post} />
              </Grid>
            ))}
          </Grid>

          {totalPages > 1 && (
            <Box sx={{ display: 'flex', justifyContent: 'center', mt: 5 }}>
              <Pagination
                count={totalPages}
                page={page}
                onChange={handlePageChange}
                color="primary"
                size="large"
              />
            </Box>
          )}
        </>
      )}
    </Container>
  )
}
