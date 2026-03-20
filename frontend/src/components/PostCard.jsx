import React from 'react'
import { useNavigate } from 'react-router-dom'
import { useDispatch, useSelector } from 'react-redux'
import {
  Card, CardContent, CardActions, Typography, Avatar,
  Box, Chip, IconButton, Tooltip, Button
} from '@mui/material'
import FavoriteIcon from '@mui/icons-material/Favorite'
import FavoriteBorderIcon from '@mui/icons-material/FavoriteBorder'
import CommentIcon from '@mui/icons-material/Comment'
import EditIcon from '@mui/icons-material/Edit'
import DeleteIcon from '@mui/icons-material/Delete'
import { format } from 'date-fns'
import { toggleLike, deletePost } from '../redux/slices/postSlice'
import { useSnackbar } from 'notistack'

/**
 * Reusable post card component for the dashboard and profile grids.
 * @param {object} post - Post data
 * @param {boolean} showActions - Whether to show edit/delete buttons (for profile page)
 */
export default function PostCard({ post, showActions = false }) {
  const navigate = useNavigate()
  const dispatch = useDispatch()
  const { enqueueSnackbar } = useSnackbar()
  const { user, isAuthenticated } = useSelector((s) => s.auth)

  const isAuthor = user && post.authorId === user.id
  const isAdmin = user?.roles?.includes('ROLE_ADMIN')

  const handleLike = async (e) => {
    e.stopPropagation()
    if (!isAuthenticated) {
      enqueueSnackbar('Please login to like posts', { variant: 'info' })
      return
    }
    dispatch(toggleLike(post.id))
  }

  const handleDelete = async (e) => {
    e.stopPropagation()
    if (!window.confirm('Delete this post?')) return
    const result = await dispatch(deletePost(post.id))
    if (deletePost.fulfilled.match(result)) {
      enqueueSnackbar('Post deleted', { variant: 'success' })
    }
  }

  // Truncate content for the card preview
  const excerpt = post.description || post.content?.slice(0, 120) + '...'

  return (
    <Card
      sx={{
        height: '100%',
        display: 'flex',
        flexDirection: 'column',
        transition: 'transform 0.2s, box-shadow 0.2s',
        cursor: 'pointer',
        '&:hover': { transform: 'translateY(-4px)', boxShadow: 6 },
      }}
      onClick={() => navigate(`/posts/${post.id}`)}
    >
      <CardContent sx={{ flexGrow: 1 }}>
        {/* Category */}
        {post.category && (
          <Chip label={post.category} size="small" color="primary" variant="outlined" sx={{ mb: 1 }} />
        )}

        {/* Title */}
        <Typography variant="h6" fontWeight={700} gutterBottom sx={{ lineHeight: 1.4 }}>
          {post.title}
        </Typography>

        {/* Excerpt */}
        <Typography variant="body2" color="text.secondary" sx={{ mb: 2, lineHeight: 1.6 }}>
          {excerpt}
        </Typography>

        {/* Author info */}
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
          <Avatar sx={{ width: 28, height: 28, bgcolor: 'secondary.main', fontSize: '0.85rem' }}>
            {post.authorName?.charAt(0).toUpperCase()}
          </Avatar>
          <Box>
            <Typography variant="caption" fontWeight={600}>{post.authorName}</Typography>
            <Typography variant="caption" color="text.secondary" display="block">
              {post.createdAt ? format(new Date(post.createdAt), 'MMM d, yyyy') : ''}
            </Typography>
          </Box>
        </Box>
      </CardContent>

      <CardActions sx={{ px: 2, pb: 1.5, justifyContent: 'space-between' }}>
        {/* Like + comment counts */}
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
          <Tooltip title={post.likedByCurrentUser ? 'Unlike' : 'Like'}>
            <IconButton
              size="small"
              onClick={handleLike}
              color={post.likedByCurrentUser ? 'error' : 'default'}
            >
              {post.likedByCurrentUser ? (
                <FavoriteIcon fontSize="small" />
              ) : (
                <FavoriteBorderIcon fontSize="small" />
              )}
            </IconButton>
          </Tooltip>
          <Typography variant="caption">{post.likesCount || 0}</Typography>

          <CommentIcon fontSize="small" color="action" sx={{ ml: 1 }} />
          <Typography variant="caption">{post.commentsCount || 0}</Typography>
        </Box>

        {/* Author/admin actions */}
        {showActions && (isAuthor || isAdmin) && (
          <Box sx={{ display: 'flex', gap: 0.5 }}>
            <IconButton
              size="small"
              onClick={(e) => { e.stopPropagation(); navigate(`/posts/${post.id}/edit`) }}
            >
              <EditIcon fontSize="small" />
            </IconButton>
            <IconButton size="small" onClick={handleDelete} color="error">
              <DeleteIcon fontSize="small" />
            </IconButton>
          </Box>
        )}
      </CardActions>
    </Card>
  )
}
