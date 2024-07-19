import { Component, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { WebsiteUser } from '../../../interfaces/websiteUser';
import { Observer } from 'rxjs';
import { UserService } from '../../../services/user.service';

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
    public dialog: MatDialog,
    private userService: UserService,
  ) {
  }

  ngOnInit(): void {
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
    this.userService.getUser().subscribe(observer);
  }
}
