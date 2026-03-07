import { useState, useEffect } from 'react';
import api from '../api/client';
import { Plus, UserMinus } from 'lucide-react';
import toast from 'react-hot-toast';

export default function Staff() {
    const [staff, setStaff] = useState([]);
    const [loading, setLoading] = useState(true);
    const [showCreate, setShowCreate] = useState(false);
    const [form, setForm] = useState({ name: '', phone: '', email: '', department: 'HOUSEKEEPING', password: '' });

    useEffect(() => { loadStaff(); }, []);

    const loadStaff = async () => {
        try {
            const res = await api.getStaff();
            setStaff(res.data || []);
        } catch (err) { console.error(err); }
        finally { setLoading(false); }
    };

    const createStaff = async (e) => {
        e.preventDefault();
        try {
            await api.createStaff(form);
            toast.success('Staff added!');
            setShowCreate(false);
            setForm({ name: '', phone: '', email: '', department: 'HOUSEKEEPING', password: '' });
            loadStaff();
        } catch (err) { toast.error(err.message); }
    };

    const deactivate = async (id) => {
        if (!confirm('Deactivate this staff member?')) return;
        try {
            await api.deleteStaff(id);
            toast.success('Staff deactivated');
            loadStaff();
        } catch (err) { toast.error(err.message); }
    };

    const deptColors = {
        HOUSEKEEPING: '#6c63ff',
        LAUNDRY: '#00d4aa',
        MAINTENANCE: '#ffb347',
        FOOD: '#ff6b6b',
    };

    return (
        <div className="page">
            <div className="page-header">
                <h1 className="page-title">Staff Management</h1>
                <button className="btn btn-primary" onClick={() => setShowCreate(true)}>
                    <Plus size={16} /> Add Staff
                </button>
            </div>

            <div className="staff-grid">
                {staff.map(s => (
                    <div className="card staff-card" key={s.id}>
                        <div className="staff-avatar" style={{ background: deptColors[s.department] || '#6c63ff' }}>
                            {s.name?.charAt(0)?.toUpperCase()}
                        </div>
                        <div className="staff-info">
                            <h3>{s.name}</h3>
                            <span className={`badge badge-${s.department?.toLowerCase()}`}
                                style={{ background: `${deptColors[s.department]}20`, color: deptColors[s.department] }}>
                                {s.department}
                            </span>
                            <p className="text-sm text-muted mt-2">📱 {s.phone}</p>
                            {s.email && <p className="text-sm text-muted">✉️ {s.email}</p>}
                        </div>
                        <button className="btn btn-ghost btn-sm" onClick={() => deactivate(s.id)} style={{ marginTop: 'auto' }}>
                            <UserMinus size={14} /> Remove
                        </button>
                    </div>
                ))}
            </div>

            {staff.length === 0 && !loading && (
                <div className="empty-state">
                    <p>No staff members yet. Click "Add Staff" to get started!</p>
                </div>
            )}

            {showCreate && (
                <div className="modal-overlay" onClick={() => setShowCreate(false)}>
                    <div className="modal-content" onClick={e => e.stopPropagation()}>
                        <h2 className="modal-title">Add Staff Member</h2>
                        <form onSubmit={createStaff} className="flex flex-col gap-4">
                            <div className="input-group">
                                <label>Name</label>
                                <input className="input-field" value={form.name}
                                    onChange={e => setForm({ ...form, name: e.target.value })}
                                    placeholder="Staff name" required />
                            </div>
                            <div className="input-group">
                                <label>Phone</label>
                                <input className="input-field" value={form.phone}
                                    onChange={e => setForm({ ...form, phone: e.target.value })}
                                    placeholder="+91 9876543210" required />
                            </div>
                            <div className="input-group">
                                <label>Email</label>
                                <input className="input-field" type="email" value={form.email}
                                    onChange={e => setForm({ ...form, email: e.target.value })}
                                    placeholder="staff@email.com" />
                            </div>
                            <div className="input-group">
                                <label>Department</label>
                                <select className="input-field" value={form.department}
                                    onChange={e => setForm({ ...form, department: e.target.value })}>
                                    <option value="HOUSEKEEPING">🧹 Housekeeping</option>
                                    <option value="LAUNDRY">👕 Laundry</option>
                                    <option value="MAINTENANCE">🔧 Maintenance</option>
                                    <option value="FOOD">🍽️ Food</option>
                                </select>
                            </div>
                            <div className="input-group">
                                <label>Password</label>
                                <input className="input-field" type="password" value={form.password}
                                    onChange={e => setForm({ ...form, password: e.target.value })}
                                    placeholder="Default: staff123" />
                            </div>
                            <div className="flex gap-3 mt-2">
                                <button type="submit" className="btn btn-primary">Add Staff</button>
                                <button type="button" className="btn btn-secondary" onClick={() => setShowCreate(false)}>Cancel</button>
                            </div>
                        </form>
                    </div>
                </div>
            )}

            <style>{`
        .staff-grid {
          display: grid;
          grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
          gap: 16px;
        }
        .staff-card {
          display: flex;
          flex-direction: column;
          align-items: center;
          text-align: center;
          padding: 24px;
        }
        .staff-avatar {
          width: 56px; height: 56px;
          border-radius: 50%;
          display: flex;
          align-items: center;
          justify-content: center;
          font-size: 20px;
          font-weight: 600;
          color: white;
          margin-bottom: 12px;
        }
        .staff-info h3 {
          font-size: 15px;
          font-weight: 600;
          margin-bottom: 8px;
        }
      `}</style>
        </div>
    );
}
