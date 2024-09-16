import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { WebsiteUser } from '../../../interfaces/websiteUser';
import { Observer } from 'rxjs';
import { UserService } from '../../../services/user.service';
import { ActivatedRoute } from '@angular/router';
import { formatDateArray } from '../../../util/formatDate';
import { ImageCacheService } from '../../../services/imageCache.service';
import { BackgroundService } from '../../../services/background.service';
import { BaseAdComponent } from '../../base-components/base-ad-component';
import { AdService } from '../../../services/ad.service';

@Component({
  selector: 'app-profile',
  templateUrl: './profile.component.html'
})
export class ProfileComponent extends BaseAdComponent implements OnInit {
  protected selectedImage: File | null = null;
  protected imageUrl: string = '';
  protected formatDate = formatDateArray;

  protected user: WebsiteUser = {
    nickname: '',
    profilepic: '',
    description: '',
    joinDate: [],
    isBanned: false
  }

  constructor(
    private userService: UserService,
    private route: ActivatedRoute,
    private imageCacheService: ImageCacheService,
    private backgroundService: BackgroundService,
    adService: AdService,
    cdRef: ChangeDetectorRef
  ) {
    super(adService, backgroundService, cdRef);
  }

  override ngOnInit(): void {
    super.ngOnInit();

    this.backgroundService.setClasses(['fallingCds']);

    this.route.params.subscribe(params => {
      if (params['name']) {
        const observer: Observer<WebsiteUser> = {
          next: response => {
            this.user = response;

            if (response.profilepic && response.nickname) {
              const didProfilePicChange = this.imageCacheService.didPictureNameChange("profilePicName" + response.nickname, response.profilepic);

              if (!didProfilePicChange && this.imageCacheService.isCached("profilePic" + response.nickname)) {
                const cachedImage = this.imageCacheService.getCachedImage("profilePic" + response.nickname);
                if (cachedImage) {
                  this.imageUrl = cachedImage;
                }
              } else {
                const observerProfilePicture: Observer<any> = {
                  next: response2 => {
                    this.imageUrl = URL.createObjectURL(response2);
                    this.imageCacheService.cacheBlob("profilePic" + response.nickname, response2);
                    if (response.profilepic) {
                      this.imageCacheService.cacheProfilePicName("profilePicName" + response.nickname, response.profilepic);
                    }
                  },
                  error: error => {
                    console.error(error);
                  },
                  complete: () => {}
                };
                this.userService.getProfilePicture(response.nickname).subscribe(observerProfilePicture);
              }
            }
          },
          error: error => {
            console.error(error);
          },
          complete: () => {}
        };
        this.userService.getUser(params['name']).subscribe(observer);
      }
    });
  }
}
