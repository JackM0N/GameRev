import { Component, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';

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

  constructor(
    public dialog: MatDialog,
  ) {
  }

  ngOnInit(): void {
  }
}
