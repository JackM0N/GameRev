--liquibase formatted sql

--changeset Stanislaw:4 labels:data,roles,users
--roles
INSERT INTO role (role_name) VALUES ('Admin');
INSERT INTO role (role_name) VALUES ('Critic');
INSERT INTO role (role_name) VALUES ('User');

--users
INSERT INTO website_user (username, password, profilepic, nickname, email, last_action_date, description, join_date, is_banned, is_deleted)
VALUES
    ('testadmin', '$2a$10$o9OcOIN.1bmooaFQJ.1PAOyZxHBA9IXo8qj2dECXrj8Vk9YpvxstO', null, 'testadmin', 'testadmin@gamerev.com', '2024-07-09 12:34:56', 'DELETE ME AFTER TESTING.', '2023-01-15', FALSE, FALSE),
    ('testcritic', '$2a$10$o9OcOIN.1bmooaFQJ.1PAOyZxHBA9IXo8qj2dECXrj8Vk9YpvxstO', null, 'testcritic', 'testcritic@gamerev.com', '2024-07-09 12:34:56', 'DELETE ME AFTER TESTING.', '2023-01-15', FALSE, FALSE),
    ('testuser', '$2a$10$o9OcOIN.1bmooaFQJ.1PAOyZxHBA9IXo8qj2dECXrj8Vk9YpvxstO', null, 'testuser', 'testuser@gamerev.com', '2024-07-09 12:34:56', 'DELETE ME AFTER TESTING.', '2023-01-15', FALSE, FALSE),
    ('brazwheel', '$2a$10$o9OcOIN.1bmooaFQJ.1PAOyZxHBA9IXo8qj2dECXrj8Vk9YpvxstO', null, 'Brazzoz Wheeler', 'brazzozw@gmail.com', '2024-07-09 12:34:56', 'Avid gamer and reviewer.', '2023-01-15', FALSE, FALSE),
    ('jane_smith', '$2a$10$o9OcOIN.1bmooaFQJ.1PAOyZxHBA9IXo8qj2dECXrj8Vk9YpvxstO', null, 'Jane', 'jane.smith@gmail.com', '2024-07-09 14:23:45', null, '2023-03-22', FALSE, FALSE),
    ('gamer_guy', '$2a$10$o9OcOIN.1bmooaFQJ.1PAOyZxHBA9IXo8qj2dECXrj8Vk9YpvxstO', null, 'GamerGuy', 'gamer.guy@gmail.com', '2024-07-08 16:12:34', 'Streaming enthusiast and competitive player.', '2023-05-10', FALSE, FALSE),
    ('amelia_flais', '$2a$10$o9OcOIN.1bmooaFQJ.1PAOyZxHBA9IXo8qj2dECXrj8Vk9YpvxstO', null, 'Amelia Engie Flais', 'aflais@gamerev.com', '2024-07-07 18:01:23', 'Professional media critic since 2011.', '2023-07-01', FALSE, FALSE),
    ('charles_zillioner', '$2a$10$o9OcOIN.1bmooaFQJ.1PAOyZxHBA9IXo8qj2dECXrj8Vk9YpvxstO', null, 'Charles Charlzie Zillioner', 'czill@gamerev.com', '2024-07-06 20:50:12', null, '2023-09-14', FALSE, FALSE);

--assigning roles
INSERT INTO user_role (user_id, role_id) VALUES (1, 1);
INSERT INTO user_role (user_id, role_id) VALUES (2, 2);
INSERT INTO user_role (user_id, role_id) VALUES (3, 3);
INSERT INTO user_role (user_id, role_id) VALUES (4, 3);
INSERT INTO user_role (user_id, role_id) VALUES (5, 3);
INSERT INTO user_role (user_id, role_id) VALUES (6, 3);
INSERT INTO user_role (user_id, role_id) VALUES (7, 2);
INSERT INTO user_role (user_id, role_id) VALUES (8, 2);



--changeset Stanislaw:5 labels:data,dictionaries
--release_status
INSERT INTO release_status (status_name) VALUES ('Released');
INSERT INTO release_status (status_name) VALUES ('Early Access');
INSERT INTO release_status (status_name) VALUES ('Announced');
INSERT INTO release_status (status_name) VALUES ('Canceled');
INSERT INTO release_status (status_name) VALUES ('Closed');

--platform
INSERT INTO platform (platform_name) VALUES ('PC');
INSERT INTO platform (platform_name) VALUES ('Playstation 5');
INSERT INTO platform (platform_name) VALUES ('Playstation 4');
INSERT INTO platform (platform_name) VALUES ('XBOX One');
INSERT INTO platform (platform_name) VALUES ('XBOX series X');
INSERT INTO platform (platform_name) VALUES ('Android');
INSERT INTO platform (platform_name) VALUES ('IOS');
INSERT INTO platform (platform_name) VALUES ('Switch');

--tag
--1000: kategorie ilości graczy, gdzie kolejność ma znaczenie
--100: normalne kategorie gdzie kolejność nie ma znaczenia
--10: jakieś mniejsze kategorie
INSERT INTO tag (tag_name, priority) VALUES ('Singleplayer', 1000);
INSERT INTO tag (tag_name, priority) VALUES ('Multiplayer', 999);
INSERT INTO tag (tag_name, priority) VALUES ('MMO', 998);
INSERT INTO tag (tag_name, priority) VALUES ('Co-op', 997);
INSERT INTO tag (tag_name, priority) VALUES ('LAN', 996);

INSERT INTO tag (tag_name, priority) VALUES ('Action', 100);
INSERT INTO tag (tag_name, priority) VALUES ('Adventure', 100);
INSERT INTO tag (tag_name, priority) VALUES ('Shooter', 100);
INSERT INTO tag (tag_name, priority) VALUES ('RPG', 100);
INSERT INTO tag (tag_name, priority) VALUES ('Simulation', 100);
INSERT INTO tag (tag_name, priority) VALUES ('Visual Novel', 100);
INSERT INTO tag (tag_name, priority) VALUES ('Strategy', 100);
INSERT INTO tag (tag_name, priority) VALUES ('Platformer', 100);
INSERT INTO tag (tag_name, priority) VALUES ('Battle Royale', 100);

INSERT INTO tag (tag_name, priority) VALUES ('Story-Rich', 10);
INSERT INTO tag (tag_name, priority) VALUES ('Turn-based Strategy', 10);
INSERT INTO tag (tag_name, priority) VALUES ('Retro', 10);
INSERT INTO tag (tag_name, priority) VALUES ('Magic', 10);
INSERT INTO tag (tag_name, priority) VALUES ('Great Soundtrack', 10);
INSERT INTO tag (tag_name, priority) VALUES ('Psychological Horror', 10);
INSERT INTO tag (tag_name, priority) VALUES ('2D', 10);
INSERT INTO tag (tag_name, priority) VALUES ('3D', 10);
INSERT INTO tag (tag_name, priority) VALUES ('Indie', 10);
INSERT INTO tag (tag_name, priority) VALUES ('Open World', 10);
INSERT INTO tag (tag_name, priority) VALUES ('Fantasy', 10);
INSERT INTO tag (tag_name, priority) VALUES ('Sci-Fi', 10);
INSERT INTO tag (tag_name, priority) VALUES ('Hero Shooter', 10);
INSERT INTO tag (tag_name, priority) VALUES ('Team-Based', 10);


--completion_status
INSERT INTO completion_status (completion_name) VALUES ('Completed');
INSERT INTO completion_status (completion_name) VALUES ('In-progress');
INSERT INTO completion_status (completion_name) VALUES ('On-hold');
INSERT INTO completion_status (completion_name) VALUES ('Planning');
INSERT INTO completion_status (completion_name) VALUES ('Dropped');

--review_status
INSERT INTO review_status (status_name) VALUES ('Approved');
INSERT INTO review_status (status_name) VALUES ('Pending');
INSERT INTO review_status (status_name) VALUES ('Deleted');
INSERT INTO review_status (status_name) VALUES ('Edited');



--changeset Stanislaw:6 labels:data,game
--game
INSERT INTO game (title, developer, publisher, release_date, release_status, description)
VALUES
    ('Limbus Company', 'ProjectMoon', 'ProjectMoon', '2023-02-27', 1, 'As the Executive Manager of Limbus Company, lead your group of twelve Sinners, venture into the buried facilities of Lobotomy Corporation, and lay claim on the Golden Boughs.'),
    ('Celeste', 'Maddy Makes Games Inc.', 'Maddy Makes Games Inc.', '2018-01-25', 1, 'Help Madeline survive her inner demons on her journey to the top of Celeste Mountain, in this super-tight platformer from the creators of TowerFall. Brave hundreds of hand-crafted challenges, uncover devious secrets, and piece together the mystery of the mountain.'),
    ('Elden Ring', 'FromSoftware Inc.', 'Bandai Namco Entertainment', '2022-02-25', 1, 'THE NEW FANTASY ACTION RPG. Rise, Tarnished, and be guided by grace to brandish the power of the Elden Ring and become an Elden Lord in the Lands Between.'),
    ('Apex Legends', 'Respawn', 'Electronic Arts', '2020-11-05', 1, 'Apex Legends is the award-winning, free-to-play Hero Shooter from Respawn Entertainment. Master an ever-growing roster of legendary characters with powerful abilities, and experience strategic squad play and innovative gameplay in the next evolution of Hero Shooter and Battle Royale.');

--game_tag_type
INSERT INTO game_tag (game_id, tag_id) VALUES ((SELECT game_id FROM game WHERE title = 'Limbus Company'), (SELECT tag_id FROM tag WHERE tag_name = 'Singleplayer'));

INSERT INTO game_tag (game_id, tag_id) VALUES ((SELECT game_id FROM game WHERE title = 'Celeste'), (SELECT tag_id FROM tag WHERE tag_name = 'Singleplayer'));

INSERT INTO game_tag (game_id, tag_id) VALUES ((SELECT game_id FROM game WHERE title = 'Elden Ring'), (SELECT tag_id FROM tag WHERE tag_name = 'Singleplayer'));
INSERT INTO game_tag (game_id, tag_id) VALUES ((SELECT game_id FROM game WHERE title = 'Elden Ring'), (SELECT tag_id FROM tag WHERE tag_name = 'Multiplayer'));
INSERT INTO game_tag (game_id, tag_id) VALUES ((SELECT game_id FROM game WHERE title = 'Elden Ring'), (SELECT tag_id FROM tag WHERE tag_name = 'Co-op'));

INSERT INTO game_tag (game_id, tag_id) VALUES ((SELECT game_id FROM game WHERE title = 'Apex Legends'), (SELECT tag_id FROM tag WHERE tag_name = 'Multiplayer'));
INSERT INTO game_tag (game_id, tag_id) VALUES ((SELECT game_id FROM game WHERE title = 'Apex Legends'), (SELECT tag_id FROM tag WHERE tag_name = 'Co-op'));

--game_tag
INSERT INTO game_tag (game_id, tag_id) VALUES ((SELECT game_id FROM game WHERE title = 'Limbus Company'), (SELECT tag_id FROM tag WHERE tag_name = 'RPG'));
INSERT INTO game_tag (game_id, tag_id) VALUES ((SELECT game_id FROM game WHERE title = 'Limbus Company'), (SELECT tag_id FROM tag WHERE tag_name = 'Strategy'));
INSERT INTO game_tag (game_id, tag_id) VALUES ((SELECT game_id FROM game WHERE title = 'Limbus Company'), (SELECT tag_id FROM tag WHERE tag_name = 'Story-Rich'));
INSERT INTO game_tag (game_id, tag_id) VALUES ((SELECT game_id FROM game WHERE title = 'Limbus Company'), (SELECT tag_id FROM tag WHERE tag_name = 'Great Soundtrack'));
INSERT INTO game_tag (game_id, tag_id) VALUES ((SELECT game_id FROM game WHERE title = 'Limbus Company'), (SELECT tag_id FROM tag WHERE tag_name = 'Turn-based Strategy'));

INSERT INTO game_tag (game_id, tag_id) VALUES ((SELECT game_id FROM game WHERE title = 'Celeste'), (SELECT tag_id FROM tag WHERE tag_name = 'Action'));
INSERT INTO game_tag (game_id, tag_id) VALUES ((SELECT game_id FROM game WHERE title = 'Celeste'), (SELECT tag_id FROM tag WHERE tag_name = 'Adventure'));
INSERT INTO game_tag (game_id, tag_id) VALUES ((SELECT game_id FROM game WHERE title = 'Celeste'), (SELECT tag_id FROM tag WHERE tag_name = 'Platformer'));
INSERT INTO game_tag (game_id, tag_id) VALUES ((SELECT game_id FROM game WHERE title = 'Celeste'), (SELECT tag_id FROM tag WHERE tag_name = 'Indie'));
INSERT INTO game_tag (game_id, tag_id) VALUES ((SELECT game_id FROM game WHERE title = 'Celeste'), (SELECT tag_id FROM tag WHERE tag_name = 'Story-Rich'));
INSERT INTO game_tag (game_id, tag_id) VALUES ((SELECT game_id FROM game WHERE title = 'Celeste'), (SELECT tag_id FROM tag WHERE tag_name = '2D'));

INSERT INTO game_tag (game_id, tag_id) VALUES ((SELECT game_id FROM game WHERE title = 'Elden Ring'), (SELECT tag_id FROM tag WHERE tag_name = 'Action'));
INSERT INTO game_tag (game_id, tag_id) VALUES ((SELECT game_id FROM game WHERE title = 'Elden Ring'), (SELECT tag_id FROM tag WHERE tag_name = 'RPG'));
INSERT INTO game_tag (game_id, tag_id) VALUES ((SELECT game_id FROM game WHERE title = 'Elden Ring'), (SELECT tag_id FROM tag WHERE tag_name = 'Open World'));
INSERT INTO game_tag (game_id, tag_id) VALUES ((SELECT game_id FROM game WHERE title = 'Elden Ring'), (SELECT tag_id FROM tag WHERE tag_name = 'Fantasy'));
INSERT INTO game_tag (game_id, tag_id) VALUES ((SELECT game_id FROM game WHERE title = 'Elden Ring'), (SELECT tag_id FROM tag WHERE tag_name = '3D'));

INSERT INTO game_tag (game_id, tag_id) VALUES ((SELECT game_id FROM game WHERE title = 'Apex Legends'), (SELECT tag_id FROM tag WHERE tag_name = 'Shooter'));
INSERT INTO game_tag (game_id, tag_id) VALUES ((SELECT game_id FROM game WHERE title = 'Apex Legends'), (SELECT tag_id FROM tag WHERE tag_name = 'Battle Royale'));
INSERT INTO game_tag (game_id, tag_id) VALUES ((SELECT game_id FROM game WHERE title = 'Apex Legends'), (SELECT tag_id FROM tag WHERE tag_name = 'Hero Shooter'));
INSERT INTO game_tag (game_id, tag_id) VALUES ((SELECT game_id FROM game WHERE title = 'Apex Legends'), (SELECT tag_id FROM tag WHERE tag_name = 'Action'));
INSERT INTO game_tag (game_id, tag_id) VALUES ((SELECT game_id FROM game WHERE title = 'Apex Legends'), (SELECT tag_id FROM tag WHERE tag_name = 'Sci-Fi'));
INSERT INTO game_tag (game_id, tag_id) VALUES ((SELECT game_id FROM game WHERE title = 'Apex Legends'), (SELECT tag_id FROM tag WHERE tag_name = 'Team-Based'));

--game_platform
INSERT INTO game_platform (game_id, platform_id) VALUES ((SELECT game_id FROM game WHERE title = 'Limbus Company'), (SELECT platform_id FROM platform WHERE platform_name = 'PC'));
INSERT INTO game_platform (game_id, platform_id) VALUES ((SELECT game_id FROM game WHERE title = 'Limbus Company'), (SELECT platform_id FROM platform WHERE platform_name = 'Android'));
INSERT INTO game_platform (game_id, platform_id) VALUES ((SELECT game_id FROM game WHERE title = 'Limbus Company'), (SELECT platform_id FROM platform WHERE platform_name = 'IOS'));

INSERT INTO game_platform (game_id, platform_id) VALUES ((SELECT game_id FROM game WHERE title = 'Celeste'), (SELECT platform_id FROM platform WHERE platform_name = 'PC'));
INSERT INTO game_platform (game_id, platform_id) VALUES ((SELECT game_id FROM game WHERE title = 'Celeste'), (SELECT platform_id FROM platform WHERE platform_name = 'Playstation 5'));
INSERT INTO game_platform (game_id, platform_id) VALUES ((SELECT game_id FROM game WHERE title = 'Celeste'), (SELECT platform_id FROM platform WHERE platform_name = 'Playstation 4'));
INSERT INTO game_platform (game_id, platform_id) VALUES ((SELECT game_id FROM game WHERE title = 'Celeste'), (SELECT platform_id FROM platform WHERE platform_name = 'XBOX One'));
INSERT INTO game_platform (game_id, platform_id) VALUES ((SELECT game_id FROM game WHERE title = 'Celeste'), (SELECT platform_id FROM platform WHERE platform_name = 'XBOX series X'));
INSERT INTO game_platform (game_id, platform_id) VALUES ((SELECT game_id FROM game WHERE title = 'Celeste'), (SELECT platform_id FROM platform WHERE platform_name = 'Switch'));

INSERT INTO game_platform (game_id, platform_id) VALUES ((SELECT game_id FROM game WHERE title = 'Elden Ring'), (SELECT platform_id FROM platform WHERE platform_name = 'PC'));
INSERT INTO game_platform (game_id, platform_id) VALUES ((SELECT game_id FROM game WHERE title = 'Elden Ring'), (SELECT platform_id FROM platform WHERE platform_name = 'Playstation 5'));
INSERT INTO game_platform (game_id, platform_id) VALUES ((SELECT game_id FROM game WHERE title = 'Elden Ring'), (SELECT platform_id FROM platform WHERE platform_name = 'Playstation 4'));
INSERT INTO game_platform (game_id, platform_id) VALUES ((SELECT game_id FROM game WHERE title = 'Elden Ring'), (SELECT platform_id FROM platform WHERE platform_name = 'XBOX One'));
INSERT INTO game_platform (game_id, platform_id) VALUES ((SELECT game_id FROM game WHERE title = 'Elden Ring'), (SELECT platform_id FROM platform WHERE platform_name = 'XBOX series X'));

INSERT INTO game_platform (game_id, platform_id) VALUES ((SELECT game_id FROM game WHERE title = 'Apex Legends'), (SELECT platform_id FROM platform WHERE platform_name = 'PC'));
INSERT INTO game_platform (game_id, platform_id) VALUES ((SELECT game_id FROM game WHERE title = 'Apex Legends'), (SELECT platform_id FROM platform WHERE platform_name = 'Playstation 5'));
INSERT INTO game_platform (game_id, platform_id) VALUES ((SELECT game_id FROM game WHERE title = 'Apex Legends'), (SELECT platform_id FROM platform WHERE platform_name = 'Playstation 4'));
INSERT INTO game_platform (game_id, platform_id) VALUES ((SELECT game_id FROM game WHERE title = 'Apex Legends'), (SELECT platform_id FROM platform WHERE platform_name = 'XBOX One'));
INSERT INTO game_platform (game_id, platform_id) VALUES ((SELECT game_id FROM game WHERE title = 'Apex Legends'), (SELECT platform_id FROM platform WHERE platform_name = 'XBOX series X'));



--changeset Stanislaw:7 labels:data,reviews
--critic_review
INSERT INTO critic_review (game_id, user_id, content, post_date, score, review_status, approved_by)
VALUES
    (2, 7, '<h2>Incredible platforming and emotional story make Celeste a surprise triumph.</h2><p>Occasionally, while playing Celeste, I’d get light-headed because I’d focus so hard on a sequence of jumps that demanded precise timing and perfect button presses that I’d forget oxygen was a thing my body needed. Trying and failing and trying again, getting a little closer each time, I let the beautiful art and adaptive music of the titular Celeste Mountain - alongside the passionate, relatable story told there - completely whisk me away.</p><p>Despite appearing at first to be yet another retro pixel-art 2D platformer, Celeste is surprising in so many different ways. From the moment I took my first jump, I fell in love with the satisfying way its protagonist, Madeline, feels to control; soon after I fell just as hard for the charming world she inhabits. But Celeste also caught me off guard with a relevant and emotional story about the pressures of modern life. What’s remarkable is that the story isn’t told in the background or overlaid on top of the action with constant interruption, but seamlessly and thoughtfully blended into the level design using both subtle themes and overt conversations. That’s especially astonishing in a genre not known as a vehicle for such delicate messages.</p><p><b>Fragment of review from IGN</b></p>', '2023-07-03', 9, 1, 8),
    (2, 7, '<h2>Incredible platforming and emotional story make Celeste a surprise triumph.</h2><p>Occasionally, while playing Celeste, I’d get light-headed because I’d focus so hard on a sequence of jumps that demanded precise timing and perfect button presses that I’d forget oxygen was a thing my body needed. Trying and failing and trying again, getting a little closer each time, I let the beautiful art and adaptive music of the titular Celeste Mountain - alongside the passionate, relatable story told there - completely whisk me away.</p><p>Despite appearing at first to be yet another retro pixel-art 2D platformer, Celeste is surprising in so many different ways. From the moment I took my first jump, I fell in love with the satisfying way its protagonist, Madeline, feels to control; soon after I fell just as hard for the charming world she inhabits. But Celeste also caught me off guard with a relevant and emotional story about the pressures of modern life. What’s remarkable is that the story isn’t told in the background or overlaid on top of the action with constant interruption, but seamlessly and thoughtfully blended into the level design using both subtle themes and overt conversations. That’s especially astonishing in a genre not known as a vehicle for such delicate messages.</p><p><b>Fragment of review from IGN</b></p><p>That’s the bigger picture, but every corner of Celeste is overflowing with charm. Its handful of characters are delightful and expressive, and the world they live in is teeming with small details. Smartly written dialogue is accompanied by silly, synthesized gibberish voices and animated character portraits that strikingly clash with the otherwise-pixelated art style, giving each character a distinct personality of their own. Little touches - like Madeline’s red hair turning blue when she’s spent her dash charge and then back again when it’s restored by touching the ground or touching a power-up, or that dash causing lanterns in the background to sway when she zips by them - make everything feel alive and dynamic.</p><p>But Celeste doesn’t succeed on charm alone – it also nails the fundamentals of its genre. All of that character is wrapped around one of the most blissfully fluid, responsive, and fun platformers I’ve played since Super Meat Boy. For more than 20 hours of gameplay, Celeste has surprised me with consistently creative and fun platforming challenges and secrets that found unexpected depth from its relatively simple mechanics.</p>', '2023-07-08', 9, 4, null),
    (3, 8, '<h2>Elden Ring</h2><p>In the 87 hours that it took me to beat Elden Ring, I was put through an absolute wringer of emotion: Anger as I was beaten down by its toughest challenges, exhilaration when I finally overcame them, and a fair amount of sorrow for the mountains of exp I lost along the way to some of the toughest boss encounters FromSoftware has ever conceived. But more than anything else I was in near-constant awe – from the many absolutely jaw-dropping vistas, the sheer scope of an absolutely enormous world, the frequently harrowing enemies, and the way in which Elden Ring nearly always rewarded my curiosity with either an interesting encounter, a valuable reward, or something even greater. FromSoftware takes the ball that The Legend of Zelda: Breath of the Wild got rolling and runs with it, creating a fascinating and dense open world about freedom and exploration above all else, while also somehow managing to seamlessly weave a full-on Dark Souls game into the middle of it. It shouldn’t be a surprise to anyone that Elden Ring ended up as one of the most unforgettable gaming experiences I’ve ever had.</p><p>To set the stage, all you know from the outset is that you play as a “Tarnished” of no renown, blessed by grace, and are compelled to make the journey to The Lands Between and become an Elden Lord. What that actually means, how one might go about doing that, and what the deal is with that giant glowing golden tree are all things that you have to discover yourself. Like other FromSoft games, the grand story is hard to fully digest on a first playthrough, especially because there’s no in-game journal to refresh you on the events, characters, or unique terms you encounter across dozens of hours. There really should be, but it is a story I nonetheless enjoyed trying to piece together for myself. I look forward to supplementing that knowledge with the inevitable painstakingly detailed lore videos that emerge from the community later.</p><p><b>Fragment of review from IGN</b></p>', '2024-07-03', 10, 1, 7),
    (1, 8, '<h2>Limbus Company</h2><p>Limbus Company is a decent gacha turn-based strategy game with interesting combat and an even more interesting story. Plus, it’s for free, technically speaking. Can’t argue with that.</p><p>The downside is that it runs the risk of content droughts, plus it takes effort to make sense of its setting and characters (mainly by playing previous games in the Lobotomy Corporation series). Plus, some people might not like the idea of taking care of yet another gacha in their daily lives.</p><p>If you liked Project Moon’s previous title, give it a whirl. Spend some money on it, if you want. Otherwise, check out the setting first or the first two games in the series. If you find that it’s your thing, then dip in.</p><h3>Great Worldbuilding and Interesting Lore</h3><p>The strongest point (and probably the main selling point) of Limbus Company is its story, characters, and world-building. The City is a dark place where the rich live in material nirvana, while the poor and downtrodden have to endure hunger, violence, and monstrosities beyond their wildest imaginations. You, as the manager of the titular Limbus Company, are trying to pursue your corporation’s goals in the middle of this hopelessness, while trying to keep your Sinners from tearing out each others’ throats.</p><p>The characters are all well-crafted, with a solid basis in popular literature. As you guessed, Dante is a reference to Dante Alighieri’s Divine Comedy, along with his guide, Vergil. Meanwhile, each of the 12 Sinners is related to literature in some way. To name a few, there’s the intellectually-gifted but arrogant Faust, the idealistic and childish Don Quixote, the laid-back half-transformed bugman Gregor, the level-headed and competent Ishmael, and the impulsive and hot-headed Heathcliff.</p><p>Each chapter of the story focuses on a specific character, where you will progress through a part of The City to look for Golden Boughs, but end up in a manifestation of that character’s psyche. The stories we’ve been presented so far (there are at least three chapters out, with a fourth one in the works), all show both progress in the main story and the backstories of your Sinners. I would say that their stories were handled quite well, considering the quality of plots in other gacha games, with a plot revolving around Limbus Company’s conflicts with rival corporations and the Sinners’ relationships with those rivals and each other.</p><p>If consuming game lore is your thing, Limbus Company has that in droves. Each enemy has its own log and story, and the game itself has its own spinoff web story. That’s on top of the content that’s already been made for both Lobotomy Corporation and Library of Ruina, along with their respective spinoff media, many of which have already been translated from Korean to English.</p><p><b>Fragment of review from game8</b></p>', '2023-09-02', 8, 2, null),
    (4, 7, '<h2>Apex Legends</h2><p>Apex Legends is the only battle royale where I can launch myself up into the sky and dodge bullets in the air while throwing a mini black hole at an enemy squad as my teammate simultaneously rains a hellfire of mortars down upon them. Since its release in 2019, Apex has continued to grow and evolve in exciting ways, adding both more content and fresh new ideas to a genre that too frequently feels derivative. The fast-paced matches never fail to get my heart racing as I jump, slide, and dodge bullets while hip-firing a sniper rifle to win a 1v1 duel and revive my teammates. Mobility, versatility, and teamwork combine for a thrilling and rewarding feeling that I haven’t experienced from any other battle royale.</p><p>While the structure here is familiar – drop into a large map, pick up randomly scattered loot, and fight inside an ever-closing circle to be the last team standing – it’s the 16 playable characters (called Legends) themselves that keep Apex from feeling like your run-of-the-mill battle royale shooter. Where traditionally you start a battle royale as a blank slate and have your role defined solely by the gear you luck into, here each has their own set of special abilities and strengths that you can choose from to fit your personal playstyle. I especially love how different Legends can interact and work with each other on a three-person team to get more out of those powers than they could alone. For example, if someone on your team is playing as Caustic or Bangalore and tossing smoke bombs around, choosing Bloodhound for their ability to see through the smoke and highlight nearby enemies will be a natural fit.</p><p><b>Fragment of review from IGN</b></p>', '2022-03-02', 7, 1, 8);

--user_review
INSERT INTO user_review (game_id, user_id, content, post_date, score)
VALUES
    (1, 4, 'Limbus Company offers a thrilling narrative and strategic gameplay. The artwork is captivating, and the storyline keeps you engaged throughout.', '2024-07-7', 9),
    (1, 5, 'As a fan of tactical RPGs, Limbus Company hits all the right notes. The character development and combat mechanics are top-notch.', '2023-07-10', 8);

INSERT INTO user_review (game_id, user_id, content, post_date, score)
VALUES
    (2, 4, 'Celeste is a masterpiece in platforming. The difficulty curve is perfectly balanced, and the story is deeply moving.', '2022-07-10', 10),
    (2, 6, 'Celeste is a platformer that challenges both your skills and your heart. Each level feels like a triumph of perseverance.', '2024-04-10', 9);

INSERT INTO user_review (game_id, user_id, content, post_date, score)
VALUES
    (3, 5, 'FromSoftware has outdone themselves with Elden Ring. The combat mechanics are refined, and the world feels alive with secrets.', '2023-11-12', 9),
    (3, 6, 'Elden Ring is an epic adventure that immerses you in its mythos. The bosses are challenging, and the freedom to explore is liberating.', '2024-01-14', 9);

INSERT INTO user_review (game_id, user_id, content, post_date, score)
VALUES
    (4, 5, 'Apex Legends is the pinnacle of battle royale games. The diverse characters and strategic gameplay keep me coming back for more.', '2024-07-10', 9),
    (4, 6, 'Apex Legends has potential, but it falls short in a few areas. The frequent balance changes can be frustrating, and the matchmaking sometimes feels uneven. While the gameplay is fun, the progression system feels grindy.', '2023-05-20', 5);

--rating
INSERT INTO rating (is_positive, user_id, user_review_id)
VALUES
    (true, 5, (SELECT user_review_id FROM user_review WHERE game_id = 1 AND user_id = 4)),   -- User 5 rates User 4's review positively
    (false, 6, (SELECT user_review_id FROM user_review WHERE game_id = 1 AND user_id = 4)),  -- User 6 rates User 4's review negatively
    (true, 4, (SELECT user_review_id FROM user_review WHERE game_id = 1 AND user_id = 5)),   -- User 4 rates User 5's review positively
-- Ratings for Celeste reviews
    (true, 5, (SELECT user_review_id FROM user_review WHERE game_id = 2 AND user_id = 4)),   -- User 5 rates User 4's review positively
    (true, 4, (SELECT user_review_id FROM user_review WHERE game_id = 2 AND user_id = 6)),   -- User 4 rates User 6's review positively
    (false, 6, (SELECT user_review_id FROM user_review WHERE game_id = 2 AND user_id = 4)),  -- User 6 rates User 4's review negatively
-- Ratings for Elden Ring reviews
    (true, 4, (SELECT user_review_id FROM user_review WHERE game_id = 3 AND user_id = 5)),   -- User 4 rates User 5's review positively
    (true, 5, (SELECT user_review_id FROM user_review WHERE game_id = 3 AND user_id = 6)),   -- User 5 rates User 6's review positively
    (false, 6, (SELECT user_review_id FROM user_review WHERE game_id = 3 AND user_id = 5)),  -- User 6 rates User 5's review negatively
-- Ratings for Apex Legends reviews
    (true, 4, (SELECT user_review_id FROM user_review WHERE game_id = 4 AND user_id = 5)),   -- User 4 rates User 5's review positively
    (false, 5, (SELECT user_review_id FROM user_review WHERE game_id = 4 AND user_id = 6)),  -- User 5 rates User 6's review negatively
    (true, 6, (SELECT user_review_id FROM user_review WHERE game_id = 4 AND user_id = 5)),   -- User 6 rates User 5's review positively
    (false, 4, (SELECT user_review_id FROM user_review WHERE game_id = 4 AND user_id = 6));  -- User 4 rates User 6's review negatively


--report
INSERT INTO report (user_review_id, user_id, approved)
VALUES
    ((SELECT user_review_id FROM user_review WHERE game_id = 4 AND user_id = 6 ), 4, NULL);


--user_game
INSERT INTO user_game (game_id, user_id, completion_status, is_favourite)
VALUES
    (1, 4, 1, true),  -- User 4 completed Limbus Company and it's their favorite
    (1, 5, 2, false); -- User 5 is currently playing Limbus Company

INSERT INTO user_game (game_id, user_id, completion_status, is_favourite)
VALUES
    (2, 4, 1, true),  -- User 4 completed Celeste and it's their favorite
    (2, 6, 1, false); -- User 6 completed Celeste

INSERT INTO user_game (game_id, user_id, completion_status, is_favourite)
VALUES
    (3, 5, 2, false), -- User 5 is currently playing Elden Ring
    (3, 6, 2, true);  -- User 6 is currently playing Elden Ring and it's their favorite

INSERT INTO user_game (game_id, user_id, completion_status, is_favourite)
VALUES
    (4, 5, 3, false), -- User 5 has put Apex Legends on hold
    (4, 6, 5, false); -- User 6 has dropped Apex Legends
