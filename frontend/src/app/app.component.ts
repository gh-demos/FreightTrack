import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Shipment } from './models/shipment.model';
import { ShipmentService } from './services/shipment.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent {
  trackingNumber = '';
  shipments: Shipment[] = [];
  selectedShipment?: Shipment;
  loading = false;
  errorMessage = '';

  constructor(private readonly shipmentService: ShipmentService) {}

  loadShipments(): void {
    this.loading = true;
    this.errorMessage = '';
    this.shipmentService.getShipments().subscribe({
      next: (data) => {
        this.shipments = data;
        this.loading = false;
      },
      error: () => {
        this.errorMessage = 'Unable to load shipments.';
        this.loading = false;
      }
    });
  }

  findShipment(): void {
    if (!this.trackingNumber.trim()) {
      this.errorMessage = 'Tracking number is required.';
      return;
    }

    this.loading = true;
    this.errorMessage = '';
    this.selectedShipment = undefined;

    this.shipmentService.getShipmentByTrackingNumber(this.trackingNumber).subscribe({
      next: (shipment) => {
        this.selectedShipment = shipment;
        this.loading = false;
      },
      error: () => {
        this.errorMessage = 'Shipment not found.';
        this.loading = false;
      }
    });
  }
}
