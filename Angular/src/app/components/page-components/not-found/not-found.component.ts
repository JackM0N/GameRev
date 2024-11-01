import { Component, OnInit } from '@angular/core';
import { AdService } from '../../../services/ad.service';
import { BackgroundService } from '../../../services/background.service';

@Component({
  selector: 'app-not-found',
  templateUrl: './not-found.component.html'
})
export class NotFoundComponent implements OnInit {
  constructor(
    private adService: AdService,
    private backgroundService: BackgroundService
  ) {}

  ngOnInit(): void {
    this.adService.setAdVisible(false);
    this.backgroundService.setClasses(['fallingCds']);
  }
}
