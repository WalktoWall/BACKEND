package com.walktowall.backend.wallart;

import com.walktowall.backend.visitcard.VisitCard;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "wallart")
public class WallartEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "wallart_id")
    private Integer wallartId;

    @OneToOne
    @JoinColumn(name = "visit_card_id", nullable = false)
    private VisitCard visitCard;

    @Column(name = "wallart_img", nullable = false)
    private String wallartImg;

    @Column(name = "wallart_text")
    private String wallartText;
}
