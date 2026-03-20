import React from 'react'
import { useDispatch, useSelector } from 'react-redux'
import { useNavigate } from 'react-router-dom'
import { useForm } from 'react-hook-form'
import {
  Container, Box, Typography, TextField, Button,
  CircularProgress, Alert, Paper
} from '@mui/material'
import { createPost } from '../redux/slices/postSlice'
import { useSnackbar } from 'notistack'

export default function CreatePostPage() {
  const dispatch = useDispatch()
  const navigate = useNavigate()
  const { enqueueSnackbar } = useSnackbar()
  const { loading, error } = useSelector((s) => s.posts)

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm()

  const onSubmit = async (data) => {
    const result = await dispatch(createPost(data))
    if (createPost.fulfilled.match(result)) {
      enqueueSnackbar('Post published!', { variant: 'success' })
      navigate(`/posts/${result.payload.id}`)
    }
  }

  return (
    <Container maxWidth="md" sx={{ py: 4 }}>
      <Typography variant="h4" fontWeight={700} gutterBottom>
        Write a New Post
      </Typography>
      <Typography color="text.secondary" sx={{ mb: 3 }}>
        Share your story with the BlogHub community
      </Typography>

      <Paper sx={{ p: 4 }} elevation={2}>
        {error && <Alert severity="error" sx={{ mb: 3 }}>{error}</Alert>}

        <Box component="form" onSubmit={handleSubmit(onSubmit)} noValidate>
          <TextField
            label="Title"
            fullWidth
            margin="normal"
            placeholder="Your post title..."
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
            placeholder="Brief description of your post..."
            {...register('description', {
              required: 'Description is required',
              minLength: { value: 10, message: 'Description must be at least 10 characters' },
            })}
            error={!!errors.description}
            helperText={errors.description?.message}
          />

          <TextField
            label="Category"
            fullWidth
            margin="normal"
            placeholder="e.g. Technology, Travel, Food..."
            {...register('category')}
          />

          <TextField
            label="Content"
            fullWidth
            margin="normal"
            multiline
            rows={12}
            placeholder="Write your full post content here..."
            {...register('content', {
              required: 'Content is required',
              minLength: { value: 20, message: 'Content must be at least 20 characters' },
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
              {loading ? <CircularProgress size={24} color="inherit" /> : 'Publish Post'}
            </Button>
            <Button
              variant="outlined"
              size="large"
              onClick={() => navigate(-1)}
            >
              Cancel
            </Button>
          </Box>
        </Box>
      </Paper>
    </Container>
  )
}
