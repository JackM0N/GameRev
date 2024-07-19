import { Component, OnInit } from '@angular/core';
import { WebsiteUser } from '../../../interfaces/websiteUser';
import { Observer } from 'rxjs';
import { UserService } from '../../../services/user.service';
import { ActivatedRoute } from '@angular/router';
import { AuthService } from '../../../services/auth.service';
import { formatDate } from '../../../util/formatDate';

@Component({
  selector: 'app-profile',
  templateUrl: './profile.component.html',
  styleUrls: [
    '/src/app/styles/shared-form-styles.css',
    './profile.component.css'
  ]
})
export class ProfileComponent implements OnInit {
  selectedImage: File | null = null;
  imageUrl: string = '';
  formatDate = formatDate;

  user: WebsiteUser = {
    nickname: '',
    profilepic: '',
    description: '',
    joinDate: [],
    isBanned: false
  }

  constructor(
    private authService: AuthService,
    private userService: UserService,
    private route: ActivatedRoute,
  ) {
  }

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      if (params['name']) {
        const observer: Observer<WebsiteUser> = {
          next: response => {
            this.user = response;
            console.log(this.user);

            const token = this.authService.getToken();
    
            if (this.user.profilepic && response.nickname && token) {
              //this.imageUrl = this.user.profilepic;
              const observerProfilePicture: Observer<any> = {
                next: response2 => {
                  this.imageUrl = URL.createObjectURL(response2);
                },
                error: error => {
                  console.error(error);
                },
                complete: () => {}
              };
              this.userService.getProfilePicture(response.nickname, token).subscribe(observerProfilePicture);
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
