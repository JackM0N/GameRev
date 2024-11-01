import { ChangeDetectorRef, Component, ElementRef, HostListener, OnInit } from '@angular/core';
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
export class AppComponent implements OnInit {
  public title = 'Gamerev';
  protected isNavbarOpen = false;

  protected styles: Record<string, string> = {};
  protected mainStyles: Record<string, string> = {};
  protected classes: string[] = [];

  protected routes = [
    { label: 'Forums', link: '/', onlyMobile: true },
    { label: 'Games', link: '/games', guestsCanAccess: true },
    { label: 'Reports', link: '/reports', roles: ['Admin'] },
    { label: 'Critics', link: '/critic-reviews', roles: ['Admin', 'Critic'] },
    { label: 'Library', link: '/library' },
    { label: 'Users', link: '/users' },
    { label: 'Profile', link: '/profile' },

    { label: 'Login', link: '/login', guestsCanAccess: true, guestOnly: true },
    { label: 'Register', link: '/register', guestsCanAccess: true, guestOnly: true }
  ];

  constructor(
    public authService: AuthService,
    private backgroundService: BackgroundService,
    private cdRef: ChangeDetectorRef,
    private elRef: ElementRef
  ) {}

  ngOnInit() {
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

  canUseRoute(route: { roles?: string[], guestOnly?: boolean, guestsCanAccess?: boolean }) {
    if (!this.authService.isAuthenticated()) {
      return route.guestOnly || route.guestsCanAccess;

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
    if (this.isNavbarOpen) {
      const targetElement = event.target as HTMLElement;
      const mobileNavbar = this.elRef.nativeElement.querySelector('#mobile-navbar-menu');
      const mobileNavbarButton = this.elRef.nativeElement.querySelector('#mobile-navbar-button');

      if (targetElement) {
        const clickedInside = mobileNavbar.contains(targetElement);
    
        if (!clickedInside && !mobileNavbarButton.contains(targetElement)) {
          this.closeNavbar();
        }
      }
    }
  }
}
