export interface Page<T = any> {
  content: T[],
  page: number,
  size: number,
  totalElements: number,
  totalPages: number,
  first: boolean,
  last: boolean,
}
