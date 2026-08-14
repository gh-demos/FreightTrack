import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';import { finalize } from 'rxjs/operators';
import { Shipment, Theme, Signal } from './models/shipment.model';
import { ShipmentService } from './services/shipment.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent implements OnInit {
  trackingNumber = '';
  shipments: Shipment[] = [];
  themes: Theme[] = [];
  totalSignals = 0;
  highImpactCount = 0;
  loading = false;
  errorMessage = '';

  constructor(private readonly shipmentService: ShipmentService) {}

  ngOnInit(): void {
    this.loadShipments();
  }

  loadShipments(): void {
    this.loading = true;
    this.errorMessage = '';
    this.shipmentService
      .getShipments()
      .pipe(finalize(() => (this.loading = false)))
      .subscribe({
        next: (data) => {
          this.shipments = data;
          this.organizeShipmentsIntoThemes();
        },
        error: () => {
          this.errorMessage = 'Unable to load shipments.';
        }
      });
  }

  findShipment(): void {
    const normalizedTrackingNumber = this.trackingNumber.trim();

    if (!normalizedTrackingNumber) {
      this.errorMessage = 'Tracking number is required.';
      return;
    }

    this.loading = true;
    this.errorMessage = '';

    this.shipmentService
      .getShipmentByTrackingNumber(normalizedTrackingNumber)
      .pipe(finalize(() => (this.loading = false)))
      .subscribe({
        next: (shipment) => {
          this.shipments = [shipment];
          this.organizeShipmentsIntoThemes();
        },
        error: () => {
          this.errorMessage = 'Shipment not found.';
        }
      });
  }

  private organizeShipmentsIntoThemes(): void {
    const themeMap: { [key: string]: Theme } = {
      'in-transit': {
        id: 'transit',
        name: 'Shipments in Transit',
        description: 'Packages currently on their way to destination.',
        signalCount: 0,
        expanded: true,
        signals: [],
        severity: 'medium'
      },
      'delivered': {
        id: 'delivered',
        name: 'Successfully Delivered',
        description: 'Shipments that have reached their destination.',
        signalCount: 0,
        expanded: false,
        signals: [],
        severity: 'low'
      },
      'delayed': {
        id: 'delayed',
        name: 'Delayed Shipments',
        description: 'Packages experiencing delays or exceptions.',
        signalCount: 0,
        expanded: false,
        signals: [],
        severity: 'high'
      },
      'other': {
        id: 'other',
        name: 'Other Status',
        description: 'Shipments with various statuses.',
        signalCount: 0,
        expanded: false,
        signals: [],
        severity: 'low'
      }
    };

    this.shipments.forEach(shipment => {
      const statusKey = this.getThemeKey(shipment.status);
      const signal: Signal = {
        id: shipment.id.toString(),
        label: `${shipment.trackingNumber} → ${shipment.origin} to ${shipment.destination}`,
        type: 'support-ticket'
      };
      themeMap[statusKey].signals.push(signal);
      themeMap[statusKey].signalCount++;
    });

    this.themes = Object.values(themeMap).filter(t => t.signalCount > 0 || t.expanded);
    this.totalSignals = this.shipments.length;
    this.highImpactCount = themeMap['delayed'].signalCount;
  }

  private getThemeKey(status?: string): string {
    const normalized = (status || '').toLowerCase();
    if (normalized.includes('transit')) return 'in-transit';
    if (normalized.includes('delivered')) return 'delivered';
    if (normalized.includes('delay')) return 'delayed';
    return 'other';
  }

  toggleTheme(theme: Theme): void {
    theme.expanded = !theme.expanded;
  }
}
