import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
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
    game: undefined,
    user: undefined,
    content: '',
    postDate: '',
    score: undefined,
  };

  constructor(
    private route: ActivatedRoute,
    private userReviewService: UserReviewService,
    private router: Router,
    private _location: Location
  ) {
  }

  ngOnInit() {
    this.route.params.subscribe(params => {
      if (params['id']) {
        this.userReviewService.getUserReview(params['id']).subscribe((userReview: UserReview) => {
          this.userReview = userReview;
        });
      }
    });
  }

  formatDate(dateArray: number[] | undefined): string {
    if (!dateArray) {
      return 'Unknown';
    }

    if (dateArray.length !== 3) {
      return 'Invalid date';
    }

    const [year, month, day] = dateArray;
    const date = new Date(year, month - 1, day, 15);

    if (isNaN(date.getTime())) {
      return 'Invalid date';
    }

    return new Intl.DateTimeFormat('en-US', {
      day: 'numeric',
      month: 'long',
      year: 'numeric'
    }).format(date);
  }

  goBack() {
    this._location.back();
  }
}
