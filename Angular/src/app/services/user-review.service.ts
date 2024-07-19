import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { JwtHelperService } from '@auth0/angular-jwt';
import { UserReview } from '../interfaces/userReview';

@Injectable({
  providedIn: 'root'
})
// Service for handling user reviews
export class UserReviewService {
  private baseUrl = 'http://localhost:8080/users-reviews';
  private getByIdUrl = 'http://localhost:8080/users-reviews/id';
  private likeStatusUrl = 'http://localhost:8080/users-reviews/like-status';

  constructor(
    private http: HttpClient,
    public jwtHelper: JwtHelperService
  ) { }

  getUserReviews(): Observable<UserReview[]> {
    return this.http.get<UserReview[]>(this.baseUrl);
  }

  getUserReviewById(id: string): Observable<UserReview> {
    return this.http.get<UserReview>(`${this.getByIdUrl}/${id}`);
  }

  getUserReviewsForGame(name: string, token: string): Observable<UserReview[]> {
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`
    });
    return this.http.get<UserReview[]>(`${this.baseUrl}/${name}`, { headers });
  }

  addUserReview(userReview: UserReview, token: string): Observable<UserReview> {
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`
    });
    return this.http.post<UserReview>(this.baseUrl, userReview, { headers: headers });
  }

  editUserReview(userReview: UserReview, token: string): Observable<UserReview> {
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`
    });
    return this.http.put<UserReview>(this.baseUrl, userReview, { headers: headers });
  }

  deleteUserReview(userReview: UserReview, token: string): Observable<UserReview> {
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`
    });
    const options = {
      headers: headers,
      body: userReview
    };
    return this.http.delete<UserReview>(this.baseUrl, options);
  }

  updateUserReviewLikeStatus(id: number, likeStatus: boolean | null): Observable<UserReview> {
    return this.http.put<UserReview>(`${this.likeStatusUrl}/${id}`, likeStatus);
  }
}