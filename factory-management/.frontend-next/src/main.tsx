import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { BrowserRouter } from 'react-router-dom'
import './index.css'
import './toast.css'
import './frontend-next.css'
import App from './App'
import ToastViewport from './components/ToastViewport'
import { AuthProvider } from './context/AuthContext'

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
