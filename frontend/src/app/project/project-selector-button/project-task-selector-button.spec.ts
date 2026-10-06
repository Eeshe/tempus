import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ProjectTaskSelectorButton } from './project-task-selector-button';

describe('ProjectTaskSelectorButton', () => {
  let component: ProjectTaskSelectorButton;
  let fixture: ComponentFixture<ProjectTaskSelectorButton>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ProjectTaskSelectorButton],
    }).compileComponents();

    fixture = TestBed.createComponent(ProjectTaskSelectorButton);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
