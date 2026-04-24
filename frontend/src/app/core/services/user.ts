import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { map, Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../models/api-response.model';
import { UserModel } from '../models/user.model';

@Injectable({
  providedIn: 'root',
})
export class UserService {
  constructor(private readonly http: HttpClient) {}

  getUsers(): Observable<UserModel[]> {
    return this.http
      .get<ApiResponse<UserModel[]>>(`${environment.apiBaseUrl}/users`)
      .pipe(map((response) => response.data));
  }
}
