import { apiGet, apiSend } from "@/lib/apiClient";
import { PageResponse, SchoolClassDto } from "@/lib/apiTypes";
import { ITEM_PER_PAGE } from "@/lib/settings";

type ClassInput = {
  name: string;
  capacity: number;
  gradeId: number;
  supervisorId?: string | null;
};

/** Lists classes for the current tenant, paginated (backend pages are 0-based). */
export async function listClasses(params: {
  page: number;
  search?: string;
}): Promise<PageResponse<SchoolClassDto>> {
  const query = new URLSearchParams({
    page: String(params.page),
    size: String(ITEM_PER_PAGE),
  });
  if (params.search) {
    query.set("search", params.search);
  }
  return apiGet<PageResponse<SchoolClassDto>>(`/api/v1/classes?${query.toString()}`);
}

// An empty supervisor selection must be sent as null, not "", to satisfy the supervisor FK.
function normalize(input: ClassInput) {
  return { ...input, supervisorId: input.supervisorId || null };
}

export function createClassApi(input: ClassInput): Promise<Response> {
  return apiSend("POST", "/api/v1/classes", normalize(input));
}

export function updateClassApi(id: number, input: ClassInput): Promise<Response> {
  return apiSend("PUT", `/api/v1/classes/${id}`, { id, ...normalize(input) });
}

export function deleteClassApi(id: number): Promise<Response> {
  return apiSend("DELETE", `/api/v1/classes/${id}`);
}
