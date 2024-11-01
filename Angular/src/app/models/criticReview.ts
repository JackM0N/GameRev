import { WebsiteUser } from "./websiteUser";

export interface CriticReview {
  id?: number;

  gameTitle?: string;
  userNickname?: string;

  content?: string;
  postDate?: [number, number, number];
  score?: number;

  user?: WebsiteUser;

  reviewStatus?: string;
  statusChangedBy?: WebsiteUser;
}
