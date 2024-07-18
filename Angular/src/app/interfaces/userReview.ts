
export interface UserReview {
  id?: number;

  gameTitle?: string;
  userUsername?: string;
  content: string;
  postDate: any;
  score?: number;
  positiveRating?: number;
  negativeRating?: number;
  
  ownRatingIsPositive?: boolean | null;
  token?: string | null;
}
