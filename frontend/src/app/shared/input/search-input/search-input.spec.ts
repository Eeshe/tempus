import { ComponentFixture, TestBed } from '@angular/core/testing';
import { SearchInput } from './search-input';

describe('SearchInput', () => {
  let component: SearchInput;
  let fixture: ComponentFixture<SearchInput>;
  let nativeInput: HTMLInputElement;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SearchInput],
    }).compileComponents();

    fixture = TestBed.createComponent(SearchInput);
    component = fixture.componentInstance;
    await fixture.whenStable();
    nativeInput = fixture.nativeElement.querySelector('input');
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('emits the typed text on input', () => {
    const emitted: string[] = [];
    component.searchChangeEvent.subscribe((value) => emitted.push(value));

    nativeInput.value = 'Foo';
    nativeInput.dispatchEvent(new Event('input'));

    expect(emitted).toEqual(['Foo']);
  });

  it('renders an externally provided value', async () => {
    fixture.componentRef.setInput('value', 'Bar');
    await fixture.whenStable();

    expect(nativeInput.value).toBe('Bar');
  });
});
