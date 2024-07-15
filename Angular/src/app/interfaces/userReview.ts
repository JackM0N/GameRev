import { Game } from "./game";
import { WebsiteUser } from "./websiteUser";

export interface UserReview {
  id?: number;

  game?: Game;
  user?: WebsiteUser;
  content: string;
  postDate: any;
  score?: number;
}
