import { LastPost } from "../models/lastPost";

export function parseTopPost(topPost: string): LastPost {
  // example: "2,Limbus Company,1,Update is finally here!,2024-08-13 10:16:00.0,Brazzoz Wheeler"

  // Split the string by commas
  const split = topPost.split(",");

  // Extract the values
  const newTopPost: LastPost = {
    forum_id: parseInt(split[0]),
    forum_name: split[1],
    forum_post_id: parseInt(split[2]),
    title: split[3],
    last_response_date: new Date(split[4]),
    nickname: split[5],
  };

  return newTopPost;
}
