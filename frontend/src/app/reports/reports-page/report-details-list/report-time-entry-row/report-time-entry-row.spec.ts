import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReportTimeEntry } from '../../../models/report.model';
import { ReportTimeEntryRow } from './report-time-entry-row';

describe('ReportTimeEntryRow', () => {
  let component: ReportTimeEntryRow;
  let fixture: ComponentFixture<ReportTimeEntryRow>;

  const startTime: string = new Date(2026, 0, 2, 9, 0, 0).toISOString();
  const endTime: string = new Date(2026, 0, 2, 10, 0, 0).toISOString();
  const projectColor: string = '#12ab34';

  const mockTimeEntry: ReportTimeEntry = {
    id: 1,
    description: 'MyDescription',
    isBillable: true,
    taskName: 'MyTask',
    startTime,
    endTime,
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ReportTimeEntryRow],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();

    fixture = TestBed.createComponent(ReportTimeEntryRow);
    fixture.componentRef.setInput('timeEntry', mockTimeEntry);
    fixture.componentRef.setInput('projectColor', projectColor);
    fixture.componentRef.setInput('isArchived', false);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('renders the description and the task name separated by a colon', () => {
    expect(leftText()).toBe('MyDescription:MyTask');
  });

  it('colors the task name with the project color', () => {
    const task: HTMLElement = fixture.nativeElement.querySelector('.report-time-entry__task');

    expect(task.style.getPropertyValue('--project-color')).toBe(projectColor);
    expect(task.classList).not.toContain('report-time-entry__task--archived');
  });

  it('mutes the task name when the project is archived', async () => {
    fixture.componentRef.setInput('isArchived', true);
    await fixture.whenStable();

    expect(
      fixture.nativeElement
        .querySelector('.report-time-entry__task')
        ?.classList.contains('report-time-entry__task--archived'),
    ).toBe(true);
  });

  it('does not render the project name', () => {
    expect(fixture.nativeElement.querySelector('.report-time-entry__project')).toBeNull();
  });

  it('renders a muted placeholder when the time entry has no description', async () => {
    fixture.componentRef.setInput('timeEntry', { ...mockTimeEntry, description: null });
    await fixture.whenStable();

    const description: HTMLElement =
      fixture.nativeElement.querySelector('.report-time-entry__description');
    expect(description.textContent).toBe('No description');
    expect(description.classList).toContain('report-time-entry__description--empty');
  });

  it('renders the placeholder followed by the task name', async () => {
    fixture.componentRef.setInput('timeEntry', { ...mockTimeEntry, description: null });
    await fixture.whenStable();

    expect(leftText()).toBe('No description:MyTask');
  });

  it('renders the placeholder for a blank description', async () => {
    fixture.componentRef.setInput('timeEntry', { ...mockTimeEntry, description: '   ' });
    await fixture.whenStable();

    expect(
      fixture.nativeElement.querySelector('.report-time-entry__description')?.textContent,
    ).toBe('No description');
  });

  it('does not render a task when the time entry has no task', async () => {
    fixture.componentRef.setInput('timeEntry', { ...mockTimeEntry, taskName: null });
    await fixture.whenStable();

    expect(fixture.nativeElement.querySelector('.report-time-entry__task')).toBeNull();
  });

  it('renders only the description when the time entry has no task', async () => {
    fixture.componentRef.setInput('timeEntry', { ...mockTimeEntry, taskName: null });
    await fixture.whenStable();

    expect(leftText()).toBe('MyDescription');
  });

  it('renders only the placeholder when the time entry has neither description nor task', async () => {
    fixture.componentRef.setInput('timeEntry', {
      ...mockTimeEntry,
      description: null,
      taskName: null,
    });
    await fixture.whenStable();

    expect(leftText()).toBe('No description');
  });

  it('renders the date as yyyy-MM-dd and the times as HH:mm', () => {
    const element: HTMLElement = fixture.nativeElement;

    expect(element.querySelector('.report-time-entry__date')?.textContent).toBe('2026-01-02');
    expect(element.querySelector('.report-time-entry__range')?.textContent).toContain('09:00');
    expect(element.querySelector('.report-time-entry__range')?.textContent).toContain('10:00');
  });

  it('renders the duration in hh:mm:ss format', () => {
    expect(fixture.nativeElement.querySelector('.report-time-entry__duration')?.textContent).toBe(
      '01:00:00',
    );
  });

  it('renders the currency symbol as the billable marker', () => {
    const billable: HTMLElement = fixture.nativeElement.querySelector('.report-time-entry__billable');

    expect(billable.textContent).toBe('$');
    expect(billable.classList).toContain('report-time-entry__billable--yes');
  });

  it('renders the non billable marker without the billable style', async () => {
    fixture.componentRef.setInput('timeEntry', { ...mockTimeEntry, isBillable: false });
    await fixture.whenStable();

    const billable: HTMLElement = fixture.nativeElement.querySelector('.report-time-entry__billable');
    expect(billable.classList).not.toContain('report-time-entry__billable--yes');
  });

  function leftText(): string {
    return fixture.nativeElement.querySelector('.report-time-entry__left').textContent.trim();
  }
});
