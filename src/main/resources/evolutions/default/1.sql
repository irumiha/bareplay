-- !Ups
CREATE TABLE access_counter(
    id INT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    counter INT
);
INSERT INTO access_counter(counter) values (0);

-- !Downs

DROP TABLE access_counter;