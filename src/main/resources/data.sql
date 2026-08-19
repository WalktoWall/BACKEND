-- 테이블 생성 (없으면 생성)
CREATE TABLE IF NOT EXISTS users (
                                     user_id BIGINT PRIMARY KEY,
                                     user_name VARCHAR(255) NOT NULL
    );

CREATE TABLE IF NOT EXISTS offline_stores (
                                              store_id BIGINT PRIMARY KEY,
                                              region_category INT,
                                              store_name VARCHAR(255) NOT NULL,
    open_time VARCHAR(10),
    close_time VARCHAR(10),
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION
    );

CREATE TABLE IF NOT EXISTS visit_cards (
                                           card_id BIGSERIAL PRIMARY KEY,
                                           user_id BIGINT,
                                           store_id BIGINT,
                                           gender INT,
                                           find_product_category INT,
                                           mood_category INT,
                                           purpose_text TEXT,
                                           visit_time TIMESTAMP,
                                           support_status INT,
                                           ai_mood TEXT,
                                           recommended_route TEXT,
                                           created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS products (
                                        product_id BIGINT PRIMARY KEY,
                                        zone VARCHAR(255),
    product_name VARCHAR(255) NOT NULL,
    product_category INT,
    product_img TEXT,
    product_detail TEXT,
    stock INT,
    location VARCHAR(255)
    );

CREATE TABLE IF NOT EXISTS best_products (
                                             id BIGSERIAL PRIMARY KEY,
                                             product_id BIGINT
);

-- 유저 더미 데이터
INSERT INTO users (user_id, user_name) VALUES
                                           (1, 'WWW'),
                                           (2, '박지우'),
                                           (3, '이서준'),
                                           (4, '정도윤'),
                                           (5, '최유진');

-- 매장 더미 데이터
INSERT INTO offline_stores
(store_id, region_category, store_name, open_time, close_time, latitude, longitude)
VALUES
    (1, 1, 'MCM HAUS 청담 플래그십', '11:00', '20:00', 37.52718187, 127.0418862),
    (2, 1, 'MCM 롯데백화점 본점', '10:30', '20:00', 37.56541171, 126.9818998),
    (3, 1, 'MCM 롯데백화점 잠실점', '10:30', '20:00', 37.51130961, 127.0981418),
    (4, 1, 'MCM 신세계백화점 강남점', '10:30', '20:00', 37.56091902, 126.980979),
    (5, 1, 'MCM 현대백화점 무역센터점', '10:30', '20:00', 37.50861513, 127.0597651),
    (6, 2, 'MCM 현대프리미엄아울렛 파주점', '10:30', '21:00', 37.76967008, 126.6962573),
    (7, 2, 'MCM 인천국제공항 T1 면세점', '06:30', '21:30', 37.45835053, 126.4276995),
    (8, 3, 'MCM 신세계백화점 센텀시티점', '10:30', '20:00', 35.16881784, 129.1295233),
    (9, 3, 'MCM 롯데백화점 부산본점', '10:30', '19:30', 35.15678998, 129.0564162),
    (10, 4, 'MCM 롯데백화점 대구점', '10:30', '20:30', 35.87605699, 128.5955651),
    (11, 4, 'MCM 롯데백화점 광주점', '10:30', '20:30', 35.15462285, 126.9118181);

-- visit card 더미 데이터
INSERT INTO visit_cards
(user_id, store_id, gender, find_product_category, mood_category,
 purpose_text, visit_time, support_status, ai_mood, recommended_route, created_at)
VALUES

-- 1번 유저는 시연용으로 사용 = 임시 visit card
-- 1. 여성 / 백팩 / 클래식 / 응대 희망
(1, 1, 2, 1, 2,
 '신상품을 보고 싶어요.',
 '2026-08-17 15:00:00',
 1, '오늘은 고전미를 품은 새로운 백팩과 함께 우아한 변화를 만나는 날', '여성존 -> 신상품-여성존 -> 라이프스타일존', CURRENT_TIMESTAMP),

-- 2. 남성 / 지갑 / 모던 / 혼자 둘러보기
(2, 2, 1, 3, 3,
 '어느 때나 잘 쓸 수 있는 가방을 찾고 있어요.',
 '2026-08-17 13:00:00',
 2, '오늘은 모던한 감각으로 언제나 곁에 두고 싶은 지갑을 찾아가는 세련된 여정의 날', '남성존 -> 라이프스타일존 -> 가방존', CURRENT_TIMESTAMP),

-- 3. 여성 / 토트백 / 스트리트 / 응대 희망
(3, 3, 2, 2, 1,
 '한 번 구경하러 왔어요.',
 '2026-08-17 17:00:00',
 1, '오늘은 자유로운 스트리트 감성 속에서 나만의 스타일을 완성할 특별한 토트백을 탐험하는 날', '여성존 -> 가방존 -> 신상품-여성존', CURRENT_TIMESTAMP),

-- 4. 남성 / 백팩 / 미니멀 / 30분 후 응대
(4, 4, 2, 1, 5,
 '여행갈 때 편하게 쓸 수 있는 가방을 찾고 있어요.',
 '2026-08-17 11:00:00',
 3, '오늘은 미니멀한 감성으로 여행의 편안함을 완성할 백팩을 찾아가는 세련된 여정의 날', '남성존 -> 트래블존 -> 가방존', CURRENT_TIMESTAMP),

-- 5. 기타 / 악세서리 / 볼드 / 혼자 둘러보기
(5, 5, 3, 4, 4,
 '평소 스타일에 포인트가 될 만한 아이템을 찾고 있어요.',
 '2026-08-17 18:00:00',
 2, '오늘은 대담한 스타일에 생기를 더할 특별한 악세서리를 조용히 찾아가는 날', '라이프스타일존 -> 가방존 -> 여성존', CURRENT_TIMESTAMP);

--상품 더미데이터
INSERT INTO products
(zone, product_id, product_name, product_category, product_img, product_detail, stock, location)
VALUES

    ('여성존', 1,
     'Aren 비세토스 E/W 숄더백',
     2,
     'https://images.mcmworldwide.com/i/mcmworldwide/MWSGATA01BK001_01/aren-e-w-black-s?$w1000$&fmt=auto&qlt=default',
     '바이에른 다이아몬드 레더 참과 가죽 트림이 더해진 비세토스 모노그램 캔버스 숄더백',
     2,
     '1F 토트/숄더백 존'),

    ('여성존', 2,
     'Aren 양가죽 숄더백',
     2,
     'https://images.mcmworldwide.com/i/mcmworldwide/MWSGSTA04BW001_01/aren-black-and-white-l?$w1000$&fmt=auto&qlt=default',
     '대비되는 컬러조합과 MCM 로고 모티프가 어우러진 램스킨 레더 숄더 백',
     3,
     '1F 토트/숄더백 존'),

    ('여성존', 3,
     'Aren 비세토스 브라스 플레이트 지갑',
     3,
     'https://images.mcmworldwide.com/i/mcmworldwide/MYSGSTA01BK001_01/aren-black-s?$w1000$&fmt=auto&qlt=default',
     '로고가 더해진 브라스 플레이트 장식과 천연 가죽 트림이 더해진 비세토스 모노그램 캔버스 지갑',
     2,
     '2F 지갑/소품 존'),

    ('여성존', 4,
     '미니 Aren 비세토스 카드 케이스',
     4,
     'https://images.mcmworldwide.com/i/mcmworldwide/MXAGATA044B001_01/aren-cinnamon-?$w1000$&fmt=auto&qlt=default',
     '나파 가죽 트림 디테일 비세토스 모노그램 카드 케이스',
     4,
     '2F 지갑/소품 존'),

    ('여성존', 5,
     'Aren 페이크 퍼 비세토스 Mars Dog 참',
     4,
     'https://images.mcmworldwide.com/i/mcmworldwide/MXZGATA111I001_01/aren-mars-dog-blue-ceramic-?$w1500$&fmt=auto&qlt=default',
     '비세토스 모노그램 캔버스 트리밍을 더한 인조 퍼 마스도그 참. 로고 각인 키링과 가죽 스트랩 구성',
     1,
     '2F 악세서리존'),

    ('여성존', 6,
     '모노그램 프린트 레더 2D Ella 비세토스 보스턴 참',
     4,
     'https://images.mcmworldwide.com/i/mcmworldwide/MXZGATA20BK001_01/-2d-ella-black-?$w1000$&fmt=auto&qlt=default',
     '프린트가 돋보이는 나파 레더 참으로, 키 링과 레더 스트랩이 함께 구성되었습니다',
     0,
     '2F 악세서리존'),

    ('여성존', 7,
     '페이즐리 코튼 실크 스카프',
     4,
     'https://images.mcmworldwide.com/i/mcmworldwide/MEFGAMM12QH001_01/-powder-pink-?$w1000$&fmt=auto&qlt=default',
     '페이즐리 프린트와 비세토스 모노그램 모티프, 그리고 대비되는 스트라이프 테두리가 조화를 이루는 실크 스카프',
     2,
     '2F 악세서리존'),

    ('여성존', 8,
     '디스코 모노그램 프린트 쁘띠 스카프',
     4,
     'https://images.mcmworldwide.com/i/mcmworldwide/MEFGAMM05Q7001_01/-aw26-sangria-sunset-?$w1500$&fmt=auto&qlt=default',
     'MCM Disco 아트워크가 더해진 손 바느질로 마감한 양면 오가닉 실크 스카프',
     2,
     '2F 악세서리존'),

    ('여성존', 9,
     'MCM X We The Best 울트라 스웨이드 캡',
     4,
     'https://images.mcmworldwide.com/i/mcmworldwide/MEHGAMM04NG001_01/mcm-x-we-the-best-taupe-grey-?$w1500$&fmt=auto&qlt=default',
     '비세토스 모노그램 모티프와 로고 엠보싱 가죽 패치가 더해진 데님 자카드 캡',
     1,
     '2F 악세서리존'),

    ('여성존', 10,
     '에센셜 울 비니',
     4,
     'https://images.mcmworldwide.com/i/mcmworldwide/MEHFSBC01BK001_01/-black-?$w1000$&fmt=auto&qlt=default',
     '바이에른 다이아몬드 로고 패치 울 니트 비니',
     1,
     '2F 악세서리존'),

    ('여성존', 11,
     '비세토스 Apple Watch 밴드',
     4,
     'https://images.mcmworldwide.com/i/mcmworldwide/MXZGSTT07BK001_01/-apple-watch-black-38-40-41mm-?$w1000$&fmt=auto&qlt=default',
     '시대를 초월하는 모던한 시계를 위한 밴드',
     1,
     '2F 악세서리존'),

    ('여성존', 12,
     'ECONYL 리버서블 모노그램 스크런치',
     4,
     'https://images.mcmworldwide.com/i/mcmworldwide/MEEFAMM01B7001_01/econyl-black-cognac-?$w1500$&fmt=auto&qlt=default',
     '앞 뒷면의 다른 모노그램 디자인이 돋보이는 ECONYL® 리사이클 나일론 소재의 스크런치',
     4,
     '2F 악세서리존'),

    ('남성존', 13,
     'Ottomar 비세토스 위켄더',
     2,
     'https://images.mcmworldwide.com/i/mcmworldwide/MMVGATT01CO001_01/ottomar-cognac-41cm-16-14-?$w1000$&fmt=auto&qlt=default',
     'MCM의 숙련된 캐리어 수공예 전통에 기반한 이 위켄더는 근처에 운동하러 가거나 멀리 여행을 떠날 때에도 편리하게 사용할 수 있는 크기로 디자인되었습니다.',
     4,
     '1F 가방 - 토트/숄더백 존'),

    ('남성존', 14,
     '미니 Aren 비세토스 메신저 백',
     2,
     'https://images.mcmworldwide.com/i/mcmworldwide/MMMEATA02BK001_01/aren-black-?$w1000$&fmt=auto&qlt=default',
     '천연 나파 가죽 트림이 돋보이는 비세토스 모노그램 캔버스 소재의 메신저 백',
     2,
     '1F 가방 - 토트/숄더백 존'),

    ('남성존', 15,
     '비세토스 오리지널 카드 케이스 반지갑',
     3,
     'https://images.mcmworldwide.com/i/mcmworldwide/MXSFATA03CO001_01/-cognac-s?$w1000$&fmt=auto&qlt=default',
     '탈부착 가능한 카드 케이스가 있는 모노그램 반지갑으로 뛰어난 가죽 제품을 제작하는 MCM의 헤리티지를 반영했습니다.',
     3,
     '2F 지갑/소품 존'),

    ('남성존', 16,
     '미니 비세토스 오리지널 N/S 카드 케이스',
     3,
     'https://images.mcmworldwide.com/i/mcmworldwide/MXAAAVI03CO001_01/-n-s-cognac-?$w1000$&fmt=auto&qlt=default',
     '열광적인 MCM 고객을 위해 제작된 비세토스 오리지널 라인은 영원한 아름다움을 선사하는 모든 제품에서 대표적인 모노그램 코팅 캔버스를 자랑스럽게 선보입니다.',
     3,
     '2F 지갑/소품 존'),

    ('남성존', 17,
     'München 폰테 캡',
     4,
     'https://images.mcmworldwide.com/i/mcmworldwide/MECGSMM01BW001_01/m-nchen-black-and-white-?$w1500$&fmt=auto&qlt=default',
     'München MCM 자수와 메시 트림, 바이에른 다이아몬드 메탈 하드웨어가 장식된 가죽 스트랩이 어우러진 폰테 트러커 캡',
     5,
     '2F 악세서리존'),

    ('남성존', 18,
     'Disco 로고 자수 코튼 트윌 캡',
     4,
     'https://images.mcmworldwide.com/i/mcmworldwide/MECGAMM01BK001_01/disco-black-?$w1500$&fmt=auto&qlt=default',
     'MCM Disco 자수 패치가 더해진 코튼 트윌 캡',
     0,
     '2F 악세서리존'),

    ('남성존', 19,
     '로레토스 모노그램 반다나 스카프',
     4,
     'https://images.mcmworldwide.com/i/mcmworldwide/MEFEAMM07CO001_01/-cognac-?$w1000$&fmt=auto&qlt=default',
     '로레토스 모노그램 모티프가 있는 수작업 봉제 반다나 스카프',
     2,
     '2F 악세서리존'),

    ('남성존', 20,
     '울 실크 자카드 모노그램 숄',
     4,
     'https://images.mcmworldwide.com/i/mcmworldwide/MEFGSMM01D5001_01/-mars-gold-?$w1500$&fmt=auto&qlt=default',
     '비세토스 모노그램 모티프가 돋보이는 울-실크 자카드 숄',
     2,
     '2F 악세서리존'),

    ('남성존', 21,
     '클라우스 M 비세토스 리버서블 벨트 4.5cm',
     4,
     'https://images.mcmworldwide.com/i/mcmworldwide/MXBAAVI03BK001_01/-m-4-5cm-black-matte-black-?$w1500$&fmt=auto&qlt=default',
     '절제된 시그니처 벨트',
     8,
     '2F 악세서리존'),

    ('남성존', 22,
     'MCM X CASETIFY 마그네틱 카드홀더 스탠드',
     4,
     'https://images.mcmworldwide.com/i/mcmworldwide/MXZGSTT10SV001_01/mcm-x-casetify-silver-?$w1500$&fmt=auto&qlt=default',
     '스탠드 기능을 갖춘 리미티드 에디션 마그네틱 카드 케이스',
     0,
     '2F 악세서리존'),

    ('가방존', 23,
     'Aren 노바 나일론 백팩',
     1,
     'https://images.mcmworldwide.com/i/mcmworldwide/MMKGATA03MT001_01/aren-multi-m?$w1500$&fmt=auto&qlt=default',
     '브라스 로고 플레이트가 더해진 멀티컬러 나일론 백팩',
     5,
     '1F 가방 - 백팩 존'),

    ('가방존', 24,
     'Aren ECONYL과 가죽 드로우스트링 백팩',
     1,
     'https://images.mcmworldwide.com/i/mcmworldwide/MWKFATA04BK001_01/aren-econyl-black-s?$w1000$&fmt=auto&qlt=default',
     '바바리안 다이아몬드 메탈 버클 하드웨어와 비세토스 모노그램 프린트 가죽 트림이 돋보이는 ECONYL® 드로우스트링 백팩',
     5,
     '1F 가방 - 백팩 존'),

    ('가방존', 25,
     'Pina 비세토스 스터드 장식 토트',
     2,
     'https://images.mcmworldwide.com/i/mcmworldwide/MWTGATA02CO001_01/pina-cognac-m?$w1000$&fmt=auto&qlt=default',
     '나파 가죽 트림과 메탈 스터드 장식이 돋보이는 비세토스 모노그램 캔버스 토트백',
     4,
     '1F 가방 - 토트/숄더백 존'),

    ('가방존', 26,
     'New Liz 엠보스드 모노그램 레더 쇼퍼',
     2,
     'https://images.mcmworldwide.com/i/mcmworldwide/MWPGALR01BK001_01/new-liz-black-m?$w1000$&fmt=auto&qlt=default',
     '엠보싱 비세토스 모노그램과 탈착 가능한 지퍼 파우치가 포함된 그레인드 나파 가죽 쇼퍼백',
     2,
     '1F 가방 - 토트/숄더백 존'),

    ('가방존', 27,
     'Tracy 비세토스 호보',
     2,
     'https://images.mcmworldwide.com/i/mcmworldwide/MWHGAXT03CO001_01/tracy-cognac-l?$w1500$&fmt=auto&qlt=default',
     '로고 락 클로저와 나파 가죽 트림이 돋보이는 비세토스 모노그램 캔버스 호보 백',
     3,
     '1F 가방 - 토트/숄더백 존'),

    ('가방존', 28,
     '미니 Ella 비세토스 보스턴 백',
     2,
     'https://images.mcmworldwide.com/i/mcmworldwide/MWBESEA01CO001_01/ella-cognac-?$w1000$&fmt=auto&qlt=default',
     '천연 가죽 트림과 로고 엠보싱 가죽 참이 더해진 비세토스 모노그램 캔버스 보스턴 백',
     5,
     '1F 가방 - 토트/숄더백 존'),

    ('가방존', 29,
     'Aren 비세토스 슬링백',
     2,
     'https://images.mcmworldwide.com/i/mcmworldwide/MMLGATA05K8001_01/aren-khaki-moss-?$w1000$&fmt=auto&qlt=default',
     '나파 가죽 트림이 더해진 비세토스 모노그램 캔버스 슬링백',
     6,
     '1F 가방 - 토트/숄더백 존'),

    ('가방존', 30,
     '비세토스 프루스톤 벨트백',
     2,
     'https://images.mcmworldwide.com/i/mcmworldwide/MMZAAFI04CO001_01/-cognac-s?$w1000$&fmt=auto&qlt=default',
     '유행 중인 스트리트웨어 클래식인 프루스튼 벨트백은 간편하게 휴대할 수 있는 전설적인 아이템입니다.',
     3,
     '1F 가방 - 토트/숄더백 존'),

    ('가방존', 31,
     '미니 Toni 맥시 모녹그램 레더 탑 지퍼 쇼퍼',
     2,
     'https://images.mcmworldwide.com/i/mcmworldwide/MWPGSMT04BK001_01/toni-black-?$w1500$&fmt=auto&qlt=default',
     '엠보싱 처리된 맥시 비세토스 모노그램 모티프가 돋보이는 내추럴 풀그레인 레더 쇼퍼',
     1,
     '1F 가방 - 토트/숄더백 존'),

    ('가방존', 32,
     'Milla 스페니시 엠보스드 레더 토트',
     2,
     'https://images.mcmworldwide.com/i/mcmworldwide/MWTGSMA01BK001_01/milla-black-m?$w1000$&fmt=auto&qlt=default',
     '스페인산 레더로 제작된 밀라 토트백이 브랜드 특유의 윙 실루엣을 기반으로 우아함을 간직하며, 더욱 견고하고 정제된 실루엣으로 만들어졌습니다.',
     8,
     '1F 가방 - 토트/숄더백 존'),

    ('가방존', 33,
     'Aren 엠보스드 모노그램 레더 크로스바디 지갑',
     3,
     'https://images.mcmworldwide.com/i/mcmworldwide/MXZGATA04BK001_01/aren-black-?$w1500$&fmt=auto&qlt=default',
     '엠보싱 비세토스 모노그램과 가죽 스트랩이 더해진 풀그레인 레더 폰 월렛',
     4,
     '2F 지갑/소품 존'),

    ('가방존', 34,
     'Pina 비세토스 템버린 백',
     2,
     'https://images.mcmworldwide.com/i/mcmworldwide/MWRGAOB01CO001_01/pina-cognac-s?$w1500$&fmt=auto&qlt=default',
     '스터드 나파 가죽 트림과 레더 태슬 장식으로 포인트를 준 비세토스 모노그램 캔버스 탬버린 백',
     2,
     '1F 가방 - 토트/숄더백 존'),

    ('가방존', 35,
     '미니 Aren 비세토스 트라이앵글 크로스바디',
     2,
     'https://images.mcmworldwide.com/i/mcmworldwide/MYZGATA05CO001_01/aren-cognac-?$w1500$&fmt=auto&qlt=default',
     '나파 가죽 트림과 가죽 크로스바디 스트랩을 더한 비세토스 모노그램 캔버스 트라이앵글 백',
     4,
     '1F 가방 - 토트/숄더백 존'),

    ('가방존', 36,
     'Diamond 비세토스 손목 스트랩 파우치',
     2,
     'https://images.mcmworldwide.com/i/mcmworldwide/MXZGSAK02BK001_01/diamond-black-m?$w1500$&fmt=auto&qlt=default',
     '가죽 손목 스트랩과 천연 가죽 트림이 더해진 비세토스 모노그램 캔버스 지퍼 파우치',
     2,
     '1F 가방 - 토트/숄더백 존'),

    ('가방존', 37,
     'Ottomar ECONYL 가죽 위켄더 백팩',
     1,
     'https://images.mcmworldwide.com/i/mcmworldwide/MMVFATT01BK001_01/ottomar-econyl-black-55cm-22-?$w1500$&fmt=auto&qlt=default',
     '백팩 기능과 Visetos 모노그램 프린트 가죽 트림이 적용된 ECONYL® 모듈러 위켄더 백',
     3,
     '1F 가방 - 백팩 존'),

    ('가방존', 38,
     'Pina 스터드 장식이 더해진 카프킨 숄더 스트랩',
     4,
     'https://images.mcmworldwide.com/i/mcmworldwide/MYZGATA09BK001_01/pina-black-?$w1500$&fmt=auto&qlt=default',
     '메탈 스터드 장식과 나파 가죽 트림이 돋보이는 그레인 송아지 가죽 숄더 스트랩',
     2,
     '2F 악세서리존'),

    ('가방존', 39,
     '엠보스드 모노그램 레더 에어팟 프로 케이스',
     4,
     'https://images.mcmworldwide.com/i/mcmworldwide/MXZGATA07BK001_01/-black-?$w1500$&fmt=auto&qlt=default',
     '헤리티지 모노그램이 돋보이는 모바일 액세서리 케이스',
     4,
     '2F 지갑/소품 존'),

    ('라이프스타일존', 40, '퍼피 비세토스 M Pup 인형', 4,
     'https://images.mcmworldwide.com/i/mcmworldwide/MELCSVD02WT001_01/-m-pup-white-m-?$w1000$&fmt=auto&qlt=default',
     '시그니처 비세토스로 제작된 미디엄 사이즈 인형은 하우스의 유쾌한 본성을 상징합니다.',
     1, '2F 악세서리존'),

    ('라이프스타일존', 41, '비세토스 수트 케이스', 2,
     'https://images.mcmworldwide.com/i/mcmworldwide/MEZESMM05CO001_01/-cognac-l?$w1000$&fmt=auto&qlt=default',
     '가죽으로 감싼 모서리와 티없이 깨끗한 마이크로파이버 스웨이드 소재의 내부 및 24K 도금 래치 잠금 장치가 돋보이는 헤리티지 동반자입니다.',
     1, '1F 가방 - 토트/숄더백 존'),

    ('라이프스타일존', 42, '모노그램 나일론 레더 펫 케리어', 2,
     'https://images.mcmworldwide.com/i/mcmworldwide/MEZFAMM03BK001_01/-black-?$w1500$&fmt=auto&qlt=default',
     '패딩 처리된 비스토스 모노그램 올오버 프린트 나일론 소재에 천연 가죽 트리밍이 더해진 반려동물 캐리어',
     5, '1F 가방 - 토트/숄더백 존'),

    ('라이프스타일존', 43, '비세토스 펫 칼라 & 크로스바디 리쉬', 4,
     'https://images.mcmworldwide.com/i/mcmworldwide/MEZFAMM01CO00M_01/-cognac-?$w1500$&fmt=auto&qlt=default',
     '비스토스 모노그램 캔버스 트리밍이 돋보이는 나일론 패브릭 웨빙 크로스바디 리쉬와 탈부착 가능한 칼라',
     4, '2F 악세서리존'),

    ('라이프스타일존', 44, '비세토스 iPhone 케이스', 4,
     'https://images.mcmworldwide.com/i/mcmworldwide/MXEFATA01K8001_01/-iphone-16-pro-khaki-moss-?$w1000$&fmt=auto&qlt=default',
     '외부 포켓과 천연 나파 가죽 트림이 더해진 비세토스 모노그램 캔버스 아이폰 케이스',
     0, '2F 악세서리존'),

    ('라이프스타일존', 45, 'Aren 비세토스 체인 스트랩이 더해진 에어팟 프로 케이스', 4,
     'https://images.mcmworldwide.com/i/mcmworldwide/MXZGATA084B001_01/aren-cinnamon-?$w1000$&fmt=auto&qlt=default',
     '레더 스트랩, 체인 스트랩, 가죽 트림이 돋보이는 비세토스 모노그램 캔버스 AirPods Pro 참',
     7, '2F 지갑/소품 존'),

    ('신상품-여성존', 46, 'Pina 비세토스 스터드 장식 토트', 2,
     'https://images.mcmworldwide.com/i/mcmworldwide/MWTGATA02CO001_01/pina-cognac-m?$w1000$&fmt=auto&qlt=default',
     '나파 가죽 트림과 메탈 스터드 장식이 돋보이는 비세토스 모노그램 캔버스 토트백',
     4, '1F 가방 - 토트/숄더백 존'),

    ('신상품-여성존', 47, '오발 선글라스', 4,
     'https://images.mcmworldwide.com/i/mcmworldwide/MEGGSMM06BK001_01/-black-?$w1000$&fmt=auto&qlt=default',
     '바이에른 다이아몬드 메탈 스터드 하드웨어와 로고 패턴 템플이 돋보이는 유니섹스 오벌 아세테이트 선글라스',
     2, '2F 악세서리존'),

    ('신상품-여성존', 48, '미니 Dessau 비세토스 드로우스트링 백', 2,
     'https://images.mcmworldwide.com/i/mcmworldwide/MWDGADU01CO001_01/dessau-cognac-?$w1500$&fmt=auto&qlt=default',
     '나파 가죽 트림과 레더 스트랩, 체인 핸들이 더해진 비세토스 모노그램 캔버스 드로스트링 백',
     3, '1F 가방 - 토트/숄더백 존'),

    ('신상품-여성존', 49, '투톤 모노그램 자카드 울 스톨', 4,
     'https://images.mcmworldwide.com/i/mcmworldwide/MEFGAMM01PZ001_01/-soft-pink-?$w1500$&fmt=auto&qlt=default',
     '비세토스 모노그램 모티브 양면 자카드 니트 울 스툴',
     2, '2F 악세서리존'),

    ('신상품-여성존', 50, 'Aren 스타드 가죽 벨트', 4,
     'https://images.mcmworldwide.com/i/mcmworldwide/MYBGATA03BK080_01/aren-black-80cm-31-5-?$w1500$&fmt=auto&qlt=default',
     '메탈 스터드 장식이 돋보이는 나파 가죽 트림이 더해진 그레인 레더 벨트',
     1, '2F 악세서리존'),

    ('트래블존', 51, 'Aren다이아몬드 퀼팅 레더 백팩', 1,
     'https://images.mcmworldwide.com/i/mcmworldwide/MMKGATA01BK001_01/aren-black-m?$w1500$&fmt=auto&qlt=default',
     '한층 세련된 실루엣으로 완성한 최신 Aren 백팩은 최고급 나파 가죽에 적용된 바이에른 다이아몬드 퀼팅 패턴을 통해 하우스의 뛰어난 장인정신을 보여줍니다.',
     3, '1F 가방 - 백팩 존'),

    ('트래블존', 52, 'Ottomar ECONYL 가죽 위켄더 백팩', 1,
     'https://images.mcmworldwide.com/i/mcmworldwide/MMVFATT01BK001_01/ottomar-econyl-black-55cm-22-?$w1500$&fmt=auto&qlt=default',
     '백팩 기능과 Visetos 모노그램 프린트 가죽 트림이 적용된 ECONYL® 모듈러 위켄더 백',
     3, '1F 가방 - 백팩 존'),

    ('트래블존', 53, '미니 비세토스 수트케이스', 2,
     'https://images.mcmworldwide.com/i/mcmworldwide/MEZESMM06CO001_01/-cognac-?$w1500$&fmt=auto&qlt=default',
     '가죽으로 감싼 모서리와 티없이 깨끗한 마이크로파이버 스웨이드 소재의 내부 및 24K 도금 래치 잠금 장치가 돋보이는 헤리티지 동반자입니다.',
     1, '1F 가방 - 토트/숄더백 존'),

    ('트래블존', 54, '비세토스 모자 박스', 4,
     'https://images.mcmworldwide.com/i/mcmworldwide/MEZDSMM05CO001_01/-cognac-m?$w1000$&fmt=auto&qlt=default',
     '여행의 로맨스를 구현한 빈티지풍 액세서리',
     1, '1F 가방 - 토트/숄더백 존'),

    ('트래블존', 55, 'Ottomar 비세토스 러기지 택', 4,
     'https://images.mcmworldwide.com/i/mcmworldwide/MXZFATT07CO001_01/ottomar-cognac-?$w1500$&fmt=auto&qlt=default',
     '로고 브라스 플레이트와 천연 가죽 트림이 더해진 비세토스 모노그램 캔버스 여행용 러기지 태그',
     3, '2F 악세서리존'),

    ('트래블존', 56, 'Ottomar 비세토스 트래블 파우치', 4,
     'https://images.mcmworldwide.com/i/mcmworldwide/MXZFSTT03CO001_01/ottomar-cognac-s?$w1000$&fmt=auto&qlt=default',
     '천연 나파 가죽 트림이 돋보이는 비세토스 모노그램 크로스바디 트래블 파우치',
     2, '2F 지갑/소품 존'),

    ('신상품-남성존', 57, '미니 Aren 비세토스 슬링백', 2,
     'https://images.mcmworldwide.com/i/mcmworldwide/MMLGATA05K8001_01/aren-khaki-moss-?$w1000$&fmt=auto&qlt=default',
     '나파 가죽 트림이 더해진 비세토스 모노그램 캔버스 슬링백',
     5, '1F 가방 - 토트/숄더백 존'),

    ('신상품-남성존', 58, '애니멀 패턴 실크 스카프', 4,
     'https://images.mcmworldwide.com/i/mcmworldwide/MEFGAMM10CO001_01/-cognac-?$w1000$&fmt=auto&qlt=default',
     '애니멀 패턴 그래픽 아트워크와 비세토스 모노그램 모티프가 더해진 실크 스카프',
     0, '2F 악세서리존'),

    ('신상품-남성존', 59, 'Aren 노바 모노그램 ECONYL 토트', 2,
     'https://images.mcmworldwide.com/i/mcmworldwide/MMTGATA01BK001_01/aren-econyl-black-x-?$w1500$&fmt=auto&qlt=default',
     '브라스 로고 플레이트와 비세토스 모노그램 모티프가 더해진 ECONYL® 재생 나일론 토트백',
     5, '1F 가방 - 토트/숄더백 존'),

    ('신상품-남성존', 60, '미니 Aren 디스코 비세토스 지퍼 지갑', 3,
     'https://images.mcmworldwide.com/i/mcmworldwide/MYLGATA02CO001_01/aren-cognac-?$w1500$&fmt=auto&qlt=default',
     '비세토스 모노그램 캔버스에 MCM Disco 모티프와 나파 가죽 트리밍을 더한 지퍼 어라운드 지갑',
     2, '2F 지갑/소품 존'),

    ('신상품-남성존', 61, 'MCM X We The Best Ottomar 비세토스 위켄더 백', 2,
     'https://images.mcmworldwide.com/i/mcmworldwide/MMVGATT08CO001_01/mcm-x-we-the-best-ottomar-cognac-50-5cm-19-9-?$w1500$&fmt=auto&qlt=default',
     '시대를 초월한 여행의 동반자이자 자신감과 끝없는 야망을 향한 DJ Khaled의 철학을 담은 We The Best 참을 더해 새롭게 재해석된 Ottomar 위켄더 백입니다',
     0, '1F 가방 - 토트/숄더백 존'),

    ('신상품-남성존', 62, '페이즐리 코튼 실크 스카프', 4,
     'https://images.mcmworldwide.com/i/mcmworldwide/MEFGAMM12CO001_01/-cognac-?$w1000$&fmt=auto&qlt=default',
     '페이즐리 프린트와 비세토스 모노그램 모티프, 그리고 대비되는 스트라이프 테두리가 조화를 이루는 실크 스카프',
     8, '2F 악세서리존'),

    ('신상품-남성존', 63, 'Aren 다이아몬드 퀼팅 레더 크로스바디', 2,
     'https://images.mcmworldwide.com/i/mcmworldwide/MMRGATA05BK001_01/aren-black-s?$w1500$&fmt=auto&qlt=default',
     '다이아몬드 퀼팅과 로고 브라스 플레이트로 클래식함을 더한 나파 가죽 크로스바디 백',
     2, '1F 가방 - 토트/숄더백 존');

INSERT INTO best_products (product_id) VALUES
                                           (1),
                                           (13),
                                           (23),
                                           (41),
                                           (51);