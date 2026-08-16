package com.walktowall.backend.store.history;

import com.walktowall.backend.visitcard.VisitCard;
import com.walktowall.backend.store.OfflineStore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "offline_history")
public class OfflineHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "history_id")
    private Integer historyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "visit_card_id", nullable = false, unique = true)
    private VisitCard visitCard;

    @Column(name = "enter_time", nullable = false)
    private LocalDateTime enterTime;

    @Column(name = "leave_time")
    private LocalDateTime leaveTime;
}