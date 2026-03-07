import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { Building2, ArrowRight } from 'lucide-react';
import toast from 'react-hot-toast';
import './Login.css';

export default function Login() {
    const [isLogin, setIsLogin] = useState(true);
    const [loading, setLoading] = useState(false);
    const { login, register } = useAuth();
    const navigate = useNavigate();

    const [form, setForm] = useState({
        identifier: '', password: '',
        name: '', email: '', phone: '', pgName: '', pgAddress: '',
    });

    const handleChange = (e) => {
        setForm({ ...form, [e.target.name]: e.target.value });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);
        try {
            if (isLogin) {
                await login(form.identifier, form.password);
                toast.success('Welcome back!');
            } else {
                await register({
                    name: form.name,
                    email: form.email,
                    phone: form.phone,
                    password: form.password,
                    pgName: form.pgName,
                    pgAddress: form.pgAddress,
                });
                toast.success('Registration successful!');
            }
            navigate('/');
        } catch (err) {
            toast.error(err.message || 'Authentication failed');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="login-page">
            <div className="login-bg-effects">
                <div className="bg-circle c1"></div>
                <div className="bg-circle c2"></div>
                <div className="bg-circle c3"></div>
            </div>

            <div className="login-container">
                <div className="login-brand">
                    <div className="login-brand-icon">
                        <Building2 size={32} />
                    </div>
                    <h1>PG Manager <span>Pro</span></h1>
                    <p>WhatsApp-Powered Facility Management</p>
                </div>

                <div className="login-card">
                    <div className="login-tabs">
                        <button
                            className={`login-tab ${isLogin ? 'active' : ''}`}
                            onClick={() => setIsLogin(true)}
                        >
                            Sign In
                        </button>
                        <button
                            className={`login-tab ${!isLogin ? 'active' : ''}`}
                            onClick={() => setIsLogin(false)}
                        >
                            Register
                        </button>
                    </div>

                    <form onSubmit={handleSubmit} className="login-form">
                        {isLogin ? (
                            <>
                                <div className="input-group">
                                    <label>Email or Phone</label>
                                    <input
                                        className="input-field"
                                        name="identifier"
                                        value={form.identifier}
                                        onChange={handleChange}
                                        placeholder="admin@example.com"
                                        required
                                    />
                                </div>
                                <div className="input-group">
                                    <label>Password</label>
                                    <input
                                        className="input-field"
                                        type="password"
                                        name="password"
                                        value={form.password}
                                        onChange={handleChange}
                                        placeholder="Enter password"
                                        required
                                    />
                                </div>
                            </>
                        ) : (
                            <>
                                <div className="input-group">
                                    <label>Your Name</label>
                                    <input className="input-field" name="name" value={form.name}
                                        onChange={handleChange} placeholder="John Doe" required />
                                </div>
                                <div className="input-row">
                                    <div className="input-group">
                                        <label>Email</label>
                                        <input className="input-field" type="email" name="email" value={form.email}
                                            onChange={handleChange} placeholder="you@email.com" required />
                                    </div>
                                    <div className="input-group">
                                        <label>Phone</label>
                                        <input className="input-field" name="phone" value={form.phone}
                                            onChange={handleChange} placeholder="+91 9876543210" required />
                                    </div>
                                </div>
                                <div className="input-group">
                                    <label>PG / Hostel Name</label>
                                    <input className="input-field" name="pgName" value={form.pgName}
                                        onChange={handleChange} placeholder="Sunrise PG" required />
                                </div>
                                <div className="input-group">
                                    <label>Address</label>
                                    <input className="input-field" name="pgAddress" value={form.pgAddress}
                                        onChange={handleChange} placeholder="City, State" />
                                </div>
                                <div className="input-group">
                                    <label>Password</label>
                                    <input className="input-field" type="password" name="password" value={form.password}
                                        onChange={handleChange} placeholder="Min 6 characters" required />
                                </div>
                            </>
                        )}

                        <button className="btn btn-primary w-full login-btn" type="submit" disabled={loading}>
                            {loading ? 'Please wait...' : (isLogin ? 'Sign In' : 'Create Account')}
                            {!loading && <ArrowRight size={16} />}
                        </button>
                    </form>
                </div>

                <p className="login-footer">
                    Automate your PG management with WhatsApp • Thermal Printing • Live Dashboard
                </p>
            </div>
        </div>
    );
}
