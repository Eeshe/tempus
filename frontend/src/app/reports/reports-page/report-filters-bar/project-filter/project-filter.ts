import { Component, computed, input, output } from '@angular/core';
import { Project } from '../../../../project/models/project.model';
import { ProjectPopupSelectorBase } from '../../../../project/project-popup-selector-base';

@Component({
  imports: [],
  selector: 'app-project-filter',
  styleUrl: './project-filter.css',
  templateUrl: './project-filter.html',
})
export class ProjectFilter extends ProjectPopupSelectorBase {
  readonly selectedProjects = input<Project[]>([]);

  readonly selectedProjectsChangeEvent = output<Project[]>();

  readonly allSelected = computed<boolean>(() => {
    const all: Project[] = this.projects();
    const selected: Project[] = this.selectedProjects();

    return all.length > 0 && all.length === selected.length;
  });

  isSelected(checkProject: Project): boolean {
    return this.selectedProjects().some((project) => project.id === checkProject.id);
  }

  toggleProject(projectToggle: Project): void {
    const current: Project[] = this.selectedProjects();
    const updated: Project[] = this.isSelected(projectToggle) ?
      current.filter((project) => project.id !== projectToggle.id)
      : [...current, projectToggle];

    this.selectedProjectsChangeEvent.emit(updated);
  }

  toggleSelectAll(): void {
    this.selectedProjectsChangeEvent.emit(this.allSelected() ? [] : [...this.projects()]);
  }
}
