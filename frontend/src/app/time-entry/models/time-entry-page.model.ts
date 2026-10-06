import { Page } from "../../shared/models/page.model";
import { TimeEntry } from "./time-entry.model";

export interface TimeEntryPage extends Page<TimeEntry> {
  currentCursor: string,
  previousCursor: string | null,
  nextCursor: string | null,
}

export function createEmptyTimeEntryPage(): TimeEntryPage {
  return {
    content: [],
    currentCursor: "",
    previousCursor: "",
    nextCursor: "",
    page: 0,
    size: 0,
    totalElements: 0,
    totalPages: 0,
    first: true,
    last: true
  };
}
