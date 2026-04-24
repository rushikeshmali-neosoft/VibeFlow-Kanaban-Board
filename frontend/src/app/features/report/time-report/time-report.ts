import { ChangeDetectionStrategy, ChangeDetectorRef, Component, DestroyRef, OnInit, inject } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { debounceTime } from 'rxjs';
import { TimeReportModel } from '../../../core/models/report.model';
import { ReportService } from '../../../core/services/report';
import { WebsocketService } from '../../../core/services/websocket';

@Component({
  selector: 'app-time-report',
  standalone: false,
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './time-report.html',
  styleUrl: './time-report.scss',
})
export class TimeReport implements OnInit {
  displayedColumns = ['title', 'status', 'assignee', 'totalHours'];
  report?: TimeReportModel;
  /** True only while the initial API call is in-flight */
  isLoading = true;

  private readonly destroyRef = inject(DestroyRef);

  constructor(
    private readonly reportService: ReportService,
    private readonly websocketService: WebsocketService,
    private readonly cdr: ChangeDetectorRef,
  ) {}

  /** Returns a CSS-safe class suffix, e.g. IN_PROGRESS → in-progress */
  getStatusClass(status: string): string {
    return (status || '').toLowerCase().replace(/_/g, '-');
  }

  /** Returns a human-readable label, e.g. IN_PROGRESS → In Progress */
  getStatusLabel(status: string): string {
    return (status || '')
      .toLowerCase()
      .replace(/_/g, ' ')
      .replace(/\b\w/g, (c) => c.toUpperCase());
  }

  ngOnInit(): void {
    this.loadReport();

    // Re-fetch whenever a WebSocket event fires (debounced to avoid spam)
    this.websocketService.events$
      .pipe(debounceTime(200), takeUntilDestroyed(this.destroyRef))
      .subscribe(() => this.loadReport());
  }

  exportCsv(): void {
    if (!this.report) return;
    const rows = [
      ['Task Title', 'Status', 'Assignee', 'Total Hours'],
      ...this.report.tasks.map((t) => [t.title, t.status, t.assignee || 'Unassigned', t.totalHours]),
      ['Grand Total', '', '', this.report.grandTotal],
    ];
    const csv = rows.map((r) => r.map(String).join(',')).join('\n');
    const blob = new Blob([csv], { type: 'text/csv' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = 'time-report.csv';
    a.click();
    URL.revokeObjectURL(url);
  }

  private loadReport(): void {
    this.isLoading = true;
    this.cdr.markForCheck();

    this.reportService.getTimeReport().subscribe({
      next: (report) => {
        this.report = report;
        this.isLoading = false;
        // markForCheck() is required because OnPush won't detect
        // changes that arrive via HTTP callbacks outside Angular's zone
        this.cdr.markForCheck();
      },
      error: () => {
        this.isLoading = false;
        this.cdr.markForCheck();
      },
    });
  }
}
