import { Game } from "./game";

export interface UserGame {
  id?: number;

  game?: Game;
  user?: { username?: string };
  completionStatus?: string;
  isFavourite: boolean;
}
