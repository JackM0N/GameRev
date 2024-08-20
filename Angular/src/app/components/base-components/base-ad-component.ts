import { AfterViewInit, ChangeDetectorRef, Component } from '@angular/core';
import { BackgroundService } from '../../services/background.service';
import { AdService } from '../../services/ad.service';

@Component({
  template: ''
})
export abstract class BaseAdComponent implements AfterViewInit {
  constructor(
    protected _adService: AdService,
    protected _backgroundService: BackgroundService,
    protected _cdRef: ChangeDetectorRef
  ) {}

  ngAfterViewInit(): void {
    this._adService.setAdVisible(true);

    this._adService.adBoxActive$.subscribe(isActive => {
      if (isActive) {
        this._backgroundService.setMainContentStyle({ 'margin-left': '220px' });
      } else {
        this._backgroundService.setMainContentStyle({ 'margin-left': '0px' });
      }
    });

    this._cdRef.detectChanges();
  }

  ngOnDestroy(): void {
    this._adService.setAdVisible(false);
  }
}
