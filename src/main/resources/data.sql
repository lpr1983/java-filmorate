INSERT INTO mpa_ratings (id, name, age)
SELECT 1, 'G', 0 WHERE NOT EXISTS (SELECT 1 FROM mpa_ratings WHERE id = 1);

INSERT INTO mpa_ratings (id, name, age)
SELECT 2, 'PG', 6 WHERE NOT EXISTS (SELECT 1 FROM mpa_ratings WHERE id = 2);

INSERT INTO mpa_ratings (id, name, age)
SELECT 3, 'PG-13', 12 WHERE NOT EXISTS (SELECT 1 FROM mpa_ratings WHERE id = 3);

INSERT INTO mpa_ratings (id, name, age)
SELECT 4, 'R', 16 WHERE NOT EXISTS (SELECT 1 FROM mpa_ratings WHERE id = 4);

INSERT INTO mpa_ratings (id, name, age)
SELECT 5, 'NC-17', 18 WHERE NOT EXISTS (SELECT 1 FROM mpa_ratings WHERE id = 5);

INSERT INTO genres (id, name)
SELECT 1, 'Комедия' WHERE NOT EXISTS (SELECT 1 FROM genres  WHERE id = 1);

INSERT INTO genres (id, name)
SELECT 2, 'Драма' WHERE NOT EXISTS (SELECT 1 FROM genres  WHERE id = 2);

INSERT INTO genres (id, name)
SELECT 3, 'Мультфильм' WHERE NOT EXISTS (SELECT 1 FROM genres  WHERE id = 3);

INSERT INTO genres (id, name)
SELECT 4, 'Триллер' WHERE NOT EXISTS (SELECT 1 FROM genres  WHERE id = 4);

INSERT INTO genres (id, name)
SELECT 5, 'Документальный' WHERE NOT EXISTS (SELECT 1 FROM genres  WHERE id = 5);

INSERT INTO genres (id, name)
SELECT 6, 'Боевик' WHERE NOT EXISTS (SELECT 1 FROM genres  WHERE id = 6);
