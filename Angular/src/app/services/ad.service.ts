import { Injectable } from '@angular/core';
import { BehaviorSubject, interval } from 'rxjs';
import { startWith, switchMap } from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class AdService {
  private ads: string[] = [
    'assets/ads/ad1.png',
    'assets/ads/ad2.png',
  ];
  private currentAdIndex = 0;
  private adSubject = new BehaviorSubject<string>(this.ads[this.currentAdIndex]);
  ad$ = this.adSubject.asObservable();

  private adBoxActiveSubject = new BehaviorSubject<boolean>(true);
  adBoxActive$ = this.adBoxActiveSubject.asObservable();
  
  private adVisibleSubject = new BehaviorSubject<boolean>(false);
  adVisible$ = this.adVisibleSubject.asObservable();

  constructor() {
    this.startAdRotation();
  }

  private startAdRotation() {
    interval(15000).pipe(
      startWith(0),
      switchMap(() => this.getNextAd())
    ).subscribe(ad => this.adSubject.next(ad));
  }

  private getNextAd() {
    this.currentAdIndex = (this.currentAdIndex + 1) % this.ads.length;
    return [this.ads[this.currentAdIndex]];
  }

  setAdBoxActive(isActive: boolean) {
    this.adBoxActiveSubject.next(isActive);
  }

  setAdVisible(visible: boolean) {
    this.adVisibleSubject.next(visible);
  }
}
