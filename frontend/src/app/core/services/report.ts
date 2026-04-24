import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { map, Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../models/api-response.model';
import { TimeReportModel } from '../models/report.model';

@Injectable({
  providedIn: 'root',
})
export class ReportService {
  constructor(private readonly http: HttpClient) {}

  getTimeReport(): Observable<TimeReportModel> {
    return this.http
      .get<ApiResponse<TimeReportModel>>(`${environment.apiBaseUrl}/reports/time`)
      .pipe(map((response) => response.data));
  }
}
