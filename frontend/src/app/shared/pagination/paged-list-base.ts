import { signal } from "@angular/core";

export abstract class PagedListBase {
  readonly currentPage = signal<number>(0);

  increasePage(): void {
    this.currentPage.update(page => page + 1);
    this.updatePage();
  }

  decreasePage(): void {
    this.currentPage.update(page => page - 1);
    this.updatePage();
  }

  abstract updatePage(): void;
}
