import { create } from 'zustand';
import { persist } from 'zustand/middleware';

interface AuthState {
    accessToken: string | null;
    userId: string | null;
    tenantId: string | null;
    name: string | null;
    role: string | null;
    propertyId: string | null;
    isAuthenticated: boolean;
    setAuth: (data: {
        accessToken: string;
        userId: string;
        tenantId: string;
        name: string;
        role: string;
        propertyId?: string;
    }) => void;
    setAccessToken: (token: string) => void;
    logout: () => void;
}

export const useAuthStore = create<AuthState>()(
    persist(
        (set) => ({
            accessToken: null,
            userId: null,
            tenantId: null,
            name: null,
            role: null,
            propertyId: null,
            isAuthenticated: false,
            setAuth: (data) => set({
                accessToken: data.accessToken,
                userId: data.userId,
                tenantId: data.tenantId,
                name: data.name,
                role: data.role,
                propertyId: data.propertyId || null,
                isAuthenticated: true,
            }),
            setAccessToken: (token) => set({ accessToken: token }),
            logout: () => set({
                accessToken: null, userId: null, tenantId: null,
                name: null, role: null, propertyId: null, isAuthenticated: false,
            }),
        }),
        { name: 'nivasio-auth' }
    )
);
