import { ComponentFixture, TestBed } from '@angular/core/testing';
import { TaskFilter } from './task-filter';

describe('TaskFilter', () => {
  let component: TaskFilter;
  let fixture: ComponentFixture<TaskFilter>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TaskFilter],
    }).compileComponents();

    fixture = TestBed.createComponent(TaskFilter);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
