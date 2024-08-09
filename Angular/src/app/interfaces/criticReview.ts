import { WebsiteUser } from "./websiteUser";

export interface CriticReview {
  id?: number;

  gameTitle?: string;
  userUsername?: string;

  content?: string;
  postDate?: any;
  score?: number;

  user?: WebsiteUser;

  reviewStatus?: string;
  approvedBy?: WebsiteUser;
}
