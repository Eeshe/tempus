import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ColorInput } from './color-input';

describe('ColorInput', () => {
  let component: ColorInput;
  let fixture: ComponentFixture<ColorInput>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ColorInput],
    }).compileComponents();

    fixture = TestBed.createComponent(ColorInput);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('emits trimmed color', () => {
    const emitted: string[] = [];
    component.valueChange.subscribe((value: string) => emitted.push(value));

    component.commitColor(' #abcdef ');

    expect(emitted).toEqual(['#abcdef']);
  });

  it('does not emit when the color is unchanged', () => {
    const emitted: string[] = [];
    component.valueChange.subscribe((value: string) => emitted.push(value));
    fixture.componentRef.setInput('value', '#FFFFFF');

    component.commitColor('#FFFFFF');

    expect(emitted).toEqual([]);
  });
});
