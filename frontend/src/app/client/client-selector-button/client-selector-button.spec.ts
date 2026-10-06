import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ClientSelectorButton } from './client-selector-button';

describe('ClientSelectorButton', () => {
  let component: ClientSelectorButton;
  let fixture: ComponentFixture<ClientSelectorButton>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ClientSelectorButton],
    }).compileComponents();

    fixture = TestBed.createComponent(ClientSelectorButton);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
