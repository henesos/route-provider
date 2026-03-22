import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { AuthProvider } from './contexts/AuthContext'
import ProtectedRoute from './components/ProtectedRoute'
import MainLayout from './layouts/MainLayout'
import LoginPage from './pages/LoginPage'
import LocationsPage from './pages/LocationsPage'
import TransportationsPage from './pages/TransportationsPage'
import RoutesPage from './pages/RoutesPage'

function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          <Route path="/login" element={<LoginPage />} />
          <Route path="/" element={<ProtectedRoute><MainLayout /></ProtectedRoute>}>
            <Route index element={<Navigate to="/routes" replace />} />
            <Route path="locations" element={<LocationsPage />} />
            <Route path="transportations" element={<TransportationsPage />} />
            <Route path="routes" element={<RoutesPage />} />
          </Route>
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  )
}

export default App
