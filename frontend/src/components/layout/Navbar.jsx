import React from 'react'
import { useNavigate } from 'react-router-dom'
import { useDispatch, useSelector } from 'react-redux'
import {
  AppBar, Toolbar, Typography, Button, Box,
  IconButton, Avatar, Menu, MenuItem, Tooltip, Container
} from '@mui/material'
import CreateIcon from '@mui/icons-material/Create'
import { logout } from '../../redux/slices/authSlice'
import { useSnackbar } from 'notistack'

export default function Navbar() {
  const navigate = useNavigate()
  const dispatch = useDispatch()
  const { enqueueSnackbar } = useSnackbar()
  const { isAuthenticated, user } = useSelector((state) => state.auth)

  const [anchorEl, setAnchorEl] = React.useState(null)

  const handleMenuOpen = (e) => setAnchorEl(e.currentTarget)
  const handleMenuClose = () => setAnchorEl(null)

  const handleLogout = () => {
    dispatch(logout())
    handleMenuClose()
    enqueueSnackbar('Logged out successfully', { variant: 'info' })
    navigate('/login')
  }

  return (
    <AppBar position="fixed" elevation={1} sx={{ bgcolor: 'white', color: 'text.primary' }}>
      <Container maxWidth="lg">
        <Toolbar disableGutters sx={{ py: 0.5 }}>
          {/* Logo */}
          <Typography
            variant="h5"
            fontWeight={800}
            sx={{ cursor: 'pointer', color: 'primary.main', mr: 4 }}
            onClick={() => navigate('/dashboard')}
          >
            BlogHub
          </Typography>

          {/* Nav links */}
          <Box sx={{ flexGrow: 1, display: 'flex', gap: 1 }}>
            <Button onClick={() => navigate('/dashboard')} color="inherit">
              Explore
            </Button>
          </Box>

          {/* Auth actions */}
          {isAuthenticated ? (
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
              <Tooltip title="Write a post">
                <Button
                  variant="outlined"
                  startIcon={<CreateIcon />}
                  onClick={() => navigate('/posts/new')}
                  size="small"
                >
                  Write
                </Button>
              </Tooltip>

              <Tooltip title="Profile">
                <IconButton onClick={handleMenuOpen} size="small">
                  <Avatar sx={{ width: 36, height: 36, bgcolor: 'primary.main', fontSize: '1rem' }}>
                    {user?.name?.charAt(0).toUpperCase()}
                  </Avatar>
                </IconButton>
              </Tooltip>

              <Menu
                anchorEl={anchorEl}
                open={Boolean(anchorEl)}
                onClose={handleMenuClose}
                transformOrigin={{ horizontal: 'right', vertical: 'top' }}
                anchorOrigin={{ horizontal: 'right', vertical: 'bottom' }}
              >
                <MenuItem disabled>
                  <Typography variant="body2" color="text.secondary">
                    @{user?.username}
                  </Typography>
                </MenuItem>
                <MenuItem onClick={() => { navigate('/profile'); handleMenuClose() }}>
                  My Profile
                </MenuItem>
                <MenuItem onClick={handleLogout} sx={{ color: 'error.main' }}>
                  Logout
                </MenuItem>
              </Menu>
            </Box>
          ) : (
            <Box sx={{ display: 'flex', gap: 1 }}>
              <Button onClick={() => navigate('/login')}>Sign In</Button>
              <Button variant="contained" onClick={() => navigate('/register')}>
                Get Started
              </Button>
            </Box>
          )}
        </Toolbar>
      </Container>
    </AppBar>
  )
}
