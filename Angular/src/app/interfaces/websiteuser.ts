
// User model
export interface WebsiteUser {
  id?: number;
  
  username?: string;
  password?: string;
  email?: string;

  nickname?: string;
  profilepic?: string;
  description?: string;
  joinDate?: string;
  isBanned?: boolean;
  isDeleted?: boolean;
  lastActionDate?: string;
}
