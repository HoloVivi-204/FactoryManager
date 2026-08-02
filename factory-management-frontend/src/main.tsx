import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { BrowserRouter } from 'react-router-dom'
import './styles/index.css'
import './styles/toast.css'
import './styles/frontend-next.css'
import App from './app/App'
import ToastViewport from './shared/toast/ToastViewport'
import { AuthProvider } from './features/auth/context/AuthContext'

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <BrowserRouter>
      <AuthProvider>
        <App />
        <ToastViewport />
      </AuthProvider>
    </BrowserRouter>
  </StrictMode>,
)
