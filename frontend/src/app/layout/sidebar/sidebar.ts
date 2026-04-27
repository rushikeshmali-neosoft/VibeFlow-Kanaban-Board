import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { Observable } from 'rxjs';
import { SidebarStateService } from '../../core/services/sidebar-state';
import { AuthService } from '../../core/services/auth';

@Component({
  selector: 'app-sidebar',
  standalone: false,
  templateUrl: './sidebar.html',
  styleUrl: './sidebar.scss',
})
export class Sidebar implements OnInit {
  baseMenuItems = [
    { label: 'Board',        icon: 'dashboard', route: '/board',        active: false },
    { label: 'Time Reports', icon: 'bar_chart', route: '/reports/time', active: false },
    { label: 'Teams',        icon: 'groups',    route: '/teams',        active: false },
  ];

  menuItems: any[] = [];

  readonly collapsed$: Observable<boolean>;

  constructor(
    private readonly router: Router,
    private readonly sidebarState: SidebarStateService,
    private readonly authService: AuthService,
  ) {
    this.collapsed$ = this.sidebarState.collapsed$;
  }

  ngOnInit(): void {
    this.authService.currentUser$.subscribe(user => {
      if (user && user.role === 'ROLE_ADMIN') {
        this.menuItems = [
          { label: 'Admin Dashboard', icon: 'admin_panel_settings', route: '/admin/dashboard', active: false }
        ];
      } else {
        this.menuItems = [...this.baseMenuItems];
      }
      this.updateActiveState();
    });

    this.router.events.subscribe(() => this.updateActiveState());
  }

  toggle(): void {
    this.sidebarState.toggle();
  }

  navigate(route: string): void {
    this.router.navigate([route]);
  }

  private updateActiveState(): void {
    const current = this.router.url;
    this.menuItems.forEach(item => {
      item.active = current.startsWith(item.route);
    });
  }
}