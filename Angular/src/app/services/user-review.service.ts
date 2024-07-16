import { Inject, Injectable, PLATFORM_ID } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { JwtHelperService } from '@auth0/angular-jwt';
import { UserReview } from '../interfaces/userReview';

@Injectable({
  providedIn: 'root'
})
// Service for handling user reviews
export class UserReviewService {
  private baseUrl = 'http://localhost:8080/users-reviews';

  constructor(
    private http: HttpClient,
    public jwtHelper: JwtHelperService,
    @Inject(PLATFORM_ID) private platformId: any
  ) { }

  getUserReviews(): Observable<UserReview[]> {
    return this.http.get<UserReview[]>(this.baseUrl);
  }

  getUserReview(id: string): Observable<UserReview> {
    return this.http.get<UserReview>(`${this.baseUrl}/${id}`);
  }

  getUserReviewsForGame(name: string): Observable<UserReview[]> {
    return this.http.get<UserReview[]>(`${this.baseUrl}/${name}`);
  }

  addUserReview(userReview: UserReview): Observable<UserReview> {
    return this.http.post<UserReview>(this.baseUrl, userReview);
  }

  editUserReview(title: string, userReview: UserReview): Observable<UserReview> {
    return this.http.put<UserReview>(`${this.baseUrl}/${title}`, userReview);
  }

  deleteUserReview(id: number): Observable<UserReview> {
    return this.http.delete<UserReview>(`${this.baseUrl}/${id}`);
  }
}