import { NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import {
    LayoutDashboard, Ticket, Users, DoorOpen, UtensilsCrossed,
    Settings, LogOut, Building2
} from 'lucide-react';
import './Sidebar.css';

export default function Sidebar() {
    const { user, logout, isAdmin } = useAuth();
    const navigate = useNavigate();

    const handleLogout = () => {
        logout();
        navigate('/login');
    };

    const navItems = [
        { path: '/', icon: LayoutDashboard, label: 'Dashboard' },
        { path: '/tickets', icon: Ticket, label: 'Tickets' },
        ...(isAdmin ? [
            { path: '/staff', icon: Users, label: 'Staff' },
            { path: '/rooms', icon: DoorOpen, label: 'Rooms' },
            { path: '/food-feedback', icon: UtensilsCrossed, label: 'Food Feedback' },
            { path: '/settings', icon: Settings, label: 'Settings' },
        ] : []),
    ];

    return (
        <aside className="sidebar">
            <div className="sidebar-brand">
                <div className="brand-icon">
                    <Building2 size={24} />
                </div>
                <div className="brand-text">
                    <h1>PG Manager</h1>
                    <span>Pro</span>
                </div>
            </div>

            <nav className="sidebar-nav">
                {navItems.map(item => (
                    <NavLink
                        key={item.path}
                        to={item.path}
                        className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`}
                        end={item.path === '/'}
                    >
                        <item.icon size={18} />
                        <span>{item.label}</span>
                    </NavLink>
                ))}
            </nav>

            <div className="sidebar-footer">
                <div className="user-info">
                    <div className="user-avatar">
                        {user?.name?.charAt(0)?.toUpperCase() || 'U'}
                    </div>
                    <div className="user-details">
                        <span className="user-name">{user?.name || 'User'}</span>
                        <span className="user-role">{user?.role?.replace('_', ' ') || 'Role'}</span>
                    </div>
                </div>
                <button className="btn-ghost logout-btn" onClick={handleLogout}>
                    <LogOut size={16} />
                </button>
            </div>
        </aside>
    );
}
