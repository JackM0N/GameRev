import { Role } from "./role";

export interface WebsiteUser {
  id?: number;
  
  username?: string;
  password?: string;
  email?: string;

  nickname?: string;
  profilepic?: string;
  description?: string;
  joinDate?: number[];
  isBanned?: boolean;
  isDeleted?: boolean;
  lastActionDate?: string;

  roles?: Role[];

  picture?: string; // dynamically loaded, not always present
}
