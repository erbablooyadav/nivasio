import { Wifi, Printer, Clock, Shield } from 'lucide-react';

export default function Settings() {
    const sections = [
        {
            title: 'WhatsApp Integration',
            icon: Wifi,
            items: [
                { label: 'Business ID', value: 'Connected (Mock Mode)', type: 'text' },
                { label: 'Phone Number ID', value: '•••••••', type: 'text' },
                { label: 'Webhook Status', value: 'Active', type: 'badge' },
            ],
        },
        {
            title: 'Thermal Printer',
            icon: Printer,
            items: [
                { label: 'Connection', value: 'Not Configured', type: 'text' },
                { label: 'Auto-Print Tickets', value: 'Disabled', type: 'toggle' },
            ],
        },
        {
            title: 'SLA Configuration',
            icon: Clock,
            items: [
                { label: 'Housekeeping', value: '2 hours', type: 'text' },
                { label: 'Laundry', value: '1 hour', type: 'text' },
                { label: 'Maintenance', value: '4 hours', type: 'text' },
                { label: 'Food Feedback', value: '4 hours', type: 'text' },
                { label: 'General', value: '8 hours', type: 'text' },
            ],
        },
        {
            title: 'Security',
            icon: Shield,
            items: [
                { label: 'JWT Expiry', value: '15 minutes', type: 'text' },
                { label: 'Token Algorithm', value: 'HS512', type: 'text' },
                { label: 'Refresh Token', value: 'HttpOnly Cookie + Rotation', type: 'text' },
                { label: 'Tenant Isolation', value: 'AOP Enforced', type: 'badge' },
            ],
        },
    ];

    return (
        <div>
            <h1 className="page-header">Settings</h1>

            <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
                {sections.map(section => (
                    <div key={section.title} className="glass-card p-6">
                        <div className="flex items-center gap-3 mb-5">
                            <div className="w-10 h-10 bg-gradient-to-br from-violet-500 to-violet-700 rounded-xl flex items-center justify-center shadow-lg">
                                <section.icon className="w-5 h-5 text-white" />
                            </div>
                            <h3 className="text-lg font-semibold text-white">{section.title}</h3>
                        </div>
                        <div className="space-y-3">
                            {section.items.map(item => (
                                <div key={item.label} className="flex items-center justify-between py-2 border-b border-slate-700/30 last:border-0">
                                    <span className="text-sm text-slate-400">{item.label}</span>
                                    {item.type === 'badge' ? (
                                        <span className="badge bg-emerald-500/20 text-emerald-400 text-[10px]">{item.value}</span>
                                    ) : (
                                        <span className="text-sm text-white font-medium">{item.value}</span>
                                    )}
                                </div>
                            ))}
                        </div>
                    </div>
                ))}
            </div>
        </div>
    );
}
