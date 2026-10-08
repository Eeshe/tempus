import { signal } from "@angular/core";

export abstract class PagedListBase {
  readonly currentPage = signal<number>(0);
  readonly isPageChanging = signal<boolean>(false);

  private pageChangingTimeoutId: number | null = null;

  increasePage(): void {
    this.currentPage.update(page => page + 1);
    this.updatePage();
  }

  decreasePage(): void {
    this.currentPage.update(page => page - 1);
    this.updatePage();
  }

  setPageChanging(changeDurationMs: number): void {
    if (this.pageChangingTimeoutId != null) {
      clearTimeout(this.pageChangingTimeoutId);
    }

    this.isPageChanging.set(true);
    this.pageChangingTimeoutId = setTimeout(() => {
      this.isPageChanging.set(false);
      this.pageChangingTimeoutId = null;
    }, changeDurationMs);
  }

  abstract updatePage(): void;
}
