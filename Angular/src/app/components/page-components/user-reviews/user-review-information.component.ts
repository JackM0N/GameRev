import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { UserReviewService } from '../../../services/user-review.service';
import { UserReview } from '../../../interfaces/userReview';
import { Location } from '@angular/common';

@Component({
  selector: 'app-user-review-information',
  templateUrl: './user-review-information.component.html',
  styleUrl: './user-review-information.component.css'
})
export class GameInformationComponent implements OnInit {
  userReview: UserReview = {
    gameTitle: '',
    userUsername: '',
    content: '',
    postDate: '',
    score: undefined,
  };

  constructor(
    private route: ActivatedRoute,
    private userReviewService: UserReviewService,
    private _location: Location
  ) {}

  ngOnInit() {
    this.route.params.subscribe(params => {
      if (params['id']) {
        this.userReviewService.getUserReviewById(params['id']).subscribe((userReview: UserReview) => {
          this.userReview = userReview;
        });
      }
    });
  }

  goBack() {
    this._location.back();
  }
}
