import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Game } from '../interfaces/game';

@Injectable({
  providedIn: 'root'
})
// Service for handling games
export class GameService {
  private baseUrl = 'http://localhost:8080/games';

  constructor(
    private http: HttpClient,
  ) { }

  getGames(page?: number, size?: number, sortBy?: string, sortDir?: string): Observable<Game> {
    var params;
    if (page && size && sortBy && sortDir) {
      params = new HttpParams()
        .set('page', (page - 1).toString())
        .set('size', size.toString())
        .set('sort', sortBy + ',' + sortDir);
    } else {
      params = new HttpParams();
    }
    return this.http.get<Game>(this.baseUrl, { params });
  }

  getGameByName(name: string): Observable<Game> {
    return this.http.get<Game>(`${this.baseUrl}/${name}`);
  }

  addGame(game: Game): Observable<Game> {
    return this.http.post<Game>(this.baseUrl, game);
  }

  editGame(title: string, game: Game): Observable<Game> {
    return this.http.put<Game>(`${this.baseUrl}/${title}`, game);
  }

  deleteGame(id: number): Observable<Game> {
    return this.http.delete<Game>(`${this.baseUrl}/${id}`);
  }
}