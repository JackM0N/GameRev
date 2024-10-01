import { Game } from "./game";
import { WebsiteUser } from "./websiteUser";

export interface UserGame {
  id?: number;

  game?: Game;
  user?: WebsiteUser;
  completionStatus?: string;
  isFavourite: boolean;
}
