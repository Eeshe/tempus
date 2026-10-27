import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { Project } from '../models/project.model';
import { ProjectModal } from './project-modal';

describe('ProjectModal', () => {
  let component: ProjectModal;
  let fixture: ComponentFixture<ProjectModal>;

  const mockProject: Project = {
    id: 1,
    name: 'Tempus',
    userId: 1,
    isPrivate: false,
    isArchived: false,
    hourlyRate: 0,
    color: '#FFFFFF',
    tasks: [],
    client: { id: 1, name: 'Tempus', userId: 1, createdAt: '2026-01-01T00:00:00Z' },
    createdAt: '2026-01-01T00:00:00Z',
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ProjectModal],
      providers: [provideRouter([])],
    }).compileComponents();

    fixture = TestBed.createComponent(ProjectModal);
    fixture.componentRef.setInput('project', mockProject);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
