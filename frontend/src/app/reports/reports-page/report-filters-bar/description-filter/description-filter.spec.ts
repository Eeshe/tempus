import { ComponentFixture, TestBed } from '@angular/core/testing';
import { DescriptionFilter } from './description-filter';

describe('DescriptionFilter', () => {
  let component: DescriptionFilter;
  let fixture: ComponentFixture<DescriptionFilter>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DescriptionFilter],
    }).compileComponents();

    fixture = TestBed.createComponent(DescriptionFilter);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
