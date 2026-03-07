import { useState, useEffect } from 'react';
import api from '../api/client';
import { Plus, Trash2, DoorOpen } from 'lucide-react';
import toast from 'react-hot-toast';

export default function Rooms() {
    const [rooms, setRooms] = useState([]);
    const [loading, setLoading] = useState(true);
    const [showCreate, setShowCreate] = useState(false);
    const [form, setForm] = useState({ roomNo: '', floor: 1, capacity: 1, type: 'SINGLE' });

    useEffect(() => { loadRooms(); }, []);

    const loadRooms = async () => {
        try {
            const res = await api.getRooms();
            setRooms(res.data || []);
        } catch (err) { console.error(err); }
        finally { setLoading(false); }
    };

    const createRoom = async (e) => {
        e.preventDefault();
        try {
            await api.createRoom({ ...form, floor: parseInt(form.floor), capacity: parseInt(form.capacity) });
            toast.success('Room added!');
            setShowCreate(false);
            setForm({ roomNo: '', floor: 1, capacity: 1, type: 'SINGLE' });
            loadRooms();
        } catch (err) { toast.error(err.message); }
    };

    const deleteRoom = async (id) => {
        if (!confirm('Delete this room?')) return;
        try {
            await api.deleteRoom(id);
            toast.success('Room deleted');
            loadRooms();
        } catch (err) { toast.error(err.message); }
    };

    // Group rooms by floor
    const grouped = rooms.reduce((acc, room) => {
        const floor = room.floor || 0;
        if (!acc[floor]) acc[floor] = [];
        acc[floor].push(room);
        return acc;
    }, {});

    return (
        <div className="page">
            <div className="page-header">
                <h1 className="page-title">Rooms</h1>
                <button className="btn btn-primary" onClick={() => setShowCreate(true)}>
                    <Plus size={16} /> Add Room
                </button>
            </div>

            {Object.keys(grouped).sort((a, b) => a - b).map(floor => (
                <div key={floor} className="mb-4">
                    <h3 style={{ fontSize: 14, color: 'var(--text-secondary)', marginBottom: 12, fontWeight: 500 }}>
                        Floor {floor}
                    </h3>
                    <div className="rooms-grid">
                        {grouped[floor].map(room => (
                            <div className="card room-card" key={room.id}>
                                <div className="room-icon">
                                    <DoorOpen size={20} />
                                </div>
                                <div className="room-number">{room.roomNo}</div>
                                <div className="room-details">
                                    <span className="badge" style={{ background: 'rgba(108,99,255,0.15)', color: '#6c63ff' }}>
                                        {room.type || 'SINGLE'}
                                    </span>
                                    <span className="text-xs text-muted">Cap: {room.capacity}</span>
                                </div>
                                <button className="btn btn-ghost btn-sm" onClick={() => deleteRoom(room.id)}>
                                    <Trash2 size={13} />
                                </button>
                            </div>
                        ))}
                    </div>
                </div>
            ))}

            {rooms.length === 0 && !loading && (
                <div className="empty-state">
                    <DoorOpen size={48} />
                    <p className="mt-2">No rooms added yet. Click "Add Room" to get started!</p>
                </div>
            )}

            {showCreate && (
                <div className="modal-overlay" onClick={() => setShowCreate(false)}>
                    <div className="modal-content" onClick={e => e.stopPropagation()}>
                        <h2 className="modal-title">Add Room</h2>
                        <form onSubmit={createRoom} className="flex flex-col gap-4">
                            <div className="input-group">
                                <label>Room Number</label>
                                <input className="input-field" value={form.roomNo}
                                    onChange={e => setForm({ ...form, roomNo: e.target.value })}
                                    placeholder="e.g. 101, 402A" required />
                            </div>
                            <div className="input-group">
                                <label>Floor</label>
                                <input className="input-field" type="number" value={form.floor}
                                    onChange={e => setForm({ ...form, floor: e.target.value })} min="0" />
                            </div>
                            <div className="input-group">
                                <label>Capacity</label>
                                <input className="input-field" type="number" value={form.capacity}
                                    onChange={e => setForm({ ...form, capacity: e.target.value })} min="1" />
                            </div>
                            <div className="input-group">
                                <label>Room Type</label>
                                <select className="input-field" value={form.type}
                                    onChange={e => setForm({ ...form, type: e.target.value })}>
                                    <option value="SINGLE">Single</option>
                                    <option value="DOUBLE">Double</option>
                                    <option value="TRIPLE">Triple</option>
                                    <option value="DORMITORY">Dormitory</option>
                                </select>
                            </div>
                            <div className="flex gap-3 mt-2">
                                <button type="submit" className="btn btn-primary">Add Room</button>
                                <button type="button" className="btn btn-secondary" onClick={() => setShowCreate(false)}>Cancel</button>
                            </div>
                        </form>
                    </div>
                </div>
            )}

            <style>{`
        .rooms-grid {
          display: grid;
          grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
          gap: 12px;
        }
        .room-card {
          display: flex;
          flex-direction: column;
          align-items: center;
          text-align: center;
          padding: 16px;
          gap: 8px;
        }
        .room-icon {
          width: 40px; height: 40px;
          background: rgba(108, 99, 255, 0.1);
          border-radius: 10px;
          display: flex;
          align-items: center;
          justify-content: center;
          color: var(--accent-primary);
        }
        .room-number {
          font-size: 18px;
          font-weight: 700;
          color: var(--text-bright);
        }
        .room-details {
          display: flex;
          flex-direction: column;
          gap: 4px;
          align-items: center;
        }
      `}</style>
        </div>
    );
}
