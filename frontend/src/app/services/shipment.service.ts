import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Shipment } from '../models/shipment.model';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class ShipmentService {
  private readonly baseUrl = `${environment.apiBaseUrl}/shipments`;

  constructor(private readonly http: HttpClient) {}

  getShipments(): Observable<Shipment[]> {
    return this.http.get<Shipment[]>(this.baseUrl);
  }

  getShipmentByTrackingNumber(trackingNumber: string): Observable<Shipment> {
    return this.http.get<Shipment>(`${this.baseUrl}/tracking/${encodeURIComponent(trackingNumber)}`);
  }
}
