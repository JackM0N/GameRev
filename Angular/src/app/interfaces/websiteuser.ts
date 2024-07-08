
// User model
export interface WebsiteUser {
  user_id?: number;
  
  username: string;
  password: string;
  email: string;

  profilepic?: string;
  nickname?: string;
  description?: string;
  join_date?: string;
  is_banned?: boolean;
  is_deleted?: boolean;
}
