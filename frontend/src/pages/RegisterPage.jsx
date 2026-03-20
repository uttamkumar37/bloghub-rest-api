import React from 'react'
import { useDispatch, useSelector } from 'react-redux'
import { Link, useNavigate } from 'react-router-dom'
import { useForm } from 'react-hook-form'
import {
  Container, Box, Card, CardContent, Typography,
  TextField, Button, Alert, CircularProgress, Grid
} from '@mui/material'
import { register as registerUser, clearError } from '../redux/slices/authSlice'
import { useSnackbar } from 'notistack'

export default function RegisterPage() {
  const dispatch = useDispatch()
  const navigate = useNavigate()
  const { enqueueSnackbar } = useSnackbar()
  const { loading, error } = useSelector((state) => state.auth)

  const {
    register,
    handleSubmit,
    watch,
    formState: { errors },
  } = useForm()

  const password = watch('password')

  const onSubmit = async (data) => {
    dispatch(clearError())
    const result = await dispatch(registerUser({
      name: data.name,
      username: data.username,
      email: data.email,
      password: data.password,
    }))

    if (registerUser.fulfilled.match(result)) {
      enqueueSnackbar('Account created! Please sign in.', { variant: 'success' })
      navigate('/login')
    }
  }

  return (
    <Container maxWidth="sm">
      <Box sx={{ mt: 6, mb: 4, display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
        <Typography variant="h4" fontWeight={700} gutterBottom>
          Create Account
        </Typography>
        <Typography color="text.secondary" gutterBottom>
          Join the BlogHub community
        </Typography>

        <Card sx={{ mt: 3, width: '100%', p: 1 }} elevation={3}>
          <CardContent>
            {error && (
              <Alert severity="error" sx={{ mb: 2 }} onClose={() => dispatch(clearError())}>
                {error}
              </Alert>
            )}

            <Box component="form" onSubmit={handleSubmit(onSubmit)} noValidate>
              <Grid container spacing={2}>
                <Grid item xs={12} sm={6}>
                  <TextField
                    label="Full Name"
                    fullWidth
                    autoFocus
                    {...register('name', {
                      required: 'Name is required',
                      minLength: { value: 2, message: 'Name must be at least 2 characters' },
                    })}
                    error={!!errors.name}
                    helperText={errors.name?.message}
                  />
                </Grid>
                <Grid item xs={12} sm={6}>
                  <TextField
                    label="Username"
                    fullWidth
                    {...register('username', {
                      required: 'Username is required',
                      minLength: { value: 3, message: 'Minimum 3 characters' },
                      pattern: { value: /^[a-zA-Z0-9_]+$/, message: 'Letters, numbers, underscores only' },
                    })}
                    error={!!errors.username}
                    helperText={errors.username?.message}
                  />
                </Grid>
                <Grid item xs={12}>
                  <TextField
                    label="Email Address"
                    type="email"
                    fullWidth
                    {...register('email', {
                      required: 'Email is required',
                      pattern: { value: /^\S+@\S+\.\S+$/, message: 'Enter a valid email' },
                    })}
                    error={!!errors.email}
                    helperText={errors.email?.message}
                  />
                </Grid>
                <Grid item xs={12} sm={6}>
                  <TextField
                    label="Password"
                    type="password"
                    fullWidth
                    {...register('password', {
                      required: 'Password is required',
                      minLength: { value: 6, message: 'Minimum 6 characters' },
                    })}
                    error={!!errors.password}
                    helperText={errors.password?.message}
                  />
                </Grid>
                <Grid item xs={12} sm={6}>
                  <TextField
                    label="Confirm Password"
                    type="password"
                    fullWidth
                    {...register('confirmPassword', {
                      required: 'Please confirm your password',
                      validate: (val) => val === password || 'Passwords do not match',
                    })}
                    error={!!errors.confirmPassword}
                    helperText={errors.confirmPassword?.message}
                  />
                </Grid>
              </Grid>

              <Button
                type="submit"
                fullWidth
                variant="contained"
                size="large"
                disabled={loading}
                sx={{ mt: 3, mb: 2, py: 1.5 }}
              >
                {loading ? <CircularProgress size={24} color="inherit" /> : 'Create Account'}
              </Button>
            </Box>

            <Typography align="center" variant="body2">
              Already have an account?{' '}
              <Link to="/login" style={{ color: '#1976d2', fontWeight: 600 }}>
                Sign In
              </Link>
            </Typography>
          </CardContent>
        </Card>
      </Box>
    </Container>
  )
}
