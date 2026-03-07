import { Settings as SettingsIcon, Printer, MessageCircle, Bell, Shield } from 'lucide-react';

export default function Settings() {
    return (
        <div className="page">
            <div className="page-header">
                <h1 className="page-title">Settings</h1>
            </div>

            <div className="settings-grid">
                <div className="card settings-section">
                    <div className="settings-section-header">
                        <MessageCircle size={20} />
                        <h3>WhatsApp Configuration</h3>
                    </div>
                    <div className="flex flex-col gap-4">
                        <div className="input-group">
                            <label>WhatsApp Business ID</label>
                            <input className="input-field" placeholder="Enter Business ID" />
                        </div>
                        <div className="input-group">
                            <label>Access Token</label>
                            <input className="input-field" type="password" placeholder="Enter Access Token" />
                        </div>
                        <div className="input-group">
                            <label>Phone Number ID</label>
                            <input className="input-field" placeholder="Enter Phone Number ID" />
                        </div>
                        <button className="btn btn-primary">Save WhatsApp Config</button>
                    </div>
                </div>

                <div className="card settings-section">
                    <div className="settings-section-header">
                        <Printer size={20} />
                        <h3>Thermal Printer</h3>
                    </div>
                    <div className="flex flex-col gap-4">
                        <div className="input-group">
                            <label>Printer IP Address</label>
                            <input className="input-field" placeholder="192.168.1.100" />
                        </div>
                        <div className="input-group">
                            <label>Port</label>
                            <input className="input-field" type="number" placeholder="9100" />
                        </div>
                        <div className="input-group">
                            <label>Connection Type</label>
                            <select className="input-field">
                                <option value="LAN">LAN</option>
                                <option value="WIFI">WiFi</option>
                                <option value="USB">USB</option>
                            </select>
                        </div>
                        <button className="btn btn-primary">Save Printer Config</button>
                    </div>
                </div>

                <div className="card settings-section">
                    <div className="settings-section-header">
                        <Bell size={20} />
                        <h3>SLA & Notifications</h3>
                    </div>
                    <div className="flex flex-col gap-4">
                        <div className="input-group">
                            <label>SLA Threshold (minutes)</label>
                            <input className="input-field" type="number" defaultValue={120} />
                        </div>
                        <div className="input-group">
                            <label>Send staff WhatsApp alerts</label>
                            <select className="input-field">
                                <option value="true">Enabled</option>
                                <option value="false">Disabled</option>
                            </select>
                        </div>
                        <button className="btn btn-primary">Save SLA Settings</button>
                    </div>
                </div>

                <div className="card settings-section">
                    <div className="settings-section-header">
                        <Shield size={20} />
                        <h3>Account</h3>
                    </div>
                    <div className="flex flex-col gap-4">
                        <div className="input-group">
                            <label>PG / Hostel Name</label>
                            <input className="input-field" placeholder="Your PG Name" />
                        </div>
                        <div className="input-group">
                            <label>Contact Email</label>
                            <input className="input-field" type="email" placeholder="admin@pg.com" />
                        </div>
                        <button className="btn btn-primary">Update Account</button>
                    </div>
                </div>
            </div>

            <style>{`
        .settings-grid {
          display: grid;
          grid-template-columns: repeat(auto-fit, minmax(340px, 1fr));
          gap: 16px;
        }
        .settings-section {
          padding: 24px;
        }
        .settings-section-header {
          display: flex;
          align-items: center;
          gap: 10px;
          margin-bottom: 20px;
          color: var(--accent-primary);
        }
        .settings-section-header h3 {
          font-size: 16px;
          font-weight: 600;
          color: var(--text-primary);
        }
      `}</style>
        </div>
    );
}
