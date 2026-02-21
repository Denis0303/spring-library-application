select * from book;

WITH RECURSIVE nums(n) AS (
    SELECT 5
    UNION ALL
    SELECT n + 1
    FROM nums
    WHERE n <= 1000000
)
INSERT INTO book (id, title, author, year)
SELECT
    n,
    'name ' || n,
    'author ' || n,
    2025
FROM nums; 

select * from book;