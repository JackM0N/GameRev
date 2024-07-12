import { Tag } from "./tag";

export interface Game {
  id?: number;

  title: string;
  developer: string;
  publisher: string;
  releaseDate?: string;
  releaseStatus: number;
  description: string;
  tags: Tag[];
}
