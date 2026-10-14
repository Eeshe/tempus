import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Page } from '../../models/page.model';
import { PageNavigator } from './page-navigator';

describe('PageNavigator', () => {
  let component: PageNavigator;
  let fixture: ComponentFixture<PageNavigator>;

  const mockPage: Page = {
    content: [],
    page: 0,
    size: 10,
    totalElements: 0,
    totalPages: 0,
    first: true,
    last: true,
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PageNavigator],
    }).compileComponents();

    fixture = TestBed.createComponent(PageNavigator);
    fixture.componentRef.setInput('page', mockPage);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
