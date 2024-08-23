import { ForumModerator } from "./forumModerator";
import { ForumPost } from "./forumPost";
import { Game } from "./game";
import { LastPost } from "./lastPost";

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

  topPost: string,
  lastPost?: LastPost,
}
