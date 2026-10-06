import { afterNextRender, Component, computed, HostListener, inject, signal } from '@angular/core';
import { timer } from 'rxjs';
import { DeleteConfirmationModal } from '../../shared/delete-confirmation-modal/delete-confirmation-modal';
import { PopupSelectorBase } from '../../shared/selector/popup-selector-base';
import { SyncData } from '../model/sync-data.model';
import { SyncDataService } from '../service/sync-data.service';

@Component({
  imports: [DeleteConfirmationModal],
  selector: 'app-sync-buttons',
  styleUrl: './sync-buttons.css',
  templateUrl: './sync-buttons.html',
})
export class SyncButtons extends PopupSelectorBase {
  private readonly syncDataService: SyncDataService = inject(SyncDataService);

  protected override readonly alignPopupRight: boolean = true;

  private isPinned: boolean = false;
  private closeTimer: ReturnType<typeof setTimeout> | null = null;

  readonly syncData = signal<SyncData | null>(null);
  readonly isExportConfirmationOpen = signal<boolean>(false);

  readonly canImport = computed<boolean>(() => {
    const syncData: SyncData | null = this.syncData();
    if (syncData == null || syncData.remoteSnapshotTime == null) {
      return false;
    }
    if (syncData.localSnapshotTime == null) {
      return true;
    }
    return syncData.remoteSnapshotTime > syncData.localSnapshotTime;
  });

  readonly formattedLastImportTime = computed<string>(() =>
    this.formatSnapshotTime(this.syncData()?.remoteSnapshotTime ?? null),
  );
  readonly formattedLastExportTime = computed<string>(() =>
    this.formatSnapshotTime(this.syncData()?.localSnapshotTime ?? null),
  );

  readonly exportConfirmationName = computed<string>(() =>
    `the newer snapshot from ${this.formattedLastImportTime()}`,
  );

  private formatSnapshotTime(dateStr: string | null): string {
    if (dateStr == null) {
      return 'N/A';
    }
    const date: Date = new Date(dateStr);
    const now: Date = new Date();
    const timeStr: string = date.toLocaleTimeString('en-US', {
      hour: '2-digit',
      minute: '2-digit',
      hour12: false,
    });

    const isToday: boolean = this.isSameDay(date, now);
    const yesterday: Date = new Date(now);
    yesterday.setDate(yesterday.getDate() - 1);
    const isYesterday: boolean = this.isSameDay(date, yesterday);

    const dayLabel: string = isToday
      ? 'Today'
      : isYesterday
        ? 'Yesterday'
        : date.toLocaleDateString('en-US', { month: 'short', day: 'numeric' });

    return `${dayLabel}, ${timeStr}`;
  }

  private isSameDay(a: Date, b: Date): boolean {
    return (
      a.getFullYear() === b.getFullYear() &&
      a.getMonth() === b.getMonth() &&
      a.getDate() === b.getDate()
    );
  }

  constructor() {
    super();

    afterNextRender(() => timer(1000, 5000).subscribe(() => this.updateSyncData()));
  }

  @HostListener('mouseenter')
  onMouseEnter(): void {
    this.cancelClose();
    if (this.isOpen()) {
      return;
    }
    this.openPopup();
    this.isOpen.set(true);
  }

  @HostListener('mouseleave')
  onMouseLeave(): void {
    if (this.isPinned) {
      return;
    }
    this.cancelClose();
    this.closeTimer = setTimeout(() => this.closePopup(), 150);
  }

  onTriggerClick(): void {
    this.cancelClose();
    if (this.isOpen() && this.isPinned) {
      this.closePopup();
      return;
    }
    if (!this.isOpen()) {
      this.openPopup();
      this.isOpen.set(true);
    }
    this.isPinned = true;
  }

  @HostListener('document:click', ['$event'])
  onOutsideClick(event: MouseEvent): void {
    if (this.hostElement.nativeElement.contains(event.target)) {
      return;
    }
    this.isPinned = false;
    this.cancelClose();
  }

  private closePopup(): void {
    this.isOpen.set(false);
    this.isPinned = false;
  }

  private cancelClose(): void {
    if (this.closeTimer == null) {
      return;
    }
    clearTimeout(this.closeTimer);
    this.closeTimer = null;
  }

  triggerExport(): void {
    if (this.canImport()) {
      this.closePopup();
      this.isExportConfirmationOpen.set(true);
      return;
    }
    this.performExport();
  }

  handleExportConfirmation(confirmed: boolean): void {
    if (confirmed) {
      this.performExport();
    }
    this.closeExportConfirmation();
  }

  closeExportConfirmation(): void {
    this.isExportConfirmationOpen.set(false);
  }

  private performExport(): void {
    this.syncDataService.triggerExport().subscribe(() => {
      this.updateSyncData();
      this.closePopup();
    });
  }

  triggerImport(): void {
    this.syncDataService.triggerImport().subscribe(() => {
      this.updateSyncData();
      window.location.reload();
    });
  }

  private updateSyncData(): void {
    this.syncDataService.getSyncData().subscribe((syncData) => this.syncData.set(syncData));
  }
}
