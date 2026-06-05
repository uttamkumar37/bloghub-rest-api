import React, { useMemo } from 'react'
import { useDispatch, useSelector } from 'react-redux'
import { useNavigate } from 'react-router-dom'
import { useForm } from 'react-hook-form'
import {
  Container, Box, Typography, TextField, Button,
  CircularProgress, Alert, Paper, Grid, MenuItem,
  Chip, Stack, Divider
} from '@mui/material'
import { createPost } from '../redux/slices/postSlice'
import { useSnackbar } from 'notistack'

const categories = [
  'General',
  'Technology',
  'Java',
  'Spring Boot',
  'Databases',
  'System Design',
  'DevOps',
  'Career',
  'Tutorial',
  'Opinion',
]

export default function CreatePostPage() {
  const dispatch = useDispatch()
  const navigate = useNavigate()
  const { enqueueSnackbar } = useSnackbar()
  const { loading, error } = useSelector((s) => s.posts)

  const {
    register,
    handleSubmit,
    watch,
    formState: { errors },
  } = useForm({
    defaultValues: {
      category: 'General',
    },
  })

  const title = watch('title', '')
  const description = watch('description', '')
  const category = watch('category', 'General')
  const content = watch('content', '')

  const contentStats = useMemo(() => {
    const trimmed = content.trim()
    return {
      words: trimmed ? trimmed.split(/\s+/).length : 0,
      minutes: Math.max(1, Math.ceil((trimmed ? trimmed.split(/\s+/).length : 0) / 200)),
    }
  }, [content])

  const onSubmit = async (data) => {
    const result = await dispatch(createPost(data))
    if (createPost.fulfilled.match(result)) {
      enqueueSnackbar('Post published!', { variant: 'success' })
      navigate(`/posts/${result.payload.id}`)
    }
  }

  return (
    <Container maxWidth="lg" sx={{ py: 4 }}>
      <Typography variant="h4" fontWeight={700} gutterBottom>
        Write a New Post
      </Typography>
      <Typography color="text.secondary" sx={{ mb: 3 }}>
        Share your story with the BlogHub community
      </Typography>

      <Paper sx={{ p: { xs: 2, md: 4 } }} elevation={2}>
        {error && <Alert severity="error" sx={{ mb: 3 }}>{error}</Alert>}

        <Box component="form" onSubmit={handleSubmit(onSubmit)} noValidate>
          <Grid container spacing={3}>
            <Grid item xs={12} md={7}>
              <TextField
                label="Title"
                fullWidth
                margin="normal"
                placeholder="A clear headline for your post"
                {...register('title', {
                  required: 'Title is required',
                  minLength: { value: 5, message: 'Title must be at least 5 characters' },
                  maxLength: { value: 200, message: 'Title must be under 200 characters' },
                })}
                error={!!errors.title}
                helperText={errors.title?.message || `${title.length}/200 characters`}
              />

              <TextField
                label="Description"
                fullWidth
                margin="normal"
                multiline
                rows={3}
                placeholder="Short summary shown on post cards"
                {...register('description', {
                  required: 'Description is required',
                  minLength: { value: 10, message: 'Description must be at least 10 characters' },
                  maxLength: { value: 500, message: 'Description must be under 500 characters' },
                })}
                error={!!errors.description}
                helperText={errors.description?.message || `${description.length}/500 characters`}
              />

              <TextField
                select
                label="Category"
                fullWidth
                margin="normal"
                {...register('category')}
              >
                {categories.map((item) => (
                  <MenuItem key={item} value={item}>
                    {item}
                  </MenuItem>
                ))}
              </TextField>

              <TextField
                label="Content"
                fullWidth
                margin="normal"
                multiline
                rows={14}
                placeholder="Write your full post content here"
                {...register('content', {
                  required: 'Content is required',
                  minLength: { value: 20, message: 'Content must be at least 20 characters' },
                })}
                error={!!errors.content}
                helperText={errors.content?.message || `${contentStats.words} words · ${contentStats.minutes} min read`}
              />

              <Box sx={{ display: 'flex', gap: 2, mt: 3, flexWrap: 'wrap' }}>
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
            </Grid>

            <Grid item xs={12} md={5}>
              <Box
                sx={{
                  border: '1px solid',
                  borderColor: 'divider',
                  borderRadius: 2,
                  p: 3,
                  position: { md: 'sticky' },
                  top: { md: 88 },
                  minHeight: 320,
                }}
              >
                <Stack direction="row" justifyContent="space-between" alignItems="center" gap={2}>
                  <Typography variant="h6" fontWeight={700}>
                    Preview
                  </Typography>
                  <Chip label={category || 'General'} size="small" color="primary" variant="outlined" />
                </Stack>

                <Divider sx={{ my: 2 }} />

                <Typography variant="h5" fontWeight={700} sx={{ wordBreak: 'break-word' }}>
                  {title || 'Your post title'}
                </Typography>
                <Typography color="text.secondary" sx={{ mt: 1, wordBreak: 'break-word' }}>
                  {description || 'A short post summary will appear here.'}
                </Typography>

                <Stack direction="row" spacing={1} sx={{ mt: 2, mb: 2, flexWrap: 'wrap', rowGap: 1 }}>
                  <Chip label={`${contentStats.words} words`} size="small" />
                  <Chip label={`${contentStats.minutes} min read`} size="small" />
                </Stack>

                <Typography
                  variant="body2"
                  sx={{
                    whiteSpace: 'pre-wrap',
                    color: content ? 'text.primary' : 'text.secondary',
                    maxHeight: 260,
                    overflow: 'auto',
                    wordBreak: 'break-word',
                  }}
                >
                  {content || 'Start writing and the first part of your content will preview here.'}
                </Typography>
              </Box>
            </Grid>
          </Grid>
        </Box>
      </Paper>
    </Container>
  )
}
