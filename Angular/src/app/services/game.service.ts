import { Inject, Injectable, PLATFORM_ID } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { JwtHelperService } from '@auth0/angular-jwt';
import { Game } from '../interfaces/game';

@Injectable({
  providedIn: 'root'
})
// Service for handling games
export class GameService {
  private baseUrl = 'http://localhost:8080/games';
  private postUrl = 'http://localhost:8080/games/add';

  constructor(
    private http: HttpClient,
    public jwtHelper: JwtHelperService,
    @Inject(PLATFORM_ID) private platformId: any
  ) { }

  getGames(): Observable<Game> {
    return this.http.get<Game>(this.baseUrl);
  }

  getGameByName(name: string): Observable<Game> {
    return this.http.get<Game>(`${this.baseUrl}/${name}`);
  }

  addGame(ServiceAvailability?: Game): Observable<Game> {
    return this.http.post<Game>(this.postUrl, ServiceAvailability);
  }
}