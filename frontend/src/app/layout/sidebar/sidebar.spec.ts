import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { RouterTestingModule } from '@angular/router/testing';
import { Sidebar } from './sidebar';

describe('Sidebar Component', () => {
  let component: Sidebar;
  let fixture: ComponentFixture<Sidebar>;
  let router: Router;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [Sidebar],
      imports: [RouterTestingModule.withRoutes([]), MatIconModule],
    }).compileComponents();

    fixture = TestBed.createComponent(Sidebar);
    component = fixture.componentInstance;
    router = TestBed.inject(Router);
    fixture.detectChanges();
  });

  it('should create the sidebar component', () => {
    expect(component).toBeTruthy();
  });

  it('should have 3 menu items', () => {
    expect(component.menuItems.length).toBe(3);
  });

  it('should have Board as the first menu item', () => {
    expect(component.menuItems[0].label).toBe('Board');
    expect(component.menuItems[0].route).toBe('/board');
  });

  it('should have Time Reports as second menu item', () => {
    expect(component.menuItems[1].label).toBe('Time Reports');
    expect(component.menuItems[1].route).toBe('/reports/time');
  });

  it('should have Teams as third menu item', () => {
    expect(component.menuItems[2].label).toBe('Teams');
  });

  it('should set active state based on current route', () => {
    // Navigate to /board and update active state
    Object.defineProperty(router, 'url', { get: () => '/board', configurable: true });
    (component as any).updateActiveState();
    expect(component.menuItems[0].active).toBeTrue();
    expect(component.menuItems[1].active).toBeFalse();
  });

  it('should navigate to route when navigate() is called', () => {
    const navigateSpy = spyOn(router, 'navigate');
    component.navigate('/board');
    expect(navigateSpy).toHaveBeenCalledWith(['/board']);
  });

  it('should render menu items in the template', () => {
    const compiled = fixture.nativeElement as HTMLElement;
    const menuItems = compiled.querySelectorAll('.sidebar__menu-item');
    expect(menuItems.length).toBe(3);
  });

  it('should apply active class to active menu item', () => {
    component.menuItems[0].active = true;
    fixture.detectChanges();
    const compiled = fixture.nativeElement as HTMLElement;
    const activeItem = compiled.querySelector('.sidebar__menu-item--active');
    expect(activeItem).not.toBeNull();
  });
});
