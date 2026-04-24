import { Component } from '@angular/core';
import { Router } from '@angular/router';

@Component({
  selector: 'app-sidebar',
  standalone: false,
  templateUrl: './sidebar.html',
  styleUrl: './sidebar.scss',
})
export class Sidebar {
  menuItems = [
    { label: 'Board', icon: 'dashboard', route: '/board', active: false },
    { label: 'Time Reports', icon: 'bar_chart', route: '/reports/time', active: false },
    { label: 'Teams', icon: 'groups', route: '/teams', active: false },
    { label: 'Settings', icon: 'settings', route: '/settings', active: false },
  ];

  constructor(private router: Router) {
    this.updateActiveState();
    this.router.events.subscribe(() => this.updateActiveState());
  }

  private updateActiveState(): void {
    const currentRoute = this.router.url;
    this.menuItems.forEach(item => {
      item.active = currentRoute.startsWith(item.route);
    });
  }

  navigate(route: string): void {
    this.router.navigate([route]);
  }
}