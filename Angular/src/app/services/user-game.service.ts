import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Game } from '../interfaces/game';

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
      .set('sortBy', sortBy)
      .set('sortDir', sortDir);
    return this.http.get<Game>(`${this.baseUrl}/${nickname}`, {params});
  }
}
