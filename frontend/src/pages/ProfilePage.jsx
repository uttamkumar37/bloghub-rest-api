import React, { useEffect, useState } from 'react'
import { useDispatch, useSelector } from 'react-redux'
import { useForm } from 'react-hook-form'
import {
  Container, Box, Typography, Avatar, Card, CardContent,
  TextField, Button, Grid, CircularProgress, Chip, Divider
} from '@mui/material'
import { updateProfile } from '../redux/slices/authSlice'
import postService from '../services/postService'
import PostCard from '../components/PostCard'
import { useSnackbar } from 'notistack'

export default function ProfilePage() {
  const dispatch = useDispatch()
  const { enqueueSnackbar } = useSnackbar()
  const { user, loading } = useSelector((s) => s.auth)

  const [userPosts, setUserPosts] = useState([])
  const [postsLoading, setPostsLoading] = useState(false)
  const [editMode, setEditMode] = useState(false)

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm()

  // Load user's posts
  useEffect(() => {
    if (user?.id) {
      setPostsLoading(true)
      postService
        .getPostsByUser(user.id)
        .then((data) => setUserPosts(data.content || []))
        .catch(console.error)
        .finally(() => setPostsLoading(false))
    }
  }, [user?.id])

  // Pre-fill form with current user data
  useEffect(() => {
    if (user) {
      reset({ name: user.name, email: user.email })
    }
  }, [user, reset])

  const onSubmit = async (data) => {
    // Only send non-empty fields
    const updates = {}
    if (data.name) updates.name = data.name
    if (data.email) updates.email = data.email
    if (data.password) updates.password = data.password

    const result = await dispatch(updateProfile({ userId: user.id, data: updates }))
    if (updateProfile.fulfilled.match(result)) {
      enqueueSnackbar('Profile updated!', { variant: 'success' })
      setEditMode(false)
    }
  }

  const isAdmin = user?.roles?.includes('ROLE_ADMIN')

  return (
    <Container maxWidth="lg" sx={{ py: 4 }}>
      <Grid container spacing={4}>
        {/* Profile card */}
        <Grid item xs={12} md={4}>
          <Card elevation={3}>
            <CardContent sx={{ textAlign: 'center', py: 4 }}>
              <Avatar
                sx={{ width: 96, height: 96, mx: 'auto', mb: 2, bgcolor: 'primary.main', fontSize: '2.5rem' }}
              >
                {user?.name?.charAt(0).toUpperCase()}
              </Avatar>
              <Typography variant="h5" fontWeight={700}>{user?.name}</Typography>
              <Typography color="text.secondary">@{user?.username}</Typography>
              <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
                {user?.email}
              </Typography>
              <Box sx={{ mt: 2, display: 'flex', gap: 1, justifyContent: 'center', flexWrap: 'wrap' }}>
                {user?.roles?.map((role) => (
                  <Chip
                    key={role}
                    label={role.replace('ROLE_', '')}
                    color={isAdmin ? 'error' : 'primary'}
                    size="small"
                  />
                ))}
              </Box>
              <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
                {userPosts.length} posts
              </Typography>
              <Button
                variant={editMode ? 'outlined' : 'contained'}
                sx={{ mt: 3, width: '100%' }}
                onClick={() => setEditMode(!editMode)}
              >
                {editMode ? 'Cancel Edit' : 'Edit Profile'}
              </Button>
            </CardContent>
          </Card>

          {/* Edit form */}
          {editMode && (
            <Card elevation={2} sx={{ mt: 2 }}>
              <CardContent>
                <Typography variant="h6" gutterBottom>Update Profile</Typography>
                <Box component="form" onSubmit={handleSubmit(onSubmit)} noValidate>
                  <TextField
                    label="Full Name"
                    fullWidth
                    margin="dense"
                    {...register('name', { minLength: { value: 2, message: 'Minimum 2 characters' } })}
                    error={!!errors.name}
                    helperText={errors.name?.message}
                  />
                  <TextField
                    label="Email"
                    fullWidth
                    margin="dense"
                    {...register('email', {
                      pattern: { value: /^\S+@\S+\.\S+$/, message: 'Invalid email' },
                    })}
                    error={!!errors.email}
                    helperText={errors.email?.message}
                  />
                  <TextField
                    label="New Password (optional)"
                    type="password"
                    fullWidth
                    margin="dense"
                    {...register('password', {
                      minLength: { value: 6, message: 'Minimum 6 characters' },
                    })}
                    error={!!errors.password}
                    helperText={errors.password?.message}
                  />
                  <Button
                    type="submit"
                    variant="contained"
                    fullWidth
                    disabled={loading}
                    sx={{ mt: 2 }}
                  >
                    {loading ? <CircularProgress size={20} /> : 'Save Changes'}
                  </Button>
                </Box>
              </CardContent>
            </Card>
          )}
        </Grid>

        {/* User's posts */}
        <Grid item xs={12} md={8}>
          <Typography variant="h5" fontWeight={700} gutterBottom>
            My Posts
          </Typography>
          <Divider sx={{ mb: 3 }} />
          {postsLoading ? (
            <Box sx={{ display: 'flex', justifyContent: 'center', py: 4 }}>
              <CircularProgress />
            </Box>
          ) : userPosts.length === 0 ? (
            <Typography color="text.secondary">You haven&apos;t written any posts yet.</Typography>
          ) : (
            <Grid container spacing={2}>
              {userPosts.map((post) => (
                <Grid item xs={12} sm={6} key={post.id}>
                  <PostCard post={post} showActions />
                </Grid>
              ))}
            </Grid>
          )}
        </Grid>
      </Grid>
    </Container>
  )
}
