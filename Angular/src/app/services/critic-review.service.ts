import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { JwtHelperService } from '@auth0/angular-jwt';
import { CriticReview } from '../interfaces/criticReview';

@Injectable({
  providedIn: 'root'
})
// Service for handling critics reviews
export class CriticReviewService {
  private baseUrl = 'http://localhost:8080/critics-reviews';

  constructor(
    private http: HttpClient,
    public jwtHelper: JwtHelperService
  ) {}

  getCriticReviewsByGameTitle(gameTitle: string): Observable<CriticReview[]> {
    return this.http.get<CriticReview[]>(`${this.baseUrl}/${gameTitle}`);
  }
}
