import React, { useEffect, useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { useDispatch, useSelector } from 'react-redux'
import {
  Container, Box, Typography, Avatar, Chip, Divider,
  Button, CircularProgress, Alert, IconButton, Tooltip
} from '@mui/material'
import FavoriteIcon from '@mui/icons-material/Favorite'
import FavoriteBorderIcon from '@mui/icons-material/FavoriteBorder'
import EditIcon from '@mui/icons-material/Edit'
import DeleteIcon from '@mui/icons-material/Delete'
import ArrowBackIcon from '@mui/icons-material/ArrowBack'
import { format } from 'date-fns'
import { fetchPostById, deletePost, toggleLike } from '../redux/slices/postSlice'
import CommentSection from '../components/CommentSection'
import { useSnackbar } from 'notistack'

export default function PostDetailPage() {
  const { postId } = useParams()
  const dispatch = useDispatch()
  const navigate = useNavigate()
  const { enqueueSnackbar } = useSnackbar()
  const { currentPost: post, loading, error } = useSelector((s) => s.posts)
  const { user, isAuthenticated } = useSelector((s) => s.auth)

  const [deleteLoading, setDeleteLoading] = useState(false)

  useEffect(() => {
    dispatch(fetchPostById(postId))
  }, [dispatch, postId])

  const isAuthor = user && post && user.id === post.authorId
  const isAdmin = user?.roles?.includes('ROLE_ADMIN')
  const canModify = isAuthor || isAdmin

  const handleLike = async () => {
    if (!isAuthenticated) {
      enqueueSnackbar('Please login to like posts', { variant: 'info' })
      return
    }
    dispatch(toggleLike(post.id))
  }

  const handleDelete = async () => {
    if (!window.confirm('Are you sure you want to delete this post?')) return
    setDeleteLoading(true)
    const result = await dispatch(deletePost(post.id))
    if (deletePost.fulfilled.match(result)) {
      enqueueSnackbar('Post deleted successfully', { variant: 'success' })
      navigate('/dashboard')
    } else {
      enqueueSnackbar('Failed to delete post', { variant: 'error' })
      setDeleteLoading(false)
    }
  }

  if (loading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', pt: 10 }}>
        <CircularProgress size={48} />
      </Box>
    )
  }

  if (error || !post) {
    return (
      <Container maxWidth="md" sx={{ pt: 4 }}>
        <Alert severity="error">{error || 'Post not found'}</Alert>
        <Button startIcon={<ArrowBackIcon />} onClick={() => navigate('/dashboard')} sx={{ mt: 2 }}>
          Back to Dashboard
        </Button>
      </Container>
    )
  }

  return (
    <Container maxWidth="md" sx={{ py: 4 }}>
      {/* Back button */}
      <Button startIcon={<ArrowBackIcon />} onClick={() => navigate(-1)} sx={{ mb: 3 }}>
        Back
      </Button>

      {/* Category chip */}
      {post.category && (
        <Chip label={post.category} color="primary" size="small" sx={{ mb: 2 }} />
      )}

      {/* Title */}
      <Typography variant="h3" fontWeight={700} gutterBottom>
        {post.title}
      </Typography>

      {/* Author + date row */}
      <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, mb: 3 }}>
        <Avatar sx={{ bgcolor: 'primary.main' }}>
          {post.authorName?.charAt(0).toUpperCase()}
        </Avatar>
        <Box>
          <Typography fontWeight={600}>{post.authorName}</Typography>
          <Typography variant="body2" color="text.secondary">
            {post.createdAt ? format(new Date(post.createdAt), 'MMMM d, yyyy') : ''}
            {post.updatedAt && post.updatedAt !== post.createdAt ? ' · Updated' : ''}
          </Typography>
        </Box>

        {/* Like + edit + delete actions */}
        <Box sx={{ ml: 'auto', display: 'flex', gap: 1, alignItems: 'center' }}>
          <Tooltip title={post.likedByCurrentUser ? 'Unlike' : 'Like'}>
            <IconButton onClick={handleLike} color={post.likedByCurrentUser ? 'error' : 'default'}>
              {post.likedByCurrentUser ? <FavoriteIcon /> : <FavoriteBorderIcon />}
            </IconButton>
          </Tooltip>
          <Typography variant="body2">{post.likesCount}</Typography>

          {canModify && (
            <>
              <Tooltip title="Edit post">
                <IconButton onClick={() => navigate(`/posts/${post.id}/edit`)}>
                  <EditIcon />
                </IconButton>
              </Tooltip>
              <Tooltip title="Delete post">
                <IconButton onClick={handleDelete} color="error" disabled={deleteLoading}>
                  {deleteLoading ? <CircularProgress size={20} /> : <DeleteIcon />}
                </IconButton>
              </Tooltip>
            </>
          )}
        </Box>
      </Box>

      <Divider sx={{ mb: 4 }} />

      {/* Post content */}
      <Typography
        variant="body1"
        sx={{
          lineHeight: 1.9,
          fontSize: '1.1rem',
          whiteSpace: 'pre-wrap',
          wordBreak: 'break-word',
        }}
      >
        {post.content}
      </Typography>

      <Divider sx={{ my: 5 }} />

      {/* Comment section */}
      <CommentSection postId={post.id} />
    </Container>
  )
}
