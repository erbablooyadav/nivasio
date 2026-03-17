import { Outlet } from 'react-router-dom';
import Sidebar from './Sidebar';
import NotificationBell from './NotificationBell';
import { useAuthStore } from '../store/auth';

export default function Layout() {
    const { name } = useAuthStore();

    return (
        <div className="flex min-h-screen bg-slate-950">
            <Sidebar />
            <main className="flex-1 ml-64">
                {/* Top header bar */}
                <header className="sticky top-0 z-40 h-14 px-6 flex items-center justify-end gap-4 border-b border-slate-800 bg-slate-950/80 backdrop-blur-xl">
                    <NotificationBell />
                    <div className="flex items-center gap-2">
                        <div className="w-8 h-8 bg-gradient-to-br from-violet-500 to-emerald-500 rounded-full flex items-center justify-center text-xs font-bold text-white">
                            {name?.charAt(0) || 'N'}
                        </div>
                    </div>
                </header>
                <div className="p-6">
                    <Outlet />
                </div>
            </main>
        </div>
    );
}
