import React, { useEffect, useState } from 'react'
import { useSelector } from 'react-redux'
import {
  Box, Typography, Avatar, TextField, Button,
  CircularProgress, Divider, IconButton, Tooltip, Alert
} from '@mui/material'
import DeleteIcon from '@mui/icons-material/Delete'
import EditIcon from '@mui/icons-material/Edit'
import SendIcon from '@mui/icons-material/Send'
import { format } from 'date-fns'
import commentService from '../services/commentService'
import { useSnackbar } from 'notistack'

/**
 * Comment section component rendered below a post's content.
 * Handles listing, adding, editing, and deleting comments.
 */
export default function CommentSection({ postId }) {
  const { enqueueSnackbar } = useSnackbar()
  const { user, isAuthenticated } = useSelector((s) => s.auth)

  const [comments, setComments] = useState([])
  const [loading, setLoading] = useState(false)
  const [newComment, setNewComment] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const [editingId, setEditingId] = useState(null)
  const [editBody, setEditBody] = useState('')

  // Load comments on mount
  useEffect(() => {
    loadComments()
  }, [postId])

  const loadComments = async () => {
    setLoading(true)
    try {
      const data = await commentService.getComments(postId)
      setComments(data)
    } catch {
      enqueueSnackbar('Failed to load comments', { variant: 'error' })
    } finally {
      setLoading(false)
    }
  }

  const handleAddComment = async () => {
    if (!newComment.trim()) return
    setSubmitting(true)
    try {
      const comment = await commentService.addComment(postId, newComment.trim())
      setComments((prev) => [...prev, comment])
      setNewComment('')
    } catch (err) {
      enqueueSnackbar(err.response?.data?.message || 'Failed to add comment', { variant: 'error' })
    } finally {
      setSubmitting(false)
    }
  }

  const handleDeleteComment = async (commentId) => {
    try {
      await commentService.deleteComment(postId, commentId)
      setComments((prev) => prev.filter((c) => c.id !== commentId))
      enqueueSnackbar('Comment deleted', { variant: 'success' })
    } catch {
      enqueueSnackbar('Failed to delete comment', { variant: 'error' })
    }
  }

  const handleEditSave = async (commentId) => {
    if (!editBody.trim()) return
    try {
      const updated = await commentService.updateComment(postId, commentId, editBody.trim())
      setComments((prev) => prev.map((c) => (c.id === commentId ? updated : c)))
      setEditingId(null)
      enqueueSnackbar('Comment updated', { variant: 'success' })
    } catch {
      enqueueSnackbar('Failed to update comment', { variant: 'error' })
    }
  }

  const isAdmin = user?.roles?.includes('ROLE_ADMIN')

  return (
    <Box>
      <Typography variant="h5" fontWeight={700} gutterBottom>
        Comments ({comments.length})
      </Typography>

      {/* Add comment form */}
      {isAuthenticated ? (
        <Box sx={{ display: 'flex', gap: 2, mb: 4, alignItems: 'flex-start' }}>
          <Avatar sx={{ bgcolor: 'primary.main', mt: 0.5 }}>
            {user?.name?.charAt(0).toUpperCase()}
          </Avatar>
          <Box sx={{ flexGrow: 1 }}>
            <TextField
              fullWidth
              multiline
              rows={2}
              placeholder="Write a comment..."
              value={newComment}
              onChange={(e) => setNewComment(e.target.value)}
              variant="outlined"
            />
            <Box sx={{ display: 'flex', justifyContent: 'flex-end', mt: 1 }}>
              <Button
                variant="contained"
                endIcon={submitting ? <CircularProgress size={16} color="inherit" /> : <SendIcon />}
                onClick={handleAddComment}
                disabled={submitting || !newComment.trim()}
              >
                Post Comment
              </Button>
            </Box>
          </Box>
        </Box>
      ) : (
        <Alert severity="info" sx={{ mb: 3 }}>
          Please <a href="/login">sign in</a> to comment.
        </Alert>
      )}

      {/* Comments list */}
      {loading ? (
        <Box sx={{ display: 'flex', justifyContent: 'center', py: 3 }}>
          <CircularProgress />
        </Box>
      ) : comments.length === 0 ? (
        <Typography color="text.secondary" sx={{ py: 2 }}>
          No comments yet. Be the first to comment!
        </Typography>
      ) : (
        comments.map((comment, idx) => {
          const canModify = user && (user.id === comment.authorId || isAdmin)
          const isEditing = editingId === comment.id

          return (
            <Box key={comment.id}>
              {idx > 0 && <Divider sx={{ my: 2 }} />}
              <Box sx={{ display: 'flex', gap: 2 }}>
                <Avatar sx={{ bgcolor: 'secondary.main', width: 36, height: 36, fontSize: '0.9rem' }}>
                  {comment.authorName?.charAt(0).toUpperCase()}
                </Avatar>
                <Box sx={{ flexGrow: 1 }}>
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 0.5 }}>
                    <Typography fontWeight={600} variant="body2">{comment.authorName}</Typography>
                    <Typography variant="caption" color="text.secondary">
                      · {comment.createdAt ? format(new Date(comment.createdAt), 'MMM d, yyyy') : ''}
                    </Typography>
                    {canModify && !isEditing && (
                      <Box sx={{ ml: 'auto' }}>
                        <Tooltip title="Edit">
                          <IconButton
                            size="small"
                            onClick={() => { setEditingId(comment.id); setEditBody(comment.body) }}
                          >
                            <EditIcon fontSize="small" />
                          </IconButton>
                        </Tooltip>
                        <Tooltip title="Delete">
                          <IconButton
                            size="small"
                            color="error"
                            onClick={() => handleDeleteComment(comment.id)}
                          >
                            <DeleteIcon fontSize="small" />
                          </IconButton>
                        </Tooltip>
                      </Box>
                    )}
                  </Box>

                  {isEditing ? (
                    <Box>
                      <TextField
                        fullWidth
                        multiline
                        rows={2}
                        value={editBody}
                        onChange={(e) => setEditBody(e.target.value)}
                        size="small"
                      />
                      <Box sx={{ display: 'flex', gap: 1, mt: 1 }}>
                        <Button size="small" variant="contained" onClick={() => handleEditSave(comment.id)}>
                          Save
                        </Button>
                        <Button size="small" onClick={() => setEditingId(null)}>Cancel</Button>
                      </Box>
                    </Box>
                  ) : (
                    <Typography variant="body2" sx={{ lineHeight: 1.7, whiteSpace: 'pre-wrap' }}>
                      {comment.body}
                    </Typography>
                  )}
                </Box>
              </Box>
            </Box>
          )
        })
      )}
    </Box>
  )
}
