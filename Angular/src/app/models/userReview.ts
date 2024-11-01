
export interface UserReview {
  id?: number;

  gameTitle?: string;
  userNickname?: string;
  content?: string;
  postDate?: [number, number, number];
  score?: number;
  positiveRating?: number;
  negativeRating?: number;
  
  ownRatingIsPositive?: boolean | null;
}
