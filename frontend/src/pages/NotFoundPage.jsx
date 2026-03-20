import React from 'react'
import { useNavigate } from 'react-router-dom'
import { Container, Box, Typography, Button } from '@mui/material'

export default function NotFoundPage() {
  const navigate = useNavigate()
  return (
    <Container maxWidth="sm">
      <Box sx={{ textAlign: 'center', py: 12 }}>
        <Typography variant="h1" fontWeight={700} color="primary" sx={{ fontSize: '6rem' }}>
          404
        </Typography>
        <Typography variant="h5" gutterBottom>Page Not Found</Typography>
        <Typography color="text.secondary" sx={{ mb: 4 }}>
          The page you're looking for doesn't exist.
        </Typography>
        <Button variant="contained" size="large" onClick={() => navigate('/dashboard')}>
          Go Home
        </Button>
      </Box>
    </Container>
  )
}
