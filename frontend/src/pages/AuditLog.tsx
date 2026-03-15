import { useState, useEffect } from 'react';
import api from '../api/client';
import { ApiResponse, Page } from '../types';
import { Shield, Filter, ChevronLeft, ChevronRight } from 'lucide-react';

interface AuditEntry {
    id: string;
    entityType: string;
    entityId: string;
    action: string;
    performedBy: string;
    newValue?: string;
    timestamp: string;
}

const entityTypes = ['', 'TICKET', 'RESIDENT', 'PROPERTY', 'RENT', 'ONBOARDING', 'STAFF', 'LOGIN', 'CONFIG'];
const actions = ['', 'CREATED', 'UPDATED', 'DELETED', 'STATUS_CHANGED', 'ASSIGNED', 'APPROVED', 'REJECTED', 'PAID'];

const actionColors: Record<string, string> = {
    CREATED: 'text-emerald-400 bg-emerald-500/10',
    UPDATED: 'text-blue-400 bg-blue-500/10',
    DELETED: 'text-red-400 bg-red-500/10',
    STATUS_CHANGED: 'text-amber-400 bg-amber-500/10',
    ASSIGNED: 'text-violet-400 bg-violet-500/10',
    APPROVED: 'text-emerald-400 bg-emerald-500/10',
    REJECTED: 'text-rose-400 bg-rose-500/10',
    PAID: 'text-green-400 bg-green-500/10',
};

export default function AuditLog() {
    const [entries, setEntries] = useState<AuditEntry[]>([]);
    const [entityType, setEntityType] = useState('');
    const [action, setAction] = useState('');
    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);

    const fetch = async () => {
        try {
            const { data } = await api.get<ApiResponse<Page<AuditEntry>>>('/api/v1/audit', {
                params: { entityType: entityType || undefined, action: action || undefined, page, size: 30 }
            });
            setEntries(data.data.content);
            setTotalPages(data.data.totalPages);
        } catch { setEntries([]); }
    };

    useEffect(() => { fetch(); }, [entityType, action, page]);

    return (
        <div>
            <div className="flex items-center gap-3 mb-6">
                <Shield className="text-violet-400" size={24} />
                <h1 className="page-header !mb-0">Audit Log</h1>
            </div>

            {/* Filters */}
            <div className="flex gap-3 mb-6 flex-wrap items-center">
                <div className="flex items-center gap-2">
                    <Filter size={16} className="text-slate-400" />
                    <select className="input-field w-auto text-sm" value={entityType} onChange={e => { setEntityType(e.target.value); setPage(0); }}>
                        <option value="">All Entities</option>
                        {entityTypes.filter(Boolean).map(t => <option key={t} value={t}>{t}</option>)}
                    </select>
                </div>
                <select className="input-field w-auto text-sm" value={action} onChange={e => { setAction(e.target.value); setPage(0); }}>
                    <option value="">All Actions</option>
                    {actions.filter(Boolean).map(a => <option key={a} value={a}>{a}</option>)}
                </select>
            </div>

            {/* Table */}
            <div className="glass-card overflow-hidden">
                <table className="w-full text-sm">
                    <thead>
                        <tr className="border-b border-slate-700">
                            <th className="text-left p-4 text-slate-400 font-medium">Timestamp</th>
                            <th className="text-left p-4 text-slate-400 font-medium">Entity</th>
                            <th className="text-left p-4 text-slate-400 font-medium">Action</th>
                            <th className="text-left p-4 text-slate-400 font-medium">Entity ID</th>
                            <th className="text-left p-4 text-slate-400 font-medium">User</th>
                            <th className="text-left p-4 text-slate-400 font-medium">Details</th>
                        </tr>
                    </thead>
                    <tbody>
                        {entries.map(e => (
                            <tr key={e.id} className="border-b border-slate-800 hover:bg-slate-800/30 transition-colors">
                                <td className="p-4 text-slate-400 text-xs whitespace-nowrap">
                                    {new Date(e.timestamp).toLocaleString()}
                                </td>
                                <td className="p-4">
                                    <span className="text-xs px-2 py-1 rounded-full bg-white/5 text-slate-300 border border-white/10">
                                        {e.entityType}
                                    </span>
                                </td>
                                <td className="p-4">
                                    <span className={`text-xs px-2 py-1 rounded-full font-medium ${actionColors[e.action] || 'text-slate-400 bg-slate-500/10'}`}>
                                        {e.action}
                                    </span>
                                </td>
                                <td className="p-4 text-slate-300 font-mono text-xs">{e.entityId || '—'}</td>
                                <td className="p-4 text-slate-300">{e.performedBy || 'System'}</td>
                                <td className="p-4 text-slate-500 text-xs truncate max-w-[200px]">{e.newValue || '—'}</td>
                            </tr>
                        ))}
                    </tbody>
                </table>
                {entries.length === 0 && (
                    <div className="text-center text-slate-500 py-12">
                        <Shield size={32} className="mx-auto mb-2 opacity-40" />
                        <p>No audit entries found</p>
                    </div>
                )}
            </div>

            {/* Pagination */}
            {totalPages > 1 && (
                <div className="flex items-center justify-center gap-4 mt-4">
                    <button disabled={page === 0} onClick={() => setPage(p => p - 1)}
                        className="p-2 rounded-lg bg-slate-800 text-slate-300 hover:bg-slate-700 disabled:opacity-30 transition-all">
                        <ChevronLeft size={18} />
                    </button>
                    <span className="text-sm text-slate-400">Page {page + 1} of {totalPages}</span>
                    <button disabled={page >= totalPages - 1} onClick={() => setPage(p => p + 1)}
                        className="p-2 rounded-lg bg-slate-800 text-slate-300 hover:bg-slate-700 disabled:opacity-30 transition-all">
                        <ChevronRight size={18} />
                    </button>
                </div>
            )}
        </div>
    );
}
