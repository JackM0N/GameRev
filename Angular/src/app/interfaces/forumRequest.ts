import { Forum } from "./forum";
import { Game } from "./game";
import { WebsiteUser } from "./websiteUser";

export interface ForumRequest {
  id?: number,
  forumName: string,
  description: string,
  game: Game,
  parentForum: Forum,
  author: WebsiteUser,
  approved?: boolean,
}
