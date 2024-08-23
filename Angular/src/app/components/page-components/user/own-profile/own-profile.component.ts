import { ChangeDetectorRef, Component, OnInit, signal } from '@angular/core';
import { AuthService } from '../../../../services/auth.service';
import { Router } from '@angular/router';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Observer } from 'rxjs';
import { NewCredentials } from '../../../../interfaces/newCredentials';
import { MatDialog, MatDialogRef } from '@angular/material/dialog';
import { UserService } from '../../../../services/user.service';
import { ImageCacheService } from '../../../../services/imageCache.service';
import { BackgroundService } from '../../../../services/background.service';
import { PopupDialogComponent } from '../../../general-components/popup-dialog.component';
import { AccountDeletionConfirmationDialogComponent } from '../account-deletion-confirmation-dialog.component';
import { BaseAdComponent } from '../../../base-components/base-ad-component';
import { AdService } from '../../../../services/ad.service';
import { NotificationService } from '../../../../services/notification.service';

@Component({
  selector: 'app-own-profile',
  templateUrl: './own-profile.component.html',
  styleUrls: [
    '/src/app/styles/shared-form-styles.css',
    './own-profile.component.css'
  ]
})
export class OwnProfileComponent extends BaseAdComponent implements OnInit {
  public changeProfileInformationForm: FormGroup;
  public changeProfilePictureForm: FormGroup;

  public hidePassword = signal(true);
  private selectedImage: File | null = null;
  public imageUrl: string = '';

  public email: string | null = null;

  constructor(
    private authService: AuthService,
    private userService: UserService,
    private imageCacheService: ImageCacheService,
    private router: Router,
    private formBuilder: FormBuilder,
    public dialog: MatDialog,
    private notificationService: NotificationService,
    private backgroundService: BackgroundService,
    adService: AdService,
    cdRef: ChangeDetectorRef
  ) {
    super(adService, backgroundService, cdRef);
    
    this.changeProfileInformationForm = this.formBuilder.group({
      currentPassword: ['', [Validators.required, Validators.minLength(6)]],
      nickname: ['', [Validators.minLength(3)]],
      description: [''],
    });
    
    this.changeProfilePictureForm = this.formBuilder.group({
      profilePicture: [null, [Validators.required]],
    });
  }

  override ngOnInit(): void {
    this.backgroundService.setClasses(['matrixNumbers']);

    const token = this.authService.getToken();
  
    if (token === null) {
      console.log("Token is null");
      return;
    }

    const observer: Observer<any> = {
      next: response => {
        if (response) {
          this.changeProfileInformationForm.get('nickname')?.setValue(response.nickname);
          this.changeProfileInformationForm.get('description')?.setValue(response.description);
          this.email = response.email;

          const didProfilePicChange = this.imageCacheService.didProfilePicNameChange("profilePicName" + response.nickname, response.profilepic);

          if (!didProfilePicChange && this.imageCacheService.isCached("profilePic" + response.nickname)) {
            const cachedImage = this.imageCacheService.getCachedImage("profilePic" + response.nickname);
            if (cachedImage) {
              this.imageUrl = cachedImage;
            }

          } else {
            const observerProfilePicture: Observer<any> = {
              next: response2 => {
                if (response2) {
                  this.imageUrl = URL.createObjectURL(response2);
                  this.imageCacheService.cacheBlob("profilePic" + response.nickname, response2);
                  this.imageCacheService.cacheProfilePicName("profilePicName" + response.nickname, response.profilepic);
                }
              },
              error: error => {
                console.error(error);
              },
              complete: () => {}
            };
            this.userService.getProfilePicture(response.nickname, token).subscribe(observerProfilePicture);
          }
        }
      },
      error: error => {
        this.notificationService.popErrorToast('Unable to retrieve profile information', error);
      },
      complete: () => {}
    };
    this.authService.getUserProfileInformation(token).subscribe(observer);
  }

  openLogoutDialog() {
    const dialogTitle = 'Confirm logout';
    const dialogContent = 'Are you sure you want to log out?';
    const submitText = 'Logout';
    const cancelText = 'Cancel';

    const dialogRef = this.dialog.open(PopupDialogComponent, {
      width: '300px',
      data: { dialogTitle, dialogContent, submitText, cancelText }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result === true) {
        this.logout();
      }
    });
  }

  logout() {
    this.authService.logout();
    this.router.navigate(['/']);
    this.notificationService.popSuccessToast('Successfully logged out', false);
  }

  openAccountDeletionDialog() {
    const dialogRef = this.dialog.open(AccountDeletionConfirmationDialogComponent);

    dialogRef.afterClosed().subscribe(result => {
      if (result === true) {
        this.deleteAccount(dialogRef);
      }
    });
  }

  deleteAccount(dialogRef: MatDialogRef<AccountDeletionConfirmationDialogComponent>) {
    const userName = this.authService.getUsername();
    const token = this.authService.getToken();
  
    if (!userName || !token || !dialogRef || !dialogRef.componentRef) {
      this.notificationService.popErrorToast('Account deletion failed');
      return;
    }

    const userData: NewCredentials = {
      username: userName,
      isDeleted: true,
      currentPassword: dialogRef.componentRef.instance.deleteAccountForm.get('currentPassword')?.value,
    };

    this.authService.deleteOwnAccount(userData, token).subscribe({
      next: () => {
        this.notificationService.popSuccessToast('Account deleted successfuly!', false);
        this.authService.logout();
        this.router.navigate(['/']);
      },
      error: error => this.notificationService.popErrorToast('Account deletion failed', error)
    });
  }

  onSubmitProfileInformationChange() {
    if (this.changeProfileInformationForm.valid) {
      const userName = this.authService.getUsername();
      const token = this.authService.getToken();

      if (!userName || !token) {
        this.notificationService.popErrorToast('Profile picture change failed', "Username or token not found");
        return;
      }

      const newData: NewCredentials = {
        username: userName,
        currentPassword: this.changeProfileInformationForm.get('currentPassword')?.value,
        nickname: this.changeProfileInformationForm.get('nickname')?.value,
        description: this.changeProfileInformationForm.get('description')?.value,
      };

      this.authService.changeProfile(newData, token).subscribe({
        next: () => { this.notificationService.popSuccessToast('Profile information change successful!', false); },
        error: error => this.notificationService.popErrorToast('Profile information change failed', error)
      });
    }
  }

  hidePasswordClickEvent(event: MouseEvent) {
    this.hidePassword.set(!this.hidePassword());
    event.stopPropagation();
  }

  onSubmitProfilePictureChange() {
    if (this.changeProfilePictureForm.valid && this.selectedImage) {
      const nickName = this.authService.getNickname();
      const userName = this.authService.getUsername();
      const token = this.authService.getToken();

      if (!userName || !token) {
        this.notificationService.popErrorToast('Profile picture change failed', "Username or token not found");
        return;
      }

      this.authService.changeProfilePicture(userName, this.selectedImage, token).subscribe({
        next: () => {
          this.notificationService.popSuccessToast('Profile picture change successful!', false);
          if (nickName) {
            this.imageCacheService.deleteCachedImage("profilePic" + nickName);
          }
        },
        error: error => this.notificationService.popErrorToast('Profile picture change failed', error)
      });
    }
  }

  onFileSelected(event: any) {
    const file: File = event.target.files[0];
    if (file) {
      this.selectedImage = file;
    }
  }

  routeToOwnReviews() {
    this.router.navigate(['/user-reviews']);
  }
}
