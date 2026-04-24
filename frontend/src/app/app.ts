import { Component, DestroyRef, OnInit, inject } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { NavigationEnd, Router } from '@angular/router';
import { Observable, bufferTime, filter, map } from 'rxjs';
import { BoardStateService } from './core/services/board-state';
import { AuthService } from './core/services/auth';
import { WebsocketService } from './core/services/websocket';
import { SidebarStateService } from './core/services/sidebar-state';

@Component({
  selector: 'app-root',
  templateUrl: './app.html',
  standalone: false,
  styleUrl: './app.scss',
})
export class App implements OnInit {
  isBootstrapped = false;
  currentUrl = '';
  readonly sidebarCollapsed$: Observable<boolean>;
  private readonly destroyRef = inject(DestroyRef);

  constructor(
    private readonly authService: AuthService,
    private readonly router: Router,
    private readonly websocketService: WebsocketService,
    private readonly boardStateService: BoardStateService,
    private readonly sidebarStateService: SidebarStateService,
  ) {
    this.sidebarCollapsed$ = this.sidebarStateService.collapsed$;
    this.router.events
      .pipe(filter((event) => event instanceof NavigationEnd))
      .subscribe((event) => {
        this.currentUrl = (event as NavigationEnd).urlAfterRedirects;
      });
  }

  ngOnInit(): void {
    this.currentUrl = this.router.url;
    this.authService.authState$
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((state) => {
        if (state.token) {
          this.websocketService.connect();
          return;
        }

        this.websocketService.disconnect();
      });

    this.websocketService.events$
      .pipe(
        bufferTime(16),
        filter((events) => events.length > 0),
        map((events) => Array.from(new Map(events.map((event) => [event.data.id, event.data])).values())),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe((tasks) => this.boardStateService.upsertTasks(tasks));

    this.authService.restoreSession().subscribe({
      next: () => {
        this.isBootstrapped = true;
      },
      error: () => {
        this.isBootstrapped = true;
      },
    });
  }

  get showNavbar(): boolean {
    return (
      this.authService.isAuthenticated() &&
      !this.currentUrl.startsWith('/login') &&
      !this.currentUrl.startsWith('/register')
    );
  }
}
