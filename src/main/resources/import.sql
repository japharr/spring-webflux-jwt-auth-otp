INSERT INTO roles(name, created_by, last_modified_by, created_date, last_modified_date) VALUES ('ROLE_USER', 'system', 'system', '2019-10-24 08:30:00', '2019-10-24 08:30:00');
INSERT INTO roles(name, created_by, last_modified_by, created_date, last_modified_date) VALUES ('ROLE_ADMIN', 'system', 'system', '2019-10-24 08:30:00', '2019-10-24 08:30:00');
INSERT INTO users(id, name, email, password, username, enabled, created_by, last_modified_by, created_date, last_modified_date)
VALUES ('cfd47eba-fec7-45d9-be2f-7864b7b488a0', 'Admin', 'admin@mail.com', 'vWdYoyWQpSM64FP7jbIZ/rYO79Sx9rxgvBMjQ1duMQs=', 'admin', true, 'system', 'system', '2019-10-24 08:30:00', '2019-10-24 08:30:00');
INSERT INTO users_roles(user_id, role_name) VALUES ('cfd47eba-fec7-45d9-be2f-7864b7b488a0', 'ROLE_ADMIN');