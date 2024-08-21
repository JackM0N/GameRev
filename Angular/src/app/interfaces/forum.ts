import { ForumModerator } from "./forumModerator";
import { ForumPost } from "./forumPost";
import { Game } from "./game";

export interface Forum {
  id: number,
  game: Game,
  forumName: string,
  isDeleted: boolean,
  parentForumId: number,
  forumModerators: ForumModerator[],
  forumPosts: ForumPost[],
  postCount: number,
  description: string,
}
