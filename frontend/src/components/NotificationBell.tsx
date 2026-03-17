import { useState, useEffect, useRef } from 'react';
import api from '../api/client';
import { Notification, ApiResponse, Page } from '../types';
import { Bell, Check, CheckCheck } from 'lucide-react';

export default function NotificationBell() {
    const [unread, setUnread] = useState(0);
    const [open, setOpen] = useState(false);
    const [notifs, setNotifs] = useState<Notification[]>([]);
    const ref = useRef<HTMLDivElement>(null);

    const fetchUnread = async () => {
        try {
            const { data } = await api.get<ApiResponse<{ count: number }>>('/api/v1/notifications/unread-count');
            setUnread(data.data.count);
        } catch { }
    };

    const fetchList = async () => {
        try {
            const { data } = await api.get<ApiResponse<Page<Notification>>>('/api/v1/notifications', { params: { size: 10 } });
            setNotifs(data.data.content);
        } catch { }
    };

    useEffect(() => {
        fetchUnread();
        const interval = setInterval(fetchUnread, 30000); // poll every 30s
        return () => clearInterval(interval);
    }, []);

    useEffect(() => {
        if (open) fetchList();
    }, [open]);

    useEffect(() => {
        const handleClick = (e: MouseEvent) => {
            if (ref.current && !ref.current.contains(e.target as Node)) setOpen(false);
        };
        document.addEventListener('mousedown', handleClick);
        return () => document.removeEventListener('mousedown', handleClick);
    }, []);

    const markRead = async (id: string) => {
        await api.put(`/api/v1/notifications/${id}/read`).catch(() => { });
        setNotifs(prev => prev.map(n => n.id === id ? { ...n, read: true } : n));
        setUnread(prev => Math.max(0, prev - 1));
    };

    const markAllRead = async () => {
        await api.put('/api/v1/notifications/read-all').catch(() => { });
        setNotifs(prev => prev.map(n => ({ ...n, read: true })));
        setUnread(0);
    };

    const typeIcon: Record<string, string> = {
        TICKET_ASSIGNED: '🎫',
        SLA_BREACH: '⚠️',
        SYSTEM: '⚙️',
        RENT_DUE: '💰',
    };

    return (
        <div className="relative" ref={ref}>
            <button onClick={() => setOpen(!open)} className="relative p-2 rounded-lg hover:bg-slate-800 transition-colors">
                <Bell size={20} className="text-slate-300" />
                {unread > 0 && (
                    <span className="absolute -top-0.5 -right-0.5 w-5 h-5 bg-rose-500 rounded-full text-[10px] font-bold text-white flex items-center justify-center animate-pulse">
                        {unread > 9 ? '9+' : unread}
                    </span>
                )}
            </button>

            {open && (
                <div className="absolute right-0 top-12 w-80 glass-card border border-slate-700 shadow-2xl z-50 max-h-96 overflow-hidden flex flex-col">
                    <div className="p-3 border-b border-slate-700 flex items-center justify-between">
                        <h3 className="text-sm font-semibold text-white">Notifications</h3>
                        {unread > 0 && (
                            <button onClick={markAllRead} className="text-xs text-violet-400 hover:text-violet-300 flex items-center gap-1">
                                <CheckCheck size={14} /> Mark all read
                            </button>
                        )}
                    </div>
                    <div className="overflow-y-auto flex-1">
                        {notifs.length > 0 ? notifs.map(n => (
                            <div key={n.id} className={`p-3 border-b border-slate-800 hover:bg-slate-800/50 transition-colors cursor-pointer ${!n.read ? 'bg-violet-500/5' : ''}`}
                                onClick={() => !n.read && markRead(n.id)}>
                                <div className="flex items-start gap-2">
                                    <span className="text-base mt-0.5">{typeIcon[n.type] || '📌'}</span>
                                    <div className="flex-1 min-w-0">
                                        <p className={`text-sm ${!n.read ? 'text-white font-medium' : 'text-slate-400'}`}>{n.title}</p>
                                        <p className="text-xs text-slate-500 mt-0.5 truncate">{n.message}</p>
                                        <p className="text-[10px] text-slate-600 mt-1">{new Date(n.createdAt).toLocaleString()}</p>
                                    </div>
                                    {!n.read && <div className="w-2 h-2 rounded-full bg-violet-500 mt-1.5 flex-shrink-0" />}
                                </div>
                            </div>
                        )) : (
                            <div className="p-8 text-center text-slate-500 text-sm">
                                <Bell size={24} className="mx-auto mb-2 opacity-40" />
                                No notifications
                            </div>
                        )}
                    </div>
                </div>
            )}
        </div>
    );
}
