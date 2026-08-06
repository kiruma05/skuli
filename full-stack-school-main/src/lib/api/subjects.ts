import { apiGet, apiSend } from "@/lib/apiClient";
import { PageResponse, SubjectDto } from "@/lib/apiTypes";
import { ITEM_PER_PAGE } from "@/lib/settings";

/** Lists subjects for the current tenant, paginated (backend pages are 0-based). */
export async function listSubjects(params: {
  page: number;
  search?: string;
}): Promise<PageResponse<SubjectDto>> {
  const query = new URLSearchParams({
    page: String(params.page),
    size: String(ITEM_PER_PAGE),
  });
  if (params.search) {
    query.set("search", params.search);
  }
  return apiGet<PageResponse<SubjectDto>>(`/api/v1/subjects?${query.toString()}`);
}

export function createSubjectApi(name: string): Promise<Response> {
  return apiSend("POST", "/api/v1/subjects", { name });
}

export function updateSubjectApi(id: number, name: string): Promise<Response> {
  return apiSend("PUT", `/api/v1/subjects/${id}`, { id, name });
}

export function deleteSubjectApi(id: number): Promise<Response> {
  return apiSend("DELETE", `/api/v1/subjects/${id}`);
}
