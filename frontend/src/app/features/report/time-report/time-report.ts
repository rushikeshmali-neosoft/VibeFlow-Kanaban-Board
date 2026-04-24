import { Component, DestroyRef, OnInit, inject } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { debounceTime } from 'rxjs';
import { TimeReportModel } from '../../../core/models/report.model';
import { ReportService } from '../../../core/services/report';
import { WebsocketService } from '../../../core/services/websocket';

@Component({
  selector: 'app-time-report',
  standalone: false,
  templateUrl: './time-report.html',
  styleUrl: './time-report.scss',
})
export class TimeReport implements OnInit {
  displayedColumns = ['title', 'status', 'assignee', 'totalHours'];
  report?: TimeReportModel;
  private readonly destroyRef = inject(DestroyRef);

  constructor(
    private readonly reportService: ReportService,
    private readonly websocketService: WebsocketService,
  ) {}

  ngOnInit(): void {
    this.loadReport();
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
    this.reportService.getTimeReport().subscribe((report) => {
      this.report = report;
    });
  }
}
