import { Tag } from "./tag";

export interface Game {
  id?: number;

  title: string;
  developer: string;
  publisher: string;
  releaseDate?: any;
  releaseStatus?: string;
  description: string;
  tags: Tag[];
  usersScore: number;
}
