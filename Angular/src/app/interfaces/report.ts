
export interface Report {
  id?: number;

  content: string;
  reviewId: number;
  userId?: number;
  approved?: boolean;
}
