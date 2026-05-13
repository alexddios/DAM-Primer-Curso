-- 1. Create the table that represents the many-to-many relationship between USERS and ACTIVITIES[cite: 31].
CREATE TABLE USER_ACTIVITIES (
    Membership_Number VARCHAR(50),
    Activity_code VARCHAR(50),
    Date_Of_Registration DATE,
    Leaving_Date DATE,
    PRIMARY KEY (Membership_Number, Activity_code),
    FOREIGN KEY (Membership_Number) REFERENCES USERS(Membership_Number),
    FOREIGN KEY (Activity_code) REFERENCES ACTIVITIES(Activity_code)
);

-- 2. List for each user: code, the number of activities and the total amount of dues they have paid[cite: 32].
SELECT 
    U.Membership_Number,
    (SELECT COUNT(*) FROM USER_ACTIVITIES UA WHERE UA.Membership_Number = U.Membership_Number) AS Number_of_Activities,
    (SELECT SUM(P.Dues) FROM PAYMENTS P WHERE P.User_Code = U.Membership_Number) AS Total_Dues_Paid
FROM USERS U;

-- 3. Display the user (USER_CODE) who made the largest payment sum. You must also show this amount[cite: 33].
SELECT User_Code, SUM(Dues) AS Total_Payment
FROM PAYMENTS
GROUP BY User_Code
ORDER BY Total_Payment DESC
FETCH FIRST 1 ROWS ONLY; -- (Nota: En Oracle se usaría "FETCH FIRST 1 ROWS ONLY" en lugar de LIMIT)

-- 4. Display the MEMBERSHIP_NUMBER and the total payments made for users from MANACOR who has not leaved any activity they were enrolled[cite: 34].
SELECT U.Membership_Number, SUM(P.Dues) AS Total_Payments
FROM USERS U
LEFT JOIN PAYMENTS P ON U.Membership_Number = P.User_Code
WHERE U.City = 'MANACOR' 
  AND U.Membership_Number NOT IN (
      SELECT Membership_Number 
      FROM USER_ACTIVITIES 
      WHERE Leaving_Date IS NOT NULL
  )
GROUP BY U.Membership_Number;

-- 5. Show the largest dues that have paid each user[cite: 36].
SELECT User_Code, MAX(Dues) AS Largest_Due
FROM PAYMENTS
GROUP BY User_Code;

-- 6. List the membership number and the name of the users who don't do any activity[cite: 37]. 
-- Make the exercise in two versions: with a SUBSELECT / using EXISTS[cite: 38].

-- 6 (Versión 1: Con SUBSELECT)
SELECT Membership_Number, Name
FROM USERS
WHERE Membership_Number NOT IN (SELECT Membership_Number FROM USER_ACTIVITIES);

-- 6 (Versión 2: Con EXISTS)
SELECT Membership_Number, Name
FROM USERS U
WHERE NOT EXISTS (
    SELECT 1 
    FROM USER_ACTIVITIES UA 
    WHERE UA.Membership_Number = U.Membership_Number
);

-- 7. Make a list of users ordered by the month of their date of registration[cite: 39].
SELECT *
FROM USERS
ORDER BY EXTRACT(MONTH FROM Date_of_Registration);

-- 8. List user's name and dues greater than or equal to 8000 from the user with code: A2222[cite: 40].
SELECT U.Name, P.Dues
FROM USERS U
JOIN PAYMENTS P ON U.Membership_Number = P.User_Code
WHERE U.Membership_Number = 'A2222' AND P.Dues >= 8000;

-- 9. List the users who have made a payment in the current month (date must be taken from the system)[cite: 41].
SELECT DISTINCT U.*
FROM USERS U
JOIN PAYMENTS P ON U.Membership_Number = P.User_Code
WHERE P.Month_Number = EXTRACT(MONTH FROM CURRENT_DATE);

-- 10. List the code of the user 'A1111', the code of her/his activities, the date of registration, a description of each activity and the dues to be paid[cite: 43].
SELECT UA.Membership_Number, UA.Activity_code, UA.Date_Of_Registration, A.Description, A.Dues
FROM USER_ACTIVITIES UA
JOIN ACTIVITIES A ON UA.Activity_code = A.Activity_code
WHERE UA.Membership_Number = 'A1111';

-- 11. Increase a 5% the dues of the activities that its due is less than a 15% to the dues average[cite: 45].
UPDATE ACTIVITIES
SET Dues = Dues * 1.05
WHERE Dues < (SELECT AVG(Dues) FROM ACTIVITIES) * 0.15;

-- 12. If you delete a user (in the USERS table), could you say if the rest of the data in the database related to this user is deleted? [cite: 47] 
-- If the answer is yes, explain why. If the answer is not, explain what we should do to achieve that[cite: 48].

-- RESPUESTA TEÓRICA: 
-- No, los datos relacionados en otras tablas (PAYMENTS, USER_ACTIVITIES) no se eliminan automáticamente. 
-- Si se intenta, el gestor de base de datos lanzará un error de integridad referencial para evitar datos huérfanos.
-- Para lograr que se eliminen automáticamente, debemos configurar las claves foráneas con "ON DELETE CASCADE".
-- Ejemplo de cómo deberíamos hacerlo:
-- ALTER TABLE USER_ACTIVITIES ADD CONSTRAINT fk_usr_act FOREIGN KEY (Membership_Number) REFERENCES USERS(Membership_Number) ON DELETE CASCADE;
-- ALTER TABLE PAYMENTS ADD CONSTRAINT fk_usr_pay FOREIGN KEY (User_Code) REFERENCES USERS(Membership_Number) ON DELETE CASCADE;

-- 13. List the name of the user and the total of payments, provided they are greater than 30000[cite: 50].
SELECT U.Name, SUM(P.Dues) AS Total_Payments
FROM USERS U
JOIN PAYMENTS P ON U.Membership_Number = P.User_Code
GROUP BY U.Membership_Number, U.Name
HAVING SUM(P.Dues) > 30000;

-- 14. Display the user code, the city, and the number of activities they are enrolled, as long as the user is enrolled in an activity at least and he/she has never left any activity[cite: 51].
SELECT U.Membership_Number, U.City, COUNT(UA.Activity_code) AS Number_of_Activities
FROM USERS U
JOIN USER_ACTIVITIES UA ON U.Membership_Number = UA.Membership_Number
WHERE U.Membership_Number NOT IN (
    SELECT Membership_Number 
    FROM USER_ACTIVITIES 
    WHERE Leaving_Date IS NOT NULL
)
GROUP BY U.Membership_Number, U.City;
