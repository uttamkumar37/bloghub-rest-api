import './App.css'
import { useEffect, useState } from 'react'

type ApiResponse<T> = {
  success: boolean
  message?: string
  data: T
}

type AuthResponse = {
  accessToken: string
  tokenType: string
}

type UserDto = {
  id: number
  name: string
  email: string
}

type PostDto = {
  id: number
  title: string
  slug: string
  content: string
  authorName: string
}

type PageResponse<T> = {
  content: T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
}

const API_BASE_URL = 'http://localhost:8080/api/v1'

function App() {
  const [token, setToken] = useState<string | null>(null)
  const [currentUser, setCurrentUser] = useState<UserDto | null>(null)
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [posts, setPosts] = useState<PostDto[]>([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    const stored = window.localStorage.getItem('bloghub_token')
    if (stored) {
      setToken(stored)
    }
  }, [])

  useEffect(() => {
    if (token) {
      fetchMe(token)
      fetchPosts()
    }
  }, [token])

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault()
    setLoading(true)
    setError(null)
    try {
      const res = await fetch(`${API_BASE_URL}/auth/login`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ email, password }),
      })

      if (!res.ok) {
        const body = await res.json().catch(() => null)
        throw new Error(body?.message || 'Login failed')
      }

      const body = (await res.json()) as ApiResponse<AuthResponse>
      const accessToken = body.data.accessToken
      setToken(accessToken)
      window.localStorage.setItem('bloghub_token', accessToken)
    } catch (err) {
      if (err instanceof Error) {
        setError(err.message)
      } else {
        setError('Unexpected error')
      }
    } finally {
      setLoading(false)
    }
  }

  const fetchMe = async (jwt: string) => {
    try {
      const res = await fetch(`${API_BASE_URL}/auth/me`, {
        headers: {
          Authorization: `Bearer ${jwt}`,
        },
      })
      if (!res.ok) {
        throw new Error('Failed to load current user')
      }
      const body = (await res.json()) as ApiResponse<UserDto>
      setCurrentUser(body.data)
    } catch (err) {
      console.error(err)
    }
  }

  const fetchPosts = async () => {
    setLoading(true)
    setError(null)
    try {
      const res = await fetch(`${API_BASE_URL}/posts`)
      if (!res.ok) {
        throw new Error('Failed to load posts')
      }
      const body = (await res.json()) as ApiResponse<PageResponse<PostDto>>
      setPosts(body.data.content)
    } catch (err) {
      if (err instanceof Error) {
        setError(err.message)
      } else {
        setError('Unexpected error while loading posts')
      }
    } finally {
      setLoading(false)
    }
  }

  const handleLogout = () => {
    setToken(null)
    setCurrentUser(null)
    window.localStorage.removeItem('bloghub_token')
  }

  return (
    <div className="app">
      <header className="app-header">
        <h1>Bloghub</h1>
        <div className="header-right">
          {currentUser ? (
            <>
              <span className="user">
                Signed in as <strong>{currentUser.name}</strong>
              </span>
              <button className="button secondary" onClick={handleLogout}>
                Logout
              </button>
            </>
          ) : (
            <span className="user">Not signed in</span>
          )}
        </div>
      </header>

      <main className="layout">
        <section className="panel panel-auth">
          <h2>Login</h2>
          <form onSubmit={handleLogin} className="form">
            <label>
              <span>Email</span>
              <input
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="you@example.com"
                required
              />
            </label>
            <label>
              <span>Password</span>
              <input
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
              />
            </label>
            <button className="button primary" type="submit" disabled={loading}>
              {loading ? 'Signing in...' : 'Sign in'}
            </button>
            {error && <p className="error">{error}</p>}
          </form>
          <p className="hint">
            Make sure you have a user in the database that can log in.
          </p>
        </section>

        <section className="panel panel-posts">
          <div className="panel-header">
            <h2>Latest posts</h2>
            <button className="button ghost" onClick={fetchPosts} disabled={loading}>
              Refresh
            </button>
          </div>
          {loading && <p>Loading...</p>}
          {!loading && posts.length === 0 && <p>No posts yet.</p>}
          <ul className="post-list">
            {posts.map((post) => (
              <li key={post.id} className="post-item">
                <h3>{post.title}</h3>
                <p className="post-meta">
                  By <strong>{post.authorName}</strong> · slug: <code>{post.slug}</code>
                </p>
                <p className="post-content">
                  {post.content.length > 180 ? `${post.content.slice(0, 180)}...` : post.content}
                </p>
              </li>
            ))}
          </ul>
        </section>
      </main>
    </div>
  )
}

export default App
