import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { Observable } from 'rxjs';
import { SidebarStateService } from '../../core/services/sidebar-state';

@Component({
  selector: 'app-sidebar',
  standalone: false,
  templateUrl: './sidebar.html',
  styleUrl: './sidebar.scss',
})
export class Sidebar implements OnInit {
  menuItems = [
    { label: 'Board',        icon: 'dashboard', route: '/board',        active: false },
    { label: 'Time Reports', icon: 'bar_chart', route: '/reports/time', active: false },
    { label: 'Teams',        icon: 'groups',    route: '/teams',        active: false },
  ];

  readonly collapsed$: Observable<boolean>;

  constructor(
    private readonly router: Router,
    private readonly sidebarState: SidebarStateService,
  ) {
    this.collapsed$ = this.sidebarState.collapsed$;
  }

  ngOnInit(): void {
    this.updateActiveState();
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