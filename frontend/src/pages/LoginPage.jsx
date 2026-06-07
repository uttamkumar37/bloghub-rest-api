import React, { useState } from 'react'
import { useDispatch, useSelector } from 'react-redux'
import { Link, useNavigate } from 'react-router-dom'
import { useForm } from 'react-hook-form'
import {
  Container, Box, Card, CardContent, Typography,
  TextField, Button, Alert, CircularProgress, Divider,
  FormControl, InputLabel, Select, MenuItem
} from '@mui/material'
import { login, clearError } from '../redux/slices/authSlice'
import { useSnackbar } from 'notistack'

const demoAccounts = [
  {
    label: 'Demo User',
    usernameOrEmail: 'demo_user',
    password: 'BlogHub@2026Demo',
  },
]

export default function LoginPage() {
  const dispatch = useDispatch()
  const navigate = useNavigate()
  const { enqueueSnackbar } = useSnackbar()
  const { loading, error } = useSelector((state) => state.auth)
  const [selectedDemoAccount, setSelectedDemoAccount] = useState('')

  const {
    register,
    handleSubmit,
    setValue,
    formState: { errors },
  } = useForm()

  const handleDemoAccountChange = (event) => {
    const selectedLabel = event.target.value
    setSelectedDemoAccount(selectedLabel)

    const account = demoAccounts.find((item) => item.label === selectedLabel)
    if (!account) {
      setValue('usernameOrEmail', '')
      setValue('password', '')
      return
    }

    setValue('usernameOrEmail', account.usernameOrEmail, { shouldValidate: true })
    setValue('password', account.password, { shouldValidate: true })
  }

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
              {import.meta.env.DEV && (
                <FormControl fullWidth margin="normal">
                  <InputLabel id="demo-account-label">Quick login</InputLabel>
                  <Select
                    labelId="demo-account-label"
                    label="Quick login"
                    value={selectedDemoAccount}
                    onChange={handleDemoAccountChange}
                  >
                    <MenuItem value="">
                      <em>Select an account</em>
                    </MenuItem>
                    {demoAccounts.map((account) => (
                      <MenuItem key={account.label} value={account.label}>
                        {account.label}
                      </MenuItem>
                    ))}
                  </Select>
                </FormControl>
              )}

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
              Don&apos;t have an account?{' '}
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
