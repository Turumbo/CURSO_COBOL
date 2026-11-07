package br.com.bora.delivery.api;

import br.com.bora.delivery.api.dto.PromotionDto;
import br.com.bora.delivery.service.PromotionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/promotions")
public class PromotionController {
    private final PromotionService promotionService;

    public PromotionController(PromotionService promotionService) {
        this.promotionService = promotionService;
    }

    @GetMapping("/banners")
    public List<PromotionDto> banners() {
        return promotionService.banners();
    }

    @GetMapping("/surprise")
    public PromotionDto surprise() {
        return promotionService.currentSurprise();
    }

    @GetMapping("/validate")
    public PromotionService.PromotionValidation validate(@RequestParam String code) {
        return promotionService.validate(code);
    }
}
