import { createContext, useContext, useState, useEffect } from 'react';
import api from '../api/client';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
    const [user, setUser] = useState(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const token = localStorage.getItem('accessToken');
        const userData = localStorage.getItem('user');
        if (token && userData) {
            try {
                setUser(JSON.parse(userData));
            } catch { /* ignore */ }
        }
        setLoading(false);
    }, []);

    const login = async (identifier, password) => {
        const result = await api.login(identifier, password);
        const data = result.data;
        localStorage.setItem('accessToken', data.accessToken);
        localStorage.setItem('refreshToken', data.refreshToken);
        const userData = {
            userId: data.userId,
            tenantId: data.tenantId,
            name: data.name,
            role: data.role,
        };
        localStorage.setItem('user', JSON.stringify(userData));
        setUser(userData);
        return userData;
    };

    const register = async (formData) => {
        const result = await api.register(formData);
        const data = result.data;
        localStorage.setItem('accessToken', data.accessToken);
        localStorage.setItem('refreshToken', data.refreshToken);
        const userData = {
            userId: data.userId,
            tenantId: data.tenantId,
            name: data.name,
            role: data.role,
        };
        localStorage.setItem('user', JSON.stringify(userData));
        setUser(userData);
        return userData;
    };

    const logout = () => {
        localStorage.clear();
        setUser(null);
    };

    const isAdmin = user?.role === 'TENANT_ADMIN' || user?.role === 'SUPER_ADMIN';

    return (
        <AuthContext.Provider value={{ user, loading, login, register, logout, isAdmin }}>
            {children}
        </AuthContext.Provider>
    );
}

export function useAuth() {
    const context = useContext(AuthContext);
    if (!context) throw new Error('useAuth must be used within AuthProvider');
    return context;
}
