-- =============================================================================
-- Royal Pearl Hyderabad – V2__seed.sql
-- Seed data: 4 rooms + ~54 menu items across 8 categories
-- =============================================================================

-- ---------------------------------------------------------------------------
-- Rooms
-- ---------------------------------------------------------------------------
INSERT INTO rooms (id, name, slug, description, price_per_night, max_guests, image_url, amenities, active)
VALUES
  (
    gen_random_uuid(),
    'Deluxe Room',
    'deluxe-room',
    'Elegantly appointed Deluxe Room offering a serene retreat with modern amenities, king-size bed, and a stunning city view. Perfect for couples or solo business travellers.',
    4999.00,
    2,
    '/images/rooms/deluxe-room.jpg',
    '["King Bed","AC","Free Wi-Fi","LED TV","Mini Fridge","24/7 Room Service","En-suite Bathroom","In-room Safe","Tea/Coffee Maker"]',
    TRUE
  ),
  (
    gen_random_uuid(),
    'Executive Suite',
    'executive-suite',
    'Spacious Executive Suite designed for the discerning business traveller. Features a separate living area, work desk, premium bath amenities, and panoramic city views.',
    8999.00,
    2,
    '/images/rooms/executive-suite.jpg',
    '["King Bed","Separate Living Area","Work Desk","AC","Free Wi-Fi","55\" Smart TV","Espresso Machine","Mini Bar","Bathtub & Shower","Butler Service"]',
    TRUE
  ),
  (
    gen_random_uuid(),
    'Family Suite',
    'family-suite',
    'Generous Family Suite ideal for families with children. Two interconnected bedrooms, a cosy lounge, and thoughtful amenities ensure a comfortable extended stay.',
    12999.00,
    4,
    '/images/rooms/family-suite.jpg',
    '["2 Queen Beds","Interconnected Rooms","Lounge Area","Kids Amenities","AC","Free Wi-Fi","Smart TV","Kitchenette","Dining Area","Laundry Service"]',
    TRUE
  ),
  (
    gen_random_uuid(),
    'Presidential Suite',
    'presidential-suite',
    'The pinnacle of luxury — our Presidential Suite commands breathtaking views, a private terrace, grand dining room, and bespoke butler service for an unparalleled stay.',
    29999.00,
    6,
    '/images/rooms/presidential-suite.jpg',
    '["Master Bedroom","Guest Bedroom","Private Terrace","Grand Dining","Personal Butler","Jacuzzi","Home Theatre","Premium Bar","Concierge","Airport Transfer"]',
    TRUE
  )
ON CONFLICT (slug) DO NOTHING;

-- ---------------------------------------------------------------------------
-- Menu Items
-- ---------------------------------------------------------------------------

-- ---- Biryani ---------------------------------------------------------------
INSERT INTO menu_items (name, category, description, price, veg, available, sort_order) VALUES
  ('Hyderabadi Dum Biryani',        'Biryani', 'Authentic dum-cooked biryani with aromatic basmati, saffron and secret spices',           350.00, FALSE, TRUE, 10),
  ('Chicken Tikka Biryani',         'Biryani', 'Tender chicken tikka pieces layered with fragrant basmati rice',                          380.00, FALSE, TRUE, 11),
  ('Mutton Biryani',                'Biryani', 'Slow-cooked mutton biryani with caramelised onions and whole spices',                     420.00, FALSE, TRUE, 12),
  ('Prawn Biryani',                 'Biryani', 'Succulent tiger prawns in a spiced rice preparation',                                     440.00, FALSE, TRUE, 13),
  ('Egg Biryani',                   'Biryani', 'Classic egg biryani with boiled eggs marinated in spices',                               280.00, FALSE, TRUE, 14),
  ('Veg Dum Biryani',               'Biryani', 'Seasonal vegetables slow-cooked with basmati rice and royal spice blend',                260.00, TRUE,  TRUE, 15),
  ('Paneer Biryani',                'Biryani', 'Creamy cottage cheese biryani with aromatic herbs',                                       300.00, TRUE,  TRUE, 16);

-- ---- Hyderabadi Mains -------------------------------------------------------
INSERT INTO menu_items (name, category, description, price, veg, available, sort_order) VALUES
  ('Mirchi Ka Salan',               'Hyderabadi Mains', 'Traditional Hyderabadi curry with long green chillies in tangy peanut gravy',    220.00, TRUE,  TRUE, 20),
  ('Bagara Baingan',                'Hyderabadi Mains', 'Baby eggplants stuffed and cooked in a rich peanut-sesame gravy',               240.00, TRUE,  TRUE, 21),
  ('Pathar Ka Gosht',               'Hyderabadi Mains', 'Slow-grilled mutton on a hot stone with Nizam''s spice blend',                  480.00, FALSE, TRUE, 22),
  ('Haleem',                        'Hyderabadi Mains', 'Slow-cooked wheat and mutton porridge — Hyderabad''s iconic dish',              350.00, FALSE, TRUE, 23),
  ('Dalcha',                        'Hyderabadi Mains', 'Lentil and lamb curry with tamarind, served alongside biryani',                  260.00, FALSE, TRUE, 24),
  ('Nihari',                        'Hyderabadi Mains', 'Slow-cooked lamb shank stew with bone marrow and warm spices',                  420.00, FALSE, TRUE, 25),
  ('Paneer Makhanwala',             'Hyderabadi Mains', 'Cottage cheese in a velvety tomato-butter sauce',                               280.00, TRUE,  TRUE, 26),
  ('Dal Tadka',                     'Hyderabadi Mains', 'Yellow lentils tempered with cumin, garlic and dried red chillies',             180.00, TRUE,  TRUE, 27);

-- ---- Starters ---------------------------------------------------------------
INSERT INTO menu_items (name, category, description, price, veg, available, sort_order) VALUES
  ('Chicken 65',                    'Starters', 'Crispy deep-fried chicken with curry leaves and chillies — a Hyderabad classic',        280.00, FALSE, TRUE, 30),
  ('Mutton Seekh Kebab',            'Starters', 'Minced mutton kebabs on skewers, grilled over charcoal',                               320.00, FALSE, TRUE, 31),
  ('Fish Fry',                      'Starters', 'Marinated fish fillets deep-fried to golden perfection',                                300.00, FALSE, TRUE, 32),
  ('Shammi Kebab',                  'Starters', 'Hyderabadi minced lamb patties with chana dal and spices',                             280.00, FALSE, TRUE, 33),
  ('Paneer Tikka',                  'Starters', 'Marinated cottage cheese cubes grilled in tandoor',                                    260.00, TRUE,  TRUE, 34),
  ('Hara Bhara Kebab',              'Starters', 'Spinach and pea patties seasoned with mint and coriander',                             220.00, TRUE,  TRUE, 35),
  ('Crispy Veg Spring Roll',        'Starters', 'Golden fried rolls stuffed with seasoned vegetables',                                  180.00, TRUE,  TRUE, 36),
  ('Prawns Koliwada',               'Starters', 'Batter-fried prawns with green chutney',                                              360.00, FALSE, TRUE, 37);

-- ---- Tandoor ----------------------------------------------------------------
INSERT INTO menu_items (name, category, description, price, veg, available, sort_order) VALUES
  ('Chicken Tikka',                 'Tandoor', 'Boneless chicken marinated in yoghurt and spices, cooked in clay oven',                 320.00, FALSE, TRUE, 40),
  ('Malai Tikka',                   'Tandoor', 'Creamy white marinated chicken, melt-in-mouth texture',                                340.00, FALSE, TRUE, 41),
  ('Tandoori Chicken (Half)',       'Tandoor', 'Classic half tandoori chicken with mint chutney',                                      300.00, FALSE, TRUE, 42),
  ('Tandoori Chicken (Full)',       'Tandoor', 'Classic full tandoori chicken — feeds two',                                            560.00, FALSE, TRUE, 43),
  ('Achari Paneer Tikka',           'Tandoor', 'Pickle-spiced cottage cheese cubes from the tandoor',                                  280.00, TRUE,  TRUE, 44),
  ('Mushroom Tikka',                'Tandoor', 'Button mushrooms in spiced yoghurt marinade, tandoor-grilled',                         240.00, TRUE,  TRUE, 45);

-- ---- Breads -----------------------------------------------------------------
INSERT INTO menu_items (name, category, description, price, veg, available, sort_order) VALUES
  ('Butter Naan',                   'Breads', 'Soft leavened flatbread glazed with butter',                                             60.00, TRUE,  TRUE, 50),
  ('Garlic Naan',                   'Breads', 'Naan topped with fresh garlic and coriander',                                            70.00, TRUE,  TRUE, 51),
  ('Laccha Paratha',                'Breads', 'Flaky layered whole-wheat paratha',                                                      55.00, TRUE,  TRUE, 52),
  ('Roomali Roti',                  'Breads', 'Paper-thin hand-stretched Mughal bread',                                                 45.00, TRUE,  TRUE, 53),
  ('Kulcha',                        'Breads', 'Stuffed leavened bread with onion and herb filling',                                     65.00, TRUE,  TRUE, 54),
  ('Sheermal',                      'Breads', 'Mildly sweet saffron flatbread — traditional Hyderabadi accompaniment',                  80.00, TRUE,  TRUE, 55);

-- ---- Chinese ----------------------------------------------------------------
INSERT INTO menu_items (name, category, description, price, veg, available, sort_order) VALUES
  ('Chicken Fried Rice',            'Chinese', 'Wok-tossed fried rice with chicken, eggs and vegetables',                              260.00, FALSE, TRUE, 60),
  ('Veg Fried Rice',                'Chinese', 'Classic wok-fried rice with mixed vegetables and soy sauce',                           220.00, TRUE,  TRUE, 61),
  ('Chicken Manchurian (Dry)',      'Chinese', 'Crispy chicken balls tossed in spicy Manchurian sauce',                                 280.00, FALSE, TRUE, 62),
  ('Gobi Manchurian (Dry)',         'Chinese', 'Cauliflower florets in a tangy Manchurian sauce',                                      220.00, TRUE,  TRUE, 63),
  ('Chicken Schezwan Noodles',      'Chinese', 'Stir-fried noodles with chicken and fiery Schezwan sauce',                             280.00, FALSE, TRUE, 64),
  ('Veg Hakka Noodles',             'Chinese', 'Stir-fried noodles with mixed vegetables — classic Indo-Chinese',                      220.00, TRUE,  TRUE, 65),
  ('Sweet & Sour Chicken',          'Chinese', 'Tender chicken in vibrant sweet and sour sauce',                                       300.00, FALSE, TRUE, 66),
  ('Chilli Paneer (Dry)',           'Chinese', 'Crispy cottage cheese tossed in chilli-garlic sauce',                                  240.00, TRUE,  TRUE, 67);

-- ---- Desserts ---------------------------------------------------------------
INSERT INTO menu_items (name, category, description, price, veg, available, sort_order) VALUES
  ('Double Ka Meetha',              'Desserts', 'Hyderabad''s signature bread pudding soaked in saffron rabri',                        160.00, TRUE,  TRUE, 70),
  ('Qubani Ka Meetha',              'Desserts', 'Dried apricot dessert with fresh cream — royal Nizam''s recipe',                      180.00, TRUE,  TRUE, 71),
  ('Shahi Tukda',                   'Desserts', 'Crispy fried bread soaked in cardamom rabri and garnished with nuts',                  160.00, TRUE,  TRUE, 72),
  ('Gulab Jamun',                   'Desserts', 'Soft milk-solid dumplings in rose-flavoured sugar syrup',                             120.00, TRUE,  TRUE, 73),
  ('Phirni',                        'Desserts', 'Chilled ground rice pudding with saffron and pistachios in clay pots',                 140.00, TRUE,  TRUE, 74),
  ('Kulfi Falooda',                 'Desserts', 'Traditional Indian ice cream with vermicelli, rose syrup and basil seeds',            160.00, TRUE,  TRUE, 75),
  ('Ice Cream (3 scoops)',          'Desserts', 'Choice of vanilla, chocolate or strawberry — premium Indian ice cream',               120.00, TRUE,  TRUE, 76);

-- ---- Drinks -----------------------------------------------------------------
INSERT INTO menu_items (name, category, description, price, veg, available, sort_order) VALUES
  ('Irani Chai',                    'Drinks', 'Authentic Hyderabadi Irani chai brewed slow and strong with milk',                       60.00, TRUE,  TRUE, 80),
  ('Masala Chai',                   'Drinks', 'Spiced tea with ginger, cardamom and tulsi',                                             50.00, TRUE,  TRUE, 81),
  ('Lassi (Sweet)',                 'Drinks', 'Chilled creamy yoghurt drink sweetened with sugar',                                      80.00, TRUE,  TRUE, 82),
  ('Lassi (Salted)',                'Drinks', 'Refreshing salted yoghurt drink with roasted cumin',                                     80.00, TRUE,  TRUE, 83),
  ('Fresh Lime Soda',               'Drinks', 'Freshly squeezed lime with sparkling water — sweet or salted',                          70.00, TRUE,  TRUE, 84),
  ('Mango Lassi',                   'Drinks', 'Rich mango pulp blended with yoghurt and a pinch of cardamom',                          100.00, TRUE,  TRUE, 85),
  ('Cold Coffee',                   'Drinks', 'Blended cold coffee with ice cream — rich and creamy',                                   120.00, TRUE,  TRUE, 86),
  ('Soft Drinks',                   'Drinks', 'Pepsi, 7Up, Mountain Dew, Mirinda (canned)',                                             60.00, TRUE,  TRUE, 87),
  ('Mineral Water (500 ml)',        'Drinks', 'Sealed bottled mineral water',                                                           30.00, TRUE,  TRUE, 88),
  ('Fresh Fruit Juice',             'Drinks', 'Seasonal freshly pressed fruit juice — ask your server for today''s selection',         120.00, TRUE,  TRUE, 89);
