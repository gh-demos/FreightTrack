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
