MERGE INTO market (id, name, district, state) KEY(id) VALUES
  (1, 'Nagpur APMC', 'Nagpur', 'Maharashtra'),
  (2, 'Pune APMC', 'Pune', 'Maharashtra'),
  (3, 'Indore Mandi', 'Indore', 'Madhya Pradesh');

MERGE INTO commodity (id, name, unit) KEY(id) VALUES
  (1, 'Tomato', 'quintal'),
  (2, 'Onion', 'quintal'),
  (3, 'Wheat', 'quintal');
