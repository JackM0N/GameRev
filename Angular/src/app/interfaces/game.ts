import { Tag } from "./tag";

export interface Game {
  game_id?: number;

  title: string;
  developer: string;
  publisher: string;
  release_date?: string;
  release_status: number;
  description: string;
  tags: Tag[];
}
