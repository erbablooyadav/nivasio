export interface Ticket {
    id: string;
    tenantId: string;
    ticketId: string;
    type: string;
    department: string;
    status: 'OPEN' | 'ASSIGNED' | 'IN_PROGRESS' | 'DONE' | 'CLOSED' | 'REOPENED';
    priority: 'NORMAL' | 'URGENT';
    roomNo: string;
    propertyId?: string;
    residentName?: string;
    assignedTo?: string;
    assignedToName?: string;
    description?: string;
    photos?: string[];
    slaDeadline?: string;
    slaBreach: boolean;
    recurringFlag?: boolean;
    createdAt: string;
    completedAt?: string;
}

export interface Staff {
    id: string;
    tenantId: string;
    name: string;
    phone: string;
    email?: string;
    department: string;
    active: boolean;
    createdAt: string;
}

export interface Room {
    id: string;
    tenantId: string;
    roomNo: string;
    floor: number;
    type: string;
    capacity: number;
    occupied: number;
    status: 'VACANT' | 'OCCUPIED' | 'MAINTENANCE';
}

export interface FoodFeedback {
    id: string;
    residentName: string;
    roomNo: string;
    category: string;
    message: string;
    rating: number;
    status: string;
    createdAt: string;
}

export interface DashboardStats {
    totalTickets: number;
    openTickets: number;
    assignedTickets: number;
    inProgressTickets: number;
    doneTickets: number;
    slaBreachTickets: number;
    todayTickets: number;
    totalRooms: number;
    totalStaff: number;
    totalResidents: number;
    slaBreachRate: number;
    staffWorkload: Record<string, number>;
    weeklyTrend: { date: string; count: number }[];
    departmentBreakdown: Record<string, number>;
}

export interface Property {
    id: string;
    tenantId: string;
    name: string;
    address: string;
    city: string;
    type: string;
    totalRooms: number;
    totalFloors: number;
    amenities: string[];
    active: boolean;
    createdAt: string;
}

export interface Resident {
    id: string;
    tenantId: string;
    propertyId?: string;
    roomNo: string;
    name: string;
    phone: string;
    email?: string;
    emergencyContact?: string;
    languagePreference?: string;
    active: boolean;
    status: string;
    moveInDate: string;
    moveOutDate?: string;
}

export interface Onboarding {
    id: string;
    tenantId: string;
    fullName: string;
    phone: string;
    email?: string;
    roomNo: string;
    idProofType: string;
    idProofUrl?: string;
    status: 'PENDING' | 'APPROVED' | 'REJECTED';
    rejectionReason?: string;
    submittedAt: string;
    reviewedAt?: string;
}

export interface RentRecord {
    id: string;
    tenantId: string;
    residentId: string;
    residentName: string;
    roomNo: string;
    amount: number;
    month: string;
    dueDate: string;
    paidDate?: string;
    status: 'PENDING' | 'PAID' | 'OVERDUE' | 'PARTIAL';
    paymentMode?: string;
    transactionId?: string;
    reminderSent: boolean;
}

export interface Notification {
    id: string;
    type: string;
    title: string;
    message: string;
    referenceId?: string;
    read: boolean;
    createdAt: string;
}

export interface AuthResponse {
    accessToken: string;
    userId: string;
    tenantId: string;
    name: string;
    role: string;
    propertyId?: string;
}

export interface ApiResponse<T> {
    success: boolean;
    data: T;
    message?: string;
    errorCode?: string;
}

export interface Page<T> {
    content: T[];
    totalPages: number;
    totalElements: number;
    number: number;
}
