
export interface NewCredentials {
  username: string;
  currentPassword: string;

  newPassword?: string;
  email?: string;
  nickname?: string;
  profilepic?: string;
  description?: string;
}
