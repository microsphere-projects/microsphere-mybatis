CREATE TABLE users (
  id INT NOT NULL,
  name VARCHAR(50) NOT NULL
);

CREATE TABLE child (
  id INT,
  name VARCHAR(100),
  father_id INT
);

CREATE TABLE father (
  id INT,
  name VARCHAR(100)
);

INSERT INTO child (id, name, father_id)
VALUES (1, 'John Smith jr', 1);
INSERT INTO child (id, name, father_id)
VALUES (2, 'John Smith jr 2', 1);

INSERT INTO father (id, name)
VALUES (1, 'John Smith');