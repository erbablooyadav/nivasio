import { useState, useEffect } from 'react';
import api from '../api/client';
import { Resident, ApiResponse, Page } from '../types';
import { Search, Plus, UserMinus, Phone, Mail, X } from 'lucide-react';
import toast from 'react-hot-toast';

export default function Residents() {
    const [residents, setResidents] = useState<Resident[]>([]);
    const [search, setSearch] = useState('');
    const [showModal, setShowModal] = useState(false);
    const [form, setForm] = useState({ name: '', phone: '', email: '', roomNo: '', emergencyContact: '' });

    const fetchResidents = async () => {
        try {
            const { data } = await api.get<ApiResponse<Page<Resident>>>('/api/v1/residents', {
                params: { search: search || undefined, size: 50 }
            });
            setResidents(data.data.content);
        } catch { setResidents([]); }
    };

    useEffect(() => { fetchResidents(); }, [search]);

    const addResident = async () => {
        try {
            await api.post('/api/v1/residents', form);
            toast.success('Resident added');
            setShowModal(false);
            setForm({ name: '', phone: '', email: '', roomNo: '', emergencyContact: '' });
            fetchResidents();
        } catch { toast.error('Failed to add resident'); }
    };

    const moveOut = async (id: string) => {
        if (!confirm('Move out this resident?')) return;
        try {
            await api.post(`/api/v1/residents/${id}/move-out`);
            toast.success('Resident moved out');
            fetchResidents();
        } catch { toast.error('Failed'); }
    };

    return (
        <div>
            <div className="flex items-center justify-between mb-6">
                <h1 className="page-header !mb-0">Residents</h1>
                <button onClick={() => setShowModal(true)} className="btn-primary flex items-center gap-2">
                    <Plus size={18} /> Add Resident
                </button>
            </div>

            <div className="relative mb-6">
                <Search className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" size={18} />
                <input value={search} onChange={e => setSearch(e.target.value)}
                    placeholder="Search by name..." className="input-field pl-10" />
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                {residents.map(r => (
                    <div key={r.id} className="glass-card p-4">
                        <div className="flex items-start justify-between mb-3">
                            <div>
                                <h3 className="text-white font-semibold">{r.name}</h3>
                                <p className="text-slate-400 text-sm">Room {r.roomNo}</p>
                            </div>
                            <span className={`badge ${r.active ? 'badge-done' : 'badge-breach'}`}>
                                {r.active ? 'Active' : 'Moved Out'}
                            </span>
                        </div>
                        <div className="space-y-1 text-sm text-slate-300">
                            <div className="flex items-center gap-2"><Phone size={14} />{r.phone}</div>
                            {r.email && <div className="flex items-center gap-2"><Mail size={14} />{r.email}</div>}
                            <p className="text-slate-500 text-xs mt-2">Since {new Date(r.moveInDate).toLocaleDateString()}</p>
                        </div>
                        {r.active && (
                            <button onClick={() => moveOut(r.id)} className="mt-3 text-sm text-red-400 hover:text-red-300 flex items-center gap-1">
                                <UserMinus size={14} /> Move Out
                            </button>
                        )}
                    </div>
                ))}
            </div>

            {showModal && (
                <div className="fixed inset-0 bg-black/60 flex items-center justify-center z-50" onClick={() => setShowModal(false)}>
                    <div className="glass-card p-6 w-full max-w-md" onClick={e => e.stopPropagation()}>
                        <div className="flex items-center justify-between mb-4">
                            <h2 className="text-white text-lg font-semibold">Add Resident</h2>
                            <button onClick={() => setShowModal(false)} className="text-slate-400 hover:text-white"><X size={20} /></button>
                        </div>
                        <div className="space-y-3">
                            <input placeholder="Full Name *" className="input-field" value={form.name} onChange={e => setForm({ ...form, name: e.target.value })} />
                            <input placeholder="Phone *" className="input-field" value={form.phone} onChange={e => setForm({ ...form, phone: e.target.value })} />
                            <input placeholder="Email" className="input-field" value={form.email} onChange={e => setForm({ ...form, email: e.target.value })} />
                            <input placeholder="Room No *" className="input-field" value={form.roomNo} onChange={e => setForm({ ...form, roomNo: e.target.value })} />
                            <input placeholder="Emergency Contact" className="input-field" value={form.emergencyContact} onChange={e => setForm({ ...form, emergencyContact: e.target.value })} />
                            <button onClick={addResident} className="btn-primary w-full">Add Resident</button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
}
