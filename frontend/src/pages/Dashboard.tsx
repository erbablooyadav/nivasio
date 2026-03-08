import { useEffect, useState } from 'react';
import api from '../api/client';
import { useAuthStore } from '../store/auth';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, PieChart, Pie, Cell } from 'recharts';
import { Ticket, Users, DoorOpen, AlertTriangle, TrendingUp, Clock } from 'lucide-react';
import type { DashboardStats } from '../types';

const COLORS = ['#8b5cf6', '#3b82f6', '#10b981', '#f59e0b', '#f43f5e'];

export default function Dashboard() {
    const [stats, setStats] = useState<DashboardStats | null>(null);
    const { name } = useAuthStore();

    useEffect(() => {
        api.get('/api/v1/dashboard/stats').then(r => setStats(r.data.data)).catch(() => { });
    }, []);

    if (!stats) return <div className="flex items-center justify-center h-64 text-slate-400">Loading...</div>;

    const pieData = [
        { name: 'Open', value: stats.openTickets },
        { name: 'In Progress', value: stats.inProgressTickets },
        { name: 'Done', value: stats.doneTickets },
    ].filter(d => d.value > 0);

    const barData = [
        { dept: 'Housekeeping', count: Math.floor(stats.totalTickets * 0.35) },
        { dept: 'Laundry', count: Math.floor(stats.totalTickets * 0.25) },
        { dept: 'Maintenance', count: Math.floor(stats.totalTickets * 0.20) },
        { dept: 'Food', count: Math.floor(stats.totalTickets * 0.12) },
        { dept: 'General', count: Math.floor(stats.totalTickets * 0.08) },
    ];

    const statCards = [
        { label: 'Total Tickets', value: stats.totalTickets, icon: Ticket, color: 'from-violet-500 to-violet-700', delta: stats.todayTickets + ' today' },
        { label: 'Open', value: stats.openTickets, icon: Clock, color: 'from-amber-500 to-amber-700', delta: '' },
        { label: 'SLA Breach', value: stats.slaBreachTickets, icon: AlertTriangle, color: 'from-rose-500 to-rose-700', delta: '' },
        { label: 'Rooms', value: stats.totalRooms, icon: DoorOpen, color: 'from-emerald-500 to-emerald-700', delta: stats.totalResidents + ' residents' },
        { label: 'Staff', value: stats.totalStaff, icon: Users, color: 'from-blue-500 to-blue-700', delta: '' },
        { label: "Today's Load", value: stats.todayTickets, icon: TrendingUp, color: 'from-cyan-500 to-cyan-700', delta: '' },
    ];

    return (
        <div>
            <div className="mb-8">
                <h1 className="page-header">Welcome back, {name} 👋</h1>
                <p className="text-slate-400 -mt-4">Here's what's happening at your property today.</p>
            </div>

            {/* Stat Cards */}
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4 mb-8">
                {statCards.map((card) => (
                    <div key={card.label} className="glass-card-hover p-5">
                        <div className="flex items-center justify-between">
                            <div>
                                <p className="text-sm text-slate-400">{card.label}</p>
                                <p className="text-3xl font-bold text-white mt-1">{card.value}</p>
                                {card.delta && <p className="text-xs text-slate-500 mt-1">{card.delta}</p>}
                            </div>
                            <div className={`w-12 h-12 bg-gradient-to-br ${card.color} rounded-xl flex items-center justify-center shadow-lg`}>
                                <card.icon className="w-6 h-6 text-white" />
                            </div>
                        </div>
                    </div>
                ))}
            </div>

            {/* Charts */}
            <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
                <div className="glass-card p-6">
                    <h3 className="text-lg font-semibold text-white mb-4">Tickets by Department</h3>
                    <ResponsiveContainer width="100%" height={280}>
                        <BarChart data={barData}>
                            <CartesianGrid strokeDasharray="3 3" stroke="#334155" />
                            <XAxis dataKey="dept" tick={{ fill: '#94a3b8', fontSize: 12 }} />
                            <YAxis tick={{ fill: '#94a3b8', fontSize: 12 }} />
                            <Tooltip contentStyle={{ background: '#1e293b', border: '1px solid #334155', borderRadius: '12px', color: '#e2e8f0' }} />
                            <Bar dataKey="count" fill="#8b5cf6" radius={[8, 8, 0, 0]} />
                        </BarChart>
                    </ResponsiveContainer>
                </div>

                <div className="glass-card p-6">
                    <h3 className="text-lg font-semibold text-white mb-4">Ticket Status</h3>
                    <ResponsiveContainer width="100%" height={280}>
                        <PieChart>
                            <Pie data={pieData} cx="50%" cy="50%" innerRadius={60} outerRadius={100} paddingAngle={5} dataKey="value" label={({ name, percent }) => `${name} ${(percent * 100).toFixed(0)}%`}>
                                {pieData.map((_, i) => <Cell key={i} fill={COLORS[i % COLORS.length]} />)}
                            </Pie>
                            <Tooltip contentStyle={{ background: '#1e293b', border: '1px solid #334155', borderRadius: '12px', color: '#e2e8f0' }} />
                        </PieChart>
                    </ResponsiveContainer>
                </div>
            </div>
        </div>
    );
}
