import { useEffect, useState } from 'react';
import api from '../api/client';
import { Plus, Filter } from 'lucide-react';
import toast from 'react-hot-toast';
import clsx from 'clsx';
import type { Ticket } from '../types';

const COLUMNS = ['OPEN', 'ASSIGNED', 'IN_PROGRESS', 'DONE'] as const;
const COL_LABELS: Record<string, string> = { OPEN: 'Open', ASSIGNED: 'Assigned', IN_PROGRESS: 'In Progress', DONE: 'Done' };
const COL_COLORS: Record<string, string> = { OPEN: 'border-amber-500', ASSIGNED: 'border-blue-500', IN_PROGRESS: 'border-violet-500', DONE: 'border-emerald-500' };

export default function Tickets() {
    const [tickets, setTickets] = useState<Ticket[]>([]);
    const [showCreate, setShowCreate] = useState(false);
    const [filter, setFilter] = useState<string>('');
    const [form, setForm] = useState({ type: 'HOUSEKEEPING', roomNo: '', description: '', priority: 'NORMAL' });

    useEffect(() => {
        api.get('/api/v1/tickets', { params: { size: 100, department: filter || undefined } })
            .then(r => setTickets(r.data.data.content || []))
            .catch(() => toast.error('Failed to load tickets'));
    }, [filter]);

    const createTicket = async () => {
        try {
            const { data } = await api.post('/api/v1/tickets', form);
            if (data.success) {
                setTickets(prev => [data.data, ...prev]);
                setShowCreate(false);
                setForm({ type: 'HOUSEKEEPING', roomNo: '', description: '', priority: 'NORMAL' });
                toast.success('Ticket created!');
            }
        } catch { toast.error('Failed to create ticket'); }
    };

    const updateStatus = async (ticketId: string, status: string) => {
        try {
            const { data } = await api.put(`/api/v1/tickets/${ticketId}/status?status=${status}`);
            if (data.success) {
                setTickets(prev => prev.map(t => t.ticketId === ticketId ? data.data : t));
            }
        } catch { toast.error('Failed to update'); }
    };

    return (
        <div>
            <div className="flex items-center justify-between mb-6">
                <h1 className="page-header mb-0">Tickets</h1>
                <div className="flex gap-3">
                    <select className="input-field w-40 text-sm" value={filter} onChange={e => setFilter(e.target.value)}>
                        <option value="">All Departments</option>
                        {['HOUSEKEEPING', 'LAUNDRY', 'MAINTENANCE', 'FOOD', 'GENERAL'].map(d => (
                            <option key={d} value={d}>{d}</option>
                        ))}
                    </select>
                    <button onClick={() => setShowCreate(true)} className="btn-primary text-sm flex items-center gap-2">
                        <Plus className="w-4 h-4" /> New Ticket
                    </button>
                </div>
            </div>

            {/* Kanban Board */}
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
                {COLUMNS.map(col => {
                    const colTickets = tickets.filter(t => t.status === col);
                    return (
                        <div key={col} className="space-y-3">
                            <div className={`flex items-center gap-2 pb-3 border-b-2 ${COL_COLORS[col]}`}>
                                <span className="text-sm font-semibold text-white">{COL_LABELS[col]}</span>
                                <span className="badge bg-slate-700/50 text-slate-300">{colTickets.length}</span>
                            </div>
                            <div className="space-y-3 max-h-[calc(100vh-220px)] overflow-y-auto pr-1">
                                {colTickets.map(ticket => (
                                    <div key={ticket.id} className={clsx('glass-card-hover p-4 cursor-pointer', ticket.slaBreach && 'border-rose-500/50 bg-rose-500/5')}>
                                        <div className="flex items-center justify-between mb-2">
                                            <span className="text-xs font-mono text-violet-400">{ticket.ticketId}</span>
                                            {ticket.priority === 'URGENT' && <span className="badge-urgent text-[10px]">Urgent</span>}
                                            {ticket.slaBreach && <span className="badge-breach text-[10px]">SLA Breach</span>}
                                        </div>
                                        <p className="text-sm text-white font-medium">{ticket.type}</p>
                                        <p className="text-xs text-slate-400 mt-1">Room {ticket.roomNo}</p>
                                        {ticket.description && <p className="text-xs text-slate-500 mt-1 line-clamp-2">{ticket.description}</p>}
                                        {ticket.assignedToName && <p className="text-xs text-blue-400 mt-2">👤 {ticket.assignedToName}</p>}
                                        <div className="flex gap-1 mt-3">
                                            {col === 'OPEN' && <button onClick={() => updateStatus(ticket.ticketId, 'IN_PROGRESS')} className="text-[10px] px-2 py-1 bg-violet-500/20 text-violet-300 rounded-md hover:bg-violet-500/30">Start</button>}
                                            {col === 'ASSIGNED' && <button onClick={() => updateStatus(ticket.ticketId, 'IN_PROGRESS')} className="text-[10px] px-2 py-1 bg-violet-500/20 text-violet-300 rounded-md hover:bg-violet-500/30">Start</button>}
                                            {col === 'IN_PROGRESS' && <button onClick={() => updateStatus(ticket.ticketId, 'DONE')} className="text-[10px] px-2 py-1 bg-emerald-500/20 text-emerald-300 rounded-md hover:bg-emerald-500/30">Done</button>}
                                            {col === 'DONE' && <button onClick={() => updateStatus(ticket.ticketId, 'CLOSED')} className="text-[10px] px-2 py-1 bg-slate-500/20 text-slate-300 rounded-md hover:bg-slate-500/30">Close</button>}
                                        </div>
                                    </div>
                                ))}
                                {colTickets.length === 0 && <p className="text-xs text-slate-600 text-center py-8">No tickets</p>}
                            </div>
                        </div>
                    );
                })}
            </div>

            {/* Create Modal */}
            {showCreate && (
                <div className="fixed inset-0 bg-black/60 backdrop-blur-sm flex items-center justify-center z-50" onClick={() => setShowCreate(false)}>
                    <div className="glass-card p-6 w-full max-w-md" onClick={e => e.stopPropagation()}>
                        <h2 className="text-lg font-bold text-white mb-4">Create Ticket</h2>
                        <div className="space-y-3">
                            <select className="input-field" value={form.type} onChange={e => setForm({ ...form, type: e.target.value })}>
                                {['HOUSEKEEPING', 'LAUNDRY', 'MAINTENANCE', 'FOOD', 'GENERAL'].map(t => <option key={t} value={t}>{t}</option>)}
                            </select>
                            <input className="input-field" placeholder="Room No" value={form.roomNo} onChange={e => setForm({ ...form, roomNo: e.target.value })} />
                            <textarea className="input-field" rows={3} placeholder="Description" value={form.description} onChange={e => setForm({ ...form, description: e.target.value })} />
                            <select className="input-field" value={form.priority} onChange={e => setForm({ ...form, priority: e.target.value })}>
                                <option value="NORMAL">Normal</option>
                                <option value="URGENT">Urgent</option>
                            </select>
                            <div className="flex gap-3 pt-2">
                                <button onClick={() => setShowCreate(false)} className="btn-ghost flex-1">Cancel</button>
                                <button onClick={createTicket} className="btn-primary flex-1">Create</button>
                            </div>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
}
