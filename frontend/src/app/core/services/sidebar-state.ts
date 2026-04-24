import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

/**
 * Single source of truth for the sidebar collapsed/expanded state.
 * Injected in the Sidebar (to toggle), the Navbar (to render the hamburger),
 * and the App shell (to shift the content area).
 */
@Injectable({ providedIn: 'root' })
export class SidebarStateService {
  private readonly _collapsed = new BehaviorSubject<boolean>(false);

  /** Emits the current collapsed state. */
  readonly collapsed$ = this._collapsed.asObservable();

  get isCollapsed(): boolean {
    return this._collapsed.value;
  }

  toggle(): void {
    this._collapsed.next(!this._collapsed.value);
  }

  collapse(): void {
    this._collapsed.next(true);
  }

  expand(): void {
    this._collapsed.next(false);
  }
}
