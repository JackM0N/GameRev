
export interface UserReview {
  id?: number;

  gameTitle?: string;
  userUsername?: string;
  content: string;
  postDate: any;
  score?: number;
  positiveRating?: number;
  negativeRating?: number;
  
  isPositiveRating?: boolean | null;
  token?: string | null;
}
