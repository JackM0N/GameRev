import { AfterViewInit, ChangeDetectorRef, Component, SimpleChanges } from '@angular/core';
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

  ngOnInit(): void {
    this._adService.setAdVisible(true);

    this._adService.adBoxActive$.subscribe(isActive => {
      this.adjustMargin(isActive);
    });

    this._cdRef.detectChanges();
  }

  public adjustMargin(isActive: boolean): void {
    if (isActive) {
      this._backgroundService.setMainContentStyle({ 'margin-left': '220px' });
    } else {
      this._backgroundService.setMainContentStyle({ 'margin-left': '0px' });
    }

    this._cdRef.detectChanges();
  }

  ngAfterViewInit(): void {
  }

  ngOnDestroy(): void {
  }
}
