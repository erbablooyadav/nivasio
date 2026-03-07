import { useState, useEffect } from 'react';
import api from '../api/client';
import { Plus, Search, Filter } from 'lucide-react';
import toast from 'react-hot-toast';
import './Tickets.css';

export default function Tickets() {
    const [tickets, setTickets] = useState([]);
    const [loading, setLoading] = useState(true);
    const [filter, setFilter] = useState({ status: '', department: '' });
    const [showCreate, setShowCreate] = useState(false);
    const [form, setForm] = useState({ type: 'HOUSEKEEPING', roomNo: '', description: '', priority: 'NORMAL' });

    useEffect(() => { loadTickets(); }, [filter]);

    const loadTickets = async () => {
        try {
            const params = {};
            if (filter.status) params.status = filter.status;
            if (filter.department) params.department = filter.department;
            const res = await api.getTickets(params);
            setTickets(res.data?.content || []);
        } catch (err) {
            console.error(err);
        } finally {
            setLoading(false);
        }
    };

    const createTicket = async (e) => {
        e.preventDefault();
        try {
            await api.createTicket(form);
            toast.success('Ticket created!');
            setShowCreate(false);
            setForm({ type: 'HOUSEKEEPING', roomNo: '', description: '', priority: 'NORMAL' });
            loadTickets();
        } catch (err) {
            toast.error(err.message);
        }
    };

    const updateStatus = async (ticketId, status) => {
        try {
            await api.updateTicketStatus(ticketId, status);
            toast.success('Status updated');
            loadTickets();
        } catch (err) {
            toast.error(err.message);
        }
    };

    return (
        <div className="page">
            <div className="page-header">
                <h1 className="page-title">Tickets</h1>
                <button className="btn btn-primary" onClick={() => setShowCreate(true)}>
                    <Plus size={16} /> New Ticket
                </button>
            </div>

            {/* Filters */}
            <div className="filters-bar">
                <select className="input-field" value={filter.status}
                    onChange={e => setFilter({ ...filter, status: e.target.value })}>
                    <option value="">All Status</option>
                    <option value="OPEN">Open</option>
                    <option value="IN_PROGRESS">In Progress</option>
                    <option value="DONE">Done</option>
                    <option value="CLOSED">Closed</option>
                </select>
                <select className="input-field" value={filter.department}
                    onChange={e => setFilter({ ...filter, department: e.target.value })}>
                    <option value="">All Departments</option>
                    <option value="HOUSEKEEPING">Housekeeping</option>
                    <option value="LAUNDRY">Laundry</option>
                    <option value="MAINTENANCE">Maintenance</option>
                    <option value="FOOD">Food</option>
                </select>
            </div>

            {/* Kanban Board */}
            <div className="kanban-board">
                {['OPEN', 'IN_PROGRESS', 'DONE', 'CLOSED'].map(status => (
                    <div className="kanban-column" key={status}>
                        <div className={`kanban-header kanban-${status.toLowerCase()}`}>
                            <span className={`badge badge-${status.toLowerCase()}`}>{status.replace('_', ' ')}</span>
                            <span className="kanban-count">
                                {tickets.filter(t => t.status === status).length}
                            </span>
                        </div>
                        <div className="kanban-cards">
                            {tickets.filter(t => t.status === status).map(ticket => (
                                <div className={`ticket-card ${ticket.slaBreach ? 'breach' : ''}`} key={ticket.id}>
                                    <div className="ticket-card-header">
                                        <span className="ticket-id">{ticket.ticketId}</span>
                                        {ticket.slaBreach && <span className="badge badge-breach">SLA ⚠️</span>}
                                    </div>
                                    <div className="ticket-dept">{ticket.department}</div>
                                    <div className="ticket-room">Room {ticket.roomNo}</div>
                                    {ticket.assignedToName && (
                                        <div className="ticket-assigned">👤 {ticket.assignedToName}</div>
                                    )}
                                    <div className="ticket-actions">
                                        {status === 'OPEN' && (
                                            <button className="btn btn-sm btn-secondary"
                                                onClick={() => updateStatus(ticket.ticketId, 'IN_PROGRESS')}>
                                                Start
                                            </button>
                                        )}
                                        {status === 'IN_PROGRESS' && (
                                            <button className="btn btn-sm btn-primary"
                                                onClick={() => updateStatus(ticket.ticketId, 'DONE')}>
                                                Complete
                                            </button>
                                        )}
                                        {status === 'DONE' && (
                                            <button className="btn btn-sm btn-ghost"
                                                onClick={() => updateStatus(ticket.ticketId, 'CLOSED')}>
                                                Close
                                            </button>
                                        )}
                                    </div>
                                </div>
                            ))}
                        </div>
                    </div>
                ))}
            </div>

            {/* Create Modal */}
            {showCreate && (
                <div className="modal-overlay" onClick={() => setShowCreate(false)}>
                    <div className="modal-content" onClick={e => e.stopPropagation()}>
                        <h2 className="modal-title">Create Ticket</h2>
                        <form onSubmit={createTicket} className="flex flex-col gap-4">
                            <div className="input-group">
                                <label>Service Type</label>
                                <select className="input-field" value={form.type}
                                    onChange={e => setForm({ ...form, type: e.target.value })}>
                                    <option value="HOUSEKEEPING">🧹 Housekeeping</option>
                                    <option value="LAUNDRY">👕 Laundry</option>
                                    <option value="MAINTENANCE">🔧 Maintenance</option>
                                    <option value="GENERAL">📝 General</option>
                                </select>
                            </div>
                            <div className="input-group">
                                <label>Room Number</label>
                                <input className="input-field" value={form.roomNo}
                                    onChange={e => setForm({ ...form, roomNo: e.target.value })}
                                    placeholder="e.g. 402" required />
                            </div>
                            <div className="input-group">
                                <label>Description</label>
                                <textarea className="input-field" rows={3} value={form.description}
                                    onChange={e => setForm({ ...form, description: e.target.value })}
                                    placeholder="Describe the issue..." />
                            </div>
                            <div className="input-group">
                                <label>Priority</label>
                                <select className="input-field" value={form.priority}
                                    onChange={e => setForm({ ...form, priority: e.target.value })}>
                                    <option value="NORMAL">Normal</option>
                                    <option value="URGENT">🔴 Urgent</option>
                                </select>
                            </div>
                            <div className="flex gap-3 mt-2">
                                <button type="submit" className="btn btn-primary">Create Ticket</button>
                                <button type="button" className="btn btn-secondary" onClick={() => setShowCreate(false)}>Cancel</button>
                            </div>
                        </form>
                    </div>
                </div>
            )}
        </div>
    );
}
