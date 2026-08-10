package com.walktowall.backend.user;

import com.walktowall.backend.user.dto.MyPageResponse;
import com.walktowall.backend.visitcard.VisitCard;
import com.walktowall.backend.visitcard.VisitCardRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor // 생성자 주입 자동화 (Lombok)
@Transactional(readOnly = true)
public class UserService {
    private final UserRepository userRepository;
    private final VisitCardRepository visitCardRepository;

    public MyPageResponse getMyPage(Integer userId) {

        // 유저 조회
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 유저를 찾을 수 없습니다. userId: " + userId));

        // VisitCard 조회 (Optional 처리)
        MyPageResponse.StyleBoard styleBoardDto = visitCardRepository
                .findFirstByUser_UserIdOrderByCreatedAtDesc(userId)
                .map(visitCard -> MyPageResponse.StyleBoard.builder()
                        .storeId(visitCard.getOfflineStore().getStoreId()) // OfflineStore의 PK 추출
                        .enterTime(visitCard.getVisitTime())
                        .build())
                .orElse(null); // 방문 카드가 없으면 styleBoard는 null 처리

        return MyPageResponse.builder()
                .message("마이페이지 조회에 성공했습니다.")
                .userId(user.getUserId())
                .userName(user.getUserName())
                .styleBoard(styleBoardDto)
                .build();
    }
}
