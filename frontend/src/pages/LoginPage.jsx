import React, { useState } from 'react'
import { useDispatch, useSelector } from 'react-redux'
import { Link, useNavigate } from 'react-router-dom'
import { useForm } from 'react-hook-form'
import {
  Container, Box, Card, CardContent, Typography,
  TextField, Button, Alert, CircularProgress, Divider
} from '@mui/material'
import { login, clearError } from '../redux/slices/authSlice'
import { useSnackbar } from 'notistack'

export default function LoginPage() {
  const dispatch = useDispatch()
  const navigate = useNavigate()
  const { enqueueSnackbar } = useSnackbar()
  const { loading, error } = useSelector((state) => state.auth)

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm()

  const onSubmit = async (data) => {
    dispatch(clearError())
    const result = await dispatch(login({
      usernameOrEmail: data.usernameOrEmail,
      password: data.password,
    }))

    if (login.fulfilled.match(result)) {
      enqueueSnackbar('Welcome back!', { variant: 'success' })
      navigate('/dashboard')
    }
  }

  return (
    <Container maxWidth="xs">
      <Box sx={{ mt: 8, mb: 4, display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
        <Typography variant="h4" fontWeight={700} gutterBottom>
          Welcome Back
        </Typography>
        <Typography color="text.secondary" gutterBottom>
          Sign in to your BlogHub account
        </Typography>

        <Card sx={{ mt: 3, width: '100%', p: 1 }} elevation={3}>
          <CardContent>
            {error && (
              <Alert severity="error" sx={{ mb: 2 }} onClose={() => dispatch(clearError())}>
                {error}
              </Alert>
            )}

            <Box component="form" onSubmit={handleSubmit(onSubmit)} noValidate>
              <TextField
                label="Username or Email"
                fullWidth
                margin="normal"
                autoFocus
                {...register('usernameOrEmail', { required: 'Username or email is required' })}
                error={!!errors.usernameOrEmail}
                helperText={errors.usernameOrEmail?.message}
              />
              <TextField
                label="Password"
                type="password"
                fullWidth
                margin="normal"
                {...register('password', { required: 'Password is required' })}
                error={!!errors.password}
                helperText={errors.password?.message}
              />

              <Button
                type="submit"
                fullWidth
                variant="contained"
                size="large"
                disabled={loading}
                sx={{ mt: 3, mb: 2, py: 1.5 }}
              >
                {loading ? <CircularProgress size={24} color="inherit" /> : 'Sign In'}
              </Button>
            </Box>

            <Divider sx={{ my: 2 }}>
              <Typography color="text.secondary" variant="body2">OR</Typography>
            </Divider>

            <Typography align="center" variant="body2">
              Don't have an account?{' '}
              <Link to="/register" style={{ color: '#1976d2', fontWeight: 600 }}>
                Sign Up
              </Link>
            </Typography>
          </CardContent>
        </Card>
      </Box>
    </Container>
  )
}
