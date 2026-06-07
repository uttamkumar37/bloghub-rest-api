import React, { useEffect } from 'react'
import { useDispatch, useSelector } from 'react-redux'
import { useNavigate, useParams } from 'react-router-dom'
import { useForm } from 'react-hook-form'
import {
  Container, Box, Typography, TextField, Button,
  CircularProgress, Alert, Paper
} from '@mui/material'
import { fetchPostById, updatePost } from '../redux/slices/postSlice'
import { useSnackbar } from 'notistack'

export default function EditPostPage() {
  const { postId } = useParams()
  const dispatch = useDispatch()
  const navigate = useNavigate()
  const { enqueueSnackbar } = useSnackbar()
  const { currentPost: post, loading, error } = useSelector((s) => s.posts)

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm()

  // Load post on mount
  useEffect(() => {
    dispatch(fetchPostById(postId))
  }, [dispatch, postId])

  // Pre-fill form when post loads
  useEffect(() => {
    if (post) {
      reset({
        title: post.title,
        description: post.description,
        content: post.content,
        category: post.category || '',
      })
    }
  }, [post, reset])

  const onSubmit = async (data) => {
    const result = await dispatch(updatePost({ postId: Number(postId), data }))
    if (updatePost.fulfilled.match(result)) {
      enqueueSnackbar('Post updated successfully!', { variant: 'success' })
      navigate(`/posts/${postId}`)
    }
  }

  if (loading && !post) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', pt: 10 }}>
        <CircularProgress />
      </Box>
    )
  }

  return (
    <Container maxWidth="md" sx={{ py: 4 }}>
      <Typography variant="h4" fontWeight={700} gutterBottom>
        Edit Post
      </Typography>

      <Paper sx={{ p: 4 }} elevation={2}>
        {error && <Alert severity="error" sx={{ mb: 3 }}>{error}</Alert>}

        <Box component="form" onSubmit={handleSubmit(onSubmit)} noValidate>
          <TextField
            label="Title"
            fullWidth
            margin="normal"
            {...register('title', {
              required: 'Title is required',
              minLength: { value: 5, message: 'Title must be at least 5 characters' },
            })}
            error={!!errors.title}
            helperText={errors.title?.message}
          />

          <TextField
            label="Description"
            fullWidth
            margin="normal"
            multiline
            rows={2}
            {...register('description', {
              required: 'Description is required',
              minLength: { value: 10, message: 'Minimum 10 characters' },
            })}
            error={!!errors.description}
            helperText={errors.description?.message}
          />

          <TextField
            label="Category"
            fullWidth
            margin="normal"
            {...register('category')}
          />

          <TextField
            label="Content"
            fullWidth
            margin="normal"
            multiline
            rows={12}
            {...register('content', {
              required: 'Content is required',
              minLength: { value: 20, message: 'Minimum 20 characters' },
            })}
            error={!!errors.content}
            helperText={errors.content?.message}
          />

          <Box sx={{ display: 'flex', gap: 2, mt: 3 }}>
            <Button
              type="submit"
              variant="contained"
              size="large"
              disabled={loading}
              sx={{ px: 5 }}
            >
              {loading ? <CircularProgress size={24} color="inherit" /> : 'Save Changes'}
            </Button>
            <Button variant="outlined" size="large" onClick={() => navigate(`/posts/${postId}`)}>
              Cancel
            </Button>
          </Box>
        </Box>
      </Paper>
    </Container>
  )
}
