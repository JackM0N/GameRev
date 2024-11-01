import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class BackgroundService {
  private mainStyleSubject = new BehaviorSubject<Record<string, string>>({});
  mainStyle$ = this.mainStyleSubject.asObservable();

  private styleSubject = new BehaviorSubject<Record<string, string>>({});
  style$ = this.styleSubject.asObservable();

  private classesSubject = new BehaviorSubject<string[]>([]);
  classes$ = this.classesSubject.asObservable();

  resetStyles() {
    this.mainStyleSubject.next({});
    this.styleSubject.next({});
    this.classesSubject.next([]);
  }

  setMainContentStyle(styles: Record<string, string>) {
    this.mainStyleSubject.next(styles);
  }

  setStyle(styles: Record<string, string>) {
    this.styleSubject.next(styles);
  }

  setClasses(classes: string[]) {
    this.classesSubject.next(classes);
  }
}
