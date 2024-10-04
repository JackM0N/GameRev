
export interface UserReview {
  id?: number;

  gameTitle?: string;
  userNickname?: string;
  content?: string;
  postDate?: any;
  score?: number;
  positiveRating?: number;
  negativeRating?: number;
  
  ownRatingIsPositive?: boolean | null;
}