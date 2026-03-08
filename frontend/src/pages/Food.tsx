import { useEffect, useState } from 'react';
import api from '../api/client';
import { Star, CheckCircle } from 'lucide-react';
import toast from 'react-hot-toast';
import type { FoodFeedback } from '../types';

const CATEGORY_COLORS: Record<string, string> = {
    QUALITY: 'bg-amber-500/20 text-amber-400',
    PORTION: 'bg-blue-500/20 text-blue-400',
    HYGIENE: 'bg-rose-500/20 text-rose-400',
    MENU_SUGGESTION: 'bg-emerald-500/20 text-emerald-400',
    SPECIAL_REQUEST: 'bg-violet-500/20 text-violet-400',
};

export default function FoodPage() {
    const [feedback, setFeedback] = useState<FoodFeedback[]>([]);

    useEffect(() => {
        api.get('/api/v1/food-feedback').then(r => setFeedback(r.data.data?.content || [])).catch(() => { });
    }, []);

    const resolve = async (id: string) => {
        try {
            await api.put(`/api/v1/food-feedback/${id}/resolve`);
            setFeedback(prev => prev.map(f => f.id === id ? { ...f, status: 'RESOLVED' } : f));
            toast.success('Feedback resolved');
        } catch { toast.error('Failed'); }
    };

    return (
        <div>
            <h1 className="page-header">Food Feedback</h1>

            <div className="space-y-3">
                {feedback.map(fb => (
                    <div key={fb.id} className="glass-card-hover p-5 flex items-center gap-4">
                        <div className="flex-1">
                            <div className="flex items-center gap-2 mb-1">
                                <span className="text-sm font-medium text-white">{fb.residentName}</span>
                                <span className="text-xs text-slate-500">Room {fb.roomNo}</span>
                                <span className={`badge text-[10px] ${CATEGORY_COLORS[fb.category] || 'bg-slate-500/20 text-slate-400'}`}>
                                    {fb.category}
                                </span>
                            </div>
                            <p className="text-sm text-slate-300">{fb.message}</p>
                            <div className="flex items-center gap-1 mt-2">
                                {[1, 2, 3, 4, 5].map(i => (
                                    <Star key={i} className={`w-3.5 h-3.5 ${i <= fb.rating ? 'text-amber-400 fill-amber-400' : 'text-slate-600'}`} />
                                ))}
                            </div>
                        </div>
                        <div className="flex items-center gap-2">
                            <span className={`badge text-[10px] ${fb.status === 'RESOLVED' ? 'bg-emerald-500/20 text-emerald-400' : 'bg-amber-500/20 text-amber-400'}`}>
                                {fb.status}
                            </span>
                            {fb.status !== 'RESOLVED' && (
                                <button onClick={() => resolve(fb.id)} className="p-2 text-emerald-400 hover:bg-emerald-500/10 rounded-lg transition-colors">
                                    <CheckCircle className="w-4 h-4" />
                                </button>
                            )}
                        </div>
                    </div>
                ))}
                {feedback.length === 0 && <div className="text-center text-slate-500 py-12">No food feedback yet</div>}
            </div>
        </div>
    );
}
