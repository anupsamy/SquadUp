import { HOBBIES } from '../hobbies';

export interface GetAllHobbiesResponse {
  message: string;
  data?: {
    hobbies: typeof HOBBIES;
  };
}
