import { Component, computed, input, output } from '@angular/core';
import { SearchInput } from '../../shared/input/search-input/search-input';
import { Project } from '../models/project.model';
import { ProjectPopupSelectorBase } from '../project-popup-selector-base';

@Component({
  imports: [SearchInput],
  selector: 'app-project-selector-button',
  styleUrl: './project-selector-button.css',
  templateUrl: './project-selector-button.html',
})
export class ProjectSelectorButton extends ProjectPopupSelectorBase {
  readonly selectedProject = input<Project | null>();

  readonly filteredProjects = computed<Project[]>(() =>
    this.projects().filter((project) => this.matchesSearch(project.name) && !project.isArchived)
  );

  readonly projectSelectEvent = output<Project>();

  changeProject(project: Project): void {
    this.projectSelectEvent.emit(project);
    this.toggle();
  }
}
