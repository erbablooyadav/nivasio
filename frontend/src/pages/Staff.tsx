import { useEffect, useState } from 'react';
import api from '../api/client';
import { Plus, UserX } from 'lucide-react';
import toast from 'react-hot-toast';
import type { Staff } from '../types';

const DEPT_COLORS: Record<string, string> = {
    HOUSEKEEPING: 'from-blue-500 to-blue-700',
    LAUNDRY: 'from-cyan-500 to-cyan-700',
    MAINTENANCE: 'from-amber-500 to-amber-700',
    FOOD: 'from-emerald-500 to-emerald-700',
    SECURITY: 'from-slate-500 to-slate-700',
    GENERAL: 'from-violet-500 to-violet-700',
};

export default function StaffPage() {
    const [staff, setStaff] = useState<Staff[]>([]);
    const [showAdd, setShowAdd] = useState(false);
    const [form, setForm] = useState({ name: '', phone: '', email: '', department: 'HOUSEKEEPING' });

    useEffect(() => {
        api.get('/api/v1/staff').then(r => setStaff(r.data.data || [])).catch(() => { });
    }, []);

    const addStaff = async () => {
        try {
            const { data } = await api.post('/api/v1/staff', form);
            if (data.success) {
                setStaff(prev => [...prev, data.data]);
                setShowAdd(false);
                setForm({ name: '', phone: '', email: '', department: 'HOUSEKEEPING' });
                toast.success('Staff added');
            }
        } catch (err: any) { toast.error(err.response?.data?.message || 'Failed'); }
    };

    const deactivate = async (id: string) => {
        try {
            await api.delete(`/api/v1/staff/${id}`);
            setStaff(prev => prev.filter(s => s.id !== id));
            toast.success('Staff deactivated');
        } catch { toast.error('Failed'); }
    };

    return (
        <div>
            <div className="flex items-center justify-between mb-6">
                <h1 className="page-header mb-0">Staff Management</h1>
                <button onClick={() => setShowAdd(true)} className="btn-primary text-sm flex items-center gap-2">
                    <Plus className="w-4 h-4" /> Add Staff
                </button>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                {staff.map(s => (
                    <div key={s.id} className="glass-card-hover p-5">
                        <div className="flex items-center gap-4">
                            <div className={`w-12 h-12 bg-gradient-to-br ${DEPT_COLORS[s.department] || 'from-violet-500 to-violet-700'} rounded-full flex items-center justify-center text-white font-bold text-sm shadow-lg`}>
                                {s.name.charAt(0)}
                            </div>
                            <div className="flex-1">
                                <p className="text-white font-semibold">{s.name}</p>
                                <p className="text-xs text-slate-400">{s.phone}</p>
                                <span className="badge bg-slate-700/50 text-slate-300 mt-1 inline-block">{s.department}</span>
                            </div>
                            <button onClick={() => deactivate(s.id)} className="p-2 text-slate-500 hover:text-rose-400 hover:bg-rose-500/10 rounded-lg transition-colors">
                                <UserX className="w-4 h-4" />
                            </button>
                        </div>
                    </div>
                ))}
                {staff.length === 0 && <div className="col-span-3 text-center text-slate-500 py-12">No staff members yet</div>}
            </div>

            {/* Add Modal */}
            {showAdd && (
                <div className="fixed inset-0 bg-black/60 backdrop-blur-sm flex items-center justify-center z-50" onClick={() => setShowAdd(false)}>
                    <div className="glass-card p-6 w-full max-w-md" onClick={e => e.stopPropagation()}>
                        <h2 className="text-lg font-bold text-white mb-4">Add Staff Member</h2>
                        <div className="space-y-3">
                            <input className="input-field" placeholder="Full Name" value={form.name} onChange={e => setForm({ ...form, name: e.target.value })} />
                            <input className="input-field" placeholder="Phone (10 digits)" maxLength={10} value={form.phone} onChange={e => setForm({ ...form, phone: e.target.value.replace(/\D/g, '') })} />
                            <input className="input-field" placeholder="Email (optional)" value={form.email} onChange={e => setForm({ ...form, email: e.target.value })} />
                            <select className="input-field" value={form.department} onChange={e => setForm({ ...form, department: e.target.value })}>
                                {['HOUSEKEEPING', 'LAUNDRY', 'MAINTENANCE', 'FOOD', 'SECURITY', 'GENERAL'].map(d => <option key={d} value={d}>{d}</option>)}
                            </select>
                            <div className="flex gap-3 pt-2">
                                <button onClick={() => setShowAdd(false)} className="btn-ghost flex-1">Cancel</button>
                                <button onClick={addStaff} className="btn-primary flex-1">Add</button>
                            </div>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
}
