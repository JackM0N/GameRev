import { Forum } from "./forum";
import { ForumComment } from "./forumComment";
import { WebsiteUser } from "./websiteUser";

export interface ForumPost {
  id: number,
  forum: Forum,
  author: WebsiteUser,
  title: string,
  content: string,
  postDate: [],
  picture: string,
  forumComments: ForumComment[]
}
