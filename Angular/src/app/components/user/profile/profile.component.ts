import { Component, OnInit } from '@angular/core';
import { WebsiteUser } from '../../../interfaces/websiteUser';
import { Observer } from 'rxjs';
import { UserService } from '../../../services/user.service';
import { ActivatedRoute } from '@angular/router';

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

  user: WebsiteUser = {
    nickname: '',
    profilepic: '',
    description: '',
    joinDate: '',
    isBanned: false
  }

  constructor(
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
    
            if (this.user.profilepic) {
              this.imageUrl = this.user.profilepic;
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
