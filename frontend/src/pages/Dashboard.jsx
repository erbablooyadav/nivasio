import { useState, useEffect } from 'react';
import api from '../api/client';
import { useAuth } from '../context/AuthContext';
import {
    Ticket, Clock, CheckCircle, AlertTriangle, DoorOpen, Users,
    TrendingUp, ArrowUpRight
} from 'lucide-react';
import {
    BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer,
    PieChart, Pie, Cell
} from 'recharts';
import './Dashboard.css';

const COLORS = ['#6c63ff', '#ffb347', '#00d4aa', '#6a6a8a'];

export default function Dashboard() {
    const { user } = useAuth();
    const [stats, setStats] = useState(null);
    const [recentTickets, setRecentTickets] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        loadData();
    }, []);

    const loadData = async () => {
        try {
            const [statsRes, ticketsRes] = await Promise.all([
                api.getStats(),
                api.getTickets({ size: 5 }),
            ]);
            setStats(statsRes.data);
            setRecentTickets(ticketsRes.data?.content || []);
        } catch (err) {
            console.error('Dashboard load error:', err);
        } finally {
            setLoading(false);
        }
    };

    if (loading) {
        return (
            <div className="page">
                <div className="dashboard-loading">
                    {[1, 2, 3, 4].map(i => (
                        <div key={i} className="loading-skeleton stat-skeleton"></div>
                    ))}
                </div>
            </div>
        );
    }

    const pieData = stats ? [
        { name: 'Open', value: stats.openTickets },
        { name: 'In Progress', value: stats.inProgressTickets },
        { name: 'Done', value: stats.doneTickets },
        { name: 'Closed', value: stats.totalTickets - stats.openTickets - stats.inProgressTickets - stats.doneTickets },
    ].filter(d => d.value > 0) : [];

    const barData = [
        { name: 'HK', value: 12 },
        { name: 'Laundry', value: 8 },
        { name: 'Maint.', value: 5 },
        { name: 'Food', value: 3 },
        { name: 'General', value: 2 },
    ];

    const statCards = [
        {
            title: 'Total Tickets',
            value: stats?.totalTickets || 0,
            icon: Ticket,
            gradient: 'var(--gradient-primary)',
            change: '+12%',
        },
        {
            title: 'Open Tickets',
            value: stats?.openTickets || 0,
            icon: Clock,
            gradient: 'linear-gradient(135deg, #ffb347, #ff6723)',
            change: null,
        },
        {
            title: 'Resolved Today',
            value: stats?.doneTickets || 0,
            icon: CheckCircle,
            gradient: 'var(--gradient-secondary)',
            change: '+5%',
        },
        {
            title: 'SLA Breached',
            value: stats?.slaBreachTickets || 0,
            icon: AlertTriangle,
            gradient: 'var(--gradient-danger)',
            change: null,
            danger: true,
        },
    ];

    return (
        <div className="page">
            <div className="page-header">
                <div>
                    <h1 className="page-title">Dashboard</h1>
                    <p className="text-muted text-sm">Welcome back, {user?.name} 👋</p>
                </div>
            </div>

            {/* Stat Cards */}
            <div className="stats-grid">
                {statCards.map((card, i) => (
                    <div className={`stat-card ${card.danger && card.value > 0 ? 'danger' : ''}`} key={i}>
                        <div className="stat-card-header">
                            <span className="stat-label">{card.title}</span>
                            <div className="stat-icon" style={{ background: card.gradient }}>
                                <card.icon size={18} />
                            </div>
                        </div>
                        <div className="stat-value">{card.value}</div>
                        {card.change && (
                            <div className="stat-change positive">
                                <ArrowUpRight size={14} />
                                <span>{card.change} vs last week</span>
                            </div>
                        )}
                    </div>
                ))}
            </div>

            {/* Quick Stats Row */}
            <div className="quick-stats">
                <div className="quick-stat">
                    <DoorOpen size={16} />
                    <span>{stats?.totalRooms || 0} Rooms</span>
                </div>
                <div className="quick-stat">
                    <Users size={16} />
                    <span>{stats?.totalStaff || 0} Staff Members</span>
                </div>
                <div className="quick-stat">
                    <TrendingUp size={16} />
                    <span>{stats?.todayTickets || 0} Tickets Today</span>
                </div>
            </div>

            {/* Charts */}
            <div className="charts-grid">
                <div className="card chart-card">
                    <h3 className="chart-title">Tickets by Department</h3>
                    <ResponsiveContainer width="100%" height={250}>
                        <BarChart data={barData}>
                            <CartesianGrid strokeDasharray="3 3" stroke="rgba(255,255,255,0.06)" />
                            <XAxis dataKey="name" stroke="var(--text-muted)" fontSize={12} />
                            <YAxis stroke="var(--text-muted)" fontSize={12} />
                            <Tooltip
                                contentStyle={{
                                    background: 'var(--bg-secondary)',
                                    border: '1px solid var(--glass-border)',
                                    borderRadius: '8px',
                                    color: 'var(--text-primary)'
                                }}
                            />
                            <Bar dataKey="value" fill="url(#barGradient)" radius={[6, 6, 0, 0]} />
                            <defs>
                                <linearGradient id="barGradient" x1="0" y1="0" x2="0" y2="1">
                                    <stop offset="0%" stopColor="#6c63ff" />
                                    <stop offset="100%" stopColor="#a855f7" />
                                </linearGradient>
                            </defs>
                        </BarChart>
                    </ResponsiveContainer>
                </div>

                <div className="card chart-card">
                    <h3 className="chart-title">Ticket Status Overview</h3>
                    {pieData.length > 0 ? (
                        <ResponsiveContainer width="100%" height={250}>
                            <PieChart>
                                <Pie
                                    data={pieData}
                                    cx="50%"
                                    cy="50%"
                                    innerRadius={65}
                                    outerRadius={95}
                                    paddingAngle={4}
                                    dataKey="value"
                                >
                                    {pieData.map((_, i) => (
                                        <Cell key={i} fill={COLORS[i % COLORS.length]} />
                                    ))}
                                </Pie>
                                <Tooltip
                                    contentStyle={{
                                        background: 'var(--bg-secondary)',
                                        border: '1px solid var(--glass-border)',
                                        borderRadius: '8px',
                                        color: 'var(--text-primary)'
                                    }}
                                />
                            </PieChart>
                        </ResponsiveContainer>
                    ) : (
                        <div className="empty-state">
                            <p>No ticket data yet</p>
                        </div>
                    )}
                    <div className="chart-legend">
                        {pieData.map((item, i) => (
                            <div key={i} className="legend-item">
                                <span className="legend-dot" style={{ background: COLORS[i] }}></span>
                                <span>{item.name}: {item.value}</span>
                            </div>
                        ))}
                    </div>
                </div>
            </div>

            {/* Recent Tickets */}
            <div className="card mt-4">
                <h3 className="chart-title mb-4">Recent Tickets</h3>
                {recentTickets.length > 0 ? (
                    <div className="table-container">
                        <table>
                            <thead>
                                <tr>
                                    <th>Ticket ID</th>
                                    <th>Type</th>
                                    <th>Room</th>
                                    <th>Status</th>
                                    <th>Priority</th>
                                    <th>Created</th>
                                </tr>
                            </thead>
                            <tbody>
                                {recentTickets.map(ticket => (
                                    <tr key={ticket.id}>
                                        <td className="font-bold">{ticket.ticketId}</td>
                                        <td>{ticket.department}</td>
                                        <td>{ticket.roomNo}</td>
                                        <td>
                                            <span className={`badge badge-${ticket.status?.toLowerCase()}`}>
                                                {ticket.status}
                                            </span>
                                        </td>
                                        <td>
                                            <span className={`badge badge-${ticket.priority?.toLowerCase()}`}>
                                                {ticket.priority}
                                            </span>
                                        </td>
                                        <td className="text-muted text-sm">
                                            {ticket.createdAt ? new Date(ticket.createdAt).toLocaleDateString() : '-'}
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                ) : (
                    <div className="empty-state">
                        <Ticket size={40} />
                        <p className="mt-2">No tickets yet. They'll appear here when created!</p>
                    </div>
                )}
            </div>
        </div>
    );
}
