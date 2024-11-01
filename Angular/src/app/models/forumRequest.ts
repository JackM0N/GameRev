import { Forum } from "./forum";
import { Game } from "./game";

export interface ForumRequest {
  id?: number,
  forumName: string,
  description: string,
  game: Game,
  parentForum: Forum,
  author: { nickname?: string },
  approved?: boolean,
}
