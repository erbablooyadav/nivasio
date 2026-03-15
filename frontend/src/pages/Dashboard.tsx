import { useEffect, useState } from 'react';
import api from '../api/client';
import { useAuthStore } from '../store/auth';
import {
    BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer,
    PieChart, Pie, Cell, LineChart, Line, AreaChart, Area
} from 'recharts';
import { Ticket, Users, DoorOpen, AlertTriangle, TrendingUp, Clock, IndianRupee, Shield } from 'lucide-react';
import type { DashboardStats } from '../types';

const COLORS = ['#8b5cf6', '#3b82f6', '#10b981', '#f59e0b', '#f43f5e', '#06b6d4'];
const TOOLTIP_STYLE = { background: '#1e293b', border: '1px solid #334155', borderRadius: '12px', color: '#e2e8f0' };

export default function Dashboard() {
    const [stats, setStats] = useState<DashboardStats | null>(null);
    const { name } = useAuthStore();

    useEffect(() => {
        api.get('/api/v1/dashboard/stats').then(r => setStats(r.data.data)).catch(() => { });
    }, []);

    if (!stats) return <div className="flex items-center justify-center h-64 text-slate-400">Loading...</div>;

    // Real department breakdown from backend
    const deptData = Object.entries(stats.departmentBreakdown || {}).map(([dept, count]) => ({
        dept: dept.charAt(0) + dept.slice(1).toLowerCase(),
        count
    }));

    // Real weekly trend from backend
    const trendData = (stats.weeklyTrend || []).map(t => ({ date: t.date, tickets: t.count }));

    // Ticket status pie
    const pieData = [
        { name: 'Open', value: stats.openTickets },
        { name: 'Assigned', value: stats.assignedTickets },
        { name: 'In Progress', value: stats.inProgressTickets },
        { name: 'Done', value: stats.doneTickets },
    ].filter(d => d.value > 0);

    // Staff workload from backend
    const workloadData = Object.entries(stats.staffWorkload || {})
        .map(([name, count]) => ({ name, tickets: count }))
        .sort((a, b) => b.tickets - a.tickets)
        .slice(0, 8);

    const statCards = [
        { label: 'Total Tickets', value: stats.totalTickets, icon: Ticket, color: 'from-violet-500 to-violet-700', delta: stats.todayTickets + ' today' },
        { label: 'Open', value: stats.openTickets, icon: Clock, color: 'from-amber-500 to-amber-700' },
        { label: 'SLA Breach', value: stats.slaBreachTickets, icon: AlertTriangle, color: 'from-rose-500 to-rose-700', delta: stats.slaBreachRate + '% rate' },
        { label: 'Rooms', value: stats.totalRooms, icon: DoorOpen, color: 'from-emerald-500 to-emerald-700', delta: stats.totalResidents + ' residents' },
        { label: 'Staff', value: stats.totalStaff, icon: Users, color: 'from-blue-500 to-blue-700' },
        { label: "Today's Load", value: stats.todayTickets, icon: TrendingUp, color: 'from-cyan-500 to-cyan-700' },
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

            {/* Row 1: Weekly Trend + Status Pie */}
            <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-6">
                <div className="glass-card p-6">
                    <h3 className="text-lg font-semibold text-white mb-4">Weekly Ticket Trend</h3>
                    <ResponsiveContainer width="100%" height={280}>
                        <AreaChart data={trendData}>
                            <defs>
                                <linearGradient id="trendGrad" x1="0" y1="0" x2="0" y2="1">
                                    <stop offset="5%" stopColor="#8b5cf6" stopOpacity={0.4} />
                                    <stop offset="95%" stopColor="#8b5cf6" stopOpacity={0} />
                                </linearGradient>
                            </defs>
                            <CartesianGrid strokeDasharray="3 3" stroke="#334155" />
                            <XAxis dataKey="date" tick={{ fill: '#94a3b8', fontSize: 12 }} />
                            <YAxis tick={{ fill: '#94a3b8', fontSize: 12 }} />
                            <Tooltip contentStyle={TOOLTIP_STYLE} />
                            <Area type="monotone" dataKey="tickets" stroke="#8b5cf6" strokeWidth={2} fillOpacity={1} fill="url(#trendGrad)" />
                        </AreaChart>
                    </ResponsiveContainer>
                </div>

                <div className="glass-card p-6">
                    <h3 className="text-lg font-semibold text-white mb-4">Ticket Status</h3>
                    <ResponsiveContainer width="100%" height={280}>
                        <PieChart>
                            <Pie data={pieData} cx="50%" cy="50%" innerRadius={60} outerRadius={100} paddingAngle={5} dataKey="value"
                                label={({ name, percent }) => `${name} ${(percent * 100).toFixed(0)}%`}>
                                {pieData.map((_, i) => <Cell key={i} fill={COLORS[i % COLORS.length]} />)}
                            </Pie>
                            <Tooltip contentStyle={TOOLTIP_STYLE} />
                        </PieChart>
                    </ResponsiveContainer>
                </div>
            </div>

            {/* Row 2: Department Breakdown + Staff Workload */}
            <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
                <div className="glass-card p-6">
                    <h3 className="text-lg font-semibold text-white mb-4">Active Tickets by Department</h3>
                    <ResponsiveContainer width="100%" height={280}>
                        <BarChart data={deptData}>
                            <CartesianGrid strokeDasharray="3 3" stroke="#334155" />
                            <XAxis dataKey="dept" tick={{ fill: '#94a3b8', fontSize: 12 }} />
                            <YAxis tick={{ fill: '#94a3b8', fontSize: 12 }} />
                            <Tooltip contentStyle={TOOLTIP_STYLE} />
                            <Bar dataKey="count" fill="#8b5cf6" radius={[8, 8, 0, 0]} />
                        </BarChart>
                    </ResponsiveContainer>
                </div>

                <div className="glass-card p-6">
                    <h3 className="text-lg font-semibold text-white mb-4">Staff Workload</h3>
                    {workloadData.length > 0 ? (
                        <div className="space-y-3">
                            {workloadData.map((staff, i) => {
                                const maxTickets = workloadData[0]?.tickets || 1;
                                const pct = (staff.tickets / maxTickets) * 100;
                                return (
                                    <div key={staff.name}>
                                        <div className="flex items-center justify-between text-sm mb-1">
                                            <span className="text-slate-300">{staff.name}</span>
                                            <span className="text-white font-medium">{staff.tickets} tickets</span>
                                        </div>
                                        <div className="h-2 bg-slate-700 rounded-full overflow-hidden">
                                            <div className="h-full rounded-full transition-all duration-500"
                                                style={{ width: `${pct}%`, background: COLORS[i % COLORS.length] }} />
                                        </div>
                                    </div>
                                );
                            })}
                        </div>
                    ) : (
                        <div className="text-center text-slate-500 py-12">
                            <Users size={32} className="mx-auto mb-2 opacity-50" />
                            <p>No active assignments</p>
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
}
