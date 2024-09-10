import { Component, Input } from '@angular/core';
import { CriticReview } from '../../../../interfaces/criticReview';
import { CriticReviewService } from '../../../../services/critic-review.service';
import { formatDateArray } from '../../../../util/formatDate';
import { AuthService } from '../../../../services/auth.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-gameinfo-critic-review',
  templateUrl: './critic-review.component.html',
  styleUrl: './game-information.component.css'
})
export class GameInfoCriticReviewComponent {
  @Input() gameTitle?: string;
  
  public criticReview?: CriticReview;
  public formatDateArray = formatDateArray;

  constructor(
    private criticReviewService: CriticReviewService,
    private authService: AuthService,
    private router: Router,
  ) {}

  ngOnInit() {
    if (!this.gameTitle) {
      console.log("Game title is undefined");
      return;
    }

    // Load critic review
    this.criticReviewService.getCriticReviewsByGameTitle(this.gameTitle).subscribe({
      next: response => {
        if (response) {
          this.criticReview = response;
        }
      },
      error: err => {
        console.error('Error fetching critic review:', err);
      }
    });
  }

  canAddCriticReview() {
    return this.authService.isAuthenticated() && this.authService.hasRole('Critic') && this.criticReview == undefined;
  }

  routeToAddNewCriticReview() {
    this.router.navigate(['/critic-reviews/add/' + this.gameTitle]);
  }
}


