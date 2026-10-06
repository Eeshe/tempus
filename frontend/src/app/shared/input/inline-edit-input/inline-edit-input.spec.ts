import { ComponentFixture, TestBed } from '@angular/core/testing';
import { InlineEditInput } from './inline-edit-input';

describe('InlineEditInput', () => {
  let component: InlineEditInput;
  let fixture: ComponentFixture<InlineEditInput>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [InlineEditInput],
    }).compileComponents();

    fixture = TestBed.createComponent(InlineEditInput);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
