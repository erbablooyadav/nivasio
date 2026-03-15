import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { Toaster } from 'react-hot-toast';
import { useAuthStore } from './store/auth';
import Layout from './components/Layout';
import Login from './pages/Login';
import Dashboard from './pages/Dashboard';
import Tickets from './pages/Tickets';
import Staff from './pages/Staff';
import Rooms from './pages/Rooms';
import Food from './pages/Food';
import Settings from './pages/Settings';
import Residents from './pages/Residents';
import OnboardingPage from './pages/Onboarding';
import Rent from './pages/Rent';
import Properties from './pages/Properties';
import AuditLog from './pages/AuditLog';
import './index.css';

function PrivateRoute({ children }: { children: React.ReactNode }) {
    const { isAuthenticated } = useAuthStore();
    return isAuthenticated ? <>{children}</> : <Navigate to="/login" />;
}

function AppRoutes() {
    const { isAuthenticated } = useAuthStore();
    return (
        <Routes>
            <Route path="/login" element={isAuthenticated ? <Navigate to="/" /> : <Login />} />
            <Route element={<PrivateRoute><Layout /></PrivateRoute>}>
                <Route path="/" element={<Dashboard />} />
                <Route path="/tickets" element={<Tickets />} />
                <Route path="/properties" element={<Properties />} />
                <Route path="/residents" element={<Residents />} />
                <Route path="/rooms" element={<Rooms />} />
                <Route path="/staff" element={<Staff />} />
                <Route path="/onboarding" element={<OnboardingPage />} />
                <Route path="/rent" element={<Rent />} />
                <Route path="/food" element={<Food />} />
                <Route path="/audit" element={<AuditLog />} />
                <Route path="/settings" element={<Settings />} />
            </Route>
            <Route path="*" element={<Navigate to="/" />} />
        </Routes>
    );
}

export default function App() {
    return (
        <BrowserRouter>
            <Toaster position="top-right" toastOptions={{
                style: {
                    background: '#1e293b', color: '#e2e8f0',
                    border: '1px solid rgba(255,255,255,0.1)', borderRadius: '12px'
                }
            }} />
            <AppRoutes />
        </BrowserRouter>
    );
}
