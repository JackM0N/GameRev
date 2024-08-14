import { Component, OnInit } from '@angular/core';
import { AdService } from '../../services/ad.service';

@Component({
  selector: 'app-ad-box',
  templateUrl: './ad-box.component.html',
  styleUrl: './ad-box.component.css'
})
export class AdBoxComponent implements OnInit {
  currentAd?: string;
  closed = false;

  constructor(
    private adService: AdService,
  ) {}

  ngOnInit() {
    this.adService.ad$.subscribe(ad => this.currentAd = ad);
  }

  close() {
    this.closed = true;
  }
}
