import { apiGet } from "@/lib/apiClient";
import { GradeDto, PageResponse } from "@/lib/apiTypes";

/**
 * All grade levels for the current tenant, used to populate the grade dropdown in the class and
 * student forms. There are only a handful, so a single large page is fetched.
 */
export async function listGrades(): Promise<GradeDto[]> {
  const data = await apiGet<PageResponse<GradeDto>>("/api/v1/grades?page=0&size=100");
  return data.content;
}
