import { useState, useEffect } from 'react';
import api from '../api/client';
import { Onboarding, ApiResponse, Page } from '../types';
import { UserCheck, UserX, Clock, Eye } from 'lucide-react';
import toast from 'react-hot-toast';

export default function OnboardingPage() {
    const [list, setList] = useState<Onboarding[]>([]);
    const [filter, setFilter] = useState<string>('PENDING');

    const fetch = async () => {
        try {
            const { data } = await api.get<ApiResponse<Page<Onboarding>>>('/api/v1/onboarding', {
                params: { status: filter, size: 50 }
            });
            setList(data.data.content);
        } catch { setList([]); }
    };

    useEffect(() => { fetch(); }, [filter]);

    const approve = async (id: string) => {
        try {
            await api.put(`/api/v1/onboarding/${id}/approve`);
            toast.success('Approved & resident created');
            fetch();
        } catch { toast.error('Failed'); }
    };

    const reject = async (id: string) => {
        const reason = prompt('Rejection reason:');
        if (!reason) return;
        try {
            await api.put(`/api/v1/onboarding/${id}/reject`, { reason });
            toast.success('Rejected');
            fetch();
        } catch { toast.error('Failed'); }
    };

    const statusColor = (s: string) =>
        s === 'PENDING' ? 'badge-open' : s === 'APPROVED' ? 'badge-done' : 'badge-breach';

    return (
        <div>
            <h1 className="page-header">Onboarding Applications</h1>

            <div className="flex gap-2 mb-6">
                {['PENDING', 'APPROVED', 'REJECTED'].map(s => (
                    <button key={s} onClick={() => setFilter(s)}
                        className={`px-4 py-2 rounded-lg text-sm font-medium transition-all ${filter === s ? 'bg-violet-600 text-white' : 'bg-slate-800 text-slate-400 hover:text-white'}`}>
                        {s}
                    </button>
                ))}
            </div>

            <div className="space-y-3">
                {list.map(ob => (
                    <div key={ob.id} className="glass-card p-5">
                        <div className="flex items-center justify-between">
                            <div className="flex-1">
                                <div className="flex items-center gap-3 mb-2">
                                    <h3 className="text-white font-semibold text-lg">{ob.fullName}</h3>
                                    <span className={`badge ${statusColor(ob.status)}`}>{ob.status}</span>
                                </div>
                                <div className="grid grid-cols-2 md:grid-cols-4 gap-2 text-sm text-slate-400">
                                    <span>📱 {ob.phone}</span>
                                    <span>🏠 Room {ob.roomNo}</span>
                                    <span>🪪 {ob.idProofType || 'N/A'}</span>
                                    <span className="flex items-center gap-1">
                                        <Clock size={14} /> {new Date(ob.submittedAt).toLocaleDateString()}
                                    </span>
                                </div>
                            </div>
                            {ob.status === 'PENDING' && (
                                <div className="flex gap-2 ml-4">
                                    <button onClick={() => approve(ob.id)}
                                        className="p-2 rounded-lg bg-emerald-600/20 text-emerald-400 hover:bg-emerald-600/30 transition-all"
                                        title="Approve">
                                        <UserCheck size={20} />
                                    </button>
                                    <button onClick={() => reject(ob.id)}
                                        className="p-2 rounded-lg bg-red-600/20 text-red-400 hover:bg-red-600/30 transition-all"
                                        title="Reject">
                                        <UserX size={20} />
                                    </button>
                                </div>
                            )}
                        </div>
                        {ob.rejectionReason && (
                            <p className="mt-2 text-sm text-red-400">Reason: {ob.rejectionReason}</p>
                        )}
                    </div>
                ))}
                {list.length === 0 && (
                    <div className="text-center text-slate-500 py-12">
                        <Eye size={40} className="mx-auto mb-3 opacity-50" />
                        <p>No {filter.toLowerCase()} applications</p>
                    </div>
                )}
            </div>
        </div>
    );
}
