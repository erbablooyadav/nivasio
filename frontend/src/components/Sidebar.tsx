import { NavLink, useNavigate } from 'react-router-dom';
import { useAuthStore } from '../store/auth';
import {
    LayoutDashboard, Ticket, Users, DoorOpen, UtensilsCrossed,
    Settings, LogOut, Building2
} from 'lucide-react';

const menuItems = [
    { path: '/', icon: LayoutDashboard, label: 'Dashboard', roles: ['PROPERTY_ADMIN', 'SUPER_ADMIN'] },
    { path: '/tickets', icon: Ticket, label: 'Tickets', roles: ['PROPERTY_ADMIN', 'SUPER_ADMIN', 'STAFF'] },
    { path: '/staff', icon: Users, label: 'Staff', roles: ['PROPERTY_ADMIN', 'SUPER_ADMIN'] },
    { path: '/rooms', icon: DoorOpen, label: 'Rooms', roles: ['PROPERTY_ADMIN', 'SUPER_ADMIN'] },
    { path: '/food', icon: UtensilsCrossed, label: 'Food Feedback', roles: ['PROPERTY_ADMIN', 'SUPER_ADMIN'] },
    { path: '/settings', icon: Settings, label: 'Settings', roles: ['PROPERTY_ADMIN', 'SUPER_ADMIN'] },
];

export default function Sidebar() {
    const { name, role, logout } = useAuthStore();
    const navigate = useNavigate();

    const handleLogout = () => {
        logout();
        navigate('/login');
    };

    const visibleItems = menuItems.filter(m => m.roles.includes(role || ''));

    return (
        <aside className="fixed left-0 top-0 bottom-0 w-64 bg-slate-900/95 backdrop-blur-xl border-r border-slate-800 flex flex-col z-50">
            {/* Logo */}
            <div className="p-6 border-b border-slate-800">
                <div className="flex items-center gap-3">
                    <div className="w-10 h-10 bg-gradient-to-br from-violet-500 to-violet-700 rounded-xl flex items-center justify-center">
                        <Building2 className="w-5 h-5 text-white" />
                    </div>
                    <div>
                        <h1 className="text-lg font-bold text-white">Nivasio</h1>
                        <p className="text-xs text-slate-500">Smart Residence Mgmt</p>
                    </div>
                </div>
            </div>

            {/* Navigation */}
            <nav className="flex-1 p-4 space-y-1 overflow-y-auto">
                {visibleItems.map((item) => (
                    <NavLink
                        key={item.path}
                        to={item.path}
                        end={item.path === '/'}
                        className={({ isActive }) => isActive ? 'sidebar-link-active' : 'sidebar-link'}
                    >
                        <item.icon className="w-5 h-5" />
                        <span className="text-sm font-medium">{item.label}</span>
                    </NavLink>
                ))}
            </nav>

            {/* User */}
            <div className="p-4 border-t border-slate-800">
                <div className="flex items-center gap-3 mb-3">
                    <div className="w-9 h-9 bg-gradient-to-br from-violet-500 to-emerald-500 rounded-full flex items-center justify-center text-sm font-bold text-white">
                        {name?.charAt(0) || 'N'}
                    </div>
                    <div className="flex-1 min-w-0">
                        <p className="text-sm font-medium text-white truncate">{name}</p>
                        <p className="text-xs text-slate-500">{role?.replace('_', ' ')}</p>
                    </div>
                </div>
                <button onClick={handleLogout} className="sidebar-link w-full text-rose-400 hover:text-rose-300 hover:bg-rose-500/10">
                    <LogOut className="w-4 h-4" />
                    <span className="text-sm">Logout</span>
                </button>
            </div>
        </aside>
    );
}
