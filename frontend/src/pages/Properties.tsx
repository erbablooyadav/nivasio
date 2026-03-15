import { useState, useEffect } from 'react';
import api from '../api/client';
import { Property, ApiResponse } from '../types';
import { Plus, Building2, MapPin, Layers, X, Edit, Power } from 'lucide-react';
import toast from 'react-hot-toast';

export default function Properties() {
    const [properties, setProperties] = useState<Property[]>([]);
    const [showModal, setShowModal] = useState(false);
    const [form, setForm] = useState({
        name: '', address: '', city: '', type: 'PG', totalRooms: 0, totalFloors: 0, amenities: ''
    });

    const fetch = async () => {
        try {
            const { data } = await api.get<ApiResponse<Property[]>>('/api/v1/properties');
            setProperties(data.data);
        } catch { setProperties([]); }
    };

    useEffect(() => { fetch(); }, []);

    const create = async () => {
        try {
            await api.post('/api/v1/properties', {
                ...form,
                totalRooms: Number(form.totalRooms),
                totalFloors: Number(form.totalFloors),
                amenities: form.amenities.split(',').map(a => a.trim()).filter(Boolean)
            });
            toast.success('Property created');
            setShowModal(false);
            setForm({ name: '', address: '', city: '', type: 'PG', totalRooms: 0, totalFloors: 0, amenities: '' });
            fetch();
        } catch { toast.error('Failed'); }
    };

    const deactivate = async (id: string) => {
        if (!confirm('Deactivate this property?')) return;
        try {
            await api.delete(`/api/v1/properties/${id}`);
            toast.success('Deactivated');
            fetch();
        } catch { toast.error('Failed'); }
    };

    const typeColors: Record<string, string> = {
        PG: 'from-violet-500/20 to-violet-600/20 border-violet-500/30',
        HOSTEL: 'from-blue-500/20 to-blue-600/20 border-blue-500/30',
        APARTMENT: 'from-emerald-500/20 to-emerald-600/20 border-emerald-500/30',
        CO_LIVING: 'from-amber-500/20 to-amber-600/20 border-amber-500/30',
        SOCIETY: 'from-rose-500/20 to-rose-600/20 border-rose-500/30',
    };

    return (
        <div>
            <div className="flex items-center justify-between mb-6">
                <h1 className="page-header !mb-0">Properties</h1>
                <button onClick={() => setShowModal(true)} className="btn-primary flex items-center gap-2">
                    <Plus size={18} /> Add Property
                </button>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5">
                {properties.map(p => (
                    <div key={p.id} className={`glass-card p-5 border bg-gradient-to-br ${typeColors[p.type] || typeColors.PG}`}>
                        <div className="flex items-start justify-between mb-3">
                            <div className="flex items-center gap-3">
                                <div className="w-11 h-11 bg-violet-600/30 rounded-xl flex items-center justify-center">
                                    <Building2 className="w-5 h-5 text-violet-300" />
                                </div>
                                <div>
                                    <h3 className="text-white font-semibold text-lg">{p.name}</h3>
                                    <span className="text-xs px-2 py-0.5 rounded-full bg-white/10 text-slate-300">{p.type}</span>
                                </div>
                            </div>
                            <span className={`w-3 h-3 rounded-full ${p.active ? 'bg-emerald-400' : 'bg-slate-600'}`} title={p.active ? 'Active' : 'Inactive'} />
                        </div>

                        <div className="space-y-2 text-sm text-slate-300 mb-4">
                            <div className="flex items-center gap-2"><MapPin size={14} className="text-slate-400" />{p.address}{p.city && `, ${p.city}`}</div>
                            <div className="flex items-center gap-2"><Layers size={14} className="text-slate-400" />{p.totalRooms} rooms · {p.totalFloors} floors</div>
                        </div>

                        {p.amenities?.length > 0 && (
                            <div className="flex flex-wrap gap-1 mb-3">
                                {p.amenities.slice(0, 4).map(a => (
                                    <span key={a} className="text-xs px-2 py-0.5 rounded-full bg-white/5 text-slate-400 border border-white/10">{a}</span>
                                ))}
                                {p.amenities.length > 4 && <span className="text-xs text-slate-500">+{p.amenities.length - 4} more</span>}
                            </div>
                        )}

                        <div className="flex gap-2 pt-2 border-t border-white/5">
                            {p.active && (
                                <button onClick={() => deactivate(p.id)} className="text-xs text-red-400 hover:text-red-300 flex items-center gap-1 transition-colors">
                                    <Power size={12} /> Deactivate
                                </button>
                            )}
                        </div>
                    </div>
                ))}
            </div>

            {properties.length === 0 && (
                <div className="text-center text-slate-500 py-16">
                    <Building2 size={48} className="mx-auto mb-3 opacity-50" />
                    <p className="text-lg">No properties yet</p>
                    <p className="text-sm mt-1">Add your first property to get started.</p>
                </div>
            )}

            {showModal && (
                <div className="fixed inset-0 bg-black/60 flex items-center justify-center z-50" onClick={() => setShowModal(false)}>
                    <div className="glass-card p-6 w-full max-w-lg" onClick={e => e.stopPropagation()}>
                        <div className="flex items-center justify-between mb-5">
                            <h2 className="text-white text-lg font-semibold">Add Property</h2>
                            <button onClick={() => setShowModal(false)} className="text-slate-400 hover:text-white"><X size={20} /></button>
                        </div>
                        <div className="space-y-3">
                            <input placeholder="Property Name *" className="input-field" value={form.name} onChange={e => setForm({ ...form, name: e.target.value })} />
                            <input placeholder="Address *" className="input-field" value={form.address} onChange={e => setForm({ ...form, address: e.target.value })} />
                            <input placeholder="City" className="input-field" value={form.city} onChange={e => setForm({ ...form, city: e.target.value })} />
                            <select className="input-field" value={form.type} onChange={e => setForm({ ...form, type: e.target.value })}>
                                <option value="PG">PG</option>
                                <option value="HOSTEL">Hostel</option>
                                <option value="APARTMENT">Apartment</option>
                                <option value="CO_LIVING">Co-Living</option>
                                <option value="SOCIETY">Society</option>
                            </select>
                            <div className="grid grid-cols-2 gap-3">
                                <input type="number" placeholder="Total Rooms" className="input-field" value={form.totalRooms || ''} onChange={e => setForm({ ...form, totalRooms: +e.target.value })} />
                                <input type="number" placeholder="Total Floors" className="input-field" value={form.totalFloors || ''} onChange={e => setForm({ ...form, totalFloors: +e.target.value })} />
                            </div>
                            <input placeholder="Amenities (comma-separated)" className="input-field" value={form.amenities} onChange={e => setForm({ ...form, amenities: e.target.value })} />
                            <button onClick={create} className="btn-primary w-full">Create Property</button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
}
