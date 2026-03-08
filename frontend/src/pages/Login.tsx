import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuthStore } from '../store/auth';
import api from '../api/client';
import { Building2, ArrowRight, Phone } from 'lucide-react';
import toast from 'react-hot-toast';
import type { ApiResponse, AuthResponse } from '../types';

type Tab = 'login' | 'register';
type Step = 'phone' | 'otp';

export default function Login() {
    const [tab, setTab] = useState<Tab>('login');
    const [step, setStep] = useState<Step>('phone');
    const [phone, setPhone] = useState('');
    const [otp, setOtp] = useState('');
    const [name, setName] = useState('');
    const [pgName, setPgName] = useState('');
    const [loading, setLoading] = useState(false);
    const navigate = useNavigate();
    const { setAuth } = useAuthStore();

    const requestOtp = async () => {
        if (!/^[6-9]\d{9}$/.test(phone)) {
            toast.error('Enter a valid 10-digit Indian mobile number');
            return;
        }
        setLoading(true);
        try {
            if (tab === 'register') {
                if (!name || !pgName) {
                    toast.error('Fill in all fields');
                    setLoading(false);
                    return;
                }
            }
            await api.post<ApiResponse<string>>('/api/v1/auth/request-otp', { phone });
            toast.success('OTP sent! Check console (dev mode)');
            setStep('otp');
        } catch (err: any) {
            toast.error(err.response?.data?.message || 'Failed to send OTP');
        } finally {
            setLoading(false);
        }
    };

    const verifyOtp = async () => {
        setLoading(true);
        try {
            if (tab === 'register') {
                const { data } = await api.post<ApiResponse<AuthResponse>>('/api/v1/auth/register', {
                    phone, name, pgName, pgType: 'PG'
                });
                if (data.success) {
                    setAuth(data.data);
                    toast.success('Welcome to Nivasio!');
                    navigate('/');
                    return;
                }
            }

            const { data } = await api.post<ApiResponse<AuthResponse>>('/api/v1/auth/verify-otp', { phone, otp });
            if (data.success) {
                setAuth(data.data);
                toast.success('Welcome back!');
                navigate('/');
            }
        } catch (err: any) {
            toast.error(err.response?.data?.message || 'Verification failed');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="min-h-screen bg-slate-950 flex items-center justify-center relative overflow-hidden">
            {/* Animated background */}
            <div className="absolute inset-0">
                <div className="absolute top-1/4 left-1/4 w-96 h-96 bg-violet-600/10 rounded-full blur-3xl animate-pulse" />
                <div className="absolute bottom-1/4 right-1/4 w-96 h-96 bg-blue-600/10 rounded-full blur-3xl animate-pulse delay-1000" />
            </div>

            <div className="relative z-10 w-full max-w-md px-6">
                {/* Brand */}
                <div className="text-center mb-8">
                    <div className="w-16 h-16 bg-gradient-to-br from-violet-500 to-violet-700 rounded-2xl flex items-center justify-center mx-auto mb-4 shadow-lg shadow-violet-500/25">
                        <Building2 className="w-8 h-8 text-white" />
                    </div>
                    <h1 className="text-3xl font-bold text-white">
                        Nivasio
                    </h1>
                    <p className="text-slate-400 mt-1">Manage Any Residence. From Anywhere.</p>
                </div>

                <div className="glass-card p-8">
                    {/* Tabs */}
                    <div className="flex bg-slate-900/80 rounded-xl p-1 mb-6">
                        {(['login', 'register'] as Tab[]).map(t => (
                            <button key={t} onClick={() => { setTab(t); setStep('phone'); }}
                                className={`flex-1 py-2.5 rounded-lg text-sm font-semibold transition-all ${tab === t ? 'bg-violet-600 text-white shadow-lg' : 'text-slate-400 hover:text-white'
                                    }`}>
                                {t === 'login' ? 'Sign In' : 'Register'}
                            </button>
                        ))}
                    </div>

                    {step === 'phone' ? (
                        <div className="space-y-4">
                            {tab === 'register' && (
                                <>
                                    <div>
                                        <label className="block text-sm text-slate-400 mb-1.5">Your Name</label>
                                        <input className="input-field" placeholder="John Doe" value={name} onChange={e => setName(e.target.value)} />
                                    </div>
                                    <div>
                                        <label className="block text-sm text-slate-400 mb-1.5">Property Name</label>
                                        <input className="input-field" placeholder="Sunrise PG" value={pgName} onChange={e => setPgName(e.target.value)} />
                                    </div>
                                </>
                            )}
                            <div>
                                <label className="block text-sm text-slate-400 mb-1.5">Mobile Number</label>
                                <div className="relative">
                                    <Phone className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-500" />
                                    <input className="input-field pl-10" placeholder="9876543210" maxLength={10}
                                        value={phone} onChange={e => setPhone(e.target.value.replace(/\D/g, ''))} />
                                </div>
                            </div>
                            <button onClick={requestOtp} disabled={loading} className="btn-primary w-full flex items-center justify-center gap-2">
                                {loading ? 'Sending...' : 'Get OTP'} <ArrowRight className="w-4 h-4" />
                            </button>
                        </div>
                    ) : (
                        <div className="space-y-4">
                            <p className="text-sm text-slate-400">Enter the 6-digit OTP sent to <span className="text-violet-400 font-medium">{phone}</span></p>
                            <input className="input-field text-center text-2xl tracking-[0.5em] font-mono" placeholder="000000" maxLength={6}
                                value={otp} onChange={e => setOtp(e.target.value.replace(/\D/g, ''))} />
                            <button onClick={verifyOtp} disabled={loading} className="btn-primary w-full flex items-center justify-center gap-2">
                                {loading ? 'Verifying...' : 'Verify & Login'} <ArrowRight className="w-4 h-4" />
                            </button>
                            <button onClick={() => setStep('phone')} className="btn-ghost w-full text-sm">Change Number</button>
                        </div>
                    )}
                </div>

                <p className="text-center text-xs text-slate-500 mt-6">
                    Smart Residence Management • WhatsApp Bot • Live Dashboard
                </p>
            </div>
        </div>
    );
}
