import { ChangeDetectorRef, Component, ElementRef, HostListener } from '@angular/core';
import { AuthService } from './services/auth.service';
import { BackgroundService } from './services/background.service';

import Quill from 'quill';
import QuillResizeImage from 'quill-resize-image';

Quill.register('modules/resize', QuillResizeImage);

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent {
  protected title = 'Gamerev';
  protected isNavbarOpen = false;

  protected styles: { [key: string]: string } = {};
  protected mainStyles: { [key: string]: string } = {};
  protected classes: string[] = [];

  protected routes = [
    { label: 'Forums', link: '/', onlyMobile: true },
    { label: 'Games', link: '/games' },
    { label: 'Users', link: '/users', roles: ['Admin', 'Critic'] },
    { label: 'Reports', link: '/reports', roles: ['Admin'] },
    { label: 'Critics', link: '/critic-reviews', roles: ['Admin', 'Critic'] },
    { label: 'Library', link: '/library' },
    { label: 'Users', link: '/users' },
    { label: 'Profile', link: '/profile' },

    { label: 'Login', link: '/login', guestOnly: true },
    { label: 'Register', link: '/register', guestOnly: true }
  ];

  constructor(
    public authService: AuthService,
    private backgroundService: BackgroundService,
    private cdRef: ChangeDetectorRef,
    private elRef: ElementRef,
  ) {}

  ngOnInit() {
    /*
    this.router.events.subscribe(event => {
      if (event instanceof NavigationStart) {
        this.backgroundService.resetStyles();
      }
    });
    */

    this.backgroundService.style$.subscribe(styles => {
      this.styles = styles;
    });

    this.backgroundService.mainStyle$.subscribe(mainStyles => {
      this.mainStyles = mainStyles;
    });

    this.backgroundService.classes$.subscribe(classes => {
      this.classes = classes;
      this.cdRef.detectChanges();
    });
  }

  toggleNavbar() {
    this.isNavbarOpen = !this.isNavbarOpen;
  }

  closeNavbar() {
    this.isNavbarOpen = false;
  }

  canUseRoute(route: any) {
    if (!this.authService.isAuthenticated()) {
      return route.guestOnly;

    } else if (route.guestOnly) {
      return false;
    }
    
    if (!route.roles || route.roles.length === 0) {
      return true;
    }
    return this.authService.hasAnyRole(route.roles);
  }

  // Close the navbar if the click is outside of it and it is currently open
  @HostListener('document:click', ['$event'])
  onClick(event: MouseEvent) {
    const targetElement = event.target as HTMLElement;
    const mobileNavbar = this.elRef.nativeElement.querySelector('#mobile-navbar-menu');
    const mobileNavbarButton = this.elRef.nativeElement.querySelector('#mobile-navbar-button');

    if (targetElement) {
      const clickedInside = mobileNavbar.contains(targetElement);
  
      if (!clickedInside && this.isNavbarOpen && !mobileNavbarButton.contains(targetElement)) {
        this.closeNavbar();
      }
    }
  }
}
