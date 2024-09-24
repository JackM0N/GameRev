import { ChangeDetectorRef, Component } from '@angular/core';
import { AuthService } from './services/auth.service';
import { BackgroundService } from './services/background.service';
import { Router, NavigationStart } from '@angular/router';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent {
  title = 'Gamerev';

  styles: { [key: string]: string } = {};
  mainStyles: { [key: string]: string } = {};
  classes: string[] = [];

  constructor(
    private router: Router,
    public authService: AuthService,
    private backgroundService: BackgroundService,
    private cdRef: ChangeDetectorRef
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
}
