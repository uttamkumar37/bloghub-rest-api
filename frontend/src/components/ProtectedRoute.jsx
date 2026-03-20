import React from 'react'
import { Navigate, Outlet } from 'react-router-dom'
import { useSelector } from 'react-redux'

/**
 * Route guard that redirects unauthenticated users to /login.
 * Renders child routes via <Outlet /> if authenticated.
 */
export default function ProtectedRoute() {
  const { isAuthenticated } = useSelector((state) => state.auth)
  return isAuthenticated ? <Outlet /> : <Navigate to="/login" replace />
}
