export interface Shipment {
  id: number;
  trackingNumber: string;
  origin: string;
  destination: string;
  status: string;
  pickupDate: string;
  expectedDeliveryDate: string;
  actualDeliveryDate?: string;
}

export interface Signal {
  id: string;
  label: string;
  type: 'user-interview' | 'teams-conversation' | 'customer-call' | 'support-ticket' | 'sales-note' | 'other';
}

export interface Theme {
  id: string;
  name: string;
  description: string;
  signalCount: number;
  expanded: boolean;
  signals: Signal[];
  severity: 'high' | 'medium' | 'low';
}
