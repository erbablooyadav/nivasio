import { useState, useEffect } from 'react';
import api from '../api/client';
import { UtensilsCrossed, CheckCircle } from 'lucide-react';
import toast from 'react-hot-toast';

export default function FoodFeedback() {
    const [feedback, setFeedback] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => { loadFeedback(); }, []);

    const loadFeedback = async () => {
        try {
            const res = await api.getFoodFeedback();
            setFeedback(res.data?.content || []);
        } catch (err) { console.error(err); }
        finally { setLoading(false); }
    };

    const resolve = async (id) => {
        try {
            await api.updateFoodFeedbackStatus(id, 'RESOLVED');
            toast.success('Feedback resolved');
            loadFeedback();
        } catch (err) { toast.error(err.message); }
    };

    const categoryColors = {
        COMPLAINT: '#ff6b6b',
        FEEDBACK: '#6c63ff',
        SPECIAL_REQUEST: '#ffb347',
    };

    return (
        <div className="page">
            <div className="page-header">
                <h1 className="page-title">Food Feedback</h1>
            </div>

            {feedback.length > 0 ? (
                <div className="table-container card">
                    <table>
                        <thead>
                            <tr>
                                <th>Category</th>
                                <th>Room</th>
                                <th>From</th>
                                <th>Message</th>
                                <th>Status</th>
                                <th>Date</th>
                                <th>Action</th>
                            </tr>
                        </thead>
                        <tbody>
                            {feedback.map(f => (
                                <tr key={f.id}>
                                    <td>
                                        <span className="badge" style={{
                                            background: `${categoryColors[f.category]}15`,
                                            color: categoryColors[f.category]
                                        }}>
                                            {f.category}
                                        </span>
                                    </td>
                                    <td>{f.roomNo}</td>
                                    <td className="text-sm">{f.userName || 'Tenant'}</td>
                                    <td className="text-sm truncate" style={{ maxWidth: 200 }}>{f.message}</td>
                                    <td><span className={`badge badge-${f.status?.toLowerCase()}`}>{f.status}</span></td>
                                    <td className="text-sm text-muted">
                                        {f.createdAt ? new Date(f.createdAt).toLocaleDateString() : '-'}
                                    </td>
                                    <td>
                                        {f.status !== 'RESOLVED' && (
                                            <button className="btn btn-sm btn-primary" onClick={() => resolve(f.id)}>
                                                <CheckCircle size={13} /> Resolve
                                            </button>
                                        )}
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>
            ) : (
                <div className="empty-state card">
                    <UtensilsCrossed size={48} />
                    <p className="mt-2">No food feedback yet.</p>
                </div>
            )}
        </div>
    );
}
