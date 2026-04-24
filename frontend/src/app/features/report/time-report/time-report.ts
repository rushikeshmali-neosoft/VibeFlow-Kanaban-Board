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

  private loadReport(): void {
    this.reportService.getTimeReport().subscribe((report) => {
      this.report = report;
    });
  }
}
