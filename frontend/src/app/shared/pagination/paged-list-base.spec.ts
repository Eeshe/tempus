import { PagedListBase } from './paged-list-base';

class PagedListBaseStub extends PagedListBase {
  override updatePage(): void {
  }
}

describe('PagedListBase', () => {
  beforeEach(() => {
    vi.useFakeTimers();
  });

  afterEach(() => {
    vi.useRealTimers();
  });

  it('extends the page-changing flag when a new change is scheduled', () => {
    const list = new PagedListBaseStub();

    list.setPageChanging(1000);
    vi.advanceTimersByTime(500);
    expect(list.isPageChanging()).toBe(true);

    // A second change must cancel the first timer, otherwise it would clear the flag
    // mid-transition and the entering list would fall back to the slide animation.
    list.setPageChanging(1000);
    vi.advanceTimersByTime(500);
    expect(list.isPageChanging()).toBe(true);

    vi.advanceTimersByTime(500);
    expect(list.isPageChanging()).toBe(false);
  });
});
