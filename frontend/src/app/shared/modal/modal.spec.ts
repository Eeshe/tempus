import { Component, input } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { AppModal } from './modal';

@Component({
  imports: [AppModal],
  template: `
    <app-modal [header]="header()" [size]="size()" (closeEvent)="closeCount = closeCount + 1">
      <div class="host-content">Projected body</div>
    </app-modal>
  `,
})
class ModalTestHost {
  readonly header = input('Test dialog');
  readonly size = input<'sm' | 'md' | 'lg'>('md');
  closeCount = 0;
}

describe('AppModal', () => {
  let fixture: ComponentFixture<ModalTestHost>;
  let host: ModalTestHost;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ModalTestHost],
    }).compileComponents();

    fixture = TestBed.createComponent(ModalTestHost);
    host = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('renders header and projected content', () => {
    const title = fixture.nativeElement.querySelector('.modal__title') as HTMLElement;
    const body = fixture.nativeElement.querySelector('.host-content') as HTMLElement;
    expect(title.textContent).toEqual('Test dialog');
    expect(body.textContent).toEqual('Projected body');
  });

  it('does not emit a native title attribute on the host', () => {
    const modal = fixture.nativeElement.querySelector('app-modal') as HTMLElement;
    expect(modal.hasAttribute('title')).toBe(false);
  });

  it('emits closeEvent when backdrop is clicked', () => {
    const backdrop = fixture.nativeElement.querySelector('.modal__backdrop') as HTMLElement;
    backdrop.click();
    expect(host.closeCount).toBe(1);
  });

  it('emits closeEvent when close button is clicked', () => {
    const closeButton = fixture.nativeElement.querySelector('.modal__close') as HTMLButtonElement;
    closeButton.click();
    expect(host.closeCount).toBe(1);
  });

  it('emits closeEvent on Escape keydown', () => {
    document.dispatchEvent(new KeyboardEvent('keydown', { key: 'Escape' }));
    expect(host.closeCount).toBe(1);
  });

  it('does not emit closeEvent when card itself is clicked', () => {
    const card = fixture.nativeElement.querySelector('.modal__card') as HTMLElement;
    card.click();
    expect(host.closeCount).toBe(0);
  });

  it('applies size class to card', () => {
    fixture.componentRef.setInput('size', 'sm');
    fixture.detectChanges();
    const card = fixture.nativeElement.querySelector('.modal__card') as HTMLElement;
    expect(card.classList.contains('modal__card--sm')).toBe(true);
  });
});
