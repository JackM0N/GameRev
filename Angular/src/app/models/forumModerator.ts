import { Forum } from "./forum";
import { WebsiteUser } from "./websiteUser";

export interface ForumModerator {
  id: number,
  forum: Forum,
  moderator: WebsiteUser
}
