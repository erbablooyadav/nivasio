export interface Ticket {
    id: string;
    tenantId: string;
    ticketId: string;
    type: string;
    department: string;
    status: 'OPEN' | 'ASSIGNED' | 'IN_PROGRESS' | 'DONE' | 'CLOSED' | 'REOPENED';
    priority: 'NORMAL' | 'URGENT';
    roomNo: string;
    residentName?: string;
    assignedTo?: string;
    assignedToName?: string;
    description?: string;
    photos?: string[];
    slaDeadline?: string;
    slaBreach: boolean;
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
    inProgressTickets: number;
    doneTickets: number;
    slaBreachTickets: number;
    todayTickets: number;
    totalRooms: number;
    totalStaff: number;
    totalResidents: number;
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
