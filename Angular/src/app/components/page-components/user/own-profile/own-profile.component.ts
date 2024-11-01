import { Component, OnInit, signal } from '@angular/core';
import { AuthService } from '../../../../services/auth.service';
import { Router } from '@angular/router';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { NewCredentials } from '../../../../models/newCredentials';
import { MatDialog, MatDialogRef } from '@angular/material/dialog';
import { UserService } from '../../../../services/user.service';
import { ImageCacheService } from '../../../../services/imageCache.service';
import { BackgroundService } from '../../../../services/background.service';
import { PopupDialogComponent } from '../../../general-components/popup-dialog.component';
import { AccountDeletionConfirmationDialogComponent } from '../account-deletion-confirmation-dialog.component';
import { AdService } from '../../../../services/ad.service';
import { NotificationService } from '../../../../services/notification.service';
import { NotificationAction } from '../../../../enums/notificationActions';
import { FileUploadOptions } from '../../../../enums/fileUploadOptions';

@Component({
  selector: 'app-own-profile',
  templateUrl: './own-profile.component.html'
})
export class OwnProfileComponent implements OnInit {
  public changeProfileInformationForm: FormGroup;
  public changeProfilePictureForm: FormGroup;

  public hidePassword = signal(true);
  private selectedImage: File | null = null;
  public imageUrl = '';

  public email?: string;

  constructor(
    private authService: AuthService,
    private userService: UserService,
    private imageCacheService: ImageCacheService,
    private router: Router,
    private formBuilder: FormBuilder,
    public dialog: MatDialog,
    private notificationService: NotificationService,
    private backgroundService: BackgroundService,
    private adService: AdService
  ) {
    this.changeProfileInformationForm = this.formBuilder.group({
      currentPassword: ['', [Validators.required, Validators.minLength(6)]],
      nickname: ['', [Validators.minLength(3)]],
      description: [''],
    });
    
    this.changeProfilePictureForm = this.formBuilder.group({
      profilePicture: [null, [Validators.required]],
    });
  }

  ngOnInit(): void {
    this.adService.setAdVisible(false);
    this.backgroundService.setClasses(['matrixNumbers']);

    this.authService.getUserProfileInformation().subscribe({
      next: response => {
        if (response && response.nickname) {
          this.changeProfileInformationForm.get('nickname')?.setValue(response.nickname);
          this.changeProfileInformationForm.get('description')?.setValue(response.description);
          this.email = response.email;

          const didProfilePicChange = this.imageCacheService.didPictureNameChange("profilePicName" + response.nickname, response.profilepic);

          if (!didProfilePicChange && this.imageCacheService.isCached("profilePic" + response.nickname)) {
            const cachedImage = this.imageCacheService.getCachedImage("profilePic" + response.nickname);
            if (cachedImage) {
              this.imageUrl = cachedImage;
            }

          } else {
            this.userService.getProfilePicture(response.nickname).subscribe({
              next: response2 => {
                if (response2 && response.profilepic) {
                  this.imageUrl = URL.createObjectURL(response2);
                  this.imageCacheService.cacheBlob("profilePic" + response.nickname, response2);
                  this.imageCacheService.cacheProfilePicName("profilePicName" + response.nickname, response.profilepic);
                }
              },
              error: error => {
                console.error(error);
              }
            });
          }
        }
      },
      error: error => {
        this.notificationService.popErrorToast('Unable to retrieve profile information', error);
      }
    });
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
    this.notificationService.popSuccessToast('Successfully logged out');
    this.router.navigate(['/login']);
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
  
    if (!userName || !dialogRef || !dialogRef.componentRef) {
      this.notificationService.popErrorToast('Account deletion failed');
      return;
    }

    const userData: NewCredentials = {
      username: userName,
      isDeleted: true,
      currentPassword: dialogRef.componentRef.instance.deleteAccountForm.get('currentPassword')?.value,
    };

    this.authService.deleteOwnAccount(userData).subscribe({
      next: () => {
        this.notificationService.popSuccessToast('Account deleted successfuly!', NotificationAction.GO_TO_HOME);
        this.authService.logout();
      },
      error: error => this.notificationService.popErrorToast('Account deletion failed', error)
    });
  }

  onSubmitProfileInformationChange() {
    if (this.changeProfileInformationForm.valid) {
      const userName = this.authService.getUsername();

      if (!userName) {
        this.notificationService.popErrorToast('Profile picture change failed', "Username not found");
        return;
      }

      const newData: NewCredentials = {
        username: userName,
        currentPassword: this.changeProfileInformationForm.get('currentPassword')?.value,
        nickname: this.changeProfileInformationForm.get('nickname')?.value,
        description: this.changeProfileInformationForm.get('description')?.value,
      };

      this.authService.changeProfile(newData).subscribe({
        next: () => { this.notificationService.popSuccessToast('Profile information change successful!'); },
        error: error => this.notificationService.popErrorToast('Profile information change failed', error)
      });
    }
  }

  hidePasswordClickEvent(event: MouseEvent) {
    this.hidePassword.set(!this.hidePassword());
    event.stopPropagation();
  }

  maxSizeError() {
    this.notificationService.popErrorToast('Image size too large. Max size is 10MB.');
  }

  onSubmitProfilePictureChange() {
    if (this.changeProfilePictureForm.valid && this.selectedImage) {
      const nickName = this.authService.getNickname();
      const userName = this.authService.getUsername();

      if (!userName) {
        this.notificationService.popErrorToast('Profile picture change failed', "Username not found");
        return;
      }

      if (this.selectedImage && this.selectedImage.size > FileUploadOptions.MAX_FILE_SIZE) {
        this.maxSizeError();
        return;
      }

      this.authService.changeProfilePicture(userName, this.selectedImage).subscribe({
        next: () => {
          this.notificationService.popSuccessToast('Profile picture change successful!');
          if (nickName) {
            this.imageCacheService.deleteCachedImage("profilePic" + nickName);
          }
          if (this.selectedImage) {
            this.imageUrl = URL.createObjectURL(this.selectedImage);
          }
        },
        error: error => {
          if (error.error == "Maximum upload size exceeded") {
            this.maxSizeError();
          } else {
            this.notificationService.popErrorToast('Profile picture change failed', error);
          }
        }
      });
    }
  }

  onFileSelected(event: Event) {
    const target = event.target as HTMLInputElement;
    const file: File | null = target.files ? target.files[0] : null;

    if (file && file.size > FileUploadOptions.MAX_FILE_SIZE) {
      this.maxSizeError();
      return;
    }

    if (file) {
      this.selectedImage = file;
      this.imageUrl = URL.createObjectURL(file);
    }
  }

  routeToOwnReviews() {
    this.router.navigate(['/user-reviews']);
  }

  routeToOwnReports() {
    this.router.navigate(['/user-reports']);
  }
}
