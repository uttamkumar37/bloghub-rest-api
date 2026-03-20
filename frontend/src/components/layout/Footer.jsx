import React from 'react'
import { Box, Container, Typography, Link, Divider } from '@mui/material'

export default function Footer() {
  return (
    <Box component="footer" sx={{ bgcolor: '#1a1a2e', color: 'grey.300', mt: 8, py: 4 }}>
      <Container maxWidth="lg">
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: 2 }}>
          <Typography variant="h6" fontWeight={700} color="primary.light">
            BlogHub
          </Typography>
          <Typography variant="body2" color="grey.500">
            © {new Date().getFullYear()} BlogHub. All rights reserved.
          </Typography>
          <Box sx={{ display: 'flex', gap: 3 }}>
            <Link href="/dashboard" underline="hover" color="inherit" variant="body2">Explore</Link>
            <Link href="#" underline="hover" color="inherit" variant="body2">Privacy</Link>
            <Link href="#" underline="hover" color="inherit" variant="body2">Terms</Link>
          </Box>
        </Box>
      </Container>
    </Box>
  )
}
