import { signal } from "@angular/core";

export abstract class PagedListBase {
  readonly isPageChanging = signal<boolean>(false);

  startPageChanging(): void {
    this.isPageChanging.set(true);
  }

  endPageChanging(): void {
    this.isPageChanging.set(false);
  }

  abstract increasePage(): void;
  abstract decreasePage(): void;
}
