import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ProjectFilter } from './project-filter';

describe('ProjectFilter', () => {
  let component: ProjectFilter;
  let fixture: ComponentFixture<ProjectFilter>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ProjectFilter],
    }).compileComponents();

    fixture = TestBed.createComponent(ProjectFilter);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
