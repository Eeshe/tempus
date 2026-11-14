import { Component, computed, input, output } from '@angular/core';
import { Project } from '../../../../project/models/project.model';
import { ProjectPopupSelectorBase } from '../../../../project/project-popup-selector-base';
import { SearchInput } from '../../../../shared/input/search-input/search-input';

@Component({
  imports: [SearchInput],
  selector: 'app-project-filter',
  styleUrl: './project-filter.css',
  templateUrl: './project-filter.html',
})
export class ProjectFilter extends ProjectPopupSelectorBase {
  readonly selectedProjects = input<Project[]>([]);

  readonly selectedProjectsChangeEvent = output<Project[]>();

  readonly matchingProjects = computed<Project[]>(() =>
    this.projects().filter((project) => this.matchesSearch(project.name))
  );

  readonly filteredProjects = computed<Project[]>(() =>
    this.sortSelectedFirst(
      this.projects().filter(
        (project) => this.isSelected(project) || this.matchesSearch(project.name)
      ),
      (project) => this.isSelected(project)
    )
  );

  readonly allSelected = computed<boolean>(() => {
    const matches: Project[] = this.matchingProjects();

    return matches.length > 0 && matches.every((project) => this.isSelected(project));
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
    this.selectedProjectsChangeEvent.emit(this.allSelected() ? [] : [...this.matchingProjects()]);
  }
}
