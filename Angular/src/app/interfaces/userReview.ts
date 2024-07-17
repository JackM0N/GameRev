
export interface UserReview {
  id?: number;

  gameTitle?: string;
  userUsername?: string;
  content: string;
  postDate: any;
  score?: number;
  positiveRating?: number;
  negativeRating?: number;
  
  userLiked?: boolean | null;
  token?: string | null;
}
