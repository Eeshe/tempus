import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ClientFilter } from './client-filter';

describe('ClientFilter', () => {
  let component: ClientFilter;
  let fixture: ComponentFixture<ClientFilter>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ClientFilter],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();

    fixture = TestBed.createComponent(ClientFilter);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
