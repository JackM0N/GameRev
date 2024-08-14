import { Component, OnInit } from '@angular/core';
import { AdService } from '../../services/ad.service';

@Component({
  selector: 'app-ad-box',
  templateUrl: './ad-box.component.html'
})
export class AdBoxComponent implements OnInit {
  currentAd?: string;
  closed = false;

  constructor(
    private adService: AdService,
  ) {}

  ngOnInit() {
    this.adService.ad$.subscribe(ad => this.currentAd = ad);

    this.adService.adVisible$.subscribe(isVisible => {
      this.closed = !isVisible;
      this.adService.setAdBoxActive(isVisible);
    });
  }

  close() {
    this.closed = true;
    this.adService.setAdBoxActive(false);
  }
}
