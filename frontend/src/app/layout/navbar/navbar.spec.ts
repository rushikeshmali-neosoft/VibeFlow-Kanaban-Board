import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatDialogModule } from '@angular/material/dialog';
import { MatTooltipModule } from '@angular/material/tooltip';
import { RouterTestingModule } from '@angular/router/testing';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { of } from 'rxjs';
import { Navbar } from './navbar';
import { AuthService } from '../../core/services/auth';

describe('Navbar Component - TDD', () => {
  let component: Navbar;
  let fixture: ComponentFixture<Navbar>;
  let mockAuthService: jasmine.SpyObj<AuthService>;
  let router: Router;

  const mockUser = { id: 1, email: 'user@vibeflow.com', name: 'Test User' };

  beforeEach(async () => {
    mockAuthService = jasmine.createSpyObj('AuthService', ['logout'], {
      currentUser$: of(mockUser),
    });

    await TestBed.configureTestingModule({
      declarations: [Navbar],
      imports: [
        RouterTestingModule.withRoutes([]),
        MatToolbarModule,
        MatIconModule,
        MatButtonModule,
        MatDialogModule,
        MatTooltipModule,
        BrowserAnimationsModule,
      ],
      providers: [
        { provide: AuthService, useValue: mockAuthService },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(Navbar);
    component = fixture.componentInstance;
    router = TestBed.inject(Router);
    fixture.detectChanges();
  });

  it('should create navbar', () => {
    expect(component).toBeTruthy();
  });

  it('should expose currentUser$ from AuthService', (done) => {
    component.currentUser$.subscribe((user) => {
      expect(user).toEqual(mockUser);
      done();
    });
  });

  it('should call authService.logout and navigate to /login on logout()', () => {
    const navigateSpy = spyOn(router, 'navigate');
    component.logout();
    expect(mockAuthService.logout).toHaveBeenCalled();
    expect(navigateSpy).toHaveBeenCalledWith(['/login']);
  });

  it('should render VibeFlow brand in the template', () => {
    const compiled = fixture.nativeElement as HTMLElement;
    const brand = compiled.querySelector('.navbar__logo-text');
    expect(brand?.textContent?.trim()).toBe('VibeFlow');
  });

  it('should render search input', () => {
    const compiled = fixture.nativeElement as HTMLElement;
    const searchInput = compiled.querySelector('.navbar__search-input');
    expect(searchInput).not.toBeNull();
  });

  it('should render Create New Task button', () => {
    const compiled = fixture.nativeElement as HTMLElement;
    const createBtn = compiled.querySelector('.navbar__create-btn');
    expect(createBtn).not.toBeNull();
  });

  it('should open create task dialog when Create New Task button is clicked', () => {
    const openCreateTaskSpy = spyOn(component, 'openCreateTask');
    const compiled = fixture.nativeElement as HTMLElement;
    const createBtn = compiled.querySelector('.navbar__create-btn') as HTMLButtonElement;
    createBtn?.click();
    expect(openCreateTaskSpy).toHaveBeenCalled();
  });
});
