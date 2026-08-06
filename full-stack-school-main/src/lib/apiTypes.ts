// Shapes returned by the Spring backend (/api/v1/**). Kept minimal and hand-written so the Next app
// no longer depends on @prisma/client types as it is cut over resource by resource.

/** RFC-friendly pagination envelope returned by list endpoints (mirrors PageResponse<T>). */
export type PageResponse<T> = {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
};

export type SubjectDto = {
  id: number;
  name: string;
};

export type SchoolClassDto = {
  id: number;
  name: string;
  capacity: number;
  supervisorId: string | null;
  gradeId: number;
};
