import { Inject, Injectable, PLATFORM_ID } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { catchError, map, Observable, throwError } from 'rxjs';
import { WebsiteUser } from '../interfaces/websiteuser';
import { LoginCredentials } from '../interfaces/loginCredentials';
import { isPlatformBrowser } from '@angular/common';
import { JwtHelperService } from '@auth0/angular-jwt';
import { NewCredentials } from '../interfaces/newCredentials';

@Injectable({
  providedIn: 'root'
})
// Service for handling website authentication
export class AuthService {
  private registerUrl = 'http://localhost:8080/register';
  private loginUrl = 'http://localhost:8080/login';
  private profileChangeUrl = 'http://localhost:8080/user/edit-profile';
  private profileInformationUrl = 'http://localhost:8080/user/account';

  constructor(
    private http: HttpClient,
    public jwtHelper: JwtHelperService,
    @Inject(PLATFORM_ID) private platformId: any
  ) { }

  registerUser(userData: WebsiteUser): Observable<any> {
    return this.http.post<any>(this.registerUrl, userData);
  }

  login(credentials: LoginCredentials): Observable<any> {
    return this.http.post<any>(this.loginUrl, credentials)
      .pipe(
        map(response => {
          const token = response.token;
          
          if (token && isPlatformBrowser(this.platformId)) {
            localStorage.setItem('access_token', token);
          }
          return response;
        }),
        catchError(error => {
          console.error('Login failed:', error);
          return throwError(error);
        })
      );
  }

  isAuthenticated(): boolean {
    if (isPlatformBrowser(this.platformId)) {
      const token = localStorage.getItem('access_token');
      return token !== null && !this.jwtHelper.isTokenExpired(token);
    }
    return false;
  }

  getToken(): string | null {
    return localStorage.getItem('access_token');
  }

  logout() {
    if (isPlatformBrowser(this.platformId)) {
      localStorage.removeItem('access_token');
    }
  }

  getUserName(): string | null {
    if (isPlatformBrowser(this.platformId)) {
      const token = localStorage.getItem('access_token');
      if (token) {
        const decodedToken = this.jwtHelper.decodeToken(token);
        return decodedToken.sub
      }
    }
    return null;
  }

  getUserProfileInformation(username: string, token: string): Observable<WebsiteUser> {
    const url = `${this.profileInformationUrl}/${username}`;
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`
    });
    return this.http.get<WebsiteUser>(url, { headers });
}

  changeProfile(userData: NewCredentials, token: string): Observable<any> {
    const url = `${this.profileChangeUrl}/${userData.username}`;
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    });
    return this.http.put<WebsiteUser>(url, userData, { headers });
  }
}