import { ComponentFixture, TestBed } from '@angular/core/testing';
import { BillableFilter } from './billable-filter';

describe('BillableFilter', () => {
  let component: BillableFilter;
  let fixture: ComponentFixture<BillableFilter>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [BillableFilter],
    }).compileComponents();

    fixture = TestBed.createComponent(BillableFilter);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should default to null', () => {
    expect(component.isBillable()).toBeNull();
  });

  it('should emit the selected value', () => {
    const emitted: (boolean | null)[] = [];
    component.isBillableChangeEvent.subscribe((value) => emitted.push(value));

    component.select(true);
    component.select(false);
    component.select(null);

    expect(emitted).toEqual([true, false, null]);
  });

  it('should close the popup after selecting', () => {
    component.isOpen.set(true);

    component.select(true);

    expect(component.isOpen()).toBe(false);
  });

  it('should report the selected state', () => {
    fixture.componentRef.setInput('isBillable', true);

    expect(component.isSelected(true)).toBe(true);
    expect(component.isSelected(false)).toBe(false);
    expect(component.isSelected(null)).toBe(false);
  });
});
