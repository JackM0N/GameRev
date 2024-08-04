import { UserReview } from "./userReview";

export interface Report {
  id?: number;

  content: string;
  userReview: UserReview;
  userId?: number;
  approved?: boolean;
}
