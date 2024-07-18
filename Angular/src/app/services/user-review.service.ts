import { Inject, Injectable, PLATFORM_ID } from '@angular/core';
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
    public jwtHelper: JwtHelperService,
    @Inject(PLATFORM_ID) private platformId: any
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

  addUserReview(userReview: UserReview): Observable<UserReview> {
    return this.http.post<UserReview>(this.baseUrl, userReview);
  }

  editUserReview(userReview: UserReview): Observable<UserReview> {
    return this.http.put<UserReview>(this.baseUrl, userReview);
  }

  deleteUserReview(userReview: UserReview): Observable<UserReview> {
    const options = {
      body: userReview
    };
    return this.http.delete<UserReview>(this.baseUrl, options);
  }

  updateUserReviewLikeStatus(id: number, likeStatus: boolean | null): Observable<UserReview> {
    return this.http.put<UserReview>(`${this.likeStatusUrl}/${id}`, likeStatus);
  }
}