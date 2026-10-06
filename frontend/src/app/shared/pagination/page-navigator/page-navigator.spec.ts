import { ComponentFixture, TestBed } from '@angular/core/testing';
import { PageNavigator } from './page-navigator';

describe('PageNavigator', () => {
  let component: PageNavigator;
  let fixture: ComponentFixture<PageNavigator>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PageNavigator],
    }).compileComponents();

    fixture = TestBed.createComponent(PageNavigator);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
