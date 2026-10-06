import { Component, computed, input, output, signal } from '@angular/core';
import { SearchInput } from '../../shared/input/search-input/search-input';
import { CreateTaskFormModal } from '../../task/create-task-form-modal/create-task-form-modal';
import { Task } from '../../task/models/task.model';
import { CreateProjectFormModal } from '../create-project-form-modal/create-project-form-modal';
import { Project } from '../models/project.model';
import { ProjectPopupSelectorBase } from '../project-popup-selector-base';

@Component({
  imports: [CreateProjectFormModal, CreateTaskFormModal, SearchInput],
  selector: 'app-project-task-selector-button',
  styleUrl: './project-task-selector-button.css',
  templateUrl: './project-task-selector-button.html',
})
export class ProjectTaskSelectorButton extends ProjectPopupSelectorBase {
  readonly selectedProject = input<Project | null>();
  readonly selectedTask = input<Task | null>();
  readonly displayText = computed<string>(() => {
    if (this.selectedProject() == null) {
      return "Select a Project";
    }
    if (this.selectedTask() == null) {
      return this.selectedProject()!.name;
    }
    return `${this.selectedProject()!.name}:${this.selectedTask()!.name}`;
  });

  readonly filteredProjects = computed<Project[]>(() =>
    this.projects().filter((project) => this.matchesSearch(project.name) && !project.isArchived)
  );

  readonly isCreateProjectFormModalOpen = signal<boolean>(false);
  readonly createTaskFormModalProject = signal<Project | null>(null);
  readonly expandedProjectIds = signal<Set<number>>(new Set());

  readonly projectSelectEvent = output<Project>();
  readonly taskSelectEvent = output<{ newProject: Project, newTask: Task }>();

  changeTimeEntryProject(clickedProject: Project): void {
    this.projectSelectEvent.emit(clickedProject);
    this.toggle();
  }

  changeTimeEntryTask(project: Project, clickedTask: Task): void {
    this.taskSelectEvent.emit({ newProject: project, newTask: clickedTask });
    this.toggle();
  }

  addCreatedProject(newProject: Project): void {
    this.projects.update(projects => [...projects, newProject]);
    this.projectSelectEvent.emit(newProject);
    this.toggle();
  }

  addCreatedTask(project: Project, task: Task): void {
    this.taskSelectEvent.emit({ newProject: project, newTask: task });
    this.toggle();
  }

  toggleCreateProjectForm(): void {
    this.isCreateProjectFormModalOpen.update(value => !value);
  }

  setCreateTaskFormModalProject(project: Project | null): void {
    this.createTaskFormModalProject.set(project);
  }

  isProjectExpanded(project: Project): boolean {
    return this.expandedProjectIds().has(project.id);
  }

  toggleProjectTasks(project: Project): void {
    this.expandedProjectIds.update(ids => {
      const next = new Set(ids);
      if (next.has(project.id)) {
        next.delete(project.id);
      } else {
        next.add(project.id);
      }
      return next;
    });
  }
}
