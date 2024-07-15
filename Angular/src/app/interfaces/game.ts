import { ReleaseStatus } from "./releaseStatus";
import { Tag } from "./tag";

export interface Game {
  id?: number;

  title: string;
  developer: string;
  publisher: string;
  releaseDate?: any;
  releaseStatus?: ReleaseStatus;
  description: string;
  tags: Tag[];
}