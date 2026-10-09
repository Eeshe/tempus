import { PagedListBase } from './paged-list-base';

class PagedListBaseStub extends PagedListBase {
  override increasePage(): void {
  }

  override decreasePage(): void {
  }
}

describe('PagedListBase', () => {
  it('tracks the page-changing flag', () => {
    const list = new PagedListBaseStub();

    expect(list.isPageChanging()).toBe(false);

    list.startPageChanging();
    expect(list.isPageChanging()).toBe(true);

    list.endPageChanging();
    expect(list.isPageChanging()).toBe(false);
  });
});
