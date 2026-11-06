import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NavigationBar } from './navigation-bar';

describe('NavigationBar', () => {
  let component: NavigationBar;
  let fixture: ComponentFixture<NavigationBar>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [NavigationBar],
    }).compileComponents();

    fixture = TestBed.createComponent(NavigationBar);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('renders the logo as a themed image', () => {
    const brand: HTMLElement | null = fixture.nativeElement.querySelector('.nav__brand');

    expect(brand?.getAttribute('role')).toBe('img');
    expect(brand?.getAttribute('aria-label')).toBe('Tempus');
  });
});
