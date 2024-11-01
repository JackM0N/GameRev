import { Component, Input, OnInit } from '@angular/core';
import { CriticReview } from '../../../../models/criticReview';
import { CriticReviewService } from '../../../../services/critic-review.service';
import { formatDateArray } from '../../../../util/formatDate';
import { AuthService } from '../../../../services/auth.service';
import { CriticReviewFormDialogComponent } from '../../critic-reviews/critic-review-form-dialog.component';
import { MatDialog } from '@angular/material/dialog';

@Component({
  selector: 'app-gameinfo-critic-review',
  templateUrl: './critic-review.component.html'
})
export class GameInfoCriticReviewComponent implements OnInit {
  @Input() public gameTitle?: string;
  
  protected criticReview?: CriticReview;
  protected formatDateArray = formatDateArray;

  constructor(
    private criticReviewService: CriticReviewService,
    private authService: AuthService,
    protected dialog: MatDialog,
  ) {}

  ngOnInit() {
    this.loadCriticReview();
  }

  loadCriticReview() {
    if (!this.gameTitle) {
      console.log("Game title is undefined");
      return;
    }

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

  canRequestCriticReview() {
    return this.authService.isAuthenticated() && this.authService.hasRole('Critic');
  }

  openRequestCriticReviewDialog() {
    if (this.gameTitle) {
      const dialogRef = this.dialog.open(CriticReviewFormDialogComponent, {
        data: {
          editing: false,
          gameTitle: this.gameTitle
        }
      });

      dialogRef.afterClosed().subscribe(result => {
        if (result === true) {
          this.loadCriticReview();
        }
      });
    }
  }

  openEditCriticReviewDialog(review: CriticReview) {
    if (this.gameTitle) {
      const dialogRef = this.dialog.open(CriticReviewFormDialogComponent, {
        data: {
          editing: true,
          gameTitle: this.gameTitle,
          review: review
        }
      });

      dialogRef.afterClosed().subscribe(result => {
        if (result === true) {
          this.loadCriticReview();
        }
      });
    }
  }
}
