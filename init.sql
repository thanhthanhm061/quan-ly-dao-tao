CREATE DATABASE IF NOT EXISTS qldt_db;
USE qldt_db;

INSERT INTO khoa (id, ma_khoa, ten_khoa) VALUES
                                             (2, 'QTKD', 'Quản trị kinh doanh'),
                                             (3, 'KT', 'Kế toán'),
                                             (4, 'DL', 'Du lịch'),
                                             (5, 'NN', 'Ngôn ngữ'),
                                             (6, 'SP', 'Sư phạm'),
                                             (7, 'Y', 'Y dược');



INSERT INTO lop (ma_lop, ten_lop, nien_khoa, khoa_id) VALUES
                                                ('QTKD01', 'Quản trị kinh doanh 01', '2018-2022', 2),
                                                ('QTKD02', 'Quản trị kinh doanh 02', '2018-2022', 2),
                                                ('KT01', 'Kế toán 01', '2018-2022', 3),
                                                ('KT02', 'Kế toán 02', '2018-2022', 3),
                                                ('DL01', 'Du lịch 01', '2018-2022', 4),
                                                ('DL02', 'Du lịch 02', '2018-2022', 4),
                                                ('NN01', 'Ngôn ngữ 01', '2018-2022', 5),
                                                ('NN02', 'Ngôn ngữ 02', '2018-2022', 5),
                                                ('SP01', 'Sư phạm 01', '2018-2022', 6),
                                                ('SP02', 'Sư phạm 02', '2018-2022', 6),
                                                ('Y01', 'Y dược 01', '2018-2022', 7),
                                                ('Y02', 'Y dược 02', '2018-2022', 7);