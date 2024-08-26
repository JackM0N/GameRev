import { ForumPost } from "./forumPost";
import { WebsiteUser } from "./websiteUser";

export interface ForumComment {
  id: number,
  forumPost: ForumPost,
  author: WebsiteUser,
  content: string,
  postDate: Date,
  isDeleted?: boolean
}
