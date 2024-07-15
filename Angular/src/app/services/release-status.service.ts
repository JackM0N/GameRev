import { Inject, Injectable, PLATFORM_ID } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { JwtHelperService } from '@auth0/angular-jwt';
import { ReleaseStatus } from '../interfaces/releaseStatus';

@Injectable({
  providedIn: 'root'
})
// Service for handling release statuses
export class ReleaseStatusService {
  private baseUrl = 'http://localhost:8080/release-statuses';

  constructor(
    private http: HttpClient,
    public jwtHelper: JwtHelperService
  ) { }

  getReleaseStatuses(): Observable<ReleaseStatus> {
    return this.http.get<ReleaseStatus>(this.baseUrl);
  }
}