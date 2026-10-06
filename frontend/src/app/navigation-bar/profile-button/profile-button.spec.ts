import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { AuthService } from '../../services/auth.service';
import { ProfileButton } from './profile-button';

describe('ProfileButton', () => {
  let component: ProfileButton;
  let fixture: ComponentFixture<ProfileButton>;

  function triggerButton(): HTMLButtonElement {
    return fixture.nativeElement.querySelector('.profile-trigger');
  }

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ProfileButton],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])],
    }).compileComponents();

    fixture = TestBed.createComponent(ProfileButton);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('opens on trigger click and closes on a second click', () => {
    triggerButton().click();
    fixture.detectChanges();
    expect(component.isOpen()).toBe(true);

    triggerButton().click();
    fixture.detectChanges();
    expect(component.isOpen()).toBe(false);
  });

  it('closes on an outside click', () => {
    triggerButton().click();
    fixture.detectChanges();
    expect(component.isOpen()).toBe(true);

    document.body.click();
    fixture.detectChanges();
    expect(component.isOpen()).toBe(false);
  });

  it('links to the settings page', () => {
    const settingsLink: HTMLAnchorElement = fixture.nativeElement.querySelector('a.profile-action');
    expect(settingsLink.getAttribute('href')).toBe('/settings');
  });

  it('logs out and navigates to login', () => {
    const authService: AuthService = TestBed.inject(AuthService);
    const router: Router = TestBed.inject(Router);
    vi.spyOn(authService, 'logout').mockReturnValue(of(undefined));
    const navigateSpy = vi.spyOn(router, 'navigate').mockResolvedValue(true);

    const logoutButton: HTMLButtonElement =
      fixture.nativeElement.querySelector('.profile-action--logout');
    logoutButton.click();
    fixture.detectChanges();

    expect(authService.logout).toHaveBeenCalled();
    expect(navigateSpy).toHaveBeenCalledWith(['/login']);
  });
});
