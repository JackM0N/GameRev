import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Game } from '../interfaces/game';
import { UserGame } from '../interfaces/userGame';

@Injectable({
  providedIn: 'root'
})
// Service for handling user's game list
export class UserGameService {
  private baseUrl = 'http://localhost:8080/library';

  constructor(
    private http: HttpClient,
  ) { }

  getUserGames(nickname: string, page: number, size: number, sortBy: string, sortDir: string): Observable<Game> {
    const params = new HttpParams()
      .set('page', (page - 1).toString())
      .set('size', size.toString())
      .set('sort', sortBy + ',' + sortDir);
    return this.http.get<Game>(`${this.baseUrl}/${nickname}`, {params});
  }

  updateUserGame(userReview: UserGame, token: string): Observable<UserGame> {
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`
    });
    return this.http.put<UserGame>(this.baseUrl, userReview, { headers: headers });
  }

  addUserGame(userReview: UserGame, token: string): Observable<UserGame> {
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`
    });
    return this.http.post<UserGame>(this.baseUrl, userReview, { headers: headers });
  }

  deleteUserGame(id: number, token: string): Observable<void> {
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`
    });
    return this.http.delete<void>(`${this.baseUrl}/${id}`, { headers: headers });
  }
}
