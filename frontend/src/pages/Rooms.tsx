import { useEffect, useState } from 'react';
import api from '../api/client';
import { Plus, Trash2 } from 'lucide-react';
import toast from 'react-hot-toast';
import clsx from 'clsx';
import type { Room } from '../types';

const STATUS_COLORS: Record<string, string> = {
    VACANT: 'border-emerald-500/50 bg-emerald-500/5',
    OCCUPIED: 'border-blue-500/50 bg-blue-500/5',
    MAINTENANCE: 'border-amber-500/50 bg-amber-500/5',
};

export default function Rooms() {
    const [rooms, setRooms] = useState<Room[]>([]);
    const [showAdd, setShowAdd] = useState(false);
    const [form, setForm] = useState({ roomNo: '', floor: '1', capacity: '1', type: 'SINGLE' });

    useEffect(() => {
        api.get('/api/v1/rooms').then(r => setRooms(r.data.data || [])).catch(() => { });
    }, []);

    const addRoom = async () => {
        try {
            const { data } = await api.post('/api/v1/rooms', {
                roomNo: form.roomNo, floor: parseInt(form.floor), capacity: parseInt(form.capacity), type: form.type
            });
            if (data.success) {
                setRooms(prev => [...prev, data.data]);
                setShowAdd(false);
                setForm({ roomNo: '', floor: '1', capacity: '1', type: 'SINGLE' });
                toast.success('Room added');
            }
        } catch (err: any) { toast.error(err.response?.data?.message || 'Failed'); }
    };

    const deleteRoom = async (id: string) => {
        try {
            await api.delete(`/api/v1/rooms/${id}`);
            setRooms(prev => prev.filter(r => r.id !== id));
            toast.success('Room deleted');
        } catch { toast.error('Failed'); }
    };

    // Group by floor
    const floors = [...new Set(rooms.map(r => r.floor))].sort();

    return (
        <div>
            <div className="flex items-center justify-between mb-6">
                <h1 className="page-header mb-0">Rooms</h1>
                <button onClick={() => setShowAdd(true)} className="btn-primary text-sm flex items-center gap-2">
                    <Plus className="w-4 h-4" /> Add Room
                </button>
            </div>

            {/* Room stats */}
            <div className="flex gap-4 mb-6">
                {[
                    { label: 'Vacant', count: rooms.filter(r => r.status === 'VACANT').length, color: 'text-emerald-400' },
                    { label: 'Occupied', count: rooms.filter(r => r.status === 'OCCUPIED').length, color: 'text-blue-400' },
                    { label: 'Maintenance', count: rooms.filter(r => r.status === 'MAINTENANCE').length, color: 'text-amber-400' },
                ].map(s => (
                    <div key={s.label} className="glass-card px-4 py-2 flex items-center gap-2">
                        <span className={`text-lg font-bold ${s.color}`}>{s.count}</span>
                        <span className="text-xs text-slate-400">{s.label}</span>
                    </div>
                ))}
            </div>

            {floors.map(floor => (
                <div key={floor} className="mb-6">
                    <h3 className="text-sm font-semibold text-slate-400 mb-3 uppercase tracking-wider">Floor {floor}</h3>
                    <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-6 gap-3">
                        {rooms.filter(r => r.floor === floor).map(room => (
                            <div key={room.id} className={clsx('glass-card-hover p-4 text-center group relative', STATUS_COLORS[room.status])}>
                                <p className="text-lg font-bold text-white">{room.roomNo}</p>
                                <p className="text-xs text-slate-400">{room.type}</p>
                                <p className="text-xs text-slate-500">{room.occupied}/{room.capacity}</p>
                                <span className={clsx('badge mt-2 text-[10px]', room.status === 'VACANT' ? 'bg-emerald-500/20 text-emerald-400' : room.status === 'OCCUPIED' ? 'bg-blue-500/20 text-blue-400' : 'bg-amber-500/20 text-amber-400')}>
                                    {room.status}
                                </span>
                                <button onClick={() => deleteRoom(room.id)} className="absolute top-2 right-2 opacity-0 group-hover:opacity-100 p-1 text-slate-500 hover:text-rose-400 transition-all">
                                    <Trash2 className="w-3 h-3" />
                                </button>
                            </div>
                        ))}
                    </div>
                </div>
            ))}

            {rooms.length === 0 && <div className="text-center text-slate-500 py-12">No rooms configured yet</div>}

            {/* Add Modal */}
            {showAdd && (
                <div className="fixed inset-0 bg-black/60 backdrop-blur-sm flex items-center justify-center z-50" onClick={() => setShowAdd(false)}>
                    <div className="glass-card p-6 w-full max-w-md" onClick={e => e.stopPropagation()}>
                        <h2 className="text-lg font-bold text-white mb-4">Add Room</h2>
                        <div className="space-y-3">
                            <input className="input-field" placeholder="Room No (e.g. 301)" value={form.roomNo} onChange={e => setForm({ ...form, roomNo: e.target.value })} />
                            <input className="input-field" type="number" placeholder="Floor" value={form.floor} onChange={e => setForm({ ...form, floor: e.target.value })} />
                            <input className="input-field" type="number" placeholder="Capacity" value={form.capacity} onChange={e => setForm({ ...form, capacity: e.target.value })} />
                            <select className="input-field" value={form.type} onChange={e => setForm({ ...form, type: e.target.value })}>
                                {['SINGLE', 'DOUBLE', 'TRIPLE', 'DORMITORY'].map(t => <option key={t} value={t}>{t}</option>)}
                            </select>
                            <div className="flex gap-3 pt-2">
                                <button onClick={() => setShowAdd(false)} className="btn-ghost flex-1">Cancel</button>
                                <button onClick={addRoom} className="btn-primary flex-1">Add</button>
                            </div>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
}
