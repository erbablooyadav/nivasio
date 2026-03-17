import { useState, useEffect } from 'react';
import api from '../api/client';
import { RentRecord, ApiResponse, Page } from '../types';
import { DollarSign, AlertTriangle, Check, Calendar } from 'lucide-react';
import toast from 'react-hot-toast';

export default function Rent() {
    const [records, setRecords] = useState<RentRecord[]>([]);
    const [filter, setFilter] = useState<string>('');
    const [month, setMonth] = useState(() => {
        const now = new Date();
        return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`;
    });

    const fetch = async () => {
        try {
            const { data } = await api.get<ApiResponse<Page<RentRecord>>>('/api/v1/rent', {
                params: { month, status: filter || undefined, size: 100 }
            });
            setRecords(data.data.content);
        } catch { setRecords([]); }
    };

    useEffect(() => { fetch(); }, [month, filter]);

    const generate = async () => {
        const amount = prompt('Default rent amount:', '5000');
        if (!amount) return;
        try {
            const { data } = await api.post('/api/v1/rent/generate', { month, amount: Number(amount) });
            toast.success(`Generated ${data.data.generated} records`);
            fetch();
        } catch { toast.error('Failed'); }
    };

    const markPaid = async (id: string) => {
        const mode = prompt('Payment mode (CASH/UPI/BANK):', 'UPI');
        if (!mode) return;
        try {
            await api.put(`/api/v1/rent/${id}/pay`, { paymentMode: mode });
            toast.success('Marked as paid');
            fetch();
        } catch { toast.error('Failed'); }
    };

    const statusBadge = (s: string) => {
        switch (s) {
            case 'PAID': return 'badge-done';
            case 'OVERDUE': return 'badge-breach';
            case 'PARTIAL': return 'badge-progress';
            default: return 'badge-open';
        }
    };

    const stats = {
        total: records.length,
        paid: records.filter(r => r.status === 'PAID').length,
        pending: records.filter(r => r.status === 'PENDING').length,
        overdue: records.filter(r => r.status === 'OVERDUE').length,
        revenue: records.filter(r => r.status === 'PAID').reduce((s, r) => s + r.amount, 0),
    };

    return (
        <div>
            <div className="flex items-center justify-between mb-6">
                <h1 className="page-header !mb-0">Rent Collection</h1>
                <button onClick={generate} className="btn-primary flex items-center gap-2">
                    <Calendar size={18} /> Generate Monthly
                </button>
            </div>

            {/* Stats */}
            <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mb-6">
                <div className="glass-card p-4 text-center">
                    <p className="text-2xl font-bold text-white">{stats.total}</p>
                    <p className="text-sm text-slate-400">Total Records</p>
                </div>
                <div className="glass-card p-4 text-center">
                    <p className="text-2xl font-bold text-emerald-400">{stats.paid}</p>
                    <p className="text-sm text-slate-400">Paid</p>
                </div>
                <div className="glass-card p-4 text-center">
                    <p className="text-2xl font-bold text-amber-400">{stats.pending}</p>
                    <p className="text-sm text-slate-400">Pending</p>
                </div>
                <div className="glass-card p-4 text-center">
                    <p className="text-2xl font-bold text-red-400">{stats.overdue}</p>
                    <p className="text-sm text-slate-400">Overdue</p>
                </div>
            </div>

            {/* Filters */}
            <div className="flex gap-3 mb-6 flex-wrap">
                <input type="month" value={month} onChange={e => setMonth(e.target.value)}
                    className="input-field w-auto" />
                {['', 'PENDING', 'PAID', 'OVERDUE'].map(s => (
                    <button key={s} onClick={() => setFilter(s)}
                        className={`px-4 py-2 rounded-lg text-sm font-medium transition-all ${filter === s ? 'bg-violet-600 text-white' : 'bg-slate-800 text-slate-400 hover:text-white'}`}>
                        {s || 'All'}
                    </button>
                ))}
            </div>

            {/* Table */}
            <div className="glass-card overflow-hidden">
                <table className="w-full text-sm">
                    <thead>
                        <tr className="border-b border-slate-700">
                            <th className="text-left p-4 text-slate-400 font-medium">Resident</th>
                            <th className="text-left p-4 text-slate-400 font-medium">Room</th>
                            <th className="text-right p-4 text-slate-400 font-medium">Amount</th>
                            <th className="text-center p-4 text-slate-400 font-medium">Status</th>
                            <th className="text-center p-4 text-slate-400 font-medium">Due Date</th>
                            <th className="text-center p-4 text-slate-400 font-medium">Action</th>
                        </tr>
                    </thead>
                    <tbody>
                        {records.map(r => (
                            <tr key={r.id} className="border-b border-slate-800 hover:bg-slate-800/30 transition-colors">
                                <td className="p-4 text-white font-medium">{r.residentName}</td>
                                <td className="p-4 text-slate-300">{r.roomNo}</td>
                                <td className="p-4 text-right text-white">₹{r.amount.toLocaleString()}</td>
                                <td className="p-4 text-center">
                                    <span className={`badge ${statusBadge(r.status)}`}>{r.status}</span>
                                </td>
                                <td className="p-4 text-center text-slate-400">
                                    {new Date(r.dueDate).toLocaleDateString()}
                                </td>
                                <td className="p-4 text-center">
                                    {(r.status === 'PENDING' || r.status === 'OVERDUE') && (
                                        <button onClick={() => markPaid(r.id)}
                                            className="text-emerald-400 hover:text-emerald-300 transition-colors flex items-center gap-1 mx-auto">
                                            <Check size={16} /> Pay
                                        </button>
                                    )}
                                    {r.status === 'PAID' && <span className="text-slate-500">✓ Paid</span>}
                                </td>
                            </tr>
                        ))}
                    </tbody>
                </table>
                {records.length === 0 && (
                    <div className="text-center text-slate-500 py-12">
                        <DollarSign size={40} className="mx-auto mb-3 opacity-50" />
                        <p>No rent records for this month</p>
                    </div>
                )}
            </div>
        </div>
    );
}
